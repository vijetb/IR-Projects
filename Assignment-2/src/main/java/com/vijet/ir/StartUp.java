package com.vijet.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import com.vijet.ir.loadfiles.IndexDocuments;
import com.vijet.ir.loadfiles.IndexDocuments1;
import com.vijet.ir.util.CleanQuery;

public class StartUp {
	
	public static Properties intToDocIdsMapping = new Properties();
	
	static{
		try {
			intToDocIdsMapping.load(new FileInputStream(new File("integerToDocIdMapping.txt")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	static final String QUERY_FILE_PATH = "query_desc.51-100.short.txt";
	public static void main(String[] args) throws Exception {
		final String FOLDER_PATH = "ap89_collection";
		
		//cleanResults();
//		IndexDocuments indexDocuments = new IndexDocuments(FOLDER_PATH, null);
//		indexDocuments.indexDocuments();
//		indexDocuments.collaborateIntermediateResults();
//		indexDocuments.generateTermLookUpFile();
//		indexDocuments.testLookUpFile();
		//testForOkapi();
		
		// Testing
//		cleanResults();
		IndexDocuments1 indexDocuments1 = new IndexDocuments1(FOLDER_PATH, null);
		indexDocuments1.indexDocuments();
		//indexDocuments1.shrinkIndexFile();
//		indexDocuments1.collaborateIntermediateResults();
//		indexDocuments1.generateTermLookUpFile();
//		indexDocuments1.testLookUpFile();
//		
		//testForOkapi();
//		testForBM25();
//		testForJelinek();
		//testForProximity();
	}
	

	private static void testForJelinek() throws IOException {
		File queryFile = new File(QUERY_FILE_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(queryFile));
		String readLine = null;
		List<String> queries = new ArrayList<String>();
		while((readLine = reader.readLine())!= null){
			if(!readLine.trim().isEmpty())
					queries.add(readLine.trim());
			}
		System.out.println("Total number of queries: " + queries.size());
		
		ListIterator<String> queryIter = queries.listIterator();
		while(queryIter.hasNext()){
			StringBuilder query = new StringBuilder(queryIter.next());
			//remove all the punctuations
			String[] queryTerms = CleanQuery.cleanQuery(query.toString().toLowerCase());
			JelinekEngine jelinekEngine = new JelinekEngine();
			jelinekEngine.configure();
			jelinekEngine.generateScores(queryTerms);
			jelinekEngine.close();
		}
	}


	private static void testForOkapi() throws IOException {
		//Read the queries;
		File queryFile = new File(QUERY_FILE_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(queryFile));
		String readLine = null;
		List<String> queries = new ArrayList<String>();
		while((readLine = reader.readLine())!= null){
			if(!readLine.trim().isEmpty())
					queries.add(readLine.trim());
			}
		System.out.println("Total number of queries: " + queries.size());
		
		ListIterator<String> queryIter = queries.listIterator();
		while(queryIter.hasNext()){
			StringBuilder query = new StringBuilder(queryIter.next());
			//remove all the punctuations
			String[] queryTerms = CleanQuery.cleanQuery(query.toString().toLowerCase());
			OkapiEngine okapiEngine = new OkapiEngine();
			okapiEngine.configure();
			okapiEngine.generateScores(queryTerms);
			okapiEngine.close();
		}
	}
	
	private static void testForProximity() throws Exception {
		//Read the queries;
		final String QUERY_FILE_PATH_PROXIMITY = "query_desc.51-100.short_proximitySearch.txt";

		File queryFile = new File(QUERY_FILE_PATH_PROXIMITY/*"query_desc.51-100.short_temp.txt"*/);
		BufferedReader reader = new BufferedReader(new FileReader(queryFile));
		String readLine = null;
		List<String> queries = new ArrayList<String>();
		while((readLine = reader.readLine())!= null){
			if(!readLine.trim().isEmpty())
					queries.add(readLine.trim());
			}
		System.out.println("Total number of queries: " + queries.size());
		
		ListIterator<String> queryIter = queries.listIterator();
		while(queryIter.hasNext()){
			StringBuilder query = new StringBuilder(queryIter.next());
			//remove all the punctuations
			String[] queryTerms = CleanQuery.cleanQuery(query.toString().toLowerCase());
			ProximityScoreEngine pEngine = new ProximityScoreEngine();
			pEngine.configure();
			pEngine.generateScores(queryTerms);
			pEngine.close();
		}
	}
	
	private static void testForBM25() throws IOException {
		//Read the queries;
		File queryFile = new File(QUERY_FILE_PATH);
		BufferedReader reader = new BufferedReader(new FileReader(queryFile));
		String readLine = null;
		List<String> queries = new ArrayList<String>();
		while((readLine = reader.readLine())!= null){
			if(!readLine.trim().isEmpty())
					queries.add(readLine.trim());
			}
		System.out.println("Total number of queries: " + queries.size());
		
		ListIterator<String> queryIter = queries.listIterator();
		while(queryIter.hasNext()){
			StringBuilder query = new StringBuilder(queryIter.next());
			//remove all the punctuations
			String[] queryTerms = CleanQuery.cleanQuery(query.toString());
			BM25Engine bm25Engine = new BM25Engine();
			bm25Engine.configure();
			bm25Engine.generateScores(queryTerms);
			bm25Engine.close();
		}
	}


	private static void cleanResults() {
		File tempFolder = new File("Results");
		if(tempFolder.exists()){
			tempFolder.delete();
		}
		tempFolder.mkdir();
	}
	
}
