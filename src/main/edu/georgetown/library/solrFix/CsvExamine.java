package edu.georgetown.library.solrFix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CsvExamine {
    private static Vector<String> parseLine(String line) {
        Vector<String> parts = new Vector<String>();
        String[] spart = line.split(",");
        
        StringBuffer buf = new StringBuffer();
        for(String part: spart) {
        	if (part == null) part = "";
        	if (buf.length() == 0) {
            	if (part.length() < 2) {
            		parts.add(part);
            	} else {
                	char chfirst = part.charAt(0); 
                	char chlast = part.charAt(part.length()-1); 
                	if (chfirst != '\'' && chfirst != '"') {
                		parts.add(part);                		
                	} else if (chfirst == chlast) {
                		parts.add(part);
                	} else {
                		buf.append(part); 
                	}
            	}
        	} else {
        		buf.append(",");
        		buf.append(part);

        		if (buf.length() >= 2) {
                	char chfirst = buf.charAt(0); 
                	char chlast = buf.charAt(buf.length()-1);         			
                	if (chfirst == chlast) {
                		parts.add(buf.toString());
                		buf.setLength(0);
                	}
        		}
        	}
        	
        }
        return parts;
        
    }
    
    public static void main(String[] args) throws IOException {
    	File f = new File("c:\\temp\\csvDspace\\temp.1090000.csv");
    	int count = 0;
    	
    	CSVWriter cw = new CSVWriter(new FileWriter(new File("c:\\temp\\csvDspace\\temp.1090000.csv.txt")));
    	
    	try (CSVReader reader = new CSVReader(new FileReader(f))){
            String [] nextLine = new String[0];
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                System.out.println(nextLine.length);
                cw.writeNext(nextLine);
            }
     	} catch (IOException e) {
			e.printStackTrace();
		}
    }
}