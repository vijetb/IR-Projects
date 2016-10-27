package com.vijet.ir.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vijet.ir.model.Document;
import com.vijet.ir.util.StemUtil;


public final class UnigramJelinek {

	static final long TOTAL_NUMBER_OF_TERMS = 20970055;
	static final double LAMBDA = 0.7;

	public static Map<String,Long> queryTermCount = new HashMap<String,Long>();

	public static double jelineckScore(String docId,String term,long termFreq, long docLength, List<Document> list, long docFreq){
		long count = 0;
		Iterator<Document> docIter = list.iterator();
		while(docIter.hasNext()){
			Document doc = docIter.next();
			if(!doc.getDocId().equals(docId) && term.equals(doc.getTerm())){
				count = count + doc.getTermFreq();
			}
		}

		queryTermCount.put(term, count+termFreq);

		long lengthOfRemainingocs = TOTAL_NUMBER_OF_TERMS - docLength;

		double term1 = LAMBDA * Double.valueOf(termFreq) / docLength;
		double term2 = (1-LAMBDA)* count/ Double.valueOf(lengthOfRemainingocs);
		return (term1+term2);
	}

	public static double penalizedScore(String docId,String term,int freq, long docLength, List<Document> list, long docFreq){
		long count = 0;
		Iterator<Document> docIter = list.iterator();
		while(docIter.hasNext()){
			Document doc = docIter.next();
			if(!doc.getDocId().equals(docId) && term.equals(doc.getTerm())){
				count = count + doc.getTermFreq();
			}
		}

		long lengthOfRemainingocs = TOTAL_NUMBER_OF_TERMS - docLength;

		//double term1 = LAMBDA * Double.valueOf(termFreq) / docLength;
		double term2 = (1-LAMBDA)* count/ Double.valueOf(lengthOfRemainingocs);
		return (term2 * freq);
	}

	public static double penalizedScoreForNonDoc(String[] queryTerms,long docLength){
		double score = 0.0;
		long lengthOfRemainingocs = TOTAL_NUMBER_OF_TERMS - docLength;
		for (String term : queryTerms) {
			if(queryTermCount.containsKey(term)){
				double term2 = (1-LAMBDA)* queryTermCount.get(term)/ Double.valueOf(lengthOfRemainingocs);
				score = score+term2;
			}
		}

		return score;
	}

	public static double score(Document doc, Long lengthDoc, String[] queryTerms) {
		double score = 0.0;
		for(int i = 1; i < queryTerms.length; i++){
			String qterm = StemUtil.getStemOfWord(queryTerms[i].trim().toLowerCase());
			if(doc.getTerm().equals(qterm)){
				score = score + computeScore(qterm, doc.getTermFreq(), lengthDoc);
			}else{
				score = score + computeScoreWithoutQueryTerm(qterm,doc.getTermFreq(),lengthDoc);
			}
		}
		return score;
	}
	
	public static double computeScore(String term, Long termFreq, Long lengthDoc){
		long lengthOfRemainingocs = TOTAL_NUMBER_OF_TERMS - lengthDoc;
		double term1 = LAMBDA * Double.valueOf(termFreq) / lengthDoc;
		double term2 = (1-LAMBDA)* (queryTermCount.get(term)-termFreq)/ Double.valueOf(lengthOfRemainingocs);
		return (term2 + term1);
	}
	
	public static double computeScoreWithoutQueryTerm(String term, Long termFreq, Long lengthDoc){
		long lengthOfRemainingocs = TOTAL_NUMBER_OF_TERMS - lengthDoc;
		if(queryTermCount.containsKey(term)){
			double term2 = (1-LAMBDA)* queryTermCount.get(term)/ Double.valueOf(lengthOfRemainingocs);
			return (term2);
		}
		return 0.0;
	}
	
	public static void computeValuesForQueryTerms(String[] qterms, List<Document> list){
		Iterator<Document> docIter;
		for(int i = 1; i < qterms.length;i++){
			String qterm = StemUtil.getStemOfWord(qterms[i].trim().toLowerCase());
			docIter = list.iterator();
			while(docIter.hasNext()){
				Document doc = docIter.next();
				if(doc.getTerm().equals(qterm)){
					if(queryTermCount.containsKey(qterm)){
						long countT = doc.getTermFreq() + queryTermCount.get(qterm);
						queryTermCount.put(qterm,countT);
					}else{
						queryTermCount.put(qterm, doc.getTermFreq());
					}
				}
			}
		}
	}

	public static double score(Document doc, Long docLength, Long total_freq_count) {
		if(docLength==0 || total_freq_count==0){
			return 0;
		}
		long lengthOfRemainingocs = TOTAL_NUMBER_OF_TERMS - docLength;

		
		double term1 = LAMBDA * Double.valueOf(doc.getTermFreq()) / docLength;
		double term2 = (1-LAMBDA) * (total_freq_count-doc.getTermFreq())/ Double.valueOf(lengthOfRemainingocs);
		double sol = term1 + term2;
		if(sol == 0.0){
			System.out.println("test");
			System.exit(1);
		}
		return Math.log10(term1+term2);
	}
}
