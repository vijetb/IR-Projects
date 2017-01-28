package com.vijet.ir.elasticsearchclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.print.Doc;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.termvectors.MultiTermVectorsItemResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequest;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequestBuilder;
import org.elasticsearch.action.termvectors.MultiTermVectorsResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.vijet.ir.model.DOC;
import com.vijet.ir.model.QueryScore;

public class ESClient {

	private final String ELASTICSERVER_HOSTNAME_KEY = "elasticserver.hostname";
	private final String ELASTICSERVER_PORT_KEY = "elasticserver.port";
	private final String INDEX_NAME = "ap_dataset";
	private final String INDEX_TYPE = "document";

	private Client client = null;
	private final Gson gson = new Gson();

	private final Set<String> docIds = new HashSet<String>();


	private static long id = 1;

	private final Map<DOC,String> docToIdMap = new HashMap<DOC,String>();

	public void configure() throws IOException{
		Properties props = new Properties();
		props.load(ESClient.class.getClassLoader().getResourceAsStream("elasticserver.config"));
		Settings settings = Settings.builder().put(props).build();
		client = TransportClient.builder().settings(settings).build().
				addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(props.getProperty(ELASTICSERVER_HOSTNAME_KEY)), Integer.parseInt(props.getProperty(ELASTICSERVER_PORT_KEY))));
	}

	public Client getClient(){
		return client;
	}

	//	public List<DOC> getAllDocuments(long startCount, long endCount){
	//		QueryBuilder qb = QueryBuilders.matchAllQuery();
	//
	//		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME)
	//				.addSort("text", SortOrder.ASC)
	//				.setScroll(new TimeValue(60000))
	//				.setExplain(true)
	//				.setQuery(qb).execute().actionGet(); //100 hits per shard will be returned for each scroll
	//		int count = 0;
	//		//Scroll until no hits are returned
	//		System.out.println(scrollResp.toString());
	//		System.out.println(scrollResp.getHits().totalHits());
	//		while (true) {
	//
	//			for (SearchHit hit : scrollResp.getHits().getHits()) {
	//				System.out.println(hit.getSource().get("docno"));
	//				System.out.println(hit.getSource().get("text"));
	//				++count;
	//			}
	//			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
	//			//Break condition: No hits are returned
	//			if (scrollResp.getHits().getHits().length == 0) {
	//				break;
	//			}
	//		}
	//		System.out.println("COUNT + " + count);
	//		return null;
	//	}
	public Map<String,QueryScore> getAllDocuments(long startCount, long endCount){		
		Map<String,QueryScore> listOfDocs = new LinkedHashMap<String, QueryScore>();

		List<String> listOfIds = new ArrayList<String>();
		for(long i = startCount ; i <=endCount; i++){
			listOfIds.add(String.valueOf(i));
		}

		MultiGetResponse multiGetItemResponses = client.prepareMultiGet()
				.add(INDEX_NAME, INDEX_TYPE, listOfIds)         
				.get();
		long count = startCount;
		for (MultiGetItemResponse itemResponse : multiGetItemResponses) { 
			GetResponse response = itemResponse.getResponse();
			if (response.isExists()) {
				DOC doc =  gson.fromJson(response.getSourceAsString(), DOC.class);
				QueryScore tempQS = new QueryScore();
				tempQS.setDoc(doc);
				listOfDocs.put(String.valueOf(count), tempQS);
				if(doc.getTEXT() == null || doc.getDOCNO()== null){
					System.out.println("Print DOC" + response.getId());
				}
				++count;
			}else{
				System.out.println("Response doesnt exist");
			}
		}
		System.out.println("TOTAL DOCUMENTS FETCHED -> " + listOfDocs.size());
		return listOfDocs;
	}

	//	public List<DOC> printAllDocuments(long startCount, long endCount){	
	//		SearchResponse response = null;
	//
	//		try {
	//			response = client.prepareSearch("ap_dataset").setTypes("document")
	//					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	//					.setQuery(QueryBuilders.matchAllQuery())
	//					// Query
	//					.setFrom(0).setSize(84678)
	//					.setExplain(true).execute()
	//					.actionGet();
	//			int count = 0;
	//			for (SearchHit hit : response.getHits()) {
	//				++count;
	//				System.out.println("******************************");
	//				System.out.println(hit.getSourceAsString());
	//				System.out.println("#########################");
	//		        //System.out.println(hit.explanation().toHtml());
	//		    }
	//			System.out.println("TOTALLLL: " + count);
	//		} catch (Throwable e) {
	//			e.printStackTrace();
	//		}
	//		
	//		return null;
	//	}

	public void updateTermVectorsForAllDocuments(Map<String, QueryScore> docList) {
		Iterator<Map.Entry<String, QueryScore>> docsMapIter = docList.entrySet().iterator();
		while(docsMapIter.hasNext()){
			Map.Entry<String, QueryScore> tempEntry = docsMapIter.next();
			QueryScore tempQS = tempEntry.getValue();
			System.out.println("Updating term vector for doc + " + tempEntry.getKey());
			//Get the term vector for this doc
			TermVectorsResponse resp = client.prepareTermVectors()
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

	/*public void updateTermVectorsForAllDocuments(Map<String, QueryScore> docList) {

		List<String> list = new ArrayList<String>();
		for(int i = 1 ; i <= 84678; i++){
			list.add(String.valueOf(i));
		}
		MultiTermVectorsResponse multiResponse = client.
				prepareMultiTermVectors().add(INDEX_NAME, INDEX_TYPE, list.toArray(new String[list.size()]))
				.get();

		int count = 0;
		for (MultiTermVectorsItemResponse itemResponse : multiResponse) { 
			TermVectorsResponse response = itemResponse.getResponse();
			if (response.isExists()) {
				XContentBuilder builder;
				try {
					builder = XContentFactory.jsonBuilder().startObject();
					response.toXContent(builder, ToXContent.EMPTY_PARAMS);
					builder.endObject();
					QueryScore qs = docList.get(response.getId());
					qs.setTermVectorDesc(builder.string());
					docList.put(response.getId(), qs);
					++count;
				}catch(Exception e){
					System.out.println("Error in fetching termvectors for Doc with ID: " + itemResponse.getId());
					e.printStackTrace();
				}
			}else{
				System.out.println("Response doesnt exist");
			}
		}
		System.out.println("TOTAL COUNT=" + count);
	}*/

	public long getMaxCount(){
		return id;
	}

	public void termVectors(){
		TermVectorsResponse resp = client.prepareTermVectors()
				.setIndex(INDEX_NAME)
				.setType(INDEX_TYPE)
				.setId("0")
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
			System.out.println(builder.string());
		}catch(Exception e){
			e.printStackTrace();
		}
	}






	public void update(List<DOC> listOfDocs) {
		if(listOfDocs== null || listOfDocs.isEmpty()){
			System.out.println("No documents to update!");
		}

		BulkRequestBuilder bulkRequest = client.prepareBulk();
		Iterator<DOC> docIter = listOfDocs.iterator();
		while(docIter.hasNext()){
			DOC tempDOC = docIter.next();
			bulkRequest.add(client.
					prepareIndex(INDEX_NAME, INDEX_TYPE, tempDOC.getDOCNO()/*String.valueOf(id)*/)
					.setSource(gson.toJson(tempDOC,DOC.class)));
			++id;
		}

		BulkResponse bulkResponse = bulkRequest.get();
		if (bulkResponse.hasFailures()) {
			System.out.println("Error in executing Bulk Request!");
		}
	}



	public String getTFOfTerm(){
		return null;
	}

	public void test() {
		TermVectorsResponse resp = client.prepareTermVectors().setIndex(INDEX_NAME)
				.setType(INDEX_TYPE).setId("0").execute().actionGet();


		XContentBuilder builder;
		try {
			builder = XContentFactory.jsonBuilder().startObject();
			resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			System.out.println(builder.string());

			JSONObject obj = new JSONObject(builder.string());
			JSONObject obj1 = obj.getJSONObject("term_vectors");
			JSONObject obj2 = obj1.getJSONObject("text");
			JSONObject obj3 = obj2.getJSONObject("field_statistics");
			System.out.println(obj3.get("sum_doc_freq"));

			//		    XContentBuilder builder1 = builder.field("term_vectors");
			//		    XContentBuilder builder2 = builder1.field("text");
			//		    XContentBuilder builder3 = builder2.field("field_statistics");
			//		    System.out.println(builder3.field("sum_doc_freq"));
			System.out.println("TERM FREQUENCY");
			QueryBuilder qb = QueryBuilders.matchQuery("text", "VIJET");

			SearchResponse scrollResp = client.prepareSearch(INDEX_NAME)
					.addSort("text", SortOrder.ASC)
					.setScroll(new TimeValue(60000))
					.setExplain(true)
					.setQuery(qb).execute().actionGet(); //100 hits per shard will be returned for each scroll
			int count = 0;
			//Scroll until no hits are returned
			System.out.println(scrollResp.toString());
			System.out.println(scrollResp.getHits().totalHits());
			while (true) {

				for (SearchHit hit : scrollResp.getHits().getHits()) {
					++count;
				}
				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
				//Break condition: No hits are returned
				if (scrollResp.getHits().getHits().length == 0) {
					break;
				}
			}
			System.out.println("COUNT + " + count);


		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public long getAvgLengthOfDocs() {
		TermVectorsResponse resp = client.prepareTermVectors()
				.setIndex(INDEX_NAME)
				.setType(INDEX_TYPE)
				.setId("1")
				.execute()
				.actionGet();

		XContentBuilder builder;
		try {
			builder = XContentFactory.jsonBuilder().startObject();
			resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			JSONObject obj = new JSONObject(builder.string());
			JSONObject obj1 = obj.getJSONObject("term_vectors");
			JSONObject obj2 = obj1.getJSONObject("text");
			JSONObject obj3 = obj2.getJSONObject("field_statistics");
			System.out.println("SUM DOC-FREQUENCY" + obj3.get("sum_doc_freq"));
			return Long.valueOf(String.valueOf(obj3.get("sum_doc_freq")));
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}

	public Long getDocLength(DOC doc) {
		TermVectorsResponse resp = client.prepareTermVectors()
				.setIndex(INDEX_NAME)
				.setType(INDEX_TYPE)
				.setId(docToIdMap.get(doc))
				.execute()
				.actionGet();

		XContentBuilder builder;
		try {
			builder = XContentFactory.jsonBuilder().startObject();
			resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			JSONObject obj = new JSONObject(builder.string());
			JSONObject obj1 = obj.getJSONObject("term_vectors");
			JSONObject obj2 = obj1.getJSONObject("text");
			JSONObject obj3 = obj2.getJSONObject("terms");
			System.out.println(obj3.toString());

			String[] values = obj3.toString().split("},");
			return Long.valueOf(obj3.toString().split("},").length);
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0L;
	}

	public void closeConnection() {
		client.close();
	}

	public long getTermFreqForDoc(String term, DOC doc) {
		TermVectorsResponse resp = client.prepareTermVectors()
				.setIndex(INDEX_NAME)
				.setType(INDEX_TYPE)
				.setId("0")
				.execute()
				.actionGet();

		XContentBuilder builder;
		try {
			builder = XContentFactory.jsonBuilder().startObject();
			resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			JSONObject obj = new JSONObject(builder.string());
			JSONObject obj1 = obj.getJSONObject("term_vectors");
			JSONObject obj2 = obj1.getJSONObject("text");
			JSONObject obj3 = obj2.getJSONObject("terms");

			JSONObject termData = obj3.getJSONObject(term);
			if(termData == null){
				System.out.println("TERM NOT PRESENT");
			}else{
				System.out.println("TERM IS PRESENT & FRE IS " + termData.get("term_freq"));
			}

			return 9L;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}


	public void testing(){
		String query = "Document will discuss allegations or measures being taken against corrupt public officials of any governmental jurisdiction worldwide";

		SearchResponse response = null;

		try {
			response = client.prepareSearch("ap_dataset").setTypes("document")
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(QueryBuilders.queryStringQuery(query))
					// Query
					.setFrom(0).setSize(5)
					.setExplain(true).execute()
					.actionGet();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		for (SearchHit hit : response.getHits()) {
			System.out.println("******************************");
			//			System.out.println(hit.getSourceAsString());
			System.out.println(hit.explanation().toHtml());
			System.out.println("#########################");
			//System.out.println(hit.explanation().toHtml());
		}
	}

	public void printIds() throws IOException{
		System.out.println("TERM FREQUENCY");
		QueryBuilder qb = QueryBuilders.matchAllQuery();
		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME)
				.addSort("text", SortOrder.ASC)
				.setScroll(new TimeValue(60000))
				.setExplain(true)
				.setQuery(qb).execute().actionGet(); //100 hits per shard will be returned for each scroll
		int count = 0;
		//Scroll until no hits are returned
		System.out.println(scrollResp.getHits().totalHits());
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("docIds")));
			while (true) {

				for (SearchHit hit : scrollResp.getHits().getHits()) {
					writer.write(hit.getId().toString().trim()+"="+System.lineSeparator());
					++count;
				}
				scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
				//Break condition: No hits are returned
				if (scrollResp.getHits().getHits().length == 0) {
					break;
				}
			}
			writer.flush();
			writer.close();
			System.out.println("COUNT + " + count);
	}
	
	
	public void printDocLengthOfAllDocuments(){
		Properties docIdProp = new Properties();
		try {
			docIdProp.load(ESClient.class.getClassLoader().getResourceAsStream("docIds"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BufferedWriter bw = null ;
		try {
			bw = new BufferedWriter(new FileWriter(new File("document_length.properties")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		Iterator<Entry<Object, Object>> docIter = docIdProp.entrySet().iterator();
		while (docIter.hasNext()){
			Entry<Object, Object> entry = docIter.next();
			
			TermVectorsResponse resp = client.prepareTermVectors().setIndex(INDEX_NAME)
					.setType(INDEX_TYPE).setId(entry.getKey().toString().trim()).execute().actionGet();


			XContentBuilder builder;
			try {
				builder = XContentFactory.jsonBuilder().startObject();
				resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
				builder.endObject();
				JSONObject obj = new JSONObject(builder.string());
				String id = obj.get("_id").toString();
				JSONObject obj1 = obj.getJSONObject("term_vectors");
				JSONObject obj2 = obj1.getJSONObject("text");
				JSONObject obj3 = obj2.getJSONObject("terms");
				
				int count = 0;
				Iterator<String> iter = obj3.keySet().iterator();
				while(iter.hasNext()){
					String key = iter.next();
					JSONObject value = obj3.getJSONObject(key);
					//System.out.println(key + "-> " + value.getInt("term_freq"));
					count = count + value.getInt("term_freq");
				}
				//System.out.println(count);
				bw.write(id+"="+count);
				bw.write(System.lineSeparator());
				//JSONObject obj3 = obj2.getJSONObject("field_statistics");
				//System.out.println(obj3.get("sum_doc_freq"));
			}catch(Exception e){
				e.printStackTrace();
				try {
					bw.write(id+"="+0);
					bw.write(System.lineSeparator());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
			
			
		}
		

		try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




}
