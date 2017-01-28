package com.vijet.ir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


public class DocIdMapping {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		File file = new File("docIdsToIntegerMapping.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		
		File file1 = new File("integerToDocIdMapping.txt");
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(file1));
		
		Properties prop = new Properties();
		prop.load(new FileInputStream(new File("docIds")));
		
		Iterator<Map.Entry<Object,Object>> docIdsIter = prop.entrySet().iterator();
		int count = 1;
		while(docIdsIter.hasNext()){
			Map.Entry<Object,Object> entry = docIdsIter.next();
			bw.write(entry.getKey().toString()+"="+ count +System.lineSeparator());
			bw1.write(count+"="+ entry.getKey().toString() +System.lineSeparator());
			++count;
		}
		
		bw.flush();bw1.flush();
		bw.close();bw1.close();
 	}
}
