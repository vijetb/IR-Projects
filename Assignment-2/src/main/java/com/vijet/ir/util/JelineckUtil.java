package com.vijet.ir.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vijet.ir.model.QueryScore;


public final class JelineckUtil{
	private static final String JELINECK_RESULTS_FILE_NAME = "./scoreresults/jelineck_results.txt";
	
	public static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Double>> list = 
			new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	public static void dumpResultsToFile(Map<String, Double> docsMap, String queryNumber) throws IOException {
		File okapiFile = new File(JELINECK_RESULTS_FILE_NAME);

		if(!okapiFile.exists())
			okapiFile.createNewFile();
		
		FileWriter fw = new FileWriter(okapiFile,true);
		Iterator<Map.Entry<String, Double>> qsIter = docsMap.entrySet().iterator();
		int count = 1;
		while(qsIter.hasNext()){
			if(count==1501){
				break;
			}
			Map.Entry<String, Double> tempEntry = qsIter.next();
			fw.write(queryNumber+" "+ "Q0" + " " + tempEntry.getKey()+" "+ count + " " + tempEntry.getValue()+" " +"Sample");
			++count;
			fw.append(System.lineSeparator());
		}
		
		fw.flush();
		fw.close();
	}
}
