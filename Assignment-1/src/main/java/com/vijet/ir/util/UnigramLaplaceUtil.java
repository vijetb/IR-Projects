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


public final class UnigramLaplaceUtil{
	private static final String LAPLACE_RESULTS_FILE_NAME = "./scoreresults/laplace_results.txt";
	
	public static Map<String, QueryScore> sort( Map<String, QueryScore> map ){
		List<Map.Entry<String, QueryScore>> list = new LinkedList<Map.Entry<String, QueryScore>>(map.entrySet());
		Collections.sort( list, new Comparator<Map.Entry<String, QueryScore>>(){
			@Override
			public int compare(Entry<String, QueryScore> o1,
					Entry<String, QueryScore> o2) {
				double qs1 = o1.getValue().getLaplaceScore();
				double qs2=  o2.getValue().getLaplaceScore();
				if(qs1==qs2)  
					return 0;  
				else if(qs1<qs2)  
					return 1;  
				else  
					return -1;  
			}
		} );

		Map<String, QueryScore> result = new LinkedHashMap<String, QueryScore>();
		for (Map.Entry<String, QueryScore> entry : list){
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

	public static void dumpResultsToFile(Map<String, Double> docsMap, String queryNumber) throws IOException {
		File unigramLaplaceFile = new File(LAPLACE_RESULTS_FILE_NAME);
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
