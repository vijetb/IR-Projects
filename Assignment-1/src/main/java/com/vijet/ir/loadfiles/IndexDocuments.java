package com.vijet.ir.loadfiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vijet.ir.elasticsearchclient.ESClient;
import com.vijet.ir.model.DOC;



public class IndexDocuments {
	private final String DEFAULT_CHAR_SET = "UTF-8";
	private final File DATA_FOLDER;
	private final ESClient elasticSearchClient;
	private final Pattern validFilePattern = Pattern.compile("^ap");
	
	public IndexDocuments(String folderPath,ESClient esClient){
		DATA_FOLDER = new File(folderPath);
		elasticSearchClient = esClient;
	}
	
	public void indexDocuments() throws Exception{
		validateDataFolder();
		File[] docFiles = DATA_FOLDER.listFiles();
		for (File docFile : docFiles) {
			if(validateFile(docFile)){
				List<DOC> listOfDocs = parseFile(docFile);
				System.out.println("ParsedFile: " + docFile.getName() + " No of <DOC>'s : " + listOfDocs.size());
				indexFileOnElasticSearch(listOfDocs);
			}else{
				System.out.println("File Skipped: " + docFile.getName());
			}
		}
	}
	
	private boolean validateFile(File docFile) {
		return validFilePattern.matcher(docFile.getName()).find();
	}

	private void validateDataFolder() throws Exception {
		if(DATA_FOLDER.exists() && DATA_FOLDER.isDirectory()){
			return;
		}
		throw new Exception("Datafolder path is incorrect!");
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
			if(tempDoc.getTEXT()==null){
				System.out.println("Text null foung for " + doc.html());
			}
		}
		return listOfDocs;
	}

	private DOC unmarshellDOC(Element doc) {
		DOC tempDOC = new DOC();
		String[] tags = {"DOCNO","TEXT"};
		
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

	private void indexFileOnElasticSearch(List<DOC> listOfDocs) {
		elasticSearchClient.update(listOfDocs);
	}


	
}
