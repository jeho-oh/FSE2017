package core.seacher;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import core.io.FMParser;
import core.io.siegmundIO.MeasurementMapper;
import core.io.siegmundIO.SiegmundDataParser;
import core.sampler.BDDRunner;
import core.sampler.BDDSet;

public class ConfigSearcher {
	private String target;
	FMParser mParser;
	ArrayList<String> features;
	BDDRunner runner;
	SiegmundDataParser dParser;
	ResultAnalyzer rs;
	ArrayList<Product> measurements;
	BDDSet set;
	
	String result;

	public ConfigSearcher(String target, String fmFile, String mdFile) {
		this.target = target;
		mParser = new FMParser();
		mParser.ParseFeatureModel(fmFile);
		features = mParser.getFeatures();

		runner = new BDDRunner(target, features);
		runner.processGrammar(mParser.getGClauses());
		runner.processConstraints(mParser.getCClauses());

		dParser = new SiegmundDataParser(features);
		measurements = dParser.ParseXML(mdFile);
		Collections.sort(measurements);
		
		rs = new ResultAnalyzer(target, features, measurements);
		
		// create CBDD
		set = new BDDSet(target, runner.convertBDD_set(), features);
		set.countConfig();
		
		result = "delta_x,delta_x_est,delta_x_nrs,delta_y,delta_y_est,sampleSize,noteworthy,recursion,features,\n";
	}


	private ArrayList<HashSet<String>> filterConfigs(ArrayList<HashSet<String>> configs, HashSet<String> includes, HashSet<String> excludes, ArrayList<HashSet<String>> invalids) {
		for(HashSet<String> c : configs) {
			boolean match = true;

			for(String s : includes) {
				if(!c.contains(s)){
					match = false;
				}
			}
			for(String s : excludes) {
				if(c.contains(s)){
					match = false;
				}
			}

			if(!match) {
				invalids.add(c);
			}
		}

		for(HashSet<String> c : invalids) {
			configs.remove(c);
		}

		return invalids;
	}


	public void searchConfigs(int recCount, int initSample, int recSample) {
		ArrayList<Product> products = null;

		boolean init = true;
		boolean valid = true;
		ArrayList<HashSet<String>> filtered = new ArrayList<HashSet<String>>();
		int recursion = 0;
		Product best = null;

		ArrayList<Double> sampleSlope = null;

		MeasurementMapper mm = new MeasurementMapper();
		FeatureSelector fs = new FeatureSelector(mParser);

		ArrayList<HashSet<String>> configs = null;
		HashSet<String> includes = new HashSet<String>();
		HashSet<String> excludes = new HashSet<String>();



		// do recursive searching
		while((recCount > 0) && valid) {
			// sample configurations
			if(init) {
				configs = set.sampleConfigs(initSample, includes, excludes, true);
			}else {
				configs = set.sampleConfigs(recSample, includes, excludes, false);
			}

			// Count filtered configs to count total configs sampled
			filtered = filterConfigs(configs, includes, excludes, filtered);
			mm.setProduct_Sample(features, configs, measurements);
			products = mm.getSortedProducts();

			ArrayList<HashSet<String>> samples = new ArrayList<HashSet<String>>();
			if(init) {
				best = products.get(products.size()-1);
				sampleSlope = rs.getSlope_sample(products, set.getSolSize());
				init = false;
			}else {
				if(products.size() != 0) {
					if(best.getPerformance() > products.get(products.size()-1).getPerformance()) {
						best = products.get(products.size()-1);
					}else{
					}
				}else {
					valid = false;
				}
			}

			if(valid) 
			{
				if(configs.size() < set.getSolSize_local()) {
					valid = fs.getStair(products, set, includes, excludes);
				}else {
					valid = false;
				}
			}

			recCount--;
			recursion++;
		}


		long stairSize = set.getSolSize_local();
		sampleSlope = rs.getSlope_sample(products, stairSize);

		// delta_x accuracy
		double delta_x = rs.checkAccuracy_config(best.getConfiguration());
		System.out.print(delta_x + ",");
		result = result + delta_x + ",";

		// delta_x estimation
		double xEstimate = (double)(stairSize / (products.size() + 1)) / set.getSolSize();
		System.out.print(xEstimate + ",");
		result = result + xEstimate + ",";

		// delta_x assuming non recursive search
		double nrs = 1 / (double)(configs.size() + filtered.size() + 1);
		System.out.print(nrs + ",");
		result = result + nrs + ",";
		
		// delta_y accuracy
		double delta_y = rs.checkAccuracy_bestPerf(best.getPerformance());
		System.out.print(delta_y + ",");
		result = result + delta_y + ",";

		// delta_y estimation
		double yEstimate = (best.getPerformance() - sampleSlope.get(5))/sampleSlope.get(5);
		System.out.print(yEstimate + ",");
		result = result + yEstimate + ",";

		// estimation accuracy
		//System.out.print(xEstimate-delta_x + ",");
		//System.out.print(yEstimate-delta_y + ",");

		// total number of samples used
		int n = configs.size() + filtered.size();
		System.out.print(n + ",");
		result = result + n + ",";
		
		// % of features that belong to actual best
		double noteworthy = rs.checkNoteworthy(includes, excludes);
		System.out.print(noteworthy + ",");
		result = result + noteworthy + ",";
		
		// number of recursions
		System.out.print(recursion + ",");
		result = result + recursion + ",";
		
		// number of selected features
		int f = includes.size() + excludes.size();
		System.out.print(includes.size() + excludes.size() + ",");
		result = result + f + ",";

		System.out.println("||,");
		result = result + "\n";
	}
	
	public void writeResults(String outFile) {
		try {
			FileWriter fw = new FileWriter(outFile, false);

			fw.append(result);
			fw.close();
		} catch (IOException e) {
			// System.err.println("CNF file generation error");
			e.printStackTrace();
		}
	}
}
