package core;

import java.util.Scanner;

import core.seacher.ConfigSearcher;

public class Main {

	public static void main(String[] args) {
		
		String target = "LLVM";
		String fmFile = "FeatureModel/"+target+".m";
		String mdFile = "SiegmundData/"+target+".xml";
		
		int maxRec = 20;
		int initSample = 20;
		int recSample = 20;
		int repeat = 100;
		
		String outFile = "Results/"+target+"_"+maxRec+"_"+initSample+"_"+recSample+".csv";
		
		if(args.length == 8)  {
			target = args[0];
			fmFile = args[1];
			mdFile = args[2];
			outFile = args[3];
			maxRec = Integer.valueOf(args[4]);
			initSample = Integer.valueOf(args[5]);
			recSample = Integer.valueOf(args[6]);
			repeat = Integer.valueOf(args[7]);
		}else if(args[0].equals("-int")) {
			Scanner input = new Scanner(System.in);
			System.out.println(">> Input target:");
			System.out.print(">> ");
			target = input.next();
			
			System.out.println(">> Input feature model:");
			System.out.print(">> ");
			fmFile = input.next();
			
			System.out.println(">> Input benchmark data:");
			System.out.print(">> ");
			mdFile = input.next();
			
			System.out.println(">> Input output file name:");
			System.out.print(">> ");
			outFile = input.next();
			
			System.out.println(">> Input max number of recursions:");
			System.out.print(">> ");
			maxRec = input.nextInt();
			
			System.out.println(">> Input sample number for initial recursion:");
			System.out.print(">> ");
			initSample = input.nextInt();
			
			System.out.println(">> Input sample number for consequent recursions:");
			System.out.print(">> ");
			recSample = input.nextInt();
			
			System.out.println(">> Input number of repetitions:");
			System.out.print(">> ");
			repeat = input.nextInt();
		}
	
		ConfigSearcher cs = new ConfigSearcher(target, fmFile, mdFile);//
		for(int j = 0; j < repeat; j++) {
			cs.searchConfigs(maxRec, initSample, recSample);
		}
		
		cs.writeResults(outFile);
		System.out.println(">> Results written");
	}
}