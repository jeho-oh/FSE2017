package core.seacher;

import java.lang.Math;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.inference.TTest;

import core.util.TTable;

public class FeatureStats implements Comparable<FeatureStats> {
	private String feature;
	private ArrayList<Product> inConfig;
	private ArrayList<Product> exConfig;
	
	private int inSize = 0;
	private int exSize = 0;

	private double inAvg = 0.0;
	private double exAvg = 0.0;

	private double delta;

	private double inStdev = 0.0;
	private double exStdev = 0.0;

	private int df = 0;
	private double level = 0.05;
	private double error = 0.0;
	private double conf_low = 0.0;
	private double conf_high = 0.0;

	private double testValue = 0.0;
	private double ruleValue = 0.0;
	private boolean hypothesis = false;

	public FeatureStats(String feature, ArrayList<Product> inConfig, ArrayList<Product> exConfig) {
		this.feature = feature;
		this.inConfig = inConfig;
		this.exConfig = exConfig;

		inSize = inConfig.size();
		exSize = exConfig.size();

		if(!(inConfig.size() < 2) && !(exConfig.size() < 2)) {
			SummaryStatistics inStats = new SummaryStatistics();
			SummaryStatistics exStats = new SummaryStatistics();
			
			for(Product p : inConfig) {
				inStats.addValue(p.getPerformance());
			}

			for(Product p : exConfig) {
				exStats.addValue(p.getPerformance());
			}
			
			TTest tt = new TTest();
			if(inStats.getMean() < exStats.getMean()){
				hypothesis = tt.tTest(inStats, exStats, 0.1);
			}else {
				hypothesis = tt.tTest(exStats, inStats, 0.1);
			}
		}

		for(Product p : inConfig) {
			inAvg += p.getPerformance();
		}
		inAvg /= inConfig.size();
		
		for(Product p : exConfig) {
			exAvg += p.getPerformance();
		}
		exAvg /= exConfig.size();

		for(Product p : inConfig) {
			inStdev += Math.pow(p.getPerformance() - inAvg, 2);
		}
		inStdev = Math.sqrt(inStdev / inConfig.size());

		for(Product p : exConfig) {
			exStdev += Math.pow(p.getPerformance() - exAvg, 2);
		}
		exStdev = Math.sqrt(exStdev / exConfig.size());

		delta = inAvg - exAvg;

		if((inConfig.size() != 0) && (exConfig.size() != 0)) {
			TTable tTable = new TTable();
			double temp_in = inStdev * inStdev / inConfig.size();
			double temp_ex = exStdev * exStdev / exConfig.size();

			df = (int) (
			(temp_in + temp_ex) / 	
				(
					(temp_in * temp_in / (inConfig.size() - 1))
					+ (temp_ex * temp_ex / (exConfig.size() - 1))
				)
			);
			error = tTable.getTTable(df, level) * Math.sqrt(temp_in + temp_ex);
			
			//error = 2.58 * Math.sqrt(temp_in + temp_ex); //2.58
			conf_low = delta - error;
			conf_high = delta + error;

//			testValue = (inAvg - exAvg) / Math.sqrt(temp_in + temp_ex);
//			ruleValue =  1.96;
//			if(testValue > ruleValue) {
//				hypothesis = false;
//			}else {
//				hypothesis = true;
//			}
		}
	}
	
	
	public String stringData() {
		String data = feature + ",";
		data += delta + ",";
		data += inAvg + ",";
		data += exAvg + ",";
		data += inConfig.size() + ",";
		data += exConfig.size() + ",";
		data += inStdev + ",";
		data += exStdev + ",";
		data += conf_low + ",";
		data += conf_high + ",";
		data += hypothesis + ",";
		data += (double)inConfig.size()/(double)(inConfig.size()+exConfig.size());

		return data;
	}
	
	public String getName() {
		return feature;
	}
	
	public Boolean getHypothesis() {
		return hypothesis;
	}
	
	public double getDelta() {
		return delta;
	}
	
	public double getConf_high() {
		return conf_high;
	}
	
	public double getConf_low() {
		return conf_low;
	}

	@Override
	public int compareTo(FeatureStats o) {
		if(!Double.isNaN(o.delta) && Double.isNaN(this.delta)){
			return 1;
		}
		if(Double.isNaN(o.delta) && !Double.isNaN(this.delta)){
			return -1;
		}
		if(Double.isNaN(o.delta) && Double.isNaN(this.delta)){
			return 0;
		}
		
		
		if(Math.abs(o.delta) > Math.abs(this.delta)) {
			return 1;
		}
		if(Math.abs(o.delta) < Math.abs(this.delta)) {
			return -1;
		}

		{
			return 0;
		}
	}
}
