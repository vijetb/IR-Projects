package com.vijet.ir.filterclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.print.Doc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vijet.ir.model.DOC;
import com.vijet.ir.util.CleanText;
import com.vijet.ir.util.JelineckUtil;

public class FilterClient {
	private final String WORDS_MAPPING_FILE_NAME = "wordsMapping.txt";
	private final Pattern validFilePattern = Pattern.compile("^ap");
	private final String STOPLIST_FILE = "input/stoplist.txt";
	private final Set<String> stopWords = new HashSet<String>();

	//private final Set<String> tokenSet = new HashSet<String>(1024);

	private final Map<String,String> tokenMapping = new HashMap<String, String>();
	private final Map<String,String> tokenReverseMapping = new HashMap<String, String>();

	private final Map<String,DOC> docMapping = new HashMap<String,DOC>();

	private final Map<String,Set<String>> queryDocumentMapping = new HashMap<String, Set<String>>();
	private final Map<String,Set<String>> queryTokensMapping = new HashMap<String, Set<String>>();
	
	private final Map<String,Integer> tfCount  = new HashMap<String, Integer>();
	
	final Map<String,Map<String,Double>> queryScoreMapping = new HashMap<String, Map<String,Double>>();
	//
	final Map<String,HashMap<String,List<String>>> queryToTopicWords = new HashMap<String, HashMap<String,List<String>>>();
	//
	public void loadFiles(String folderName) throws Exception {
		//load StopLists	
		BufferedReader reader = new BufferedReader(new FileReader(STOPLIST_FILE));
		String line = new String();
		while((line=reader.readLine())!=null){
			stopWords.add(line.trim());
		}
		reader.close();

		//load all the Documents
		File folder = new File(folderName);

		validateDataFolder(folder);
		File[] docFiles = folder.listFiles();
		for (File docFile : docFiles) {
			if(validateFile(docFile)){
				List<DOC> listOfDocs = parseFile(docFile);
				for (DOC doc : listOfDocs) {
					docMapping.put(doc.getDOCNO(), doc);
				}
				System.out.println("ParsedFile: " + docFile.getName() + " No of <DOC>'s : " + listOfDocs.size());
			}else{
				System.out.println("File Skipped: " + docFile.getName());
			}
		}

		System.out.println("*************** ALL DOCUEMENTS LOADEDED SUCCESSFULLY *********");

		//Load the data from the bm25
		BufferedReader bm25Rreader  = new BufferedReader(new FileReader("input/OkapiBM25Results.txt"));
		while((line=bm25Rreader.readLine())!=null){
			String[] data = line.split(" ");
			if(queryDocumentMapping.containsKey(data[0].trim())){
				queryDocumentMapping.get(data[0].trim()).add(data[2].trim());
			}else{
				Set<String> docIds = new HashSet<String>();
				docIds.add(data[2].trim());
				queryDocumentMapping.put(data[0].trim(),docIds);
			}
		}
		bm25Rreader.close();

		//load the qrel file
		BufferedReader qrelRreader  = new BufferedReader(new FileReader("input/qrels.adhoc.51-100.AP89.txt"));
		while((line=qrelRreader.readLine())!=null){
			String[] data = line.split(" ");
			if(queryDocumentMapping.containsKey(data[0].trim())){
				queryDocumentMapping.get(data[0].trim()).add(data[2].trim());
			}
		}
		qrelRreader.close();
		
//		BufferedReader qrelRreader  = new BufferedReader(new FileReader("input/qrels.adhoc.51-100.AP89.txt"));
//		while((line=qrelRreader.readLine())!=null){
//			String[] data = line.split(" ");
//			if(queryDocumentMapping.containsKey(data[0].trim())){
//				queryDocumentMapping.get(data[0].trim()).add(data[2].trim());
//			}else{
//				Set<String> docIds = new HashSet<String>();
//				docIds.add(data[2].trim());
//				queryDocumentMapping.put(data[0].trim(),docIds);
//			}
//		}
//		qrelRreader.close();
		
		//REad tf file
		BufferedReader tfReader  = new BufferedReader(new FileReader("tfMapping.txt"));
		while((line=tfReader.readLine())!=null){
			String[] data = line.split(" ");
			tfCount.put(data[0].trim(), Integer.valueOf(data[1].trim()));
		}
		tfReader.close();
	}

