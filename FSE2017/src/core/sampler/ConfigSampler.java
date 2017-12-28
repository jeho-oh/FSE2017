package core.sampler;

import java.util.ArrayList;
import java.util.HashSet;

import core.io.siegmundIO.SiegmundDataParser;

public class ConfigSampler {
	private String target;
	private ArrayList<String> features = new ArrayList<String>();
	private ArrayList<HashSet<String>> configs = new ArrayList<HashSet<String>>();
	//private ArrayList<Product> sampleProducts = new ArrayList<Product>();
	
	private int clauses;
	private int nodes;
	private int paths;
	private long totalConfigs;
	
	private double totalTime;
	private double bddTime;

	public ConfigSampler(String target, ArrayList<String> features,
			ArrayList<HashSet<String>> configs, 
			int clauses, int nodes, int paths, long totalConfigs,
			double totalTime, double bddTime){
		this.target = target;
		this.features = features;
		this.configs = configs;
		this.clauses = clauses;
		this.totalConfigs = totalConfigs;
		this.nodes = nodes;
		this.paths = paths;
		this.totalTime = totalTime;
		this.bddTime = bddTime;
		//convertToProducts(sampleConfigs, features);
	}
	
	
	public ArrayList<HashSet<String>> getConfigs() {
		return configs;
	}
	
	
	public String printData() {
		String res = target + ", " + 
					features.size() + ", " + clauses + ", " + nodes + ", " + paths + ", " + 
					totalConfigs + ", " + configs.size() + ", " + 
					totalTime + ", " + bddTime;
		System.out.println(res);
		return res;
	}
}
