package core.io.siegmundIO;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import core.sampler.ConfigSampler;
import core.seacher.Product;

public class CSVGenerator {
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";

	private ArrayList<String> features;

	public CSVGenerator(ArrayList<String> features) {
		this.features = features;
	}

	public void generatePerformanceData(ArrayList<Product> products, String filename) {
		FileWriter fw = null;
		Integer configNo = 0;;

		try {
			fw = new FileWriter(filename);

			fw.append("config#,performance,");
			for(String f: features) {
				fw.append(f);
				fw.append(COMMA_DELIMITER);
			}
			fw.append(NEW_LINE_SEPARATOR);

			for(Product p: products) {
				if(p.getPerformance() != 0) {
					fw.append(configNo.toString());
					fw.append(COMMA_DELIMITER);

					fw.append(String.valueOf(p.getPerformance()));
					fw.append(COMMA_DELIMITER);

					for(Boolean s: p.getConfigMap()) {
						if(s) {
							fw.append("1");
						}else {
							fw.append("0");
						}
						//fw.append(s.toString());
						fw.append(COMMA_DELIMITER);
					}
					fw.append(NEW_LINE_SEPARATOR);
				}
				++configNo;
			}
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
	
	public void generatePartitionData(ArrayList<Product> part1, ArrayList<Product> part2, String filename) {
		FileWriter fw = null;
		Integer configNo = 0;;

		try {
			fw = new FileWriter(filename);

			fw.append("config#,selected,");
			for(String f: features) {
				fw.append(f);
				fw.append(COMMA_DELIMITER);
			}

			fw.append("de-selected,");
			for(String f: features) {
				fw.append(f);
				fw.append(COMMA_DELIMITER);
			}

			fw.append(NEW_LINE_SEPARATOR);

			for(int i = 0; i < part1.size(); i++) {
				Product p1 = part1.get(i);
				Product p2 = part2.get(i);

				fw.append(configNo.toString());
				fw.append(COMMA_DELIMITER);
				
				if(p1.getPerformance() == 0) {
					fw.append(COMMA_DELIMITER);
					for(Boolean s: p1.getConfigMap()) {
						fw.append(COMMA_DELIMITER);
					}
				}else {
					fw.append(String.valueOf(p1.getPerformance()));
					fw.append(COMMA_DELIMITER);

					for(Boolean s: p1.getConfigMap()) {
						if(s) {
							fw.append("1");
						}else {
							fw.append("0");
						}
						//fw.append(s.toString());
						fw.append(COMMA_DELIMITER);
					}
				}
				
				if(p2.getPerformance() == 0) {
					fw.append(COMMA_DELIMITER);
					for(Boolean s: p2.getConfigMap()) {
						fw.append(COMMA_DELIMITER);
					}
				}else {
					fw.append(String.valueOf(p2.getPerformance()));
					fw.append(COMMA_DELIMITER);

					for(Boolean s: p2.getConfigMap()) {
						if(s) {
							fw.append("1");
						}else {
							fw.append("0");
						}
						//fw.append(s.toString());
						fw.append(COMMA_DELIMITER);
					}
				}
				
				fw.append(NEW_LINE_SEPARATOR);
				++configNo;
			}

			//			for(Product p: products) {
			//				if(p.getPerformance() != 0) {
			//					fw.append(configNo.toString());
			//					fw.append(COMMA_DELIMITER);
			//					
			//					fw.append(String.valueOf(p.getPerformance()));
			//					fw.append(COMMA_DELIMITER);
			//
			//					for(Boolean s: p.getConfiguration()) {
			//						if(s) {
			//							fw.append("1");
			//						}else {
			//							fw.append("0");
			//						}
			//						//fw.append(s.toString());
			//						fw.append(COMMA_DELIMITER);
			//					}
			//					fw.append(NEW_LINE_SEPARATOR);
			//				}
			//				++configNo;
			//			}
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
	
	public void generateSampleReport(ArrayList<ConfigSampler> samples, String filename) {
		FileWriter fw = null;

		try {
			fw = new FileWriter(filename);

			fw.append("target,features,clauses,nodes,paths,configs,samples,totalTime,bddTime");
			fw.append(NEW_LINE_SEPARATOR);
			
			for(ConfigSampler p: samples) {
				fw.append(p.printData());
				fw.append(NEW_LINE_SEPARATOR);
			}
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
	
	public void generateSampleData(ConfigSampler sample, String filename) {
		FileWriter fw = null;

		try {
			fw = new FileWriter(filename);

			fw.append("target,features,clauses,nodes,paths,configs,samples,totalTime,bddTime");
			fw.append(NEW_LINE_SEPARATOR);
			
			fw.append(sample.printData());
			fw.append(NEW_LINE_SEPARATOR);
			
			fw.append("Configurations");
			fw.append(NEW_LINE_SEPARATOR);
			
//			for(Sample p: sample.) {
//				fw.append(p.printData());
//				fw.append(NEW_LINE_SEPARATOR);
//			}
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
}
