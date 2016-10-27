package com.vijet.ir;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import com.vijet.ir.elasticsearchclient.ESClient;
import com.vijet.ir.model.DocScore;
import com.vijet.ir.model.Document;
import com.vijet.ir.models.OkapiBM25;
import com.vijet.ir.models.OkapiTF;
import com.vijet.ir.models.TFIDF;
import com.vijet.ir.models.UnigramJelinek;
import com.vijet.ir.models.UnigramLaplace;
import com.vijet.ir.util.JelineckUtil;
import com.vijet.ir.util.OkapiBM25Util;
import com.vijet.ir.util.OkapiUtil;
import com.vijet.ir.util.StemUtil;
import com.vijet.ir.util.TFIDFUtil;
import com.vijet.ir.util.UnigramLaplaceUtil;

public class ScoreEngine {
	private final String INDEX_NAME = "ap_dataset";
	private final String INDEX_TYPE = "document";
	private final ESClient elasticServerClient;

	private final Map<String, List<Document>> querytermList = new HashMap<>();
	private static final List<String> docIds = new ArrayList<String>();

	public static Properties docsLengthMap = new Properties();

	public static Properties docProperties = new Properties();
	static{
		try {
			docsLengthMap.load(ScoreEngine.class.getClassLoader().getResourceAsStream("docLength.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Generate DOC IDS
		docProperties = new Properties();
		try {
			docProperties.load(ScoreEngine.class.getClassLoader().getResourceAsStream("docIds"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Iterator<Map.Entry<Object, Object>> propIter = docProperties.entrySet().iterator();
		while(propIter.hasNext()){
			docIds.add(propIter.next().getKey().toString());
		}

	}

	public ScoreEngine(ESClient client){
		this.elasticServerClient = client;
	}

	public void fetchDocuments(String[] queryTerms) {
		System.out.println("New Query: " + queryTerms[0] + " " + (new SimpleDateFormat("HH:MM:SS")).format(new Date()));

		for(int i = 1; i < queryTerms.length; i++){

			String term = StemUtil.getStemOfWord(queryTerms[i].trim().toLowerCase());
			if(querytermList.containsKey(term)){

				continue;
			}

			List<Document> docList = new ArrayList<Document>();
			//TODO
			QueryBuilder qb = QueryBuilders.matchQuery("text", term);
			SearchResponse scrollRes = elasticServerClient.getClient().prepareSearch(INDEX_NAME)
					.addSort("text", SortOrder.ASC)
					.setScroll(new TimeValue(600000))
					.setQuery(qb)
					.setExplain(true)
					.execute()
					.actionGet();
			System.out.println("Term " + term + " Size-> " + scrollRes.getHits().getTotalHits());
			outer:
				while (true) {

					for (SearchHit hit : scrollRes.getHits().getHits()) {
											if(Long.valueOf(scrollRes.getHits().getTotalHits()) > 10000){
												System.out.println(term + "->" + "ignored");
												
												break outer;
											}
											
						Document document = new Document();
						document.setDocId(hit.getId()); 
						document.setDocFreq(Long.valueOf(scrollRes.getHits().getTotalHits()));
						document.setTerm(term);
						try{
							long val = (long) Double.parseDouble((hit.getExplanation().toString().split("termFreq=")[1])
									.split("\n")[0]);
							document.setTermFreq(val);
						}catch(Exception e){
							e.printStackTrace();
							System.out.println("Term: " + term);
							System.out.println(hit.getExplanation().toString());
							System.exit(1);
						}
						docList.add(document);
					}

					scrollRes = elasticServerClient.getClient().prepareSearchScroll(scrollRes.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
					//Break condition: No hits are returned
					if (scrollRes.getHits().getHits().length == 0) {
						break;
					}
				}
			querytermList.put(term, docList);
		}
		System.out.println("New END Query: " + queryTerms[0] + " " + (new SimpleDateFormat("HH:MM:SS")).format(new Date()));
	}

	public void generateOkapiScore(String[] queryTerms) {
		List<Document> queryDocument = new ArrayList<Document>();

		for(int i = 1 ; i < queryTerms.length;i++){
			String term = StemUtil.getStemOfWord(queryTerms[i].trim().toLowerCase());
			if(querytermList.containsKey(term)){
				List<Document> docsList = querytermList.get(term);
				queryDocument.addAll(docsList);
			}
		}

		Map<String,Double> okapiScoreDocList= new HashMap<String, Double>();

		Iterator<Document> docIter = queryDocument.iterator();
		while(docIter.hasNext()){
			Document doc = docIter.next();
			//double okapiScore = OkapiTF.okapiScore(doc.getTermFreq(), Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()));
			double okapiScore = OkapiTF.okapiScore(doc.getTermFreq(), Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()), doc.getDocId(),doc.toString());
			if(okapiScoreDocList.containsKey(doc.getDocId())){
				double oScore = okapiScore + okapiScoreDocList.get(doc.getDocId());
				okapiScoreDocList.put(doc.getDocId(), oScore);
			}else{
				okapiScoreDocList.put(doc.getDocId(), okapiScore);
			}
		}
		System.out.println("DOC-SIZE" + okapiScoreDocList.size());
		System.out.println("RANKING DOCUMENT COMPLETE");


		//		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String,Double>>(okapiScoreDocList.entrySet());
		//		Collections.sort(list, new Comparator<Map.Entry<String,Double>>() {
		//
		//			@Override
		//			public int compare(Entry<String, Double> o1,
		//					Entry<String, Double> o2) {
		//				return (o1.getValue()).compareTo(o2.getValue());
		//			}
		//		});

		//OkapiUtil.sort(okapiScoreDocList);
		//System.out.println("SORTING DOCUMENT COMPLETE");


		//removed
		//		int count = 0;
		//		Iterator<Map.Entry<String, Double>> qsIter = okapiScoreDocList.entrySet().iterator();
		//		while(qsIter.hasNext()){
		//			if(count==101){
		//				break;
		//			}
		//			Map.Entry<String, Double> tempEntry = qsIter.next();
		//			System.out.println(tempEntry.getKey()+" "+ count + " " + tempEntry.getValue());
		//			++count;
		//		}
		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(okapiScoreDocList);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			OkapiUtil.dumpResultsToFile(sortedScore,queryTerms[0]);
			System.out.println("RESULTS DUMPED FOR OKAPI for QUERY " + queryTerms[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DUMPED COMPLETE");


		// FOR TF_IDF EVALUATION
		tfIDFScoring(queryDocument,okapiScoreDocList,queryTerms[0]);
		// FOR BM-25 SCORING
		//bm25scoring(queryDocument,queryTerms[0]);
		//LaplaceScoring
		//laplaceScoring(queryDocument,queryTerms[0]);

	}

	private void tfIDFScoring(List<Document> queryDocument,Map<String,Double> okapiScores,String query){
		Map<String,Double> tfIdfScoreDocList= new HashMap<String, Double>();

		Iterator<Document> docIter = queryDocument.iterator();
		while(docIter.hasNext()){
			Document doc = docIter.next();

			double tfIdfScore = TFIDF.tfidfScore(okapiScores.get(doc.getDocId()), 84678, doc.getDocFreq());

			if(tfIdfScoreDocList.containsKey(doc.getDocId())){
				double tfScore = tfIdfScore + tfIdfScoreDocList.get(doc.getDocId());
				tfIdfScoreDocList.put(doc.getDocId(), tfScore);
			}else{
				tfIdfScoreDocList.put(doc.getDocId(), tfIdfScore);
			}
		}
		System.out.println("TF-IDF-DOC-SIZE" + tfIdfScoreDocList.size());
		System.out.println("RANKING DOCUMENT COMPLETE");

		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(tfIdfScoreDocList);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			TFIDFUtil.dumpResultsToFile(sortedScore,query);
			System.out.println("RESULTS DUMPED FOR TFIDF for QUERY " + query);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DUMPED COMPLETE");
	}


	private void bm25scoring(List<Document> queryDocument,String query){
		Map<String,Double> bm25ScoreDocList= new HashMap<String, Double>();
		Iterator<Document> docIter = queryDocument.iterator();
		while(docIter.hasNext()){
			Document doc = docIter.next();

			double bm25Score = OkapiBM25.okapiBM25Score(84678, doc.getDocFreq(), doc.getTermFreq(), Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()));
			if(bm25ScoreDocList.containsKey(doc.getDocId())){
				double bmScore = bm25Score + bm25ScoreDocList.get(doc.getDocId());
				bm25ScoreDocList.put(doc.getDocId(), bmScore);
			}else{
				bm25ScoreDocList.put(doc.getDocId(), bm25Score);
			}
		}
		System.out.println("TF-IDF-DOC-SIZE" + bm25ScoreDocList.size());
		System.out.println("RANKING DOCUMENT COMPLETE");

		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(bm25ScoreDocList);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			OkapiBM25Util.dumpResultsToFile(sortedScore,query);
			System.out.println("RESULTS DUMPED FOR BM25 for QUERY " + query);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DUMPED COMPLETE");

	}

	private void laplaceScoring(List<Document> queryDocument,String query){
		Map<String,Double> laplaceScoreDocList= new HashMap<String, Double>();
		Iterator<Document> docIter = queryDocument.iterator();
		while(docIter.hasNext()){
			Document doc = docIter.next();
			double uniLaplaceScore = UnigramLaplace.lapaceSmoothingScore(doc.getTermFreq(),Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()));
			if(laplaceScoreDocList.containsKey(doc.getDocId())){
				double uScore = uniLaplaceScore + laplaceScoreDocList.get(doc.getDocId());
				laplaceScoreDocList.put(doc.getDocId(), uScore);
			}else{
				laplaceScoreDocList.put(doc.getDocId(), uniLaplaceScore);
			}
		}



		System.out.println("Laplace-DOC-SIZE" + laplaceScoreDocList.size());
		System.out.println("RANKING DOCUMENT COMPLETE");

		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(laplaceScoreDocList);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			UnigramLaplaceUtil.dumpResultsToFile(sortedScore,query);
			System.out.println("RESULTS DUMPED FOR Laplace for QUERY " + query);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DUMPED COMPLETE");

	}

	public void generateLaplaceScore(String[] queryTerms){
		List<Document> queryDocument = new ArrayList<Document>();

		for(int i = 1 ; i < queryTerms.length;i++){
			String term = StemUtil.getStemOfWord(queryTerms[i].trim().toLowerCase());
			if(querytermList.containsKey(term)){
				List<Document> docsList = querytermList.get(term);
				queryDocument.addAll(docsList);
			}
		}


		Map<String,Integer> penalizedDocs = new HashMap<String,Integer>();

		Iterator<Document> docIter = queryDocument.iterator();
		while(docIter.hasNext()){
			Document document = docIter.next();
			for (String term : queryTerms) {
				String qterm = StemUtil.getStemOfWord(term.trim().toLowerCase());
				if(document.getTerm().equals(qterm)){
					if(penalizedDocs.containsKey(document.getDocId())){
						penalizedDocs.put(document.getDocId(), penalizedDocs.get(document.getDocId())+1);
					}else{
						penalizedDocs.put(document.getDocId(),1);
					}
				}
			}
		}
		// Obtain scores for those corrosponding docs.

		Map<String,Double> laplaceScoreDocList= new HashMap<String, Double>();
		/*Iterator<Document> */docIter = queryDocument.iterator();
		while(docIter.hasNext()){
			Document doc = docIter.next();
			double uniLaplaceScore = UnigramLaplace.lapaceSmoothingScore(doc.getTermFreq(),Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()));

			if(laplaceScoreDocList.containsKey(doc.getDocId())){
				double uScore = uniLaplaceScore + laplaceScoreDocList.get(doc.getDocId());
				laplaceScoreDocList.put(doc.getDocId(), uScore);
			}else{
				laplaceScoreDocList.put(doc.getDocId(), uniLaplaceScore);
			}
		}

		docIter = queryDocument.iterator();
		while(docIter.hasNext()){
			Document doc = docIter.next();

			double penalizedScore = UnigramLaplace.penalizedScore(Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()), queryTerms.length - penalizedDocs.get(doc.getDocId()));
			double lscore = penalizedScore + laplaceScoreDocList.get(doc.getDocId());
			laplaceScoreDocList.put(doc.getDocId(), lscore);
		}

		// update the scores for all other documents documents
		Iterator<Map.Entry<Object, Object>> lengthIter = docsLengthMap.entrySet().iterator();
		while(lengthIter.hasNext()){
			Map.Entry<Object, Object> entry = lengthIter.next();
			if(laplaceScoreDocList.containsKey(entry.getKey().toString().trim())){
				continue;
			}else{
				double penalizedScore = UnigramLaplace.penalizedScore(Long.valueOf(entry.getValue().toString()), queryTerms.length);
				//double penalizedScore = UnigramLaplace.lapaceSmoothingScore(0,Long.valueOf(entry.getValue().toString()));
				laplaceScoreDocList.put(entry.getKey().toString().trim(), penalizedScore);
			}
		}

		Iterator<Map.Entry<String,Double>> logIter = laplaceScoreDocList.entrySet().iterator();
		while(logIter.hasNext()){
			Map.Entry<String,Double> entry = logIter.next();
			entry.setValue(Math.log10(entry.getValue()));
		}

		System.out.println("Laplace-DOC-SIZE" + laplaceScoreDocList.size());
		System.out.println("RANKING DOCUMENT COMPLETE");

		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(laplaceScoreDocList);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			UnigramLaplaceUtil.dumpResultsToFile(sortedScore,queryTerms[0]);
			System.out.println("RESULTS DUMPED FOR Laplace for QUERY " + queryTerms[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DUMPED COMPLETE");
	}

	public void generateBM25Score(String[] queryTerms) {

		List<Document> queryDocument = new ArrayList<Document>();

		for(int i = 1 ; i < queryTerms.length;i++){
			String term = StemUtil.getStemOfWord(queryTerms[i].trim().toLowerCase());
			if(querytermList.containsKey(term)){
				List<Document> docsList = querytermList.get(term);
				queryDocument.addAll(docsList);
			}
		}

		Map<String,Double> bm25ScoreDocList= new HashMap<String, Double>();
		Iterator<Document> docIter = queryDocument.iterator();
		while(docIter.hasNext()){
			Document doc = docIter.next();

			double bm25Score = OkapiBM25.okapiBM25Score(84678, doc.getDocFreq(), doc.getTermFreq(), Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()));
			if(bm25ScoreDocList.containsKey(doc.getDocId())){
				double bmScore = bm25Score + bm25ScoreDocList.get(doc.getDocId());
				bm25ScoreDocList.put(doc.getDocId(), bmScore);
			}else{
				bm25ScoreDocList.put(doc.getDocId(), bm25Score);
			}
		}
		System.out.println("TF-IDF-DOC-SIZE" + bm25ScoreDocList.size());
		System.out.println("RANKING DOCUMENT COMPLETE");

		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(bm25ScoreDocList);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			OkapiBM25Util.dumpResultsToFile(sortedScore,queryTerms[0]);
			System.out.println("RESULTS DUMPED FOR BM25 for QUERY " + queryTerms[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DUMPED COMPLETE");

	}


	public void generateJelineckScore(String[] queryTerms){

		List<Document> queryDocument = new ArrayList<Document>();

		for(int i = 1 ; i < queryTerms.length;i++){
			String term = StemUtil.getStemOfWord(queryTerms[i].trim().toLowerCase());
			if(querytermList.containsKey(term)){
				List<Document> docsList = querytermList.get(term);
				queryDocument.addAll(docsList);
			}
		}

		UnigramJelinek.computeValuesForQueryTerms(queryTerms, queryDocument);

		Map<String,List<Document>> uniqueDocList = new HashMap<String,List<Document>>();

		Iterator<Document> docIterator = queryDocument.iterator();
		while(docIterator.hasNext()){
			Document doc = docIterator.next();
			List<Document> docList;
			if(uniqueDocList.containsKey(doc.getDocId())){
				docList = uniqueDocList.get(doc.getDocId());
				docList.add(doc);
			}else{
				docList = new ArrayList<Document>();
				docList.add(doc);
			}
			uniqueDocList.put(doc.getDocId(), docList);
		}




		Map<String,Integer> penalizedDocs = new HashMap<String,Integer>();

		Map<String,Double> jelineckScoreDocList= new HashMap<String, Double>();

		Iterator<Document> docIter = queryDocument.iterator();

		while(docIter.hasNext()){
			Document doc = docIter.next();

			if(jelineckScoreDocList.containsKey(doc.getDocId())){
				continue;
			}
			double jelinekScore = UnigramJelinek.score(doc,Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()),queryTerms);
			jelineckScoreDocList.put(doc.getDocId(), jelinekScore);
		}



		//		// update the scores for all other documents documents
		//		Iterator<Map.Entry<Object, Object>> lengthIter = docsLengthMap.entrySet().iterator();
		//		while(lengthIter.hasNext()){
		//			Map.Entry<Object, Object> entry = lengthIter.next();
		//			if(jelineckScoreDocList.containsKey(entry.getKey().toString().trim())){
		//				continue;
		//			}else{
		//				double penalizedScore = UnigramJelinek.penalizedScoreForNonDoc(queryTerms, Long.valueOf(docsLengthMap.get(entry.getKey().toString()).toString()));
		//				//double penalizedScore = UnigramLaplace.lapaceSmoothingScore(0,Long.valueOf(entry.getValue().toString()));
		//				jelineckScoreDocList.put(entry.getKey().toString().trim(), penalizedScore);
		//			}
		//		}
		//		
		//		Iterator<Map.Entry<String,Double>> logIter = jelineckScoreDocList.entrySet().iterator();
		//		while(logIter.hasNext()){
		//			Map.Entry<String,Double> entry = logIter.next();
		//			entry.setValue(Math.log10(entry.getValue()));
		//		}

		System.out.println("Jelineck-DOC-SIZE" + jelineckScoreDocList.size());
		System.out.println("RANKING DOCUMENT COMPLETE");

		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(jelineckScoreDocList);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			JelineckUtil.dumpResultsToFile(sortedScore,queryTerms[0]);
			System.out.println("RESULTS DUMPED FOR Laplace for QUERY " + queryTerms[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DUMPED COMPLETE");

	}


	//	public void generateJelineckScore(String[] queryTerms){
	//		List<Document> queryDocument = new ArrayList<Document>();
	//
	//		for(int i = 1 ; i < queryTerms.length;i++){
	//			String term = StemUtil.getStemOfWord(queryTerms[i].trim().toLowerCase());
	//			if(querytermList.containsKey(term)){
	//				List<Document> docsList = querytermList.get(term);
	//				queryDocument.addAll(docsList);
	//			}
	//		}
	//		
	//		Map<String,Double> jelineckScoreDocList= new HashMap<String, Double>();
	//		Iterator<Document> docIter = queryDocument.iterator();
	//		while(docIter.hasNext()){
	//			Document doc = docIter.next();
	//			double jelineckScore = UnigramJelinek.lapaceSmoothingScore(doc.getDocId(),
	//					doc.getTerm(), doc.getTermFreq(),Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()), queryDocument,doc.getDocFreq());
	//			if(jelineckScoreDocList.containsKey(doc.getDocId())){
	//				double jnScore = jelineckScore + jelineckScoreDocList.get(doc.getDocId());
	//				jelineckScoreDocList.put(doc.getDocId(), jnScore);
	//			}else{
	//				jelineckScoreDocList.put(doc.getDocId(), jelineckScore);
	//			}
	//		}
	//		System.out.println("JELINEK DOC-SIZE" + jelineckScoreDocList.size());
	//		System.out.println("RANKING DOCUMENT COMPLETE");
	//		
	//		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(jelineckScoreDocList);
	//
	//		System.out.println("SORTING DOCUMENT COMPLETE");
	//		//removed
	//		try {
	//			JelineckUtil.dumpResultsToFile(sortedScore,queryTerms[0]);
	//			System.out.println("RESULTS DUMPED FOR JELINECK for QUERY " + queryTerms[0]);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//
	//		System.out.println("DUMPED COMPLETE");
	//	}


	public void generateJelineckScoreNew(String[] queryTerms){

//		Properties prop = new Properties();
//		try {
//			prop.load(ScoreEngine.class.getClassLoader().getResourceAsStream("docIds"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		Iterator<Map.Entry<Object, Object>> propIter = prop.entrySet().iterator();
//		while(propIter.hasNext()){
//			docIds.add(propIter.next().getKey().toString());
//		}

		Map<String,Double> docScores = new HashMap<String,Double>();

		List<Document> tempQueryTermList = null;
		for(int i = 1; i < queryTerms.length; i++){
			String term = StemUtil.getStemOfWord(queryTerms[i].trim().toLowerCase());
			if(querytermList.containsKey(term)){
				tempQueryTermList = null;
				System.out.println("->" +querytermList.get(term).size());
				tempQueryTermList = new ArrayList<Document>(querytermList.get(term));

				//compute total freq of all this term;
				long TOTAL_TERM_COUNT = 0;
				Iterator<Document> countDocIter = tempQueryTermList.iterator();
				while(countDocIter.hasNext()){
					TOTAL_TERM_COUNT= TOTAL_TERM_COUNT + countDocIter.next().getTermFreq();
				}
				
				if(TOTAL_TERM_COUNT == 0){
					continue;
				}
				
				final List<String> docIdsOfExistingQueryTerms = new ArrayList<String>();
				Iterator<Document> docIter = tempQueryTermList.iterator();
				while(docIter.hasNext()){
					docIdsOfExistingQueryTerms.add(docIter.next().getDocId());
				}
				
				System.out.println("DOCS+SIZE" + docIds.size());
				Iterator<String>  allDocIds = docIds.iterator();
				while(allDocIds.hasNext()){
					String docId = allDocIds.next();
					if(docIdsOfExistingQueryTerms.contains(docId)){
						continue;
					}else{
						if(Long.valueOf(docsLengthMap.get(docId).toString()) == 0){
							continue;
						}
						Document document = new Document();
						document.setDocId(docId);
						document.setTerm(term);
						tempQueryTermList.add(document);
					}
				}

				System.out.println("docSizeExisting" + tempQueryTermList.size());
				System.out.println("VERIFICATION: " + (tempQueryTermList.size() == 84678));
				// for each of the 
				Iterator<Document> docsIter = tempQueryTermList.iterator();
				while(docsIter.hasNext()){
					Document doc = docsIter.next();
					double jScore = UnigramJelinek.score(doc, Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()),TOTAL_TERM_COUNT);
					if(docScores.containsKey(doc.getDocId())){
						double score = jScore+ docScores.get(doc.getDocId());
						docScores.put(doc.getDocId(), score);
					}else{
						docScores.put(doc.getDocId(), jScore);
					}
				}
				
//				System.out.println("Jelineck-DOC-SIZE" + docScores.size());
//				System.out.println("RANKING DOCUMENT COMPLETE");
//
//				Map<String,Double> sortedScore = OkapiUtil.sortByComparator(docScores);
//
//				System.out.println("SORTING DOCUMENT COMPLETE");
//				//removed
//				try {
//					JelineckUtil.dumpResultsToFile(sortedScore,queryTerms[0]);
//					System.out.println("RESULTS DUMPED FOR Laplace for QUERY " + queryTerms[0]);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//				System.out.println("DUMPED COMPLETE");
				

			}

		}
		System.out.println("Jelineck-DOC-SIZE" + docScores.size());
		System.out.println("RANKING DOCUMENT COMPLETE");

		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(docScores);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			JelineckUtil.dumpResultsToFile(sortedScore,queryTerms[0]);
			System.out.println("RESULTS DUMPED FOR Laplace for QUERY " + queryTerms[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DUMPED COMPLETE");
		
		
	}


}
