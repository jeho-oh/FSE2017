package core.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import core.util.InfixToPostfix;

public class FMParser {
	ArrayList<String> features = new ArrayList<String>();
	ArrayList<ArrayList<String>> alternatives = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> gClauses = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> cClauses = new ArrayList<ArrayList<String>>();
	ArrayList<String> multiples = new ArrayList<String>();
	String target;
	
//	public void ParseFeatureList(String filename) {
//		try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
//			for(String line; (line = br.readLine()) != null;) {
//				features.add(line);
//			}
//			// line is not visible here.
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public FMParser() {
		
	}
	
	
	public ArrayList<String> getFeatures() {
		if(features.isEmpty()) {
			return null;
		}else {
			return features;
		}
	}
	
	public void ParseFeatureModel(String filename) {
		boolean constFlag = false;	

		try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
			for(String line; (line = br.readLine()) != null;) {
				int comment = line.indexOf("//");

				if(comment != -1) {
					if(comment == 0) {
						continue;
					}else {
						line = line.substring(0, comment -1);
					}
				}

				String[] stmt = line.split(";");
				for(int i = 0; i < stmt.length; i++) {
					if(stmt[i].length() != 0) {
						if(stmt[i].contains("%%")) {
							constFlag = true;
						}else {
							if(constFlag) {
								ConvertConstraints(stmt[i]);
							}
							else {
								ConvertGrammar(stmt[i]);
							}
						}
					}
				}
			}
			// line is not visible here.
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void addFeature(String feature) {
		if(!features.contains(feature)) {
			features.add(feature);
		}
	}
	
	private void ConvertGrammar(String stmt) {
		ArrayList<String> frag = new ArrayList<String>();
		ArrayList<String> atoms = new ArrayList<String>();
		ArrayList<String> optionals = new ArrayList<String>();
		
		boolean altFlag = false;
		boolean multiFlag = false;
		
		//Separate literals
		String[] raw = stmt.split("\\s+");
		
		if(raw.length == 0){
			return;
		}
		
		//Filter whitespace and colon
		for(int i = 0; i < raw.length; i++) {
			raw[i].trim();
			if(raw[i].length() != 0) {
				if(!raw[i].equals(":")) {
					frag.add(raw[i]);
				}
			}
		}

		//Get clause root feature
		String root = frag.get(0);
		addFeature(root);
		frag.remove(0);

		//Check multiples clause
		if(multiples.contains(root)) {
			multiFlag = true;
		}

		for(String s: frag) {
			if(s.equals("::")) {
				if(altFlag) {
					ArrayList<String> clause = new ArrayList<String>();
					if(multiFlag) {
						clause.add("multiple");
					}else {
						clause.add("alternative");
					}
					clause.add(root);
					clause.addAll(atoms);
					gClauses.add(clause);
				}else {
					if(!atoms.isEmpty()) {
						ArrayList<String> clause = new ArrayList<String>();
						clause.add("mandatory");
						clause.add(root);
						clause.addAll(atoms);
						gClauses.add(clause);
					}
					
					if(!optionals.isEmpty()) {
						ArrayList<String> clause = new ArrayList<String>();
						clause.add("optional");
						clause.add(root);
						clause.addAll(optionals);
						gClauses.add(clause);
					}
				}

				break;
			}

			if(s.charAt(s.length() - 1) == '+') {
				s = s.substring(0, s.length()-1);
				multiples.add(s);
			}

			if((s.charAt(0) == '[') && (s.charAt(s.length() - 1) == ']')) {
				
				s = s.substring(1, s.length()-1);

				optionals.add(s);

				addFeature(s);
			}else {
				if(s.equals("|")) {
					altFlag = true;
				} else {
					atoms.add(s);
					addFeature(s);
				}
			}
		}
	}

	private void ConvertConstraints(String stmt) {
		String[] raw = stmt.split("\\s+");
		
		ArrayList<String> frag = new ArrayList<String>();
		
		for(int i = 0; i < raw.length; i++) {
			raw[i].trim();
			int split = 0;
			
			for(int j = 0; j < raw[i].length(); j++) {
				if(raw[i].charAt(j) == '(') {
					if(j == raw[i].length() - 1) {
						frag.add(raw[i].substring(split, j));
						frag.add("(");
					}else {
						String s = raw[i].substring(split, j);
						if(s.length() != 0) {
							frag.add(s);
						}						
						frag.add("(");
						split = j + 1;
					}
				}else if(raw[i].charAt(j) == ')') {
					if(j == raw[i].length() - 1) {
						frag.add(raw[i].substring(split, j));
						frag.add(")");
					}else {
						String s = raw[i].substring(split, j);
						if(s.length() != 0) {
							frag.add(s);
						}
						frag.add(")");
						split = j + 1;
					}
				}
				else if(j == raw[i].length() - 1) {
					frag.add(raw[i].substring(split, j+1));
				}
			}
		}
		
		InfixToPostfix postFix = new InfixToPostfix(frag);
		
		cClauses.add(postFix.doTrans());
	}

	public ArrayList<ArrayList<String>> getGClauses() {
		return gClauses;
	}
	
	public ArrayList<ArrayList<String>> getCClauses() {
		return cClauses;
	}
	
	public void printClauses() {
		for(ArrayList<String> c: gClauses) {
			for(String s: c) {
				System.out.print(s + " ");
			}
			System.out.println();
		}
		
		for(ArrayList<String> c: cClauses) {
			for(String s: c) {
				System.out.print(s + " ");
			}
			System.out.println();
		}
	}

	public int getClauseNumbers() {
		return gClauses.size() + cClauses.size();
	}
	
	public HashSet<String> addMandatory() {
		HashSet<String> mandatories = new HashSet<String>();
		
		addMandatory_rec(target, mandatories);
		
		return mandatories;
	}
	
	private void addMandatory_rec(String root, HashSet<String> mandatories) {
		for(ArrayList<String> a : gClauses) {
			if(a.get(0).equals("mandatory") && a.get(1).equals(root)) {
				for(int i = 2; i < a.size(); i++) {
					mandatories.add(a.get(i));
					addMandatory_rec(a.get(i), mandatories);
				}
			}
		}
	}
	
	public boolean checkAlternative(String feature, HashSet<String> includes, HashSet<String> excludes) {
		for(ArrayList<String> a : gClauses) {
			if(a.contains(feature) && a.get(0).equals("alternative") && !a.get(1).equals(feature)) {
				for(int i = 2; i < a.size(); i++) {
					if(a.get(i).equals(feature)){
						includes.add(a.get(i));
						for(int j = 2; j < a.size(); j++) {
							if(i != j) {
								excludes.add(a.get(j));
							}
						}
						
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean passAlternatives(String feature, HashSet<String> passFeature) {
		for(ArrayList<String> a : gClauses) {
			if(a.contains(feature) && a.get(0).equals("alternative") && !a.get(1).equals(feature)) {
				for(int i = 2; i < a.size(); i++) {
					passFeature.add(a.get(i));
				}
				return true;
			}
		}
		
		return false;
	}
	
	
//	public boolean featureMandatory(String feature) {
//		for(ArrayList<String> a : gClauses) {
//			if(a.get(0).equals("mandatory")) {
//				for(int i = 2; i < a.size(); i++) {
//					if(a.get(i).equals(feature)) {
//						return true;
//					}
//				}
//			}
//		}
//		return false;
//	}
	
	
	public static void main(String[] args) {
		FMParser parser = new FMParser();
		parser.ParseFeatureModel("FeatureModel/BerkeleyDBC.m");

		parser.printClauses();
		
		//		Tool t = new Tool("FeatureModel/notepad.m", false);
		//		System.out.println(t.getModel().toString());
		//		System.out.println(guidsl.pattern.formula.toString());
	}
}
