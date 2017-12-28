package core.sampler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import core.io.FMParser;
import core.io.siegmundIO.CSVGenerator;

public class Main {

	public static ConfigSampler getSample_set(String target, int sampleSize, int cycle) {
		ArrayList<HashSet<String>> configs = new ArrayList<HashSet<String>>();
		long totalAcc = 0;
		long bddAcc = 0;
		int cycles = cycle;
		long countAcc = 0;

		FMParser parser = new FMParser();
		BDDRunner runner = null;
		BDDSet set = new BDDSet("",null, null);

		//parser.ParseFeatureList("FeatureModel/"+target+"_Features.m");
		parser.ParseFeatureModel("FeatureModel/"+target+".m");

		for(int i = 0; i < cycles; i++) {
			configs.clear();

			long startTime = System.currentTimeMillis();

			if(parser.getFeatures() == null) {
				runner = new BDDRunner(target);
			}else {
				runner = new BDDRunner(target, parser.getFeatures());
			}

			runner.processGrammar(parser.getGClauses());
			runner.processConstraints(parser.getCClauses());

			long bddTime = System.currentTimeMillis();
			bddAcc = bddAcc + (bddTime - startTime);

			set = new BDDSet(target, runner.convertBDD_set(), parser.getFeatures()); 

			set.countConfig();

			long countTime = System.currentTimeMillis();
			countAcc = countAcc + (countTime - startTime);

			configs = set.sampleConfigs(sampleSize);

			long endTime = System.currentTimeMillis();
			totalAcc = totalAcc + (endTime - startTime);
		}

		ConfigSampler sample = new ConfigSampler(target, parser.getFeatures(), configs,
				parser.getClauseNumbers(), runner.getNodeSize(), set.getPathSize(), set.getSolSize(),
				((double)totalAcc / (double)cycles), ((double)bddAcc / (double)cycles));
		//System.out.println(target + ": " + (double)countAcc / (double)cycles);
		return sample;
	}

	public static void main(String [] args) {
		ArrayList<ConfigSampler> samples = new ArrayList<ConfigSampler>();
		CSVGenerator csv = new CSVGenerator(null);
		
		String[] targets = {"BerkeleyDBJ"};
		System.out.println("target, features, clauses, nodes, configs, samples, totalTime, bddTime");
		for(int i = 0; i < targets.length; i++) {
			ConfigSampler sample = getSample_set(targets[i], 0, 1);
			samples.add(sample);
			sample.printData();
			csv.generateSampleData(sample, targets[i]+".csv");
		}
	}
}
