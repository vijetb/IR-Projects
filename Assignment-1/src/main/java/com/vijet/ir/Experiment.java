package com.vijet.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.ext.PorterStemmer;

import com.vijet.ir.elasticsearchclient.ESClient;
import com.vijet.ir.models.OkapiTF;
import com.vijet.ir.util.OkapiUtil;

public class Experiment {
	
	
	public static void main(String[] args) throws IOException {
		List<String> docIds = new ArrayList<String>();
		Properties prop = new Properties();
		try {
			prop.load(Experiment.class.getClassLoader().getResourceAsStream("docIds"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Iterator<Map.Entry<Object, Object>> propIter = prop.entrySet().iterator();
		while(propIter.hasNext()){
			docIds.add(propIter.next().getKey().toString());
		}
		
		System.out.println(docIds.size());
		
	}
	
	public static String getStemOfWord(String input){
		
		PorterStemmer stemmer = new PorterStemmer();
	    stemmer.setCurrent(input);
	    stemmer.stem();
	    return stemmer.getCurrent();
	    
	    
	}
	
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

}
