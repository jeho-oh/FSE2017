package core.io.siegmundIO;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import core.seacher.Product;

public class SiegmundDataParser {
	ArrayList<String> features;
	ArrayList<Product> products = new ArrayList<Product>();
	
	int productCount = 1;;
	
	public SiegmundDataParser(ArrayList<String> features) {
		this.features = features;
	}
	
	public ArrayList<Product> ParseXML(String filename){
		try {	
			File inputFile = new File(filename);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			ParseHandler handler = new ParseHandler();
			saxParser.parse(inputFile, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return products;
	}
	
	public void printProducts() {
		for(Product p: products) {
			System.out.print(" > " + p.getPerformance() + " : \t");
			
			for(Boolean s: p.getConfigMap()) {
				System.out.print(s + "|");
			}
			System.out.println("");
		}
	}
	
	class ParseHandler extends DefaultHandler {
		private double measurement;
		private HashSet<String> configs;

		private boolean configSet = false;
		private boolean measureSet = false;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equals("row"))
			{
				configSet = false;
				measureSet = false;
			} else if (qName.equals("data")) {
				String attribute = attributes.getValue("columname");
				
				if(attribute.equals("Configuration")) {
					configSet = true;
					measureSet = false;
				} else if(attribute.equals("Measured Value")) {
					configSet = false;
					measureSet = true;
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equals("row")) {
				Product p = new Product(features, measurement, configs);
				products.add(p);
				//System.out.println("Converted meaurement : " + productCount);
				productCount++;
			}
		}

		@Override
		public void characters(char ch[], int start, int length) throws SAXException {
			String value = new String(ch, start, length).trim();

			if (value.length() == 0)
			{
				return; // ignore white space
			}

			//handle the value based on to which element it belongs
			if(configSet) {
				configs = new HashSet<String>();
				StringTokenizer st = new StringTokenizer(value, ", ");
				while(st.hasMoreElements()) {
					configs.add((String) st.nextElement());
				}
				configSet = false;
			}
			if(measureSet) {
				value = value.replace(',', '.');
				measurement = Double.parseDouble(value);
				measureSet = false;
			}
		}
	}
}

