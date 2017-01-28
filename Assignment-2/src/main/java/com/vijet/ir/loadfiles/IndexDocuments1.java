package com.vijet.ir.loadfiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vijet.ir.MergeSort;
import com.vijet.ir.TokenDesc;
import com.vijet.ir.elasticsearchclient.ESClient;
import com.vijet.ir.model.DOC;
import com.vijet.ir.util.StemUtil;



public class IndexDocuments1 {
	final File invertedIndexFile = new File("finalInvertedIndex.txt");

	private final String DEFAULT_CHAR_SET = "UTF-8";
	private final File DATA_FOLDER;
	private final ESClient elasticSearchClient;
	private final Pattern validFilePattern = Pattern.compile("^ap");
	private int fileCount = 1;
	
	private List<Map<String, List<TokenDesc>>> listOfIntermediateInvtdIndexes = new LinkedList<Map<String,List<TokenDesc>>>();
	private Map<String, List<TokenDesc>> finalInvertedIndexes = new HashMap<String, List<TokenDesc>>();
	
	private Properties docIdsToIntegerMapping = new Properties();
	
	private static final Set<String> stopWordList = new HashSet<String>();
	
	private static final Map<String,List<Integer>> termPositionsForDocument = new HashMap<String, List<Integer>>();
	
	static{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("stoplist.txt")));
			String stopTerm = new String();
			while((stopTerm=reader.readLine())!=null){
				stopWordList.add(stopTerm);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Stoplist file is not present");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	
	public IndexDocuments1(String folderPath,ESClient esClient){
		DATA_FOLDER = new File(folderPath);
		elasticSearchClient = esClient;
		
		try {
			docIdsToIntegerMapping.load(new FileInputStream(new File("docIdsToIntegerMapping.txt")));
		} catch (IOException e) {
			System.out.println("DocIds To Integer mapping file is absent.");
			System.exit(1);
		}
	}

	public void indexDocuments() throws Exception{
		validateDataFolder();
		File[] docFiles = DATA_FOLDER.listFiles();
		List<DOC> docsForIndex = new ArrayList<DOC>();
		for (File docFile : docFiles) {			
			if(validateFile(docFile)){
				List<DOC> listOfDocs = parseFile(docFile);
				System.out.println("ParsedFile: " + docFile.getName() + " No of <DOC>'s : " + listOfDocs.size());
				//indexFileOnElasticSearch(listOfDocs);

				if((docsForIndex.size()+listOfDocs.size())>5000){
					indexTermsForDocs(docsForIndex);
					docsForIndex.clear();
					docsForIndex.addAll(listOfDocs);
				}else{
					docsForIndex.addAll(listOfDocs);
				}
			}else{
				System.out.println("File Skipped: " + docFile.getName());
			}
		}
		indexTermsForDocs(docsForIndex);
		System.out.println("Total size of invertedIndexes: " + listOfIntermediateInvtdIndexes.size());

	}

	private void indexTermsForDocs(List<DOC> listOfDocs) {
		Map<String,List<TokenDesc>> tempInvertedTokens = new HashMap<String,List<TokenDesc>>();

		String[] tokens = null;
		for (DOC doc : listOfDocs) {
			//System.out.println(doc.getTEXT());
			tokens = cleanDocument(doc.getTEXT().trim().toLowerCase());
			//System.out.println(Arrays.asList(tokens));
			Map<String,Integer> tokensWithFreq = processTokens(tokens);
			// Update the tempInvertedTokens
			Iterator<Map.Entry<String, Integer>> tokensWithFreqIter = tokensWithFreq.entrySet().iterator();
			while(tokensWithFreqIter.hasNext()){
				Map.Entry<String, Integer> tokenWithFreq = tokensWithFreqIter.next();
				TokenDesc tokenDesc = new TokenDesc(doc.getDOCNO(),tokenWithFreq.getValue(), null, getPositionOfTerm(tokenWithFreq.getKey()));

				//clean tokens(Remove tokens like stop words)
				if(tokenWithFreq.getKey().isEmpty()) continue;

				if(tempInvertedTokens.containsKey(tokenWithFreq.getKey())){
					tempInvertedTokens.get(tokenWithFreq.getKey()).add(tokenDesc);
				}else{
					List<TokenDesc> tempTokenList = new ArrayList<TokenDesc>();
					tempTokenList.add(tokenDesc);
					tempInvertedTokens.put(tokenWithFreq.getKey(), tempTokenList);
				}
			}
		}

		//print the inverted tokens to the file;
		printTokensToFile(tempInvertedTokens,fileCount);
		generateOffsetFileForIntermediateIndex(fileCount);
		try {
			mergeIntermediateIndexes(fileCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		try {
//			mergeIntermediateIndexesWithFinalList(fileCount);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		++fileCount;
	}
	
	/**
	 * cool=12345#3!12-12-12~
	 */
	private String getPositionOfTerm(String key) {
		if(!termPositionsForDocument.containsKey(key)){
			throw new IllegalStateException("Function malfunctioning");
		}
		List<Integer> termPos = termPositionsForDocument.get(key);
		StringBuilder termPosBuilder = new StringBuilder();
		for (Integer pos : termPos) {
			termPosBuilder.append(pos);
			termPosBuilder.append("-");
		}

		String st = termPosBuilder.toString().substring(0, termPosBuilder.toString().length()-1);
		return st;
	}

	private void mergeIntermediateIndexes(int fileCount) throws IOException{
		// Intermediate files
		final File intermediateInvertedIndexFile = new File("Results/IntermediateInvertedIndex_"+ fileCount +".txt");
		final File intermediateCatalogFile = new File("Results/IntermediateCatalogFile_"+ fileCount +".txt");
		// Final files
		final File finalInvertedIndexFile = new File("Results/FinalInvertedIndex.txt");
		final File finalCatalogFile = new File("Results/FinalCatalogFile.txt");
		// TempFile
		final File tempFinalIndexFile = new File("Results/tempFinalIndexFile.txt");
		final File tempCatalogFile  = new File("Results/tempCatalogFile.txt");
		
		if(fileCount == 1){
			intermediateInvertedIndexFile.renameTo(finalInvertedIndexFile);
			intermediateCatalogFile.renameTo(finalCatalogFile);
			return;
		}
		
		RandomAccessFile rafIntermediateInvertedIndexFile = new RandomAccessFile(intermediateInvertedIndexFile, "r");
		RandomAccessFile rafFinalInvertedIndexFile = new RandomAccessFile(finalInvertedIndexFile, "r");
		BufferedWriter bfw = new BufferedWriter(new FileWriter(tempFinalIndexFile));

		// Load the catlog files into memory
		Map<String,Long> intermediateCatalogMap = deserializeCatalogFile(intermediateCatalogFile);
		Map<String,Long> finalCatalogMap = 	deserializeCatalogFile(finalCatalogFile);
		
		Iterator<Map.Entry<String, Long>> intermediateCatalogMapIter = intermediateCatalogMap.entrySet().iterator();
		while(intermediateCatalogMapIter.hasNext()){
			Map.Entry<String, Long> tempEntry = intermediateCatalogMapIter.next();
			rafIntermediateInvertedIndexFile.seek(tempEntry.getValue());
			List<TokenDesc> tokenDescList = getTokenDescList(rafIntermediateInvertedIndexFile.readLine());
			
			if(finalCatalogMap.containsKey(tempEntry.getKey())){
				long value = finalCatalogMap.get(tempEntry.getKey());
				rafFinalInvertedIndexFile.seek(value);
				tokenDescList.addAll(getTokenDescList(rafFinalInvertedIndexFile.readLine()));
				
				finalCatalogMap.remove(tempEntry.getKey());
			}
			//Collections.sort(tokenDescList);
			MergeSort.MergeSort(tokenDescList, 0, tokenDescList.size()-1);
			bfw.write(tempEntry.getKey().trim()+"="+getStringFromTokenDescList(tokenDescList) +System.lineSeparator());
		}
		//TODO: do the same things to the other side of the parts
		Iterator<Map.Entry<String, Long>> finalCatalogMapIter = finalCatalogMap.entrySet().iterator();
		while(finalCatalogMapIter.hasNext()){
			Map.Entry<String, Long> tempEntry = finalCatalogMapIter.next();
			long value = finalCatalogMap.get(tempEntry.getKey());
			rafFinalInvertedIndexFile.seek(value);
			List<TokenDesc> tokenDescList = new ArrayList<TokenDesc>();
			tokenDescList.addAll(getTokenDescList(rafFinalInvertedIndexFile.readLine()));
			//Collections.sort(tokenDescList);
			MergeSort.MergeSort(tokenDescList, 0, tokenDescList.size()-1);
			bfw.write(tempEntry.getKey().trim()+"="+getStringFromTokenDescList(tokenDescList) +System.lineSeparator());
		}
		
		bfw.flush();
		bfw.close();
		
		rafFinalInvertedIndexFile.close();
		rafIntermediateInvertedIndexFile.close();
		//generate the offset file for the final
		generateOffsetFileForTempFinal(tempFinalIndexFile,tempCatalogFile);
		//rename the temp files to the final files.
		tempFinalIndexFile.renameTo(finalInvertedIndexFile);
		tempCatalogFile.renameTo(finalCatalogFile);
		//TODO: DELETE the intermediate Files
		//intermediateInvertedIndexFile.delete();
		//intermediateCatalogFile.delete();
		return;
	}
	
	private void generateOffsetFileForTempFinal(File tempInvertedIndexFile, File tempCatalogFile){

			try {
				BufferedWriter termlookUpWriter = new BufferedWriter(new FileWriter(tempCatalogFile));
				//Read the file from the invertedIndex
				BufferedReader reader = new BufferedReader(new FileReader(tempInvertedIndexFile));
				
				String termString  = new String();
				long offsetCount = 0;
				while((termString =  reader.readLine())!=null){
					String[] termStringTokens = termString.split("=");
					termlookUpWriter.write(termStringTokens[0]+" "+offsetCount+" "+termString.length()+System.lineSeparator());
					offsetCount+=termString.length()+1;
				}
				termlookUpWriter.flush();
				termlookUpWriter.close();
				reader.close();
				System.out.println("Completely generated the index file");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private String getStringFromTokenDescList(List<TokenDesc> tokenDescList) {
		StringBuilder string = new StringBuilder();
		Iterator<TokenDesc> iter = tokenDescList.iterator();
		while(iter.hasNext()){
			TokenDesc token = iter.next();
			string.append(token.getDocId()+"#"+token.getPositions() +"~");
		}
		return string.toString();
	}
//TPPP
	//cool=12345#3!12-12-12~
	// cool=12345#12-12-12~
	private List<TokenDesc> getTokenDescList(String tokenDescString) {
		List<TokenDesc> temp = new ArrayList<TokenDesc>();
		String[] tokenDescList = tokenDescString.split("=")[1].split("~");
		for (String tokenDesc : tokenDescList) {//12345#3!12-12-12
			String[] tokenData = tokenDesc.split("#");
			
			String[] tokenDescData = tokenData[1].split("!");
			try{
				temp.add(new TokenDesc(tokenData[0], tokenData[1].split("-").length, null, tokenData[1]));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return temp;
	}

	private Map<String,Long> deserializeCatalogFile(File catalogFile) throws IOException{
		Map<String,Long> tempCatalogMap = new HashMap<String,Long>();
		BufferedReader brCatalogFile = new BufferedReader(new FileReader(catalogFile));
		String brStr = new String();
		while((brStr = brCatalogFile.readLine())!=null ){
			String[] data = brStr.split(" ");
			tempCatalogMap.put(data[0], Long.valueOf(data[1]));
		}
		brCatalogFile.close();
		return tempCatalogMap;
	}

	private void generateOffsetFileForIntermediateIndex(int fileCount) {
		final File tempOffsetFile = new File("Results/IntermediateCatalogFile_"+ fileCount +".txt");
		final File tempIndexFile = new File("Results/IntermediateInvertedIndex_"+ fileCount +".txt");

		try {
			BufferedWriter termlookUpWriter = new BufferedWriter(new FileWriter(tempOffsetFile));
			//Read the file from the temp invertedIndex
			BufferedReader reader = new BufferedReader(new FileReader(tempIndexFile));
			
			String termString  = new String();
			long offsetCount = 0;
			while((termString =  reader.readLine())!=null){
				String[] termStringTokens = termString.split("=");
				termlookUpWriter.write(termStringTokens[0]+" "+offsetCount+" "+termString.length()+System.lineSeparator());
				offsetCount+=termString.length()+1;
			}
			
			termlookUpWriter.flush();
			termlookUpWriter.close();
			
			reader.close();
			
			System.out.println("Intermediate Offset file written successfully: " + tempOffsetFile.getName());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printTokensToFile(Map<String, List<TokenDesc>> tempInvertedTokens, int fileCount) {
		File file = new File("Results/IntermediateInvertedIndex_"+ fileCount +".txt");

		FileWriter fileWrite = null;
		try {
			fileWrite = new FileWriter(file);

			Iterator<Map.Entry<String, List<TokenDesc>>> tempInvertedTokensIter = tempInvertedTokens.entrySet().iterator();
			while(tempInvertedTokensIter.hasNext()){
				Map.Entry<String, List<TokenDesc>> tempToken = tempInvertedTokensIter.next();
				fileWrite.write(tempToken.getKey()+"=");
				for (TokenDesc tokenDesc : tempToken.getValue()) {
					fileWrite.write(tokenDesc.getDocId()+"#"+tokenDesc.getPositions()+"~");
				}
				fileWrite.write(System.lineSeparator());
			}

			fileWrite.flush();
			fileWrite.close();

			System.out.println("TempInvertedIndex File written successfully " + file.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	private Map<String,Integer> processTokens(String[] tokens) {
		// term -> freq (of one document)
		Map<String,Integer> tokenListForADoc = new HashMap<String,Integer>();
		for (String token : tokens) {
			token = cleanToken(token);
			if(tokenListForADoc.containsKey(token)){
				tokenListForADoc.put(token, tokenListForADoc.get(token)+1);
			}else{
				tokenListForADoc.put(token,1);
			}
		}
		return tokenListForADoc;
	}

	/*This method is used to play with token*/
	private String cleanToken(String token) {
		return StemUtil.getStemOfWord(token);
		//return token;
	}

	private String[] cleanDocument(String docTextString){
		StringBuilder docTEXTString = new StringBuilder();
		
		docTEXTString.append(docTextString.replaceAll("'s", " is")
				.replaceAll("-", " ")
				//.replaceAll("(a-z)*(\\.)+", " ")//added new
				//.replaceAll("[a-z](\\.)[a-z]", " ") // added new
				.replaceAll("[^A-Za-z0-9. ]", ""));
		String[] tokens = docTEXTString.toString().split(" ");
		for(int i = 0; i < tokens.length; i++){
			tokens[i] = tokens[i].replaceAll("(\\.$|^\\.)", "");
		}
		
		termPositionsForDocument.clear();
		int position = 0;
		//
		
		//REMOVE FOLLOWING TOKENS
		// a,b,c, .
		List<String> cleanTokens = new ArrayList<String>();
		for (String token : tokens) {
			//update the positions of the tokens
			
		
			String stemmedToken = null;
			// updating token positions over
			if(stopWordList.contains(token.trim())){
				position++;
				continue;
			}else if(token.length() == 1){
				position++;
				continue;
			}else if(token.equals(".")){
				position++;
				continue;
			}else if(token.matches("[\\d]+(\\.)[a-z]+")){// 1.Name, 2.Name
				stemmedToken = StemUtil.getStemOfWord(token.split("\\.")[1].trim());
				try{
					cleanTokens.add(token.split("\\.")[1].trim());
				}catch(Exception e){
					System.out.println(token);
					e.printStackTrace();
				}
			}
			else{ 
				stemmedToken = StemUtil.getStemOfWord(token);
				cleanTokens.add(token);
			}
			
			if(termPositionsForDocument.containsKey(stemmedToken)){
				termPositionsForDocument.get(stemmedToken).add(position - termPositionsForDocument.get(stemmedToken).get(termPositionsForDocument.get(stemmedToken).size()-1));
			}else{
				List<Integer> tempList = new ArrayList<Integer>();
				tempList.add(position);
				termPositionsForDocument.put(stemmedToken, tempList);
			}
			position++;
		}
		
		String[] test = new String[cleanTokens.size()];
		
		return cleanTokens.toArray(test);
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
			//case "DOCNO":tempDOC.setDOCNO(textValue.split("AP89")[1]);break;
			case "DOCNO":tempDOC.setDOCNO(docIdsToIntegerMapping.getProperty(textValue));break;
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
	
	public void shrinkIndexFile() throws IOException{
		System.out.println("Shrinking Index File");
		BufferedWriter bfw = new BufferedWriter(new FileWriter(new File("tempCatalogFile.txt")));
		BufferedWriter bfw1 = new BufferedWriter(new FileWriter(new File("tempIndexFile.txt")));

		BufferedReader reader = new BufferedReader(new FileReader(new File("Results/FinalCatalogFile.txt")));
		RandomAccessFile raf = new RandomAccessFile(new File("Results/FinalInvertedIndex.txt"), "r");
		String str =  null;
		long count = 0;
//		while((str=reader.readLine())!=null){
//			String[] info = str.split(" ");
//			long offset = Long.valueOf(info[1]);
//			raf.seek(offset);
//			String termDesc = raf.readLine();
//			String data = termDesc.split("=")[1];
//			data = data.substring(0,data.length()-1);
//			bfw1.write(data+System.lineSeparator());
//			bfw.write(info[0]+" "+count+ System.lineSeparator());
//			count=count+data.length()+1;
//		}
		
		while((str = raf.readLine())!=null){
			bfw1.write(str.split("=")[1]+System.lineSeparator());
		}
		
		bfw.flush();
		bfw1.flush();
		bfw.close();
		bfw1.close();
		raf.close();
	}

}
