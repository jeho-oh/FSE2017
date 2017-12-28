package core.seacher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import core.sampler.ConfigSampler;

public class FeaturePartition {
	private ArrayList<String> features;
	private ArrayList<Product> products;
	private ArrayList<FeatureStats> stats = new ArrayList<FeatureStats>();
	
	public FeaturePartition(ArrayList<String> features, ArrayList<Product> products) {
		this.features = features;
		this.products = products;
	}

	
	public ArrayList<ArrayList<Product>> partitionByFeature(String feature) {
		ArrayList<ArrayList<Product>> partitions = new ArrayList<ArrayList<Product>>();

		ArrayList<Product> partition1 = new ArrayList<Product>();
		ArrayList<Product> partition2 = new ArrayList<Product>();

		int index = features.indexOf(feature);
		
		for(Product p: products) {
			if(p.getConfigMap().get(index)){
				partition1.add(p);
			}else {
				partition2.add(p);
			}
		}
		
		partitions.add(partition1);
		partitions.add(partition2);

		stats.add(new FeatureStats(feature, partition1, partition2));
		
		return partitions;
	}
	

	public ArrayList<FeatureStats> getSortedStats() {
		
		Collections.sort(stats);
		return stats;
	}
	
	
	public void printFeatureStats() {
		Collections.sort(stats);
		//System.out.println(stats.size());
		for(FeatureStats fs : stats) {
			System.out.println(fs.stringData());

		}
	}
}
