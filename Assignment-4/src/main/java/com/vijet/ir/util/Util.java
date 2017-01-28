package com.vijet.ir.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utility class to sort the values and to print the values to the file.
 * @author Vijet Badigannavar
 */
public final class Util {
	private final static int MAX_NO_OF_LINKS = 501;
	/**
	 * Utility method to sort the Map
	 * @param unsortMap
	 * @return sortedMap sorted by the values.
	 */
	public static final Map<String, Double> sortByComparator(final Map<String, Double> unsortMap) {

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
	
	/**
	 * Prints the sorted Links to the disk
	 * @param sortedLinks sorted links
	 * @param fileName name of the file to dump the links
	 * @throws IOException
	 */
	public static final void dumpScoresToFile(final Map<String, Double> sortedLinks, final String fileName) throws IOException {
		BufferedWriter bfWriter = new BufferedWriter(new FileWriter(fileName));
		Iterator<Map.Entry<String, Double>> iter = sortedLinks.entrySet().iterator();
		int count = 1;
		while(iter.hasNext()){
			if(count++ == MAX_NO_OF_LINKS) break;
			Map.Entry<String, Double> entry = iter.next();
			bfWriter.write(entry.getKey()+"\t"+entry.getValue()+ System.lineSeparator());
		}
		bfWriter.flush();
		bfWriter.close();
	}
}
