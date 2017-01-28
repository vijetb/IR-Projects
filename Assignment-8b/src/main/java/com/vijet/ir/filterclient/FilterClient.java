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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

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

	private final Set<String> tokenSet = new HashSet<String>(1024);
	private final Map<String,DOC> docMapping = new LinkedHashMap<String,DOC>();

	private final Map<String,String> tokenMapping = new HashMap<String, String>();
	private final Map<String,String> tokenReverseMapping = new HashMap<String, String>();
	
	
	private final Map<String,Map<String,Double>> topicToTokenMapping = new HashMap<String, Map<String,Double>>();
	
	private final Map<String,Map<String,Double>> docToTopicMapping = new HashMap<String, Map<String,Double>>();

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
	}

	public void generateWordsMapping() throws Exception{
		for (DOC doc : docMapping.values()) {
			String[] tokens = getDocText(doc).split(" ");
			for (String token : tokens) {
				tokenSet.add(token.toLowerCase().trim());
			}			
		}

		generateTokenMappingFile();

		generateMatrixFile();
		System.out.println("MATRIX FILE GENERATED SUCCESFULLY");
		System.out.println("TOTAL NO OF DOCS = "+ docMapping.size());
	}

	public void generateTokenMappingFile() throws IOException{
		final BufferedWriter writer = new BufferedWriter(new FileWriter("TokensMapping.txt"));
		long count = 0;
		for (String token : tokenSet) {
			if(validToken(token) && !token.equals("null")){
				writer.write(token+" "+count+System.lineSeparator());
				tokenMapping.put(token, String.valueOf(count));
				tokenReverseMapping.put(String.valueOf(count),token);
				++count;
			}
		}
		writer.flush();
		writer.close();
		System.out.println("TOKEN MAPPING FILE WRITTERN SUCCESSFULLY");
	}
	
	public boolean validToken(String token){
		return !stopWords.contains(token.toLowerCase().trim()) && token.length()>3;
	}

	private void generateMatrixFile() throws IOException{
		final BufferedWriter writer = new BufferedWriter(new FileWriter("MatrixFile.txt"));
		final BufferedWriter docIdWriter = new BufferedWriter(new FileWriter("DocIdWriter.txt"));
		for (String docId : docMapping.keySet()) {
			docIdWriter.write(docId+System.lineSeparator());
			writer.write("|");
			DOC doc  = docMapping.get(docId);
			String[] words = getDocText(doc).split(" ");
			for (String word : words) {
				String token = word.toLowerCase().trim();
				if(tokenMapping.containsKey(token)){
					writer.write(" "+tokenMapping.get(token));
				}
			}
			writer.write(System.lineSeparator());
		}
		writer.flush();docIdWriter.flush();
		writer.close();docIdWriter.close();
	}
	
	
	
