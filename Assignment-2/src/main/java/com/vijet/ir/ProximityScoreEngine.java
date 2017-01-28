package com.vijet.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.vijet.ir.model.Document;
import com.vijet.ir.models.OkapiTF;
import com.vijet.ir.util.OkapiUtil;
import com.vijet.ir.util.ProximitySerachUtil;
import com.vijet.ir.util.StemUtil;
import com.vijet.ir.util.UnigramLaplaceUtil;

public class ProximityScoreEngine {

	private RandomAccessFile randomAccessFile = null;
	private BufferedReader brForLookupFile = null;
	
	public static Properties docsLengthMap = new Properties();
	public static Map<String, Long> termLookUpMap = new HashMap<String, Long>();
	public static Properties intToDocIdsMapping = new Properties();

	static{
		try {
			intToDocIdsMapping.load(new FileInputStream(new File("integerToDocIdMapping.txt")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("FinalCatalogFile.txt")));
			String brStr = null;
			while((brStr=  br.readLine())!= null){
				String[] temp = brStr.split(" ");
				termLookUpMap.put(temp[0], Long.valueOf(temp[1]));
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			docsLengthMap.load(ProximityScoreEngine.class.getClassLoader().getResourceAsStream("docLength.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void configure(){
		try {
			randomAccessFile = new RandomAccessFile(new File("FinalInvertedIndex.txt"), "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void generateScores(String[] queryTerms) throws Exception {
		List<TokenDesc> queryDocs = new ArrayList<TokenDesc>();
			
		for(int i = 1 ; i < queryTerms.length;i++){
			//String term = queryTerms[i].trim().toLowerCase();
			String term = StemUtil.getStemOfWord(queryTerms[i].trim().toLowerCase());

			if(!termLookUpMap.containsKey(term)){
				System.out.println("Query term that is skipped: " + term);
				continue;
			}
			List<TokenDesc> docs = getDocumentsForTerm(term);
			queryDocs.addAll(docs);
		}
		// DocId -> List<Docs>
		Map<String,List<String>> tempList = new HashMap<String, List<String>>();
		
		
		for (TokenDesc tokenDesc : queryDocs) {
			if(tempList.containsKey(tokenDesc.getDocId())){
				tempList.get(tokenDesc.getDocId()).add(tokenDesc.getPositions());
			}else{
				List<String> tList = new ArrayList<String>();
				tList.add(tokenDesc.getPositions());
				tempList.put(tokenDesc.getDocId(), tList);
			}
		}
		//DocId -> proximityScore
		Map<String,Double> proximityScoreDocList= new HashMap<String, Double>();

		Iterator<Map.Entry<String,List<String>>>  iter = tempList.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,List<String>> entry = iter.next();
			List<String> matchingTermsInDocList = entry.getValue();
			List<List<Integer>> positionList = new ArrayList<List<Integer>>();

			for (String matchingString : matchingTermsInDocList) {
				System.out.println(matchingString);
				List<Integer> posList = new ArrayList<Integer>();
				String[] pos = matchingString.split("-");
				posList.add(Integer.valueOf(pos[0]));
				int positionCount = Integer.valueOf(pos[0]);
				for (int i = 1 ; i < pos.length;i++) {
					positionCount = positionCount+ Integer.valueOf(pos[i]);
					posList.add(positionCount);
				}
				positionList.add(posList);
			}
			int minSpan; 
			if(positionList.size()==1){
				minSpan = 1;
			}else{
				try{
					minSpan = ProximitySearch.scoreForDocument(positionList);
				}catch(Exception e){
					minSpan = 1;
				}
			}
			
			double score = proximityScore(minSpan,positionList.size(),entry.getKey());
			proximityScoreDocList.put(entry.getKey(), score);
			//System.out.println("Score: ->" + score);
			//System.out.println("Value: " + ProximitySearch.scoreForDocument(positionList));
			//System.out.println(entry.getValue().size());
			
		}
		
		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(proximityScoreDocList);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			ProximitySerachUtil.dumpResultsToFile(sortedScore,queryTerms[0]);
			System.out.println("RESULTS DUMPED FOR OKAPI for QUERY " + queryTerms[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DUMPED COMPLETE");
	}
	
	public static double proximityScore(int minRange, int noOfContainingTerms, String docId){
		return (1500.0 - minRange)*noOfContainingTerms/(Long.valueOf(String.valueOf(docsLengthMap.get(docId))) + 174584);
	}

	private List<TokenDesc> getDocumentsForTerm(String term) {
		List<TokenDesc> tokens = new ArrayList<TokenDesc>();
		Long termOffset = termLookUpMap.get(term);
		try {
			randomAccessFile.seek(termOffset);
			StringBuilder docsDescAsStr = new StringBuilder(randomAccessFile.readLine());
			String[] docsTokens = docsDescAsStr.toString().split("=")[1].split("~");
			for (String docToken : docsTokens) {
				String[] tempData = docToken.split("#");
				String[] tempData1 = tempData[1].split("-");
				tokens.add(new TokenDesc(StartUp.intToDocIdsMapping.getProperty(tempData[0]), tempData1.length,term,tempData[1]));
			}
			return tokens;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void close(){
		try {
			randomAccessFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
