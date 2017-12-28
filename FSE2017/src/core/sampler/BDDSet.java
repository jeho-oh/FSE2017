package core.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class BDDSet {
	private String target;
	private byte[][] rawData;
	private ArrayList<String> features;

	private ArrayList<Long> configSize = new ArrayList<Long>();
	private ArrayList<Long> configSize_local = new ArrayList<Long>();

	private long solSize;
	private long solSize_local;

	ArrayList<ArrayList<Integer>> pool = new ArrayList<ArrayList<Integer>>();

	private ArrayList<HashSet<String>> samples = new ArrayList<HashSet<String>>();
	ArrayList<HashSet<String>> pastSamples = new ArrayList<HashSet<String>>();
	
	
	public BDDSet(String target, byte[][] rawData, ArrayList<String> features) {
		this.target = target;
		this.rawData = rawData;
		this.features = features;
	}

	
	public long countConfig() {
		for(byte[] s : rawData) {
			long count = 1;
			for(int i = 0; i < s.length; i++) {
				if(s[i] == 2) {
					count = count * 2;
				}
			}

			solSize = solSize + count;
			configSize.add(solSize);
		}

		return solSize;
	}

	
	public long countConfig(HashSet<String> selecteds, HashSet<String> unselecteds) {
		configSize_local.clear();
		
		ArrayList<Integer> infids = new ArrayList<Integer>();
		ArrayList<Integer> exfids = new ArrayList<Integer>();
		solSize_local = 0;

		if(selecteds != null) {
			for(String s : selecteds) {
				infids.add(features.indexOf(s));
			}
		}

		if(unselecteds != null) {
			for(String s : unselecteds) {
				exfids.add(features.indexOf(s));
			}
		}

		for(int i = 0; i < rawData.length; i++) {
			long count = 0;
			if(i == 0) {
				count = configSize.get(i);
			}else {
				count = configSize.get(i) - configSize.get(i-1);
			}

			for(Integer f : infids) {
				if(rawData[i][f] == 0) {
					count = 0;
					break;
				}
				if(rawData[i][f] == 2) {
					count = count / 2;
				}
			}
			for(Integer f : exfids) {
				if(rawData[i][f] == 1) {
					count = 0;
					break;
				}
				if(rawData[i][f] == 2) {
					count = count / 2;
				}
			}
			solSize_local = solSize_local + count;
			configSize_local.add(solSize_local);
		}

		return solSize_local;
	}

	
	public HashSet<String> sampleConfig(long sn) {
		HashSet<String> config = new HashSet<String>();

		byte[] s = null;

		for(int i = 0; i < rawData.length; i++) {
			if(sn < configSize.get(i)) {
				s = rawData[i];
				if(i != 0){
					sn = sn - configSize.get(i-1);
				}
				break;
			}		
		}

		for(int j = 0; j < s.length; j++) {
			if(s[j] == 1) {
				config.add(features.get(j));
			}else if (s[j] == 2) {
				if((sn % 2) == 1) {
					config.add(features.get(j));
				}
				sn = sn / 2;
			}
		}

		config.remove(target);
		return config;
	}

	
	public HashSet<String> sampleConfig(long sn, HashSet<String> selecteds, HashSet<String> unselecteds) {
		HashSet<String> config = new HashSet<String>();
		ArrayList<Integer> infids = new ArrayList<Integer>();
		ArrayList<Integer> exfids = new ArrayList<Integer>();
		byte[] s = null;
		long sn1 = 0;

		// compute local config size if not computed
		if(configSize_local.isEmpty()) {
			countConfig(selecteds, unselecteds);
		}

		// get fid of selecteds
		if(selecteds != null) {
			for(String f : selecteds) {
				infids.add(features.indexOf(f));
			}
		}

		// get fid of selecteds
		if(unselecteds != null) {
			for(String f : unselecteds) {
				exfids.add(features.indexOf(f));
			}
		}
		
		// locate the path that contains the sn
		for(int i = 0; i < rawData.length; i++) {
			if(sn < configSize_local.get(i)) {
				s = rawData[i];
				if(i != 0){
					sn = sn - configSize_local.get(i-1);
					sn1 = sn;
				}
				
				break;
			}		
		}
		
		for(int j = 0; j < s.length; j++) {
			if(infids.contains(j)) {
				config.add(features.get(j));
			}else if(exfids.contains(j)) {

			}
			else {
				if(s[j] == 1) {
					config.add(features.get(j));
				}else if (s[j] == 2) {
					if((sn % 2) == 1) {
						config.add(features.get(j));
						sn = sn / 2;
					}else {
						sn = sn / 2;
					}
				}
			}
		}

		config.remove(target);
		return config;
	}

	
	private long nextLong(Random rng, long n) {
		long bits, val;
		do {
			bits = (rng.nextLong() << 1) >>> 1;
			val = bits % n;
		} while (bits-val+(n-1) < 0L);
		return val;
	}

	
	public ArrayList<HashSet<String>> sampleConfigs(long sample) {
		Random randomGenerator = new Random();

		long listMax = Integer.MAX_VALUE;
		int cluster = (int)(solSize / listMax);
		int remainder = (int)(solSize % listMax);

		for(int i = 0; i <= cluster; i++) {
			pool.add(new ArrayList<Integer>());
			if(i < cluster) {
				for(int j = 0; j < listMax; j++) {
					pool.get(i).add(j);
				}
			}else {
				for(int j = 0; j < remainder; j++) {
					pool.get(i).add(j);
				}
			}
		}

		long poolSize = solSize;
		int[] clusterSize = new int[cluster+1];
		for(int l = 0; l < clusterSize.length; l++) {
			clusterSize[l] = pool.get(l).size();
		}
		
		int i = 0;
		while((poolSize > 0) && (i < sample)) {
			int j = 0;
			int k = 0;
			
			int rn = (int)nextLong(randomGenerator, poolSize);
			//change listMat for j computation to be cluster specific
			boolean found = false;
			
			for(int l = 0; l < clusterSize.length; l++) {
				if(!found) {
					if(((rn+1) - clusterSize[l]) < 0) {
						j = l;
						k = rn;
						--clusterSize[l];
						found = true;
					}else {
						rn = rn-clusterSize[l];
					}
				}
			}
			
			//j = (int)(rn / listMax);
			//k = (int)(rn % listMax);

			int sn = pool.get(j).get(k);
			pool.get(j).remove(k);
			poolSize--;

			samples.add(sampleConfig(sn));
			i++;
		}

		pastSamples.addAll(samples);
		
		return samples;
	}

	
	public ArrayList<HashSet<String>> sampleConfigs(long sample, HashSet<String> selecteds, HashSet<String> unselecteds, Boolean newList) {

		if(newList){
			pastSamples.clear();
		}
		
		Random randomGenerator = new Random();
		ArrayList<ArrayList<Integer>> pool_local = new ArrayList<ArrayList<Integer>>();
		solSize_local = countConfig(selecteds, unselecteds);

		long listMax = (long)(Math.pow(2, 32));
		int cluster = (int)(solSize_local / listMax);
		int remainder = (int)(solSize_local % listMax);

		for(int i = 0; i <= cluster; i++) {
			pool_local.add(new ArrayList<Integer>());
			if(i < cluster) {
				for(int j = 0; j < listMax; j++) {
					pool_local.get(i).add(j);
				}
			}else {
				for(int j = 0; j < remainder; j++) {
					pool_local.get(i).add(j);
				}
			}
		}

		long poolSize = solSize_local;
		if(sample == -1) {
			sample = solSize_local;
		}
		int[] clusterSize = new int[cluster+1];
		for(int l = 0; l < clusterSize.length; l++) {
			clusterSize[l] = pool_local.get(l).size();
		}
		
		int i = 0;
		while((poolSize > 0) && i < sample) {
			int j = 0;
			int k = 0;
			
			int rn = (int)nextLong(randomGenerator, poolSize);
//			//change listMat for j computation to be cluster specific
			boolean found = false;
			
			for(int l = 0; l < clusterSize.length; l++) {
				if(!found) {
					if(((rn+1) - clusterSize[l]) < 0) {
						j = l;
						k = rn;
						--clusterSize[l];
						found = true;
					}else {
						rn = rn-clusterSize[l];
					}
				}
			}
			
			//j = (int)(rn / listMax);
			//k = (int)(rn % listMax);

			int sn = pool_local.get(j).get(k);
			pool_local.get(j).remove(k);
			poolSize--;

			HashSet<String> config = sampleConfig(sn, selecteds, unselecteds);
			boolean flag = true;
			if(!newList) {
				for(HashSet<String> s : pastSamples) {
					if(s.equals(config)){
						flag = false;
					}
				}
			}
			if(flag) {
				pastSamples.add(config);
				i++;
			}
		}

		return pastSamples;
	}

	
	public int getPathSize() {
		return rawData.length;
	}

	
	public long getSolSize() {
		return solSize;
	}
	
	
	public long getSolSize_local() {
		return solSize_local;
	}
}

