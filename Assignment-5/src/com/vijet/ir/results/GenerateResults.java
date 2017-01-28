package com.vijet.ir.results;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Class that generates the Intermediate results in QREL Format for
 * Combined merged ranked files from elastic search
 * @author Vijet
 */
public class GenerateResults {
	/**
	 * File names for Combined Query
	 */
	private static final String COMBINED_QUERY_RESULTS_FILENAME = "qrelFileBoolean.txt";
	private static final String NON_BOOLEAN_COMBINED_QUERY_RESULTS_FILENAME = "qrelFileNonBoolean.txt";
	/**
	 * Pref values
	 */
	private final Map<String, Map<String, List<Integer>>> prefValues = new HashMap<String, Map<String, List<Integer>>>();
	
	/**
	 * Generates the Combined results
	 */
	public void generateCombinedResults(final String originalQrelFile) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(originalQrelFile));
		String line = new String();
		while((line=reader.readLine())!=null){
			String[] values = line.split(" ");
			if(prefValues.containsKey(values[0].trim())){
				if(prefValues.get(values[0].trim()).containsKey(values[2].trim())){
					prefValues.get(values[0].trim()).get(values[2].trim()).add(Integer.parseInt(values[3].trim()));
				}else{
					List<Integer> tempList = new ArrayList<Integer>();
					tempList.add(Integer.parseInt(values[3].trim()));
					prefValues.get(values[0].trim()).put(values[2].trim(),tempList);
				}
			}else{
				Map<String,List<Integer>> tempMap = new HashMap<String, List<Integer>>();
				List<Integer> tempList = new ArrayList<Integer>();
				tempList.add(Integer.parseInt(values[3].trim()));
				tempMap.put(values[2].trim(), tempList);
				prefValues.put(values[0].trim(), tempMap);
			}
		}
		
		reader.close();

		//Write to the qrel File:
		BufferedWriter bfw = new BufferedWriter(new FileWriter(COMBINED_QUERY_RESULTS_FILENAME));
		BufferedWriter bfwNonBoolean = new BufferedWriter(new FileWriter(NON_BOOLEAN_COMBINED_QUERY_RESULTS_FILENAME));
		for(Map.Entry<String, Map<String, List<Integer>>> entry: prefValues.entrySet()){
			String queryNo = entry.getKey();
			Map<String,List<Integer>> values = entry.getValue();
			for (Map.Entry<String, List<Integer>> queryEntry: values.entrySet()) {
				//FOR BOOLEAN
				int value1 = queryEntry.getValue().get(0);
				int value2 = queryEntry.getValue().get(1);
				int value3 = queryEntry.getValue().get(2);
				double avg = (value1+value2+value3)/6.0;
//				double avg = (value1+value2+value3)/3.0;
				if(avg < 0.5){
					bfw.write(queryNo+" "+ queryEntry.getKey()+" "+0+System.lineSeparator());
				}else{
					bfw.write(queryNo+" "+ queryEntry.getKey()+" "+1+System.lineSeparator());
				}
				// FOR NON-BOOLEAN
				bfwNonBoolean.write(queryNo+" "+ queryEntry.getKey()+" "+ (value1+value2+value3)/3.0 +System.lineSeparator());
				
//				double avg = (value1+value2+value3)/3.0;
//				if(avg < 1.0){
//					bfw.write(queryNo+" "+ queryEntry.getKey()+" "+0+System.lineSeparator());
//				}else{
//					bfw.write(queryNo+" "+ queryEntry.getKey()+" "+1+System.lineSeparator());
//				}
//				
//				// FOR NON-BOOLEAN
//				bfwNonBoolean.write(queryNo+" "+ queryEntry.getKey()+" "+ avg +System.lineSeparator());
			}
		}
		bfw.flush();
		bfw.close();
		bfwNonBoolean.flush();
		bfwNonBoolean.close();
	}
}	
