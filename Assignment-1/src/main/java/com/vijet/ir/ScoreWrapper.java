package com.vijet.ir;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import com.vijet.ir.model.QueryScore;
import com.vijet.ir.models.OkapiBM25;
import com.vijet.ir.models.OkapiTF;
import com.vijet.ir.models.TFIDF;
import com.vijet.ir.models.UnigramLaplace;
import com.vijet.ir.util.OkapiBM25Util;
import com.vijet.ir.util.OkapiUtil;
import com.vijet.ir.util.TFIDFUtil;
import com.vijet.ir.util.UnigramLaplaceUtil;

public class ScoreWrapper {
	private Map<String, QueryScore> docsMap;
	private final Map<String, Long> docLengthMap;

	public ScoreWrapper(Map<String,QueryScore> docsMap){
		this.docsMap = docsMap;
		docLengthMap = new HashMap<String, Long>();
	}

	public void generateOkapiResult(final String[] queryTerms){

		computeLengthOfAllDocs(); 

		Iterator<Map.Entry<String, QueryScore>> mapIter = docsMap.entrySet().iterator();
		while(mapIter.hasNext()){
			Map.Entry<String, QueryScore> tempMapEntry = mapIter.next();
			QueryScore okapiScore = tempMapEntry.getValue();

			double score = 0.0;
			for(int i = 1; i < queryTerms.length; i++){
				long termFrequency = getTermFreqFromDoc(okapiScore.getTermVectorDesc(),queryTerms[i].toLowerCase());
				score+=OkapiTF.okapiScore(termFrequency, docLengthMap.get(tempMapEntry.getKey()),docsMap.size());
			}
			okapiScore.setOkapiScore(score);
			tempMapEntry.setValue(okapiScore);
		}
		//print the list
//		Iterator<QueryScore> qsIter = docsMap.values().iterator();
//		while(qsIter.hasNext()){
//			System.out.println(qsIter.next().toString());
//		}
		
		//Sort the list
	//	docsMap = OkapiUtil.sort(docsMap);
		//print the list
//		Iterator<QueryScore> qsIter1 = docsMap.values().iterator();
//		while(qsIter1.hasNext()){
//			System.out.println(qsIter1.next().toString());
//		}
		//Print the 1000 values in a particular format.
//		try {
//			OkapiUtil.dumpResultsToFile(docsMap,queryTerms[0]);
//			System.out.println("RESULTS DUMPED FOR OKAPI for QUERY " + queryTerms[0]);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}

	private long getTermFreqFromDoc(String termVectorDesc, String term) {
		try{
		JSONObject obj = new JSONObject(termVectorDesc);
		JSONObject termVectorObject = obj.getJSONObject("term_vectors");
		JSONObject termVector_textObject = termVectorObject.getJSONObject("text");
		JSONObject termVector_text_termsObj = termVector_textObject.getJSONObject("terms");
		if(termVector_text_termsObj.has(term)){
			return termVector_text_termsObj.getJSONObject(term).getLong("term_freq");
		}
		}catch(Exception e){
			return 0L;
		}
		return 0L;
	}
	//WILL BE REMOVED
	private long getTotalTermsInCorpus() {
		QueryScore tempQueryScore = docsMap.entrySet().iterator().next().getValue();
		JSONObject obj = new JSONObject(tempQueryScore.getTermVectorDesc());
		JSONObject termVectorObject = obj.getJSONObject("term_vectors");
		JSONObject termVector_textObject = termVectorObject.getJSONObject("text");
		JSONObject termVector_text_fieldStatisticsObj = termVector_textObject.getJSONObject("field_statistics");
		return termVector_text_fieldStatisticsObj.getLong("sum_doc_freq");
	}

	private void computeLengthOfAllDocs() {
		Iterator<Map.Entry<String, QueryScore>> docsMapIter = docsMap.entrySet().iterator();
		while(docsMapIter.hasNext()){
			Map.Entry<String, QueryScore> tempEntry = docsMapIter.next();
			String id = tempEntry.getKey();
			QueryScore qScore = tempEntry.getValue();

			try{
			JSONObject obj = new JSONObject(qScore.getTermVectorDesc());
			JSONObject termVectorObject = obj.getJSONObject("term_vectors");
			JSONObject termVector_textObject = termVectorObject.getJSONObject("text");
			JSONObject termVector_termsObject = termVector_textObject.getJSONObject("terms");
			docLengthMap.put(id, Long.valueOf(termVector_termsObject.length()));
			}catch(Exception e){
//				e.printStackTrace();
//				System.out.println("ERROR REPORT");
//				System.out.println("id"+id);
//				System.out.println("ScoreDesc"+qScore.toString());
				System.out.println("Length of Id: " + id +" is 0");
				docLengthMap.put(id, 0L);
			}
		}
	}
	
	public void generateTF_IDFResults(final String[] queryTerms){
		Iterator<Map.Entry<String, QueryScore>> mapIter = docsMap.entrySet().iterator();
		while(mapIter.hasNext()){
			Map.Entry<String, QueryScore> tempMapEntry = mapIter.next();
			QueryScore queryScore = tempMapEntry.getValue();
			double score = 0.0;
			for(int i = 1; i < queryTerms.length; i++){
				long docFreq = getDocFreqForTerm(queryTerms[i], queryScore.getTermVectorDesc());
				score+=TFIDF.tfidfScore(queryScore.getOkapiScore(), docsMap.size(), docFreq);
			}
			queryScore.setTf_idfScore(score);
			tempMapEntry.setValue(queryScore);
		}
		//sort the list
	//	docsMap = OkapiUtil.sort(docsMap);
		//Print the 1000 values in a particular format.
//		try {
//			TFIDFUtil.dumpResultsToFile(docsMap,queryTerms[0]);
//			System.out.println("RESULTS DUMPED FOR TF-IDF for QUERY " + queryTerms[0]);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	private long getDocFreqForTerm(String term, String termVectorDesc) {
		JSONObject obj = new JSONObject(termVectorDesc);
		JSONObject termVectorObject = obj.getJSONObject("term_vectors");
		JSONObject termVector_textObject = termVectorObject.getJSONObject("text");
		JSONObject termVector_termsObject = termVector_textObject.getJSONObject("terms");
		
		if(termVector_termsObject.has(term)){
			return termVector_termsObject.getJSONObject(term).getLong("doc_freq");
		}
		
		return 0L;
	}

	public void generateBM25Results(String[] queryTerms) {
		Iterator<Map.Entry<String, QueryScore>> mapIter = docsMap.entrySet().iterator();
		while(mapIter.hasNext()){
			Map.Entry<String, QueryScore> tempMapEntry = mapIter.next();
			QueryScore queryScore = tempMapEntry.getValue();
			double score = 0.0;
			for(int i = 1; i < queryTerms.length; i++){
				long docFreq = getDocFreqForTerm(queryTerms[i], queryScore.getTermVectorDesc());
				long termFreq = getTermFreqFromDoc(queryScore.getTermVectorDesc(),queryTerms[i].toLowerCase());
				score+=OkapiBM25.okapiBM25Score(docsMap.size(), docFreq, termFreq, docLengthMap.get(tempMapEntry.getKey()));
			}
			queryScore.setBm25Score(score);
			tempMapEntry.setValue(queryScore);
		}
		//sort the list
		docsMap = OkapiBM25Util.sort(docsMap);
		//Print the 1000 values in a particular format.
//		try {
//			OkapiBM25Util.dumpResultsToFile(docsMap,queryTerms[0]);
//			System.out.println("RESULTS DUMPED FOR BM25 for QUERY " + queryTerms[0]);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public void generateLaplaceSmoothingResults(String[] queryTerms) {
		Iterator<Map.Entry<String, QueryScore>> mapIter = docsMap.entrySet().iterator();
		while(mapIter.hasNext()){
			Map.Entry<String, QueryScore> tempMapEntry = mapIter.next();
			QueryScore queryScore = tempMapEntry.getValue();
			double score = 0.0;
			for(int i = 1; i < queryTerms.length; i++){
				long termFreq = getTermFreqFromDoc(queryScore.getTermVectorDesc(),queryTerms[i].toLowerCase());
				score+=UnigramLaplace.lapaceSmoothingScore(termFreq, docLengthMap.get(tempMapEntry.getKey()));
			}
			queryScore.setLaplaceScore(score);
			tempMapEntry.setValue(queryScore);
		}
		//sort the list
		docsMap = UnigramLaplaceUtil.sort(docsMap);
		//Print the 1000 values in a particular format.
//		try {
//			UnigramLaplaceUtil.dumpResultsToFile(docsMap,queryTerms[0]);
//			System.out.println("RESULTS DUMPED FOR LAPALCE SMOOTHING for QUERY " + queryTerms[0]);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
	}
}