//	public void generateTopics() throws IOException{
//		//		dumpToTopicFile("100");
//		dumpToTopicFile();
////		for (Entry<String, Set<String>> entry : queryDocumentMapping.entrySet()) {
////			try{
////				dumpToTopicFile(entry.getKey());
////			}catch(FileNotFoundException fe){
////
////			}
////			System.out.println("TOPICS FILE CREATED FOR QUERY: "+ entry.getKey());
////		}
//	}
//
//
//
////	private void updateTokensMapping(String queryId, Set<String> docIds) throws Exception{
////		final BufferedWriter writer = new BufferedWriter(new FileWriter("MappingFiles/TokensMapping_"+queryId+".txt"));
////		final Set<String> tokens = new HashSet<String>();
////		for (String docId : docIds) {
////			DOC tempDoc = docMapping.get(docId);
////			String[] cleantTextTokens = getDocText(tempDoc).split(" ");
////			for (String token : cleantTextTokens) {
////				if(!stopWords.contains(token.toLowerCase().trim()) && (token.length()>3)){
////					tokens.add(token.toLowerCase().trim());
////				}
////			}
////		}
////		long count = 0;
////		for (String token : tokens) {
////			writer.write(token+" "+ count+ System.lineSeparator());
////			++count;
////		}
////		writer.flush();
////		writer.close();
////		queryTokensMapping.put(queryId, tokens);
////	}
//
////	public void generateMatrixFile(String queryId) throws IOException{
////		final BufferedWriter writer = new BufferedWriter(new FileWriter("MatrixFile/FeatureMatrix_"+queryId+".txt"));
////		final BufferedWriter docMappingwriter = new BufferedWriter(new FileWriter("MatrixFileDocIdMapping/FeatureMatrixDocMapping_"+queryId+".txt"));
////
////		final Map<String,String> tokenMapping = getTokenMapping(queryId);
////
////		Set<String> docIds = queryDocumentMapping.get(queryId);
////		int count = 1;
////		for (String docId : docIds) {
////			writer.write("|"+" ");
////			DOC doc = docMapping.get(docId);
////			String[] cleantTextTokens = getDocText(doc).split(" ");
////			for (String token : cleantTextTokens) {
////				if(tokenMapping.containsKey(token)){
////					if(token.equals("null"))continue;
////					writer.write(tokenMapping.get(token)+" ");
////				}
////			}
////			writer.write(System.lineSeparator());
////			docMappingwriter.write(count+" "+docId+System.lineSeparator());
////			++count;
////		}
////		writer.flush();docMappingwriter.flush();
////		writer.close();docMappingwriter.close();
////	}
//
//	private Map<String, String> getTokenMapping(String queryId) throws IOException {
//		Map<String,String> tempMapping = new HashMap<String, String>();
//		BufferedReader reader  = new BufferedReader(new FileReader("MappingFiles/TokensMapping_"+queryId+".txt"));
//		String line = new String();
//		while((line=reader.readLine())!=null){
//			String[] values = line.split(" ");
//			tempMapping.put(values[0].trim(), values[1].trim());
//		}
//		reader.close();
//		return tempMapping;
//	}

	private String getDocText(DOC doc){
		return CleanText.cleanText(new StringBuilder().append(doc.getHEAD()).append(" ").append(doc.getTEXT()).toString());
	}

	private void dumpToTopicFile() throws IOException{
		final int NUM_OF_TOPICS  = 200;

		final BufferedWriter writer = new BufferedWriter(new FileWriter("Topics/Topics_Final.txt"));
		final BufferedReader reader  = new BufferedReader(new FileReader("Model/sampleModel"));
		String line = new String();
		//skip lines
		reader.readLine();reader.readLine();reader.readLine();reader.readLine();reader.readLine();
		reader.readLine();reader.readLine();reader.readLine();reader.readLine();reader.readLine();
		//skip lines end
		List<Map<String,Double>> topicsList = new ArrayList<Map<String,Double>>(NUM_OF_TOPICS);
		
		for(int i = 0 ; i < NUM_OF_TOPICS ;i++){
			topicsList.add(new HashMap<String, Double>());
		}

		//0 0.100027 7.8584 0.10002 0.1 0.1 0.1 0.100089 0.100007 0.1 0.1 
		for(int i = 0; i < tokenMapping.size();i++){
			String[] values = reader.readLine().trim().split(" ");

			for(int topicIndex = 1;topicIndex <= 200; topicIndex++){
				double value = 0.0;
				try{
					value = Double.valueOf(values[topicIndex]);
				}catch(Exception e){
					System.out.println("Exception for parsing: "+ i);
				}
				topicsList.get(topicIndex-1).put(tokenReverseMapping.get(values[0].trim()), value);
			}
		}
		
		System.out.println("COMPLETED");
//		int count = 1;
//		for (Map<String, Double> topicMap : topicsList) {
//			Map<String,Double> sortedMap = JelineckUtil.sortByComparator(topicMap);
//
//			writer.write("------------------ TOPIC-"+count+"--------------------"+System.lineSeparator());
//			Iterator<Map.Entry<String, Double>> iter = sortedMap.entrySet().iterator();
//
//			for(int k = 0 ; k < 10; k++){
//				Entry entry = iter.next();
//				writer.write(entry.getKey()+System.lineSeparator());
//				weightedScore[count-1] = weightedScore[count-1]+ iter.next().getValue() * tfCount.get(iter.next().getKey());
//			}
//			//			writer.write(iter.next().getKey()+System.lineSeparator());
//			//			writer.write(iter.next().getKey()+System.lineSeparator());
//			//			writer.write(iter.next().getKey()+System.lineSeparator());
//			//			writer.write(iter.next().getKey()+System.lineSeparator());
//			//			writer.write(iter.next().getKey()+System.lineSeparator());
//			//			writer.write(iter.next().getKey()+System.lineSeparator());
//			//			writer.write(iter.next().getKey()+System.lineSeparator());
//			//			writer.write(iter.next().getKey()+System.lineSeparator());
//			//			writer.write(iter.next().getKey()+System.lineSeparator());
//			//			writer.write(iter.next().getKey()+System.lineSeparator());
//			writer.write("AVG: "+ weightedScore[count-1]+System.lineSeparator());
//			writer.write(System.lineSeparator());
//			map.put("TOPIC-"+count, weightedScore[count-1]);
//			++count;
//		}
		writer.flush();
		writer.close();
		//queryScoreMapping.put(queryId, map);
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

	public void generateTopics() throws IOException {
		final int NO_OF_TOPICS = 200;
		BufferedReader reader = new BufferedReader(new FileReader("sampleModel"));
		for(int i=0;i<10;i++){
			reader.readLine();
		}
		
		List<Map<String,Double>> topics = new ArrayList<Map<String,Double>>();
		for(int i = 0;i < NO_OF_TOPICS;i++){
			topics.add(new HashMap<String, Double>());
		}
		
		for(int word = 0; word <= tokenMapping.size(); word++){
			String[] values = reader.readLine().trim().split(" ");
			String token = tokenReverseMapping.get(values[0].trim());
			
			for(int topicIndex = 0; topicIndex< NO_OF_TOPICS;topicIndex++){
				double value = 0.1;
				try{
					value = Double.valueOf(values[topicIndex+1]);
				}catch(Exception e){
					System.out.println("Error in parsing for word "+ word + " & index: "+ topicIndex);
				}
				topics.get(topicIndex).put(token, value);
			}
		}
		
		printTopics(topics);
		
		reader.close();
	}

	private void printTopics(List<Map<String, Double>> topics) throws IOException {
		final int NO_OF_WORDS_PER_TOPIC = 20;
		BufferedWriter writer = new BufferedWriter(new FileWriter("Topics.txt"));
		for (int i = 0 ; i < topics.size() ;i++) {
			writer.write("TOPIC-"+i+System.lineSeparator());
			//write the topic
			Map<String,Double> tempMap = new HashMap<String, Double>(NO_OF_WORDS_PER_TOPIC);
			Map<String,Double> sortedMap = JelineckUtil.sortByComparator(topics.get(i));
			Iterator<Map.Entry<String, Double>> iter = sortedMap.entrySet().iterator();
			for(int j = 0 ; j < NO_OF_WORDS_PER_TOPIC;j++){
				Entry<String,Double> entry = iter.next();
				writer.write(entry.getKey()+" "+ entry.getValue()+System.lineSeparator());
				tempMap.put(entry.getKey(), entry.getValue());
			}
			topicToTokenMapping.put("TOPIC-"+i, tempMap);
		}
		writer.flush();
		writer.close();
	}

	public void generateTopicDocDistribution() {
		int count = 0;
		for (String docId : docMapping.keySet()) {
			Map<String,Integer> docTf = getDocTf(docMapping.get(docId));
			
			Map<String,Double> tempMap = new HashMap<String,Double>();
			for(int i = 0; i < 200; i++){
				Map<String,Double> listOfWordsForTopic = topicToTokenMapping.get("TOPIC-"+i);
				double value = 0.0;
				for (String word : listOfWordsForTopic.keySet()) {
					if(docTf.containsKey(word)){
						value = value+ docTf.get(word) * listOfWordsForTopic.get(word);
					}
				}
				tempMap.put("TOPIC-"+i, value);
			}
			docToTopicMapping.put(docId, tempMap);
			
			System.out.println("TopicDoc created for "+ count++ +" successfully");
		}
	}

	private Map<String, Integer> getDocTf(DOC doc) {
		Map<String,Integer> map = new HashMap<String, Integer>();
		String[] words = getDocText(doc).toLowerCase().split(" ");
		for (String word : words) {
			word = word.toLowerCase().trim();
			if(map.containsKey(word)){
				map.put(word, map.get(word)+1);
			}else{
				map.put(word,1);
			}
		}
		return map;
	}

	public void generateMatrix() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("test.arff"));
		for (String docId : docToTopicMapping.keySet()) {
			writer.write(docId+",");
			Map<String,Double> topicsMap = docToTopicMapping.get(docId);
			for(int i=0;i<200;i++){
				if(i==199){
					writer.write(topicsMap.get("TOPIC-"+i)+System.lineSeparator());
				}else{
					writer.write(topicsMap.get("TOPIC-"+i)+",");
				}
			}
		}
		writer.flush();
		writer.close();
	}
	
	
}
