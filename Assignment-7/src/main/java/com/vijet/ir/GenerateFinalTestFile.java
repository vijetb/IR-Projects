package com.vijet.ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.vijet.ir.util.JelineckUtil;

public class GenerateFinalTestFile {

	public static void main(String[] args) throws IOException {
		Map<String,String> testingIdsMapping = new LinkedHashMap<String, String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("mappings/testingIdsMapper.txt"));
			String line = new String();
			while((line=reader.readLine())!=null){
				String[] values = line.split(" ");
				testingIdsMapping.put(values[0].trim(), values[1].trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("TestQueiresFinalResults.txt"));
		Map<String,Double> scores = new HashMap<String, Double>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("info.txt"));
			
//			for(int i = 0 ; i < 7 ; i++){
//				System.out.println(Double.valueOf(reader.readLine()));
//			}
//			System.out.println(trainingIdsMapping.size());
			for (String key : testingIdsMapping.keySet()) {
				scores.put(key, Double.valueOf(reader.readLine().trim()));
			}
			//sort the values
			
			Map<String,Double> sortedMap = JelineckUtil.sortByComparator(scores);
			
			for (Entry<String, Double> string : sortedMap.entrySet()) {
				writer.write(string.getKey()+" "+ string.getValue()+System.lineSeparator());
			}
			
			writer.flush();writer.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
