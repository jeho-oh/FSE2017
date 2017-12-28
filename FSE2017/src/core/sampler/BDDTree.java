package core.sampler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import javax.swing.text.StyleContext.SmallAttributeSet;

import jdd.bdd.BDD;

public class BDDTree {
	String target;
	ArrayList<String> features;
	
	private ArrayList<int[]> rawData = new ArrayList<int[]>();
	private ArrayList<Node> tree = new ArrayList<Node>();
	private ArrayList<HashSet<String>> configs = new ArrayList<HashSet<String>>();
	
	private int nodeSize = 0;
	private long solSize;
	private long solSize_local;

	private Node ROOT;
	private Node TRUE;
	private Node FALSE;

	ArrayList<ArrayList<Integer>> pool = new ArrayList<ArrayList<Integer>>();
	private ArrayList<HashSet<String>> samples = new ArrayList<HashSet<String>>();
	ArrayList<HashSet<String>> pastSamples = new ArrayList<HashSet<String>>();
	
	public BDDTree(String target, ArrayList<String> features) {
		this.target = target;
		this.features = features;
	}
	
	
	public void MakeTree(ArrayList<int[]> rawData) {
		this.rawData = rawData;
		TRUE = new Node(1);
		FALSE = new Node(0);	

		if(rawData.size() != 0) {
			ROOT = new Node(rawData.get(0)[0]);
		}
	}

	
	/**
	 * Assign possible number of configurations for each node
	 * @param node	Node to compute its number of configurations
	 * @param root	The parent of this node
	 * @return node.config	Number of configurations for this node
	 * 						End return value is number of total configurations
	 */
	private long countConfig_rec(Node node, ArrayList<Integer> infids, ArrayList<Integer> exfids) {
		//Invalid terminal - no configuration available if solution is invalid
		if(node.equals(FALSE)) {
			return 0;
		}
		//Valid terminal
		//Sum up eliminated nodes before & after the last node of the path
		else if(node.equals(TRUE)) {
			return 1;
		}
		//Recursive steps
		else {
			int lowRed = 0;
			int highRed = 0;
			node.lowconfig = 1;
			node.highconfig = 1;
			
			//Recursively get configuration numbers from its successors
			for(Integer f : infids){
				if(node.fid == f) {
					node.lowconfig = 0;
				}
				
				if((f > node.fid) && (f < node.low.fid)) {
					lowRed++;
				}
				if((f > node.fid) && (f < node.high.fid)) {
					highRed++;
				}
			}
			
			for(Integer f : exfids){
				if(node.fid == f) {
					node.highconfig = 0;
				}
				
				if((f > node.fid) && (f < node.low.fid)) {
					lowRed++;
				}
				if((f > node.fid) && (f < node.high.fid)) {
					highRed++;
				}
			}
			
			node.lowconfig *= countConfig_rec(node.low, infids, exfids) * (long)Math.pow(2, (node.low.fid - node.fid - lowRed - 1));
			node.highconfig *= countConfig_rec(node.high, infids, exfids) * (long)Math.pow(2, (node.high.fid - node.fid - highRed - 1));
			
			return node.config = node.lowconfig + node.highconfig;
		}
	}
	
	
	public long countConfig() {
		ArrayList<Integer> infids = new ArrayList<Integer>();
		ArrayList<Integer> exfids = new ArrayList<Integer>();

		return solSize = countConfig_rec(ROOT, infids, exfids);
	}
	
	
	public long countConfig(HashSet<String> selecteds, HashSet<String> unselecteds) {		
		ArrayList<Integer> infids = new ArrayList<Integer>();
		ArrayList<Integer> exfids = new ArrayList<Integer>();
		
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

		return solSize_local = countConfig_rec(ROOT, infids, exfids);
	}
	
	
	private void sampleConfig_rec(long sn, Node node, HashSet<String> config, ArrayList<Integer> inc, ArrayList<Integer> exc) {
		if(sn <= node.lowconfig) {
			for(int i = node.fid+1; i < node.low.fid; i++) {
				if((sn % 2) == 1) {
					config.add(features.get(i));
				}
				sn /= 2;
			}
			
			if(!node.low.equals(TRUE)) {
				sampleConfig_rec(sn, node.low, config, inc, exc);
			}
		}else {
			config.add(features.get(node.fid));
			sn -= node.lowconfig;
			
			for(int i = node.fid+1; i < node.high.fid; i++) {
				if((sn % 2) == 1) {
					config.add(features.get(i));
				}
				sn /= 2;
			}
			
			if(!node.high.equals(TRUE)) {
				sampleConfig_rec(sn, node.high, config, inc, exc);
			}
		}
	}
	
	
	private HashSet<String> sampleConfig(long sn, ArrayList<Integer> inc, ArrayList<Integer> exc){
		HashSet<String> config = new HashSet<String>();
		
		sampleConfig_rec(sn, ROOT, config, inc, exc);
		
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

		countConfig();
		
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

			samples.add(sampleConfig(sn, null, null));
			i++;
		}

