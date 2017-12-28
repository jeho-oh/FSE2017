package core.seacher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import core.io.FMParser;
import core.io.siegmundIO.MeasurementMapper;
import core.io.siegmundIO.SiegmundDataParser;
import core.sampler.BDDRunner;
import core.sampler.BDDSet;

public class ResultAnalyzer {
	ArrayList<String> features;

	ArrayList<HashSet<String>> total;
	ArrayList<Product> solutionSet;

	private final WeightedObservedPoints obs = new WeightedObservedPoints();

	public ResultAnalyzer(String target, ArrayList<String> features, ArrayList<Product> solutionSet) {
		this.features = features;
		this.solutionSet = solutionSet;
		Collections.sort(solutionSet);
	}
	
	
	public double checkAccuracy_config(HashSet<String> config) {
		int rank = -1;

		for(int i = solutionSet.size()-1; i > -1; i--){
			if(solutionSet.get(i).getConfiguration().equals(config)) {
				rank = i;
				break;
			}
		}

		return (1 - ((double)(rank + 1) / (double)solutionSet.size()));
	}
	
	
	public int getRank(HashSet<String> config) {
		int rank = -1;
		
		for(int i = solutionSet.size()-1; i > -1; i--){
			if(solutionSet.get(i).getConfiguration().equals(config)) {
				rank = solutionSet.size() - i - 1;
			}
		}
		return rank;
	}
	
	
	public double checkAccuracy_perf(double perf) {
		double minPerf = solutionSet.get(0).getPerformance();
		double maxPerf = solutionSet.get(solutionSet.size()-1).getPerformance();

		return Math.abs(perf - maxPerf)/ Math.abs(minPerf - maxPerf) ;
	}
	
	
	public double checkAccuracy_bestPerf(double perf) {
		double maxPerf = solutionSet.get(solutionSet.size()-1).getPerformance();

		return Math.abs(perf - maxPerf) / Math.abs(maxPerf);
	}
	
	
	public double getBestperf() {
		return solutionSet.get(solutionSet.size()-1).getPerformance();
	}
	
	
	public double getSearchperf(HashSet<String> config) {
		for(int i = solutionSet.size()-1; i > -1; i--){
			if(solutionSet.get(i).getConfiguration().equals(config)) {
				return solutionSet.get(i).getPerformance();
			}
		}
		return Double.NaN;
	}
	
	
	public ArrayList<Double> getSlope_sample(ArrayList<Product> products, long stairSize) {
		SimpleRegression sr = new SimpleRegression();
		ArrayList<Double> sampleSlope = new ArrayList<>();
		
		double delta = products.get(products.size()-1).getPerformance() - products.get(0).getPerformance();
		double delta_x = (double)stairSize / (double)(products.size() + 1);
		int i = 0;
		
		for(Product p : products) {
			sr.addData(i*delta_x, p.getPerformance());
			i++;
		}
		
		double slope = sr.getSlope();
		sampleSlope.add(slope);//0
		sampleSlope.add(delta);//1
		sampleSlope.add(sr.getR());//2
		
		SimpleRegression sr_right = new SimpleRegression();
		i = 1;
		int third = (int) Math.round(((double)products.size() / 3));
		
		for(int j = (int)(third * 2); j < products.size(); j++) {
			sr_right.addData(i*delta_x, products.get(j).getPerformance());
			i++;
		}
		
		double rSlope = sr_right.getSlope();
		sampleSlope.add(rSlope);//3
		
		double r = -1 * rSlope / slope;
		double a = (r / 3) + 1;
		double k = Math.log(a) / (Math.log(2)-Math.log(3));
		sampleSlope.add(k);//4
		
		i = 1;
		obs.clear();
		for(int j = 0; j < products.size(); j++) {
			obs.add(i*delta_x, products.get(j).getPerformance());
			++i;
		}
		
		final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
		final double[] coeff = fitter.fit(obs.toList());
		
		double lowerEst;
		if(products.size() < 6) {
			lowerEst = products.get(products.size()-1).getPerformance() + (slope * delta_x);
		}else {
			lowerEst = products.get(products.size()-1).getPerformance() + (rSlope * delta_x);
		}
		
		if(lowerEst < 0) {
			lowerEst = products.get(products.size()-1).getPerformance();
		}
		sampleSlope.add(lowerEst);//5
		
		double upperEst = products.get(0).getPerformance() - (slope * delta_x);
		sampleSlope.add(upperEst);//6
		
		return sampleSlope;
	}
	
	
	public double checkNoteworthy(HashSet<String> includes, HashSet<String> excludes) {
		double count = 0.0;
		
		HashSet<String> bestConfig = solutionSet.get(solutionSet.size()-1).getConfiguration();

		for(String s : includes){
			if(bestConfig.contains(s)) {
				count++;
			}
		}
		
		for(String s : excludes){
			if(!bestConfig.contains(s)) {
				count++;
			}
		}
		
		return count / (includes.size() + excludes.size());

	}
	
	
	public double noteworthyCount(HashSet<String> includes, HashSet<String> excludes) {
		double count = 0.0;
		
		HashSet<String> bestConfig = solutionSet.get(solutionSet.size()-1).getConfiguration();

		for(String s : includes){
			if(bestConfig.contains(s)) {
				count++;
			}
		}
		
		for(String s : excludes){
			if(!bestConfig.contains(s)) {
				count++;
			}
		}
		
		return count;
	}
}
