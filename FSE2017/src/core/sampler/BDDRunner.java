package core.sampler;

import jdd.bdd.BDD;

import java.util.Stack;

import core.io.FMParser;

import java.util.ArrayList;
import java.util.HashSet;

public class BDDRunner {
	private BDD bdd = new BDD(1000,1000);
	private int rootVar = 1;
	private int varSize;
	private long solSize;
	ArrayList<Long> solSizes = new ArrayList<Long>();
	private int nodeSize;
	private FeatureMap fm = new FeatureMap();
	private ArrayList<int[]> rawData;
	private byte[][] rawSet;


	public BDDRunner(String r) {
		//bdd.cleanup();

		//		root = r;
		//		rootVar = bdd.createVar();
	}


	public BDDRunner(String target, ArrayList<String> features) {
		//bdd.cleanup();

		//root = features.get(0);
		//rootVar = bdd.createVar();

		for(String f : features) {
			if(!fm.exists(f) /*&& !features.get(i).equals(target)*/) {
				fm.add(f);
			}
		}

		if(fm.exists(target)) {
			rootVar = bdd.biimp(rootVar, fm.getVar(target));
		}

		//rootVar = fm.getVar(features.get(0));

	}


	public void processGrammar(ArrayList<ArrayList<String>> clauses) {
		boolean init = true;

		for(ArrayList<String> clause : clauses) {
			int f = 0;

			switch(clause.get(0)) {
			case "mandatory": {
				int tmp = 1;
				for(int i = 2; i < clause.size(); i++) {
					if(!fm.exists(clause.get(i))) {
						fm.add(clause.get(i));
					}

					tmp = bdd.andTo(tmp, fm.getVar(clause.get(i)));
				}

				//				if(init) {
				//					rootVar = bdd.biimp(fm.getVar(clause.get(1)), tmp);
				//					bdd.ref(rootVar);
				//					bdd.deref(tmp);
				//					init = false;
				//				}else {
				f = bdd.biimp(fm.getVar(clause.get(1)), tmp);
				bdd.ref(f);
				bdd.deref(tmp);
				rootVar = bdd.andTo(rootVar, f);
				bdd.deref(f);
				//				}

				break;
			}
			case "optional": {
				int tmp = 0;
				for(int i = 2; i < clause.size(); i++) {
					if(!fm.exists(clause.get(i))) {
						fm.add(clause.get(i));
					}

					tmp = bdd.orTo(tmp, fm.getVar(clause.get(i)));
				}

				//				if(init) {
				//					rootVar = bdd.imp(tmp, fm.getVar(clause.get(1)));
				//					bdd.ref(rootVar);
				//					bdd.deref(tmp);
				//					init = false;
				//				}else {
				f = bdd.imp(tmp, fm.getVar(clause.get(1)));
				bdd.ref(f);
				bdd.deref(tmp);
				rootVar = bdd.andTo(rootVar, f);
				bdd.deref(f);
				//				}

				break;
			}
			case "alternative": {
				int[] term = new int[clause.size()-2];
				int tmp = 0;
				int preTmp = 0;

				for(int i = 0; i < term.length; i++) {
					term[i] = 1;
				}

				for(int i = 2; i < clause.size(); i++) {
					for(int j = 2; j < clause.size(); j++) {
						if(i == j){
							term[i-2] = bdd.andTo(term[i-2], fm.getVar(clause.get(j)));
						}else {
							int notVar = bdd.ref(bdd.not(fm.getVar(clause.get(j))));
							term[i-2] = bdd.andTo(term[i-2], notVar);
							bdd.deref(notVar);
						}
					}
					bdd.ref(term[i-2]);
					tmp = bdd.orTo(tmp, term[i-2]);
					bdd.deref(term[i-2]);

					preTmp = bdd.orTo(preTmp, fm.getVar(clause.get(i)));
				}

				bdd.ref(tmp);
				bdd.ref(preTmp);
				int f1 = bdd.biimp(fm.getVar(clause.get(1)), tmp);
				int f2 = bdd.biimp(fm.getVar(clause.get(1)), preTmp);
				bdd.ref(f1);
				bdd.ref(f2);
				bdd.deref(tmp);
				bdd.deref(preTmp);

				//				if(init) {
				//					rootVar = bdd.and(f1, f2);
				//					bdd.ref(rootVar);
				//					bdd.deref(f1);
				//					bdd.deref(f2);
				//					init = false;
				//				}else {
				f = bdd.and(f1, f2);
				bdd.ref(f);
				bdd.deref(f1);
				bdd.deref(f2);
				rootVar = bdd.andTo(rootVar, f);
				bdd.deref(f);
				//				}

				break;
			}
			case "multiple": {
				f = 0;

				break;
			}
			default: {
				f = 0;
			}
			}
		}
	}