		pastSamples.addAll(samples);
		
		return samples;
	}

	
	public ArrayList<HashSet<String>> sampleConfigs(long sample, HashSet<String> selecteds, HashSet<String> unselecteds, Boolean newList) {
		Random randomGenerator = new Random();
		ArrayList<ArrayList<Integer>> pool_local = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> infids = new ArrayList<Integer>();
		ArrayList<Integer> exfids = new ArrayList<Integer>();
		
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
		
		countConfig(selecteds, unselecteds);

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
		
		if(newList){
			pastSamples.clear();
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

			HashSet<String> config = sampleConfig(sn, infids, exfids);
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
	
	
	public void printConfigs() {
		for(HashSet<String> l : configs) {
			for(String s : l) {
				System.out.print(s + "|");
			}
			System.out.println();
		}
	}

	
	private void printTree_rec(Node node) {
		if(node == FALSE) {
			return;
		}else if(node == TRUE) {
			return;
		}else {
			System.out.println(node.nid + "\t" + node.fid + "\t" + node.low.nid + "\t" + node.high.nid + "\t" + node.config);	
			printTree_rec(node.low);
			printTree_rec(node.high);
		}
	}

	
	public void printTree() {
		System.out.println();
		System.out.println("Total number of configs: " + solSize);
		System.out.println("Enumerated tree");
		System.out.println("nid" + "\tfid" + "\tlow" + "\thigh" + "\tconfig" + "\tfeature");
		printTree_rec(ROOT);
		System.out.println();
	}

	
	public long getSolSize() {
		return solSize;
	}
	
	
	public long getSolSize_local() {
		return solSize_local;
	}

	
	public int getNodeSize() {
		return nodeSize;
	}

	
	private Node checkExists(int nid, ArrayList<Node> t) {
		for(Node n : t) {
			if(n.nid == nid) {
				return n;
			}
		}
		return null;
	}
	
	
	class Node {
		int nid;
		int fid;
		long config;
		Node high;
		Node low;
		long lowconfig = -1;
		long highconfig = -1;

		public Node(int nid) {
			if(nid == 0) {
				this.nid = 0;
				fid = features.size();
				high = null;
				low = null;
				config = 0;
				tree.add(this);
			}else if (nid == 1) {
				this.nid = 1;
				fid = features.size();
				high = null;
				low = null;
				config = 1;
				tree.add(this);
			}else {
				nodeSize++;
				
				this.nid = nid;
				int[] data = null;

				for(int[] d : rawData){
					if(d[0] == this.nid){
						data = d;
						break;
					}
				}

				if(data == null){
					System.out.println("Tree broken");
				}else {
					fid = data[1];
					tree.add(this);

					if(data[2] == 0) {
						this.low = FALSE;
					}else if(data[2] == 1) {
						this.low = TRUE;
					}else {
						Node nl = checkExists(data[2], tree);
						if(nl == null) {
							this.low = new Node(data[2]);
						}else {
							this.low = nl;
						}
					}
					
					if(data[3] == 0) {
						this.high = FALSE;
					}else if(data[3] == 1) {
						this.high = TRUE;
					}else {
						Node nh = checkExists(data[3], tree);
						if(nh == null) {
							this.high = new Node(data[3]);
						}else {
							this.high = nh;
						}
					}
				}
			}
		}
	}
}

///**
// * Assign possible number of configurations for each node
// * @param node	Node to compute its number of configurations
// * @param root	The parent of this node
// * @param red	List of eliminated nodes, accumulated throughout the path
// * @return node.config	Number of configurations for this node
// * 						End return value is number of total configurations
// */
//private long countConfig_rec(Node node, Node root, ArrayList<String> red) {
//	//Invalid terminal - no configuration available if solution is invalid
//	if(node == FALSE) {
//		return 0;
//	}
//	//Valid terminal
//	//Sum up eliminated nodes before & after the last node of the path
//	else if(node == TRUE) {
//		if((varSize - root.fid) > 1) {
//			for(int i = root.fid + 1; i < varSize; i++) {
//				root.reduced.add(runner.getFName(i));
//			}
//		}
//		//Enumerate all 'don't care' nodes 
//		return (int)Math.pow(2, root.reduced.size());
//	}
//	//Recursive steps
//	else {
//		//Accumulate all eliminated node between this node and its parent
//		if(node != ROOT) {
//			node.reduced.addAll(red);
//			if((node.fid - root.fid) > 1) {
//				for(int i = root.fid + 1; i < node.fid; i++) {
//					node.reduced.add(runner.getFName(i));
//				}
//			}
//		}
//		//Recursively get configuration numbers from its successors
//		long configLow = countConfig_rec(node.low, node, node.reduced);
//		long configHigh = countConfig_rec(node.high, node, node.reduced);
//		return node.config = configLow + configHigh;
//	}
//}

//private HashSet<Integer> sampleConfig_rec(long sn, Node node, HashSet<String> config, HashSet<String> inc, HashSet<String> exc) {
//	if(node.high == TRUE && node.low == FALSE){
//		config.add(node.name);
//
//		for(int i = node.reduced.size() - 1; i > -1; i--){
//			long select = sn / (long)Math.pow(2, i);
//			sn = sn % (int)Math.pow(2, i);
//
//			if(select == 1) {
//				config.add(node.reduced.get(i));
//			}
//		}
//	}else if(node.high == FALSE && node.low == TRUE) {
//		for(int i = node.reduced.size() - 1; i > -1; i--){
//			long select = sn / (long)Math.pow(2, i);
//			sn = sn % (int)Math.pow(2, i);
//
//			if(select == 1) {
//				config.add(node.reduced.get(i));
//			}
//		}
//	}else if(sn+1 > node.low.config) {
//		config.add(node.name);
//
//		if(node.high == TRUE) {
//			for(int i = node.reduced.size() - 1; i > -1; i--){
//				long select = sn / (long)Math.pow(2, i);
//				sn = sn % (long)Math.pow(2, i);
//
//				if(select == 1) {
//					config.add(node.reduced.get(i));
//				}
//			}
//		}else {
//			sampleConfig_rec(sn - node.low.config, node.high, config);
//		}
//	}else {
//		if(node.low == TRUE) {
//			for(int i = node.reduced.size() - 1; i > -1; i--){
//				long select = sn / (long)Math.pow(2, i);
//				sn = sn % (long)Math.pow(2, i);
//
//				if(select == 1) {
//					config.add(node.reduced.get(i));
//				}
//			}
//		}else {
//			sampleConfig_rec(sn, node.low, config);
//		}
//	}
//}