	public void generateWordsMapping() throws Exception{
		System.out.println("TOTAL QUERIES: " + queryDocumentMapping.size());


		for (Entry<String, Set<String>> entry : queryDocumentMapping.entrySet()) {
			updateTokensMapping(entry.getKey(),entry.getValue());
			System.out.println("MAPPING FILE CREATED FOR QUERY: "+ entry.getKey());
		}
		
		for (String queryId : queryDocumentMapping.keySet()) {
			generateMatrixFile(queryId);
			System.out.println("MATRIX FILE CREATED FOR QUERY: "+ queryId);
		}
		
		for (Entry<String, Set<String>> entry : queryDocumentMapping.entrySet()) {
			System.out.println("QUerY: "+ entry.getKey()+" No Of Documents: "+ entry.getValue().size());
		}
	}
	
	public void generateTopics() throws IOException{
//		dumpToTopicFile("100");
		for (Entry<String, Set<String>> entry : queryDocumentMapping.entrySet()) {
			try{
				dumpToTopicFile(entry.getKey());
			}catch(FileNotFoundException fe){
				
			}
			System.out.println("TOPICS FILE CREATED FOR QUERY: "+ entry.getKey());
		}
		printWeightedScores();
	}

	private void printWeightedScores() {
		for (String key : queryScoreMapping.keySet()) {
			Map<String,Double> map = queryScoreMapping.get(key);
			Map<String,Double> sortedMap = JelineckUtil.sortByComparator(map);
			System.out.println("------------"+key+"--------------------");
			for (Entry<String, Double> entry : sortedMap.entrySet()) {
				System.out.println(entry.getKey()+":"+entry.getValue());
			}
		}
	}

	private void updateTokensMapping(String queryId, Set<String> docIds) throws Exception{
		final BufferedWriter writer = new BufferedWriter(new FileWriter("MappingFiles/TokensMapping_"+queryId+".txt"));
		final Set<String> tokens = new HashSet<String>();
		for (String docId : docIds) {
			DOC tempDoc = docMapping.get(docId);
			String[] cleantTextTokens = getDocText(tempDoc).split(" ");
			for (String token : cleantTextTokens) {
				if(!stopWords.contains(token.toLowerCase().trim()) && (token.length()>3)){
					tokens.add(token.toLowerCase().trim());
				}
			}
		}
		long count = 0;
		for (String token : tokens) {
			writer.write(token+" "+ count+ System.lineSeparator());
			++count;
		}
		writer.flush();
		writer.close();
		queryTokensMapping.put(queryId, tokens);
	}

	public void generateMatrixFile(String queryId) throws IOException{
		final BufferedWriter writer = new BufferedWriter(new FileWriter("MatrixFile/FeatureMatrix_"+queryId+".txt"));
		final BufferedWriter docMappingwriter = new BufferedWriter(new FileWriter("MatrixFileDocIdMapping/FeatureMatrixDocMapping_"+queryId+".txt"));

		final Map<String,String> tokenMapping = getTokenMapping(queryId);
		
		Set<String> docIds = queryDocumentMapping.get(queryId);
		int count = 1;
		for (String docId : docIds) {
			writer.write("|"+" ");
			DOC doc = docMapping.get(docId);
			String[] cleantTextTokens = getDocText(doc).split(" ");
			for (String token : cleantTextTokens) {
				if(tokenMapping.containsKey(token)){
					if(token.equals("null"))continue;
						writer.write(tokenMapping.get(token)+" ");
				}
			}
			writer.write(System.lineSeparator());
			docMappingwriter.write(count+" "+docId+System.lineSeparator());
			++count;
		}
		writer.flush();docMappingwriter.flush();
		writer.close();docMappingwriter.close();
	}
	
	private Map<String, String> getTokenMapping(String queryId) throws IOException {
		Map<String,String> tempMapping = new HashMap<String, String>();
		BufferedReader reader  = new BufferedReader(new FileReader("MappingFiles/TokensMapping_"+queryId+".txt"));
		String line = new String();
		while((line=reader.readLine())!=null){
			String[] values = line.split(" ");
			tempMapping.put(values[0].trim(), values[1].trim());
		}
		reader.close();
		return tempMapping;
	}

	private String getDocText(DOC doc){
		return CleanText.cleanText(new StringBuilder().append(doc.getHEAD()).append(" ").append(doc.getTEXT()).toString());
	}
	
