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

public class GenerateMatrix {
	private final Pattern validFilePattern = Pattern.compile("^ap");
	private final String STOPLIST_FILE = "input/stoplist.txt";
	private final Set<String> stopWords = new HashSet<String>();


	private final Map<String,DOC> docMapping = new HashMap<String,DOC>();
	private final Map<String,Set<String>> queryDocumentMapping = new HashMap<String, Set<String>>();


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
	}
	
	public void generateRepresentation() throws IOException{
		File folder = new File("Topics");
		File[] docFiles = folder.listFiles();
		for (File docFile : docFiles) {
			genereateTopicDistMatrix(docFile.getName().split("_")[1].replaceAll(".txt", ""), docFile);
		}
	}

	
	private void genereateTopicDistMatrix(String queryId,File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("TopicsDist/topicDist_"+queryId+".txt"));
		Map<String,Double> topicsValues = new HashMap<String, Double>();
		List<DOC> docs  = getListOfDocs(queryId);
		List<Map<String,Double>> topicsList = getTopicsList(queryId);
		for (DOC doc : docs) {
			writer.write(doc.getDOCNO()+ getRepData(doc,topicsList,topicsValues)+System.lineSeparator());
		}
		writer.flush();
		writer.close();
		BufferedWriter writer1 = new BufferedWriter(new FileWriter("TopicsDist1/topicDist_"+queryId+".txt"));
		Map<String,Double> topicsValues1 = JelineckUtil.sortByComparator(topicsValues);
		for (Entry<String, Double> map : topicsValues1.entrySet()) {
			writer1.write(map.getKey()+":"+map.getValue()+System.lineSeparator());
		}
		writer1.flush();
		writer1.close();
	}

	
	private String getRepData(DOC doc, List<Map<String, Double>> topicsList,Map<String,Double> topicsValues) {
		StringBuilder builder = new StringBuilder();
		Map<String, Integer> tfCount = getTfForDoc(doc);
		for (int i = 0;i < 20;i++) {
			Map<String,Double> map = topicsList.get(i);
			Set<String> keys = map.keySet();
			double value = 0.0;
			for (String key : keys) {
				if(tfCount.containsKey(key)){
					value = value + tfCount.get(key) * map.get(key);
				}
			}
			if(value!=0.0){
				builder.append(" TOPIC-"+i+" "+value);
				if(topicsValues.containsKey("TOPIC-"+i)){
					topicsValues.put("TOPIC-"+i, topicsValues.get("TOPIC-"+i)+value);
				}else{
					topicsValues.put("TOPIC-"+i,value);
				}
			}
		}
		return builder.toString();
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
	
	private String getDocText(DOC doc){
		return CleanText.cleanText(new StringBuilder().append(doc.getHEAD()).append(" ").append(doc.getTEXT()).toString());
	}

	private List<Map<String, Double>> getTopicsList(String queryId) throws IOException {
		List<Map<String,Double>> list = new ArrayList<Map<String,Double>>();
		BufferedReader reader = new BufferedReader(new FileReader("Topics/Topics_"+queryId+".txt"));
		String line = new String();
		while((line=reader.readLine())!= null){
			if(line.contains("TOPIC")){
				Map<String,Double> map = new HashMap<String, Double>();
				String topicName = line;
				for(int i = 0 ; i < 30; i++){
					String[] value = reader.readLine().split(":");
					map.put(value[0], Double.valueOf(value[1]));
				}
				list.add(map);
			}
		}
		return list;
	}

	private List<DOC> getListOfDocs(String queryId) {
		List<DOC> list = new ArrayList<DOC>();
		Set<String> set = queryDocumentMapping.get(queryId);
		for (String docId : set) {
			list.add(docMapping.get(docId));
		}
		return list;
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

	


	

}
