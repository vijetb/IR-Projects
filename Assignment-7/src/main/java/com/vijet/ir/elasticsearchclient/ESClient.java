package com.vijet.ir.elasticsearchclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import org.jsoup.Jsoup;

import com.google.gson.Gson;
import com.vijet.ir.model.Mail;
import com.vijet.ir.util.CleanText;



public class ESClient implements ITermVector {

	private final String ELASTICSERVER_HOSTNAME_KEY = "elasticserver.hostname";
	private final String ELASTICSERVER_PORT_KEY = "elasticserver.port";
	private final String INDEX_NAME = "spam_dataset";
	private final String INDEX_TYPE = "document";
	
	
	private final int TOTAL_TRAINING_DOCS = 60335;
	private final int TOTAL_SPAM_TRAINING_DOCS = 40223;
	private final int TOTAL_HAM_TRAINING_DOCS = 20112;

	private Client client = null;
	private final Gson gson = new Gson();

	private final Map<String,String> mailsSpamMapping = new HashMap<String, String>();
	private final Map<String,String> testingIdsMapping = new HashMap<String, String>();
	private final Map<String,String> trainingIdsMapping = new HashMap<String, String>();
	private static final List<Mail> mailList = new LinkedList<Mail>();
	private BufferedWriter trainingIdsMapper, testingIdsMapper;
	
	int indexCount = 0;

	private final Set<String> sparseFeatures = new HashSet<String>();

