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

public class SiegmundFeatureParser {
	ArrayList<String> features = new ArrayList<String>();
	
	public ArrayList<String> ParseXML(String filename){
		try {	
			File inputFile = new File(filename);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			ParseHandler handler = new ParseHandler();
			saxParser.parse(inputFile, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return features;
	}
	
	public void printFeatures() {
		for(String f: features) {
			System.out.print(f + "|");
		}
		System.out.println("");
	}

	class ParseHandler extends DefaultHandler {
		private boolean nameSet = false;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equals("name"))
			{
				nameSet = true;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
		}

		@Override
		public void characters(char ch[], int start, int length) throws SAXException {
			String value = new String(ch, start, length).trim();

			if (value.length() == 0)
			{
				return; // ignore white space
			}

			//handle the value based on to which element it belongs
			if(nameSet) {
				features.add(value);
				nameSet = false;
			}
		}
	}
}

