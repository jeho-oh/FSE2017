package core.seacher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import core.io.FMParser;
import core.sampler.BDDSet;

public class FeatureSelector {
	private FMParser fm;
	private ArrayList<String> features;

	public FeatureSelector(FMParser fm) {
		this.fm = fm;
		features = fm.getFeatures();
	}

	
	public boolean getCommonFeatures(ArrayList<HashSet<String>> configs, HashSet<String> includes, HashSet<String> excludes, HashSet<String> passFeature) {
		boolean selected = false;

		for(String s : fm.getFeatures()) {
			if((passFeature == null) || (!passFeature.contains(s))){
				if(!includes.contains(s) && !excludes.contains(s)) {
					int included = 0;
					int excluded = 0;

					for(HashSet<String> h : configs){
						if(h.contains(s)) {
							++included;
						}else {
							++excluded;
						}
					}

					if(included == configs.size()) {
						includes.add(s);
						selected = true;
					}
					else if(excluded == configs.size()) {
						excludes.add(s);
						selected = true;
					}
				}
			}
		}

		return selected;
	}


	private HashSet<String> filterFeatures(ArrayList<Product> products) {
		HashSet<String> passFeature = new HashSet<String>();
		int[][] hit = new int[fm.getFeatures().size()][2];

		for(Product p : products) {
			int i = 0;
			for(String s : fm.getFeatures()) {
				if(p.getConfiguration().contains(s)) {
					hit[i][0]++;
				}else {
					hit[i][1]++;
				}
				i++;
			}	
		}

		for(int i = 0; i < fm.getFeatures().size(); i++) {
			if((hit[i][0] <= 1) || (hit[i][1] <= 1)) {
				if(!fm.passAlternatives(fm.getFeatures().get(i), passFeature)) {
					passFeature.add(fm.getFeatures().get(i));
				}
			}
		}

		return passFeature;
	}


	boolean detail = false;
	int k = 2;

	public boolean getStair(ArrayList<Product> products, BDDSet set, HashSet<String> includes, HashSet<String> excludes) {
		FeaturePartition sp = new FeaturePartition(features, products);
		HashSet<String> prevIn = new HashSet<String>();
		HashSet<String> prevEx = new HashSet<String>();

		HashSet<String> passFeatures = filterFeatures(products);

		for(String s : includes) {
			prevIn.add(s);
		}
		for(String s : excludes) {
			prevEx.add(s);
		}

		for(String f: features) {
			sp.partitionByFeature(f);
		}

		//sp.printFeatureStats();

		ArrayList<HashSet<String>>samples = new ArrayList<HashSet<String>>();

		if(products.size() >= k) {
			for(int i = products.size()-1; i > products.size()-1-k; i--) {
				samples.add(products.get(i).getConfiguration());
			}
		}else {
			return false;
		}

		ArrayList<FeatureStats> fss = new ArrayList<FeatureStats>();
		fss.addAll(sp.getSortedStats());

		if(getCommonFeatures(samples, includes, excludes, passFeatures)) {		
			for(FeatureStats fs : sp.getSortedStats()) {
				if(includes.contains(fs.getName()) && (!prevIn.contains(fs.getName()))) {
					HashSet<String> alternatives = new HashSet<String>();
					boolean pass = false;
					if(fm.passAlternatives(fs.getName(), alternatives))
					{
						for(String s : alternatives) {
							for(FeatureStats af : fss) {
								if(af.getName().equals(s)) {
									if(af.getDelta() < fs.getDelta()) {
										pass = true;
									}
								}
							}
						}

						if(pass) {
							includes.remove(fs.getName());
						}else {
							for(String s : alternatives) {
								if(!s.equals(fs.getName()))
								excludes.add(s);
							}
						}
					}else {
						if(!fs.getHypothesis()) {
							includes.remove(fs.getName());
						}

						if(fs.getDelta() > 0) {
							includes.remove(fs.getName());
						}
					}
				}else if(excludes.contains(fs.getName())&& (!prevEx.contains(fs.getName()))) {
					HashSet<String> alternatives = new HashSet<String>();
					boolean pass = false;
					if(fm.passAlternatives(fs.getName(), alternatives))
					{
						for(String s : alternatives) {
							for(FeatureStats af : fss) {
								if(af.getName().equals(s)) {
									if(af.getDelta() < fs.getDelta()) {
										pass = true;
									}
								}
							}
						}

						if(!pass) {
							excludes.remove(fs.getName());
						}
					}else {
						if(!fs.getHypothesis()) {
							excludes.remove(fs.getName());
						}

						if(fs.getDelta() < 0) {
							excludes.remove(fs.getName());
						}
					}
				}
			}
		}else {

		}

		if(prevIn.equals(includes) && prevEx.equals(excludes)) {
			boolean selected = false;
			return selected;
		}else {
			return true;
		}
	}
}
