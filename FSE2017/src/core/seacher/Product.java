package core.seacher;

import java.util.ArrayList;
import java.util.HashSet;

public class Product implements Comparable<Product> {
	private double performance;
	private ArrayList<Boolean> configuration = new ArrayList<Boolean>();
	private HashSet<String> config = new HashSet<String>();
	
	public Product(ArrayList<String> features, double performance, HashSet<String> config) {
		this.performance = performance;
		this.config = config;
		for(String f: features) {
			if(config.contains(f)){
				configuration.add(true);
			}else {
				configuration.add(false);
			}
		}
	}
	
	public double getPerformance() {
		return performance;
	}
	
	public ArrayList<Boolean> getConfigMap() {
		return configuration;
	}
	
	public HashSet<String> getConfiguration() {
		return config;
	}
	
	public void setPerformance(double perf) {
		this.performance = perf;
	}

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;
        if (v instanceof Product){
        	Product p = (Product) v;
            retVal = p.config.equals(this.config);
        }
     return retVal;
  }
	
	
	
	@Override
	public int compareTo(Product o) {
		if(o.getPerformance() > this.performance) {
			return 1;
		}else if(o.getPerformance() < this.performance) {
			return -1;
		}else {
			return 0;
		}
	}
}
