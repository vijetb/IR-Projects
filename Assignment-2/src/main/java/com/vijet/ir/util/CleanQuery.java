package com.vijet.ir.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;




public class CleanQuery {
	private static final Set<String> stopWords = new HashSet<String>();
	
	static{
		File stopListFile = new File("stoplist.txt");
		
		try (BufferedReader br = new BufferedReader(new FileReader(stopListFile))){
			String readLine;
			while ((readLine = br.readLine()) != null) {
				stopWords.add(readLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static final String[] cleanQuery(String query){
//		query.trim();
//		query = query.replaceAll(", ", " ");
//		query = query.replaceAll("\\.", "");
//		return query.split(" ");
		
		
//		englishStemmer stemmer = new englishStemmer();
//
		query.trim();
		query = query.replaceAll(",", "");
		query = query.replaceAll("\\.", "");
		query = query.replaceAll("[^a-zA-Z0-9 ]", "");
//		
		List<String> queryStringList = new LinkedList<String>(Arrays.asList(query.split(" ")));
		ListIterator<String> strIter = queryStringList.listIterator();
		while(strIter.hasNext()){
			String term = strIter.next();
			if(stopWords.contains(term)){
				strIter.remove();
				continue;
			}
		}
		return queryStringList.toArray(new String[queryStringList.size()]);
	}
}	
