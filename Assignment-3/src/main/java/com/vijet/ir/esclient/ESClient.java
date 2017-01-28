package com.vijet.ir.esclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.print.Doc;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexResponse;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.vijet.ir.constants.Constants;
import com.vijet.ir.model.DOC;

public class ESClient {

	private final String ELASTICSERVER_HOSTNAME_KEY = "elasticserver.hostname";
	private final String ELASTICSERVER_PORT_KEY = "elasticserver.port";
	private final String INDEX_NAME = "van_dataset";
	private final String INDEX_TYPE = "document";
	private final String DEFAULT_CHAR_SET = "UTF-8";

	private final File DATA_FOLDER = new File("Results/");
	
	private Client client = null;
	private final Gson gson = new Gson();

	private final Set<String> docIds = new HashSet<String>();


	private static long id = 1;

	private final Map<String,Set<String>> inlinksMap = new HashMap<String,Set<String>>();

	private final String author = "VIJET";
	private int count = 0;
	
	private final HttpClient httpClient = HttpClientBuilder.create().build();
	
	public void configure() throws IOException{
		Properties props = new Properties();
		props.load(ESClient.class.getClassLoader().getResourceAsStream("elasticserver.config"));
		Settings settings = Settings.builder().put(props).build();
		client = TransportClient.builder().settings(settings).build().
				addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(props.getProperty(ELASTICSERVER_HOSTNAME_KEY)), Integer.parseInt(props.getProperty(ELASTICSERVER_PORT_KEY))));
		
		loadInlinksFile();
	}
	
	private void loadInlinksFile() {
		try {
			BufferedReader bfr = new BufferedReader(new FileReader(new File("VAN_inlinks.txt")));
			String inlinkString = new String();
			while((inlinkString=bfr.readLine())!=null){
				String[] values = inlinkString.split("=");
				inlinksMap.put(values[0], getOutLinksFromString(values[1]));
			}
			bfr.close();
			System.out.println(inlinksMap.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void indexDocuements() throws Exception{
		validateDataFolder();
		
//		File file = new File("misc/test.txt");
//		List<DOC> listOfDocs = parseFile(file);
//		System.out.println(listOfDocs.size());
//		new Thread(new IndexClientThread(listOfDocs, client)).start();
//		//testing(listOfDocs.get(0));

		File[] docFiles = DATA_FOLDER.listFiles();
		System.out.println("Total files found: "+ docFiles.length);
		for (File docFile : docFiles) {
			if(docFile.getName().contains("VAN_")){
				List<DOC> listOfDocs = parseFile(docFile);
				//new Thread(new IndexClientThread(listOfDocs, client)).start();
				indexAllDocs(listOfDocs);
			}
		}		
	}
	
	private void indexAllDocs(List<DOC> listOfDocs) {
		
		if(listOfDocs!=null){
			for (DOC doc : listOfDocs) {
				if(doc!=null){
					boolean isSourceHtmlPresent = false;
					//obtain the handler to elasticsearch
					QueryBuilder qb = QueryBuilders.matchQuery("_id",doc.getDOCNO());
					SearchResponse scrollResp = client.prepareSearch(Constants.INDEX_NAME)
							.setQuery(qb).execute().actionGet();
					
					if(scrollResp.getHits().getHits().length > 1){
						System.out.println("DUPLICATE RECORD FOUND! " + doc.getDOCNO());
						continue;
					}else if(scrollResp.getHits().getHits().length == 1){
						isSourceHtmlPresent = true;
						SearchHit hit = scrollResp.getHits().getHits()[0];
						List tempList = (List) hit.getSource().get("in_links");
						if(tempList!=null){
							doc.getInlinks().addAll(tempList);
						}
						tempList = (List) hit.getSource().get("out_links");
						if(tempList!=null){
							doc.getOutlinks().addAll(tempList);
						}
						tempList = (List) hit.getSource().get("authors");
						if(tempList!=null){
							doc.getAuthors().addAll(tempList);
						}
						doc.setHtmlSource(hit.getSource().get("html_Source").toString());
						doc.setHttpHeader(hit.getSource().get("HTTPheader").toString());
					}
						// NOW Index the updated document!
						try{
							if(!isSourceHtmlPresent){
								updateHtmlForDoc(doc);
							}
							IndexResponse indexResponse = client.prepareIndex(Constants.INDEX_NAME, Constants.INDEX_TYPE)
									.setId(doc.getDOCNO())
									.setSource(gson.toJson(doc)).get();
														
						}catch(Exception e){
							e.printStackTrace();
							System.out.println("*** UNABLE TO INDEX: "+ doc.getDOCNO()+" **********");
						}
						
					}
				}
				Constants.NO_OF_DOCS_INDEXED += listOfDocs.size();
				System.out.println("*** TOTAL FILES INDEXED ****** : " + Constants.NO_OF_DOCS_INDEXED);
			}
		
	}

	private boolean updateHtmlForDoc(DOC doc) {
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
		HttpGet request = new HttpGet(doc.getUrl());
		HttpResponse response;
		try {
			response = httpClient.execute(request);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				StringBuilder headerStringBuilder = new StringBuilder();
				Header[] headers = response.getAllHeaders();
				for (Header header : headers) {
					headerStringBuilder.append(header.toString());
				}
				doc.setHttpHeader(headerStringBuilder.toString());
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				StringBuilder htmlSourceCode = new StringBuilder();
				String line = "";
				while ((line = rd.readLine()) != null) {
					htmlSourceCode.append(line);
				}
				doc.setHtmlSource(htmlSourceCode.toString());
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private List<DOC> parseFile(File docFile) throws IOException {
		List<DOC> listOfDocs = new ArrayList<DOC>();
		
		Document fileAsDocList = Jsoup.parse(docFile, DEFAULT_CHAR_SET);
		Elements docs = fileAsDocList.getElementsByTag("DOC");
		
		Iterator<Element> docElemIter = docs.iterator();
		while(docElemIter.hasNext()){
			Element doc = docElemIter.next();
			DOC tempDoc = unmarshellDOC(doc);
			if(tempDoc!=null){
				listOfDocs.add(tempDoc);
			}
		}
		return listOfDocs;
	}
	
	private DOC unmarshellDOC(Element doc) {
		DOC tempDOC = new DOC();
		String[] tags = {"DOCNO","TEXT","DEPTH","URL","OUTLINKS","TITLE"};
		
		for (String tag : tags) {
			updateDOC(tag,tempDOC,doc);
		}
	
		//TODO: update the inlinks for the document
		Set<String> tempInlinkSet = getInlinksForDocument(tempDOC.getDOCNO());
		if(null!=tempInlinkSet)
			tempDOC.getInlinks().addAll(tempInlinkSet);
		//TODO: update the author for the document
		tempDOC.getAuthors().add(author);
		return tempDOC;
	}
	
	private Set<String> getInlinksForDocument(String docno) {
		return inlinksMap.get(docno);
	}

	private void updateDOC(String tag, DOC tempDOC, Element doc) {
		Elements docTextElements = doc.getElementsByTag(tag);
		Iterator<Element> textIter = docTextElements.iterator();
		while(textIter.hasNext()){
			String textValue = textIter.next().text();
			switch(tag){
				case "DOCNO":tempDOC.setDOCNO(textValue);break;
				case "DEPTH":tempDOC.setDEPTH(Integer.valueOf(textValue));break;
				case "URL":tempDOC.setUrl(textValue);break;
				case "OUTLINKS":tempDOC.setOutlinks(getOutLinksFromString(textValue));break;
				case "TITLE":tempDOC.setHEAD(textValue);break;
				case "TEXT":tempDOC.setTEXT(textValue);break;
			}
		}
	}
	
	private Set<String> getOutLinksFromString(final String outlinksAsString){
		if(outlinksAsString == null || outlinksAsString.isEmpty()){
			return null;
		}
		String[] outlinkSet = outlinksAsString.split(" ");
		Set<String> tempOutlinkSet = new LinkedHashSet<String>();
		for (String outlink : outlinkSet) {
			tempOutlinkSet.add(outlink.trim());
		}
		return tempOutlinkSet;
	}
	
	
	private void validateDataFolder() throws Exception {
		if(DATA_FOLDER.exists() && DATA_FOLDER.isDirectory()){
			return;
		}
		throw new Exception("Datafolder path is incorrect!");
	}
	
	public void testing(DOC doc){
		
//		DOC testingDOc = new DOC();
//		Set<String> set = new HashSet<String>();
//		set.add("Vijet");
//		set.add("Amit");
//		set.add("Nikhil");
//		testingDOc.setAuthors(set);
//		testingDOc.setDEPTH(1);
//		testingDOc.setDOCNO("doc-1");
//		testingDOc.setHEAD("Heading");
//		Set<String> inlinks = new HashSet<String>();
//		inlinks.add("inlnks");
//		inlinks.add("inlnks1");
//		inlinks.add("inlnks2");
//		
//		testingDOc.setInlinks(inlinks);
//		Set<String> outlinks = new HashSet<String>();
//		outlinks.add("outlinks");
//		outlinks.add("outlinks1");
//		outlinks.add("outlinks2");
//		testingDOc.setOutlinks(outlinks);
//		testingDOc.setTEXT("testing");
//		testingDOc.setUrl("url-1");
//		
		IndexResponse res = client.prepareIndex(Constants.INDEX_NAME, Constants.INDEX_TYPE).setId(doc.getDOCNO()).setSource(new Gson().toJson(doc)).get();
		if(res == null){
			System.out.println("index failed");
		}else{
			System.out.println("index success");
		}
		
		
		
//		QueryBuilder qb = QueryBuilders.matchQuery("_id","doc-2");
//		SearchResponse scrollResp = client.prepareSearch("test_dataset")
//				.setQuery(qb).execute().actionGet();
//		
//		SearchHit[] hits = scrollResp.getHits().getHits();
//		for (SearchHit hit : hits) {
//			System.out.println(hit.getSourceAsString());
//			List list = (List) hit.getSource().get("in_links");
//			System.out.println(list);
//			List list2 = (List) hit.getSource().get("out_links");
//			System.out.println(list2);
//			List list3 = (List) hit.getSource().get("authors");
//			System.out.println(list3);
//		}
//		System.out.println(hits.length);
	}
}
