package core.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ModelGenerator {
	private String data;
	
	
	public ModelGenerator() {
		
	}

	public void generateAlternativeFM(int degree, int height) {
		data = "";
		generateAlternativeFM_rec("Alternative_" + degree + "_" + height, degree, height);
		generateMFile("Alternative_" + degree + "_" + height, data);
	}
	
	private void generateAlternativeFM_rec(String f, int degree, int height) {
		if(height == 0) {
			return;
		}
		
		data += f + " : "; 
		for(int i = 0; i < degree; i++) {
			if(i == degree - 1) {
				data += f + "_" + i + " :: _" + f + " ;\n";
			}else {
				data += f + "_" + i + " | ";
				
			}	
		}
		
		for(int j = 0; j < degree; j++){
			generateAlternativeFM_rec((f+"_"+j), degree, height - 1);
		}
	}
	
	private void generateMFile(String target, String data) {
		FileWriter fw = null;

		try {
			fw = new FileWriter("FeatureModel/Alternative/" + target + ".m");
			fw.append(data);			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fw.flush();
				fw.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		ModelGenerator gen = new ModelGenerator();
		for(int i = 2; i < 3; i++) {
			for(int j = 16; j < 18; j++) {
				gen.generateAlternativeFM(i, j);
				System.out.println("Completed: " + i + "," + j);
			}
		}
	}
}

