package core.io.siegmundIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import core.seacher.Product;

public class MeasurementMapper {
	private ArrayList<Product> products;
	
	public MeasurementMapper() {

	}
	
	
	// map configuration with benchmark data
	public void setProduct_Sample(ArrayList<String> features, ArrayList<HashSet<String>> sample, ArrayList<Product> base) {
		ArrayList<Product> samples = new ArrayList<Product>();
		
		for(HashSet<String> config : sample) {
			samples.add(new Product(features, 0, config));
		}
		
		for(Product sp: samples) {
			for(Product bp: base) {
				if((sp.getConfiguration().equals(bp.getConfiguration()))) {
					sp.setPerformance(bp.getPerformance());
				}
			}
		}
		
		this.products = samples;
	}

	
	// sort products by performance
	public void sortProducts() {
		Collections.sort(products);
	}

	
	public ArrayList<Product> getSortedProducts() {
		Collections.sort(products);
		return products;
	}
}
