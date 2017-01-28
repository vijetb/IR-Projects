package com.vijet.ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdk.jfr.events.FileWriteEvent;

import com.vijet.ir.model.DOC;

public class Experiment {
	public static void main(String[] args) throws IOException {
		File folder = new File("MappingFiles");
		Map<String,Integer> wordCount = new HashMap<String, Integer>();
		File[] docFiles = folder.listFiles();
		BufferedWriter writer = new BufferedWriter(new FileWriter("tfMapping.txt"));
		for (File docFile : docFiles) {
			BufferedReader reader = new BufferedReader(new FileReader(docFile));
			String line = new String();
			while((line=reader.readLine())!=null){
				String[] values = line.split(" ");
				if(wordCount.containsKey(values[0].trim())){
					wordCount.put(values[0].trim(), wordCount.get(values[0].trim())+1);
				}else{
					wordCount.put(values[0].trim(), 1);
				}
			}
			reader.close();
		}
		
		
		for (Entry<String, Integer> entry : wordCount.entrySet()) {
			writer.write(entry.getKey()+" "+entry.getValue()+System.lineSeparator());
		}
		
		writer.flush();
		writer.close();
	}
}
