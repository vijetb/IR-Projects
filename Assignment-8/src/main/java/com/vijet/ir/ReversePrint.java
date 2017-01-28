package com.vijet.ir;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.vijet.ir.util.JelineckUtil;

public class ReversePrint {
	
	private final static Map<String,String> tokenMapping = new HashMap<String, String>();
	private final static Map<String,String> tokenReverseMapping = new HashMap<String, String>();
	
	private final static Map<String, Double> topic1 = new TreeMap<String, Double>();
	private final static Map<String, Double> topic2 = new TreeMap<String, Double>();
	private final static Map<String, Double> topic3 = new TreeMap<String, Double>();
	private final static Map<String, Double> topic4 = new TreeMap<String, Double>();
	private final static Map<String, Double> topic5 = new TreeMap<String, Double>();
	private final static Map<String, Double> topic6 = new TreeMap<String, Double>();
	private final static Map<String, Double> topic7 = new TreeMap<String, Double>();
	private final static Map<String, Double> topic8 = new TreeMap<String, Double>();
	private final static Map<String, Double> topic9 = new TreeMap<String, Double>();
	private final static Map<String, Double> topic10 = new TreeMap<String, Double>();

	public static void main(String[] args) throws Exception {
		loadMappings();
		readFile();
	
	}
	
	public static void readFile() throws Exception{
		BufferedReader  reader = new BufferedReader(new FileReader("Model/sampleModel_87"));
		for(int i =0;i<10;i++){
			reader.readLine();
		}
		for(int i = 0 ; i <= 23803 ; i++){
			String[] values = reader.readLine().split(" ");
			topic1.put(tokenReverseMapping.get(values[0].trim()), Double.valueOf(values[1].trim()));
			topic2.put(tokenReverseMapping.get(values[0].trim()), Double.valueOf(values[2].trim()));
			topic3.put(tokenReverseMapping.get(values[0].trim()), Double.valueOf(values[3].trim()));
			topic4.put(tokenReverseMapping.get(values[0].trim()), Double.valueOf(values[4].trim()));
			topic5.put(tokenReverseMapping.get(values[0].trim()), Double.valueOf(values[5].trim()));
			topic6.put(tokenReverseMapping.get(values[0].trim()), Double.valueOf(values[6].trim()));
			topic7.put(tokenReverseMapping.get(values[0].trim()), Double.valueOf(values[7].trim()));
			topic8.put(tokenReverseMapping.get(values[0].trim()), Double.valueOf(values[8].trim()));
			topic9.put(tokenReverseMapping.get(values[0].trim()), Double.valueOf(values[9].trim()));
			topic10.put(tokenReverseMapping.get(values[0].trim()), Double.valueOf(values[10].trim()));

		}
		
		Map<String,Double> temp1 = JelineckUtil.sortByComparator(topic1);
		printDocs(temp1,"topic-1");
		Map<String,Double> temp2 = JelineckUtil.sortByComparator(topic2);
		printDocs(temp2,"topic-2");
		Map<String,Double> temp3 = JelineckUtil.sortByComparator(topic3);
		printDocs(temp3,"topic-3");
		Map<String,Double> temp4 = JelineckUtil.sortByComparator(topic4);
		printDocs(temp4,"topic-4");
		Map<String,Double> temp5 = JelineckUtil.sortByComparator(topic5);
		printDocs(temp5,"topic-5");
		Map<String,Double> temp6 = JelineckUtil.sortByComparator(topic6);
		printDocs(temp6,"topic-6");
		Map<String,Double> temp7 = JelineckUtil.sortByComparator(topic7);
		printDocs(temp7,"topic-7");
		Map<String,Double> temp8 = JelineckUtil.sortByComparator(topic8);
		printDocs(temp8,"topic-8");
		Map<String,Double> temp9 = JelineckUtil.sortByComparator(topic9);
		printDocs(temp9,"topic-9");
		Map<String,Double> temp10 = JelineckUtil.sortByComparator(topic10);
		printDocs(temp10,"topic-10");
		
	}
	
	public static void printDocs(Map<String,Double> topicMap, String topic){
		System.out.println("------------------"+ topic +"-----------------");
		int count = 0;
		for (Map.Entry<String, Double> entry : topicMap.entrySet()) {
			if(count<10){
				System.out.println(entry.getKey());
			}else{
				break;
			}
			++count;
		}
	}
	
	public static void loadMappings(){
		// update the word mappings
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("MappingFiles/TokensMapping_87.txt"));
			String line = new String();
			while((line=reader.readLine())!=null){
				String[] data = line.split(" ");
				tokenMapping.put(data[0], data[1]);
				tokenReverseMapping.put(data[1], data[0]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