	private void dumpToTopicFile(String queryId) throws IOException{
		final int NUM_OF_TOPICS = 20;
		Map<String,Double> map = new HashMap<String, Double>();
		
		final BufferedWriter writer = new BufferedWriter(new FileWriter("Topics/Topics_"+queryId+".txt"));
		final BufferedReader reader  = new BufferedReader(new FileReader("Model/sampleModel_"+queryId));
		String line = new String();
		//skip lines
		reader.readLine();reader.readLine();reader.readLine();reader.readLine();reader.readLine();
		reader.readLine();reader.readLine();reader.readLine();reader.readLine();reader.readLine();
		//skip lines end
		List<Map<String,Double>> topicsList = new ArrayList<Map<String,Double>>(10);
		//for(int i = 0 ; i < 10;i++){
		for(int i = 0 ; i < NUM_OF_TOPICS;i++){
			topicsList.add(new HashMap<String, Double>());
		}
		
		//double[] weightedScore = new double[10];
		double[] weightedScore = new double[NUM_OF_TOPICS];
		
		Map<String,String> tokenMapping = new HashMap<String, String>();
		Map<String,String> reverseTokenMapping = new HashMap<String, String>();
		//0 0.100027 7.8584 0.10002 0.1 0.1 0.1 0.100089 0.100007 0.1 0.1 
		uploadTokenMappings(tokenMapping,reverseTokenMapping,queryId);
		for(int i = 0; i < queryTokensMapping.get(queryId).size();i++){
			String[] values = reader.readLine().split(" ");
			//for(int topicIndex = 1;topicIndex <=10; topicIndex++){
			for(int topicIndex = 1;topicIndex <=NUM_OF_TOPICS; topicIndex++){
				topicsList.get(topicIndex-1).put(reverseTokenMapping.get(values[0].trim()), Double.valueOf(values[topicIndex]));
			}
		}
		int count = 1;
		for (Map<String, Double> topicMap : topicsList) {
			Map<String,Double> sortedMap = JelineckUtil.sortByComparator(topicMap);
			
			writer.write("TOPIC-"+count+System.lineSeparator());
			Iterator<Map.Entry<String, Double>> iter = sortedMap.entrySet().iterator();
			
			for(int k = 0 ; k < 30; k++){
				Entry<String,Double> entry = iter.next();
				writer.write(entry.getKey()+":"+iter.next().getValue()+System.lineSeparator());
				weightedScore[count-1] = weightedScore[count-1]+ iter.next().getValue() * tfCount.get(iter.next().getKey());
			}

			//writer.write("AVG: "+ weightedScore[count-1]+System.lineSeparator());
			//writer.write(System.lineSeparator());
			map.put("TOPIC-"+count, weightedScore[count-1]);
			++count;
		}
		writer.flush();
		writer.close();
		queryScoreMapping.put(queryId, map);
		reader.close();
//		//TODO: GENERATE TOPIC-DOCUMENT DISTRIBUTION
//		generateTopicDistForQuery(queryId,topicsList);
	}
	