	public void processConstraints(ArrayList<ArrayList<String>> clauses) { 
		for(ArrayList<String> clause : clauses) {
			Stack<Integer> stack = new Stack<Integer>();

			for(int i = 0; i < clause.size(); i++) {
				if(clause.get(i).equals("not")) {
					Integer t0 = stack.pop();	
					//System.out.println("not"+ t0.toString());
					stack.push(bdd.ref(bdd.not(t0)));
					bdd.deref(t0);
				}
				else if(clause.get(i).equals("and")) {
					Integer t1 = stack.pop();
					Integer t2 = stack.pop();
					//System.out.println(t1.toString() +"&"+ t2.toString());
					stack.push(bdd.ref(bdd.and(t2, t1)));
					bdd.deref(t1);
					bdd.deref(t2);
				}
				else if(clause.get(i).equals("or")) {
					Integer t3 = stack.pop();
					Integer t4 = stack.pop();
					//System.out.println(t3.toString() +"|"+ t4.toString());
					stack.push(bdd.ref(bdd.or(t4, t3)));
					bdd.deref(t3);
					bdd.deref(t4);
				}
				else if(clause.get(i).equals("implies")) {
					Integer t5 = stack.pop();
					Integer t6 = stack.pop();
					//System.out.println(t6.toString() +"->"+ t5.toString());
					stack.push(bdd.ref(bdd.imp(t6, t5)));
					bdd.deref(t5);
					bdd.deref(t6);
				}
				else {
					stack.push(fm.getVar(clause.get(i)));
				}
			}

			rootVar = bdd.andTo(rootVar, stack.pop());
		}
	}
	
	
	public ArrayList<int[]> convertBDD_tree() {
		varSize = bdd.numberOfVariables();
		solSize = (long)bdd.satCount(rootVar);

		rawData = bdd.getTree(rootVar);

		return rawData;
	}


	public byte[][] convertBDD_set() {
		rawSet = bdd.getSet(rootVar);

		return rawSet;
	}
	
	
	public byte[][] convertBDD_set(int vars) {
		rawSet = bdd.getSet(rootVar, vars);

		return rawSet;
	}


	public String getFName(int fid) {
		return fm.getName(fid);
	}


	public int getVarSize() {
		return varSize;
	}


	public long getSolSize() {
		return solSize;
	}

	public void terminate() {
		bdd.cleanup();
	}


	public void printJDDResult() {
		varSize = bdd.numberOfVariables();
		solSize = (long)bdd.satCount(rootVar);
		nodeSize = bdd.nodeCount(rootVar);
		//System.out.println();
		System.out.println("Number of variables: " + varSize);
		System.out.println("Number of nodes: " + nodeSize);
		System.out.println("Number of solutions: " + solSize);
	}


	public int getNodeSize() {
		return nodeSize;
	}


	private class FeatureMap {
		ArrayList<Feature> fm = new ArrayList<Feature>();
		int order = 1;

		public void add(String name){
			fm.add(new Feature(name, bdd.createVar(), order++));
		}

		public int getVar(String name) {
			//			if(name.equals(root)) {
			//				return rootVar;
			//			}

			for(Feature f : fm) {
				if(f.name.equals(name)) {
					return f.var;
				}
			}

			return -1;
		}

		public boolean exists(String name) {
			boolean ret = false;

			//			if(name.equals(root)) {
			//				return true;
			//			}

			for(Feature f : fm) {
				if(f.name.equals(name)) {
					return true;
				}
			}

			return ret;
		}

		public String getName(int index) {
			//			if(index == 0) {
			//				return root;
			//			}
			return fm.get(index).name;
		}

		public void printFM() {
			for(Feature f : fm) {
				System.out.println(f.name + "\t" + f.order);//"\t" + f.var+ 
			}
		}
	}

	private class Feature {
		String name;
		int var;
		int order;

		public Feature(String f, int v, int o) {
			name = f;
			var = v;
			order = o;
		}
	}
}