	public void configure() throws IOException{
		Properties props = new Properties();
		props.load(ESClient.class.getClassLoader().getResourceAsStream("elasticserver.config"));
		Settings settings = Settings.builder().put(props).build();
		client = TransportClient.builder().settings(settings).build().
				addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(props.getProperty(ELASTICSERVER_HOSTNAME_KEY)), Integer.parseInt(props.getProperty(ELASTICSERVER_PORT_KEY))));
	}

	public void loadFiles(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader("mappings/index"));
			String line = new String();
			while((line=reader.readLine())!=null){
				String[] values = line.split(" ");
				mailsSpamMapping.put(values[1].trim(), values[0].trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File testingIdsMapper = new File("mappings/testingIdsMapper.txt");
		File trainingIdsMapper = new File("mappings/trainingIdsMapper.txt");
		if(testingIdsMapper.exists() && trainingIdsMapper.exists()){
			loadDataHelper(testingIdsMapper,testingIdsMapping);
			loadDataHelper(trainingIdsMapper,trainingIdsMapping);
		}
		
	}
	
	private void loadDataHelper(File fileName, Map<String,String> map){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = new String();
			while((line=reader.readLine())!=null){
				String[] values = line.split(" ");
				map.put(values[0].trim(), values[1].trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void closeConnection() {
		if(client!=null) client.close();
	}


	public void indexFiles(String FOLDER_NAME) throws IOException {
		trainingIdsMapper = new BufferedWriter(new FileWriter("trainingIdsMapper.txt"));
		testingIdsMapper = new BufferedWriter(new FileWriter("testingIdsMapper.txt"));
		
		File FOLDER_PATH = new File(FOLDER_NAME);
		File[] mails = FOLDER_PATH.listFiles();
		//int count = 1;
		int no_of_ham_training = 0;
		int no_of_spam_training = 0;
		for (File mailFile : mails) {
			if(validateFile(mailFile)){
				String mailText = parseFile(mailFile);
				int id = Integer.valueOf(mailFile.getName().split("\\.")[1]);
				String spamLabel = mailsSpamMapping.get(mailFile.getName().trim());
				
				if(spamLabel.equals("spam") && no_of_spam_training < TOTAL_SPAM_TRAINING_DOCS){
					indexFileOnElasticSearch(id,mailText, mailsSpamMapping.get(mailFile.getName().trim()), mailFile.getName(), "train",false);
					++no_of_spam_training;
					trainingIdsMapper.write(id+" "+mailFile.getName().trim()+System.lineSeparator());
				}
				else if(spamLabel.equals("ham") &&  no_of_ham_training < TOTAL_HAM_TRAINING_DOCS){
					indexFileOnElasticSearch(id,mailText, mailsSpamMapping.get(mailFile.getName().trim()), mailFile.getName(), "train",false);
					++no_of_ham_training;
					trainingIdsMapper.write(id+" "+mailFile.getName().trim()+System.lineSeparator());
				}else{
					indexFileOnElasticSearch(id,mailText, mailsSpamMapping.get(mailFile.getName().trim()), mailFile.getName(), "test",false);
					testingIdsMapper.write(id+" "+mailFile.getName().trim()+System.lineSeparator());
				}
			}else{
				System.out.println("FILE SKIPPED: "+ mailFile.getName());
			}
			
		}
		indexFileOnElasticSearch(-1,null,null,null,null,true);
		trainingIdsMapper.flush();
		trainingIdsMapper.close();
		testingIdsMapper.flush();
		testingIdsMapper.close();
		System.out.println("TOTAL HAM TRAINING :" + no_of_ham_training );
		System.out.println("TOTAL SPAM TRAINING :" + no_of_spam_training );

	}

	private boolean validateFile(File mailFile) {
		return mailFile.getName().contains("inmail");
	}

	private void indexFileOnElasticSearch(int id,String text, String spamLabel, String fileName, String split, boolean indexAll){
		if(!indexAll){
			mailList.add(new Mail(String.valueOf(id),text,spamLabel,fileName, split));
			if(mailList.size()<1000){
				return;
			}
		}
		
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		Iterator<Mail> docIter = mailList.iterator();
		while(docIter.hasNext()){
			Mail mail = docIter.next();
			bulkRequest.add(client.
					prepareIndex(INDEX_NAME, INDEX_TYPE, mail.getId())
					.setSource(gson.toJson(mail,Mail.class)));
		}

		BulkResponse bulkResponse = bulkRequest.get();
		if (bulkResponse.hasFailures()) {
			System.out.println("Error in executing Bulk Request!");
		}
		indexCount = indexCount + mailList.size();
		System.out.println("TOTAL DOCUMENTS INDEXED: "+indexCount);
		mailList.clear();
	}

	private String parseFile(File mailFile) throws IOException{
		return CleanText.cleanText(Jsoup.parse(mailFile, "UTF-8").text());
	}

	@Override
	public String getTermVector(String id){
		TermVectorsResponse resp = client.prepareTermVectors()
				.setIndex(INDEX_NAME)
				.setType(INDEX_TYPE)
				.setId(id)
				.setSelectedFields("text")
				.execute().actionGet();
		XContentBuilder builder;
		try {
			builder = XContentFactory.jsonBuilder().startObject();
			resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			JSONObject obj = new JSONObject(builder.string());
			JSONObject obj1 = obj.getJSONObject("term_vectors").getJSONObject("text").getJSONObject("terms");
			return obj1.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void buildSparseFeatureMatrix() throws IOException{
		Set<String> ids = new HashSet<String>();
		ids.addAll(trainingIdsMapping.keySet());
		ids.addAll(testingIdsMapping.keySet());
//		ids.add("11");
		int totalcount = 0;
		List<String> idList = new ArrayList<String>();
		for (String id : ids) {
			idList.add(id);
			if(idList.size() > 1000){
				totalcount = totalcount + idList.size();
				System.out.println("TOTAL FILES SCANNED: "+ totalcount);
				updateFeatureMatrix(idList);
				idList.clear();
			}
			
		}
		updateFeatureMatrix(idList);
		System.out.println("WRITING INTO FILE!");
		BufferedWriter writer = new BufferedWriter(new FileWriter("SparseFeatureMatrix.txt"));
		int count = 1;
		for (String feature : sparseFeatures) {
			if(!feature.isEmpty()){
				writer.write(feature+System.lineSeparator());
				//++count;
			}
		}
		writer.flush();
		writer.close();
	}

	private void updateFeatureMatrix(List<String> idList) {
		MultiGetResponse multiGetItemResponses = client.prepareMultiGet()
				.add(INDEX_NAME, INDEX_TYPE, idList)         
				.get();
		for (MultiGetItemResponse itemResponse : multiGetItemResponses) { 
			GetResponse response = itemResponse.getResponse();
			if (response.isExists()) {
				String[] values = new JSONObject(response.getSourceAsString()).getString("text").split(" ");
				for (String value : values) {
					if(!value.trim().isEmpty())
						sparseFeatures.add(value.trim());
				}
			}else{
				System.out.println("Response doesnt exist");
			}
		}
		
	}

}
