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

public class Experiment1 {
	public static void main(String[] args) throws IOException {
		File folder = new File("Topics");
		Map<String,Integer> wordCount = new HashMap<String, Integer>();
		File[] docFiles = folder.listFiles();
		BufferedWriter writer = new BufferedWriter(new FileWriter("sortedValues.txt"));
		for (File docFile : docFiles) {
			String[] values = docFile.getName().split("_");
			BufferedReader reader = new BufferedReader(new FileReader(docFile));
			String line = new String();
			while((line=reader.readLine())!=null){
				if(line.contains("--")){
					writer.write(values[1]+line.replaceAll("-", "").replaceAll("/.txt", ""));
				}else if(line.contains("AVG:")){
					String[] val = line.split(":");
					writer.write(val[1]+System.lineSeparator());	
				}
			}
			//writer.write("--------------------------------------------------"+System.lineSeparator());
			reader.close();
		}
		
		
		for (Entry<String, Integer> entry : wordCount.entrySet()) {
			writer.write(entry.getKey()+" "+entry.getValue()+System.lineSeparator());
		}
		
		writer.flush();
		writer.close();
	}
}
