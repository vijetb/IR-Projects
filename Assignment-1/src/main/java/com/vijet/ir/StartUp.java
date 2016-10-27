package com.vijet.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.tartarus.snowball.ext.PorterStemmer;

import com.vijet.ir.elasticsearchclient.ESClient;
import com.vijet.ir.loadfiles.IndexDocuments;
import com.vijet.ir.model.QueryScore;
import com.vijet.ir.util.CleanQuery;


public class StartUp {

	public static void main(String[] args) throws Exception {
		final String FOLDER_PATH = "ap89_collection";
		final String QUERY_FILE_PATH = "query_desc.51-100.short.txt";

		ESClient elasticServerClient = new ESClient();
		IndexDocuments documents = null;
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		try {
			elasticServerClient.configure();
		} catch (Exception  e) {
			e.printStackTrace();
			System.exit(1);
		} 
		System.out.println("Client is successfully connected to Elasticserver!!!");
		
//		elasticServerClient.printDocLengthOfAllDocuments();
//		System.exit(1);
		documents = new IndexDocuments(FOLDER_PATH, elasticServerClient);
		documents.indexDocuments();
		System.out.println("__Indexing of document complete!___");
//		
//		System.exit(1);
		
		
		
		//printing all queries
		
//		elasticServerClient.printIds();
//		System.exit(1);
		int TOTAL_NO_OF_DOCUMENTS = 84678;
		
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
	
		ScoreEngine scoreEngine = new ScoreEngine(elasticServerClient);
		
		//Prebuild engine
		ListIterator<String> queryIter = queries.listIterator();
		while(queryIter.hasNext()){
			StringBuilder query = new StringBuilder(queryIter.next());
			//remove all the punctuations
			String[] queryTerms = CleanQuery.cleanQuery(query.toString());
			scoreEngine.fetchDocuments(queryTerms);
		}
		System.out.println("FETCHING DOCUMENT COMPLETE");
		//FOR OKAPI(WORKING)
		queryIter = queries.listIterator();
		while(queryIter.hasNext()){
			StringBuilder query = new StringBuilder(queryIter.next());
			//remove all the punctuations
			String[] queryTerms = CleanQuery.cleanQuery(query.toString());
			scoreEngine.generateOkapiScore(queryTerms);
		}
		
		//FOR BM-25(WORKING)
//		System.out.println("Completed");
//		queryIter = queries.listIterator();
//		while(queryIter.hasNext()){
//			StringBuilder query = new StringBuilder(queryIter.next());
//			//remove all the punctuations
//			String[] queryTerms = CleanQuery.cleanQuery(query.toString());
//			scoreEngine.generateBM25Score(queryTerms);
//		}
		
		//UNIGRAM LAPLACE
//		queryIter = queries.listIterator();
//		while(queryIter.hasNext()){
//			StringBuilder query = new StringBuilder(queryIter.next());
//			//remove all the punctuations
//			String[] queryTerms = CleanQuery.cleanQuery(query.toString());
//			scoreEngine.generateLaplaceScore(queryTerms);
//		}
		
		//Jelineck
//		queryIter = queries.listIterator();
//		while(queryIter.hasNext()){
//			StringBuilder query = new StringBuilder(queryIter.next());
//			//remove all the punctuations
//			String[] queryTerms = CleanQuery.cleanQuery(query.toString());
//			//scoreEngine.generateJelineckScore(queryTerms);
//			scoreEngine.generateJelineckScoreNew(queryTerms);
//		}
		
		elasticServerClient.closeConnection();
	}
	
	

}
