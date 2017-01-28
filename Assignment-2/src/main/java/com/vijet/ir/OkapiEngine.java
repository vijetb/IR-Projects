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
import com.vijet.ir.util.StemUtil;

public class OkapiEngine {

	private RandomAccessFile randomAccessFile = null;
	private BufferedReader brForLookupFile = null;
	
	public static Properties docsLengthMap = new Properties();
	public static Map<String, Long> termLookUpMap = new HashMap<String, Long>();

	static{
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
			docsLengthMap.load(OkapiEngine.class.getClassLoader().getResourceAsStream("docLength.properties"));
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
	
	public void generateScores(String[] queryTerms) {
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
		
		Map<String,Double> okapiScoreDocList= new HashMap<String, Double>();

		Iterator<TokenDesc> docIter = queryDocs.iterator();
		while(docIter.hasNext()){
			TokenDesc doc = docIter.next();
			//double okapiScore = OkapiTF.okapiScore(doc.getTermFreq(), Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()));
			double okapiScore = OkapiTF.okapiScore(Long.valueOf(doc.getCount()), Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()));
			if(okapiScoreDocList.containsKey(doc.getDocId())){
				double oScore = okapiScore + okapiScoreDocList.get(doc.getDocId());
				okapiScoreDocList.put(doc.getDocId(), oScore);
			}else{
				okapiScoreDocList.put(doc.getDocId(), okapiScore);
			}
		}
		
		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(okapiScoreDocList);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			OkapiUtil.dumpResultsToFile(sortedScore,queryTerms[0]);
			System.out.println("RESULTS DUMPED FOR OKAPI for QUERY " + queryTerms[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("DUMPED COMPLETE");
		
		
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
				tokens.add(new TokenDesc(StartUp.intToDocIdsMapping.getProperty(tempData[0]), tempData1.length,term));
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