	private void generateTopicDistForQuery(final String queryId, List<Map<String,Double>> topicList) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter("TopicsDist/topicDist"+queryId+".txt"));
		for (String docId : queryDocumentMapping.get(queryId)) {
			DOC doc = docMapping.get(docId);
			Map<String,Integer> docTermCount = getTfForDoc(doc);
			writer.write(doc.getDOCNO());
			for(int i=0;i<20;i++){
				Map<String,Double> sortedMap = JelineckUtil.sortByComparator(topicList.get(i));
				double value = 0.0;
				
				Iterator<Map.Entry<String, Double>> iter = sortedMap.entrySet().iterator();
				
				for(int k = 0 ; k < 30; k++){
					Entry<String,Double> entry = iter.next();
					if(docTermCount.containsKey(entry.getKey())){
						value = value + docTermCount.get(entry.getKey()) * entry.getValue();
					}
				}
				if(value!= 0.0)
				writer.write(" TOPIC-"+i+":"+value);
			}
			writer.write(System.lineSeparator());
		}
		
		writer.flush();
		writer.close();
	}


	private Map<String, Integer> getTfForDoc(DOC doc) {
		Map<String,Integer> map = new HashMap<String, Integer>();
		String[] words = getDocText(doc).split(" ");
		for (String word : words) {
			word = word.toLowerCase().trim();
			if(map.containsKey(word)){
				map.put(word, map.get(word));
			}else{
				map.put(word, 1);
			}
		}
		return map;
	}

	private void uploadTokenMappings(Map<String, String> tokenMapping2,
			Map<String, String> reverseTokenMapping, String queryId) throws IOException  {
		BufferedReader reader  = new BufferedReader(new FileReader("MappingFiles/TokensMapping_"+queryId+".txt"));
		String line = new String();
		while((line=reader.readLine())!=null){
			String[] values = line.split(" ");
			tokenMapping2.put(values[0].trim(), values[1].trim());
			reverseTokenMapping.put(values[1].trim(), values[0].trim());
		}
		reader.close();
	}

	private boolean validateFile(File docFile) {
		return validFilePattern.matcher(docFile.getName()).find();
	}

	private void validateDataFolder(File folder) throws Exception {
		if(folder.exists() && folder.isDirectory()){
			return;
		}
		throw new Exception("Datafolder path is incorrect!");
	}

	private List<DOC> parseFile(File docFile) throws IOException {
		List<DOC> listOfDocs = new ArrayList<DOC>();

		Document fileAsDocList = Jsoup.parse(docFile, "UTF-8");
		Elements docs = fileAsDocList.getElementsByTag("DOC");

		Iterator<Element> docElemIter = docs.iterator();
		while(docElemIter.hasNext()){
			Element doc = docElemIter.next();
			DOC tempDoc = unmarshellDOC(doc);
			if(tempDoc!=null){
				listOfDocs.add(tempDoc);
			}
			if(tempDoc.getTEXT()==null){
				System.out.println("Text null foung for " + doc.html());
			}
		}
		return listOfDocs;
	}

	private DOC unmarshellDOC(Element doc) {
		DOC tempDOC = new DOC();
		String[] tags = {"DOCNO","HEAD","TEXT"};

		for (String tag : tags) {
			updateDOC(tag,tempDOC,doc);
		}
		return tempDOC;
	}

	private void updateDOC(String tag, DOC tempDOC, Element doc) {
		Elements docTextElements = doc.getElementsByTag(tag);
		Iterator<Element> textIter = docTextElements.iterator();
		while(textIter.hasNext()){
			String textValue = textIter.next().text();
			switch(tag){
			case "DOCNO":tempDOC.setDOCNO(textValue);break;
			case "FILEID":tempDOC.setFILEID(textValue);break;
			case "FIRST":tempDOC.setFIRST(textValue);break;
			case "SECOND":tempDOC.setSECOND(textValue);break;
			case "HEAD":tempDOC.setHEAD(textValue);break;
			case "BYLINE":tempDOC.setBYLINE(textValue);break;
			case "DATELINE":tempDOC.setDATELINE(textValue);break;
			case "TEXT":tempDOC.setTEXT(textValue);break;
			}
		}
	}

	

	public void generateMatrixFile(boolean isQrel, String file) throws Exception {
		String queryId = "85";
		Set<String> queryDocIds = new HashSet<String>();
		if(isQrel){
			BufferedReader reader  = new BufferedReader(new FileReader("input/OkapiBM25Results.txt"));
			String line = new String();
			while((line=reader.readLine())!=null){
				String[] data = line.split(" ");
				if(data[0].equals(queryId)){
					queryDocIds.add(data[2]);
				}
			}
			reader.close();
			BufferedReader reader1  = new BufferedReader(new FileReader("input/qrels.adhoc.51-100.AP89.txt"));
			String line1 = new String();
			while((line1=reader1.readLine())!=null){
				String[] data = line1.split(" ");
				if(data[0].equals(queryId)){
					queryDocIds.add(data[2]);
				}
			}
			reader1.close();

			System.out.println("TOTAL NUMBER OF DOCUMENTS: "+ queryDocIds.size());
			dumpToMatrixFile(file,queryDocIds);
		}else{

		}
	}

	private void dumpToMatrixFile(String folderName, Set<String> docs) throws Exception {
		BufferedWriter docIdWriter = new BufferedWriter(new FileWriter("docIdsMappingForQuery.txt"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("MatrixFile.txt"));

		Map<String,DOC> docMapping = new HashMap<String,DOC>();
		File folder = new File(folderName);
		validateDataFolder(folder);
		File[] docFiles = folder.listFiles();
		for (File docFile : docFiles) {
			if(validateFile(docFile)){
				List<DOC> listOfDocs = parseFile(docFile);
				for (DOC doc : listOfDocs) {
					docMapping.put(doc.getDOCNO(), doc);
				}
				System.out.println("ParsedFile: " + docFile.getName() + " No of <DOC>'s : " + listOfDocs.size());
			}else{
				System.out.println("File Skipped: " + docFile.getName());
			}
		}

		//create the file here
		long count = 1;
		for (String docId : docs) {
			DOC doc = docMapping.get(docId);
			String docText = getDocText(doc);
			String[] tokens = docText.split(" ");
			writer.write("|"+" ");
			for (String token : tokens) {
				if(tokenMapping.containsKey(token.toLowerCase().trim())){
					writer.write(tokenMapping.get(token.toLowerCase().trim())+" ");
				}
			}
			writer.append(System.lineSeparator());
			docIdWriter.write(count+" "+ docId+System.lineSeparator());
			++count;
		}

		writer.flush();
		writer.close();

		docIdWriter.flush();
		docIdWriter.close();
	}

	

}
