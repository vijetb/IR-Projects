package com.vijet.ir;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONObject;

import com.vijet.ir.elasticsearchclient.ESClient;
import com.vijet.ir.model.QueryScore;
import com.vijet.ir.models.OkapiTF;
import com.vijet.ir.util.OkapiUtil;

public class ScoreEngineOld {

	private final String ELASTICSERVER_HOSTNAME_KEY = "elasticserver.hostname";
	private final String ELASTICSERVER_PORT_KEY = "elasticserver.port";
	private final String INDEX_NAME = "ap_dataset";
	private final String INDEX_TYPE = "document";

	private Map<String, Long> docLengthMap;


	private final ESClient elasticServerClient;
	
	HashMap<String,Long> termsMap = new HashMap<String,Long>();
	Map<String,Map<String,Long>> termMap = new HashMap<>();

	public ScoreEngineOld(ESClient client){
		this.elasticServerClient = client;
	}

	public void fetchDocuments(Map<String, QueryScore> queryMap, String[] queryTerms) {
		for(String term : queryTerms){
			if(term.trim().isEmpty()){
				continue;
			}
			QueryBuilder qb = QueryBuilders.matchQuery("text", term);
			SearchResponse scrollRes = elasticServerClient.getClient().prepareSearch(INDEX_NAME)
					.addSort("text", SortOrder.ASC)
					.setScroll(new TimeValue(600000))
					.setQuery(qb)
					.execute()
					.actionGet();
			System.out.println("Term " + term + " Size-> " + scrollRes.getHits().getTotalHits());

			while (true) {
				for (SearchHit hit : scrollRes.getHits().getHits()) {
					queryMap.put(hit.getId(), new QueryScore());
				}

				scrollRes = elasticServerClient.getClient().prepareSearchScroll(scrollRes.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
				//Break condition: No hits are returned
				if (scrollRes.getHits().getHits().length == 0) {
					break;
				}
			}

		}
		updateTermVectorsForDocs(queryMap);
	}

	private void updateTermVectorsForDocs(Map<String,QueryScore> docList) {
		Iterator<Map.Entry<String, QueryScore>> docsMapIter = docList.entrySet().iterator();
		while(docsMapIter.hasNext()){
			Map.Entry<String, QueryScore> tempEntry = docsMapIter.next();
			QueryScore tempQS = tempEntry.getValue();
			System.out.println("Updating term vector for doc + " + tempEntry.getKey());
			//Get the term vector for this doc
			TermVectorsResponse resp = elasticServerClient.getClient().prepareTermVectors()
					.setIndex(INDEX_NAME)
					.setType(INDEX_TYPE)
					.setId(tempEntry.getKey())
					.setOffsets(true)
					.setPayloads(true)
					.setPositions(true)
					.setTermStatistics(true)
					.setFieldStatistics(true)
					.execute().actionGet();
			XContentBuilder builder;
			try {
				builder = XContentFactory.jsonBuilder().startObject();
				resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
				builder.endObject();
				tempQS.setTermVectorDesc(builder.string());
				tempEntry.setValue(tempQS);
			}catch(Exception e){
				System.out.println("Error in fetching termvectors for Doc with ID: " + tempEntry.getKey());
				e.printStackTrace();
			}
		}
	}


	public void generateOkapiScoreForDocuments(Map<String, QueryScore> queryMap, String[] queryTerms){
		docLengthMap = new HashMap<String,Long>();
		computeLengthOfAllDocs(queryMap);

		Iterator<Map.Entry<String, QueryScore>> mapIter = queryMap.entrySet().iterator();
		while(mapIter.hasNext()){
			Map.Entry<String, QueryScore> tempMapEntry = mapIter.next();
			QueryScore okapiScore = tempMapEntry.getValue();

			double score = 0.0;
			//TODO
			for(int i = 0; i < queryTerms.length; i++){
				long termFrequency = getTermFreqFromDoc(okapiScore.getTermVectorDesc(),queryTerms[i].toLowerCase());
				score+=OkapiTF.okapiScore(termFrequency, docLengthMap.get(tempMapEntry.getKey()),84678);
			}
			okapiScore.setOkapiScore(score);
			tempMapEntry.setValue(okapiScore);
		}

		//Sort the list
//		queryMap = OkapiUtil.sort(queryMap);
//		//print the list
//		//				Iterator<QueryScore> qsIter1 = docsMap.values().iterator();
//		//				while(qsIter1.hasNext()){
//		//					System.out.println(qsIter1.next().toString());
//		//				}
//		//Print the 1000 values in a particular format.
//		try {
//			OkapiUtil.dumpResultsToFile(queryMap,queryTerms[0]);
//			System.out.println("RESULTS DUMPED FOR OKAPI for QUERY " + queryTerms[0]);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	private void computeLengthOfAllDocs(Map<String, QueryScore> docsMap) {
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

		Iterator<Map.Entry<String,Long>> itr = docLengthMap.entrySet().iterator();
		while(itr.hasNext()){
			System.out.println("Id->" + itr.next().getKey() + " length->"  + itr.next().getValue());
		}
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

}
