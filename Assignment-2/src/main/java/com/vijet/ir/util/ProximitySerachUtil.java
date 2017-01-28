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

import com.vijet.ir.StartUp;
import com.vijet.ir.model.QueryScore;


public final class ProximitySerachUtil{
	private static final String PROXIMITY_SEARCH_UTIL = "./scoreresults/proximity_search_results.txt";
	
	public static void dumpResultsToFile(Map<String, Double> docsMap, String queryNumber) throws IOException {
		File unigramLaplaceFile = new File(PROXIMITY_SEARCH_UTIL);
		if(!unigramLaplaceFile.exists()){
			unigramLaplaceFile.createNewFile();
		}
		
		FileWriter fw = new FileWriter(unigramLaplaceFile, true);
		Iterator<Map.Entry<String, Double>> qsIter = docsMap.entrySet().iterator();
		int count =1;
		while(qsIter.hasNext()){
			if(count==2001){
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