//public ArrayList<HashSet<String>> sampleConfigs(long sample) {
//	Random randomGenerator = new Random();
//	ArrayList<Long> pastSN = new ArrayList<Long>();
//	ArrayList<Long> rndSeq = new ArrayList<Long>();
//
//	//		if(solSize >= sample) {
//	//			rndSeq = randomSequence(solSize, sample);
//	//		}else {
//	//			rndSeq = randomSequence(solSize, solSize);
//	//		}
//
//	int i = 0;
//	while((i < sample) && (i < solSize)) {
//		long sn = nextLong(randomGenerator, solSize);
//		if(!pastSN.contains(sn)){
//			samples.add(sampleConfig(sn));
//			pastSN.add(sn);
//
//			i++;
//		}
//	}
//	//			for(long ld : rndSeq){
//	//				samples.add(sampleConfig(l));
//	//			}
//
//	return samples;
//}
//
//private ArrayList<Long> randomSequence(Long solSize, long sample) {
//	ArrayList<Long> total = new ArrayList<Long>();
//	ArrayList<Long> seq = new ArrayList<Long>();
//
//	for(int i=0; i<solSize; i++) {
//		total.add(new Long(i));
//	}
//	Collections.shuffle(total);
//
//	for(int i = 0; i < sample; i++) {
//		seq.add(total.get(i));
//	}
//
//	return seq;
//}
