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
import com.vijet.ir.models.OkapiBM25;
import com.vijet.ir.models.OkapiTF;
import com.vijet.ir.models.UnigramJelinek;
import com.vijet.ir.util.JelineckUtil;
import com.vijet.ir.util.OkapiBM25Util;
import com.vijet.ir.util.OkapiUtil;
import com.vijet.ir.util.StemUtil;

public class JelinekEngine {

	private RandomAccessFile randomAccessFile = null;
	private BufferedReader brForLookupFile = null;
	
	public static Properties docsLengthMap = new Properties();
	public static Map<String, Long> termLookUpMap = new HashMap<String, Long>();
	public static Properties termDocFreqMap = new Properties();
	private static final List<String> docIds = new ArrayList<String>();

	static{
		// for lookup
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
		// For length
		try {
			docsLengthMap.load(JelinekEngine.class.getClassLoader().getResourceAsStream("docLength.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// for termDocFreq
		try {
			termDocFreqMap.load(new FileInputStream(new File("docFreq.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// for docIds
		Properties docProperties = new Properties();
		try {
			docProperties.load(JelinekEngine.class.getClassLoader().getResourceAsStream("docIds"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Iterator<Map.Entry<Object, Object>> propIter = docProperties.entrySet().iterator();
		while(propIter.hasNext()){
			docIds.add(propIter.next().getKey().toString());
		}
	}
	
	public void configure(){
		try {
			randomAccessFile = new RandomAccessFile(new File("finalInvertedIndex.txt"), "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void generateScores(String[] queryTerms) {
		Map<String,Double> docScores = new HashMap<String,Double>();
		List<TokenDesc> tempQueryTermList = null;
		
		for(int i = 1; i < queryTerms.length; i++){
			//String term = queryTerms[i].trim().toLowerCase();
			String term = StemUtil.getStemOfWord(queryTerms[i].trim().toLowerCase());
			
			if(termLookUpMap.containsKey(term)){
				tempQueryTermList = null;
				tempQueryTermList = new ArrayList<TokenDesc>(getDocumentsForTerm(term));

				//compute total freq of all this term;
				long TOTAL_TERM_COUNT = 0;
				Iterator<TokenDesc> countDocIter = tempQueryTermList.iterator();
				while(countDocIter.hasNext()){
					TOTAL_TERM_COUNT= TOTAL_TERM_COUNT + countDocIter.next().getCount();
				}
				
				if(TOTAL_TERM_COUNT == 0){
					continue;
				}
				
				final List<String> docIdsOfExistingQueryTerms = new ArrayList<String>();
				Iterator<TokenDesc> docIter = tempQueryTermList.iterator();
				while(docIter.hasNext()){
					docIdsOfExistingQueryTerms.add(docIter.next().getDocId());
				}
				
				Iterator<String>  allDocIds = docIds.iterator();
				while(allDocIds.hasNext()){
					String docId = allDocIds.next();
					if(docIdsOfExistingQueryTerms.contains(docId)){
						continue;
					}else{
						if(Long.valueOf(docsLengthMap.get(docId).toString()) == 0){
							continue;
						}
						tempQueryTermList.add(new TokenDesc(docId, 0, term));
					}
				}
				// for each of the 
				Iterator<TokenDesc> docsIter = tempQueryTermList.iterator();
				while(docsIter.hasNext()){
					TokenDesc doc = docsIter.next();
					double jScore = UnigramJelinek.score(doc, Long.valueOf(docsLengthMap.get(doc.getDocId()).toString()),TOTAL_TERM_COUNT);
					if(docScores.containsKey(doc.getDocId())){
						double score = jScore+ docScores.get(doc.getDocId());
						docScores.put(doc.getDocId(), score);
					}else{
						docScores.put(doc.getDocId(), jScore);
					}
				}
			}else{
				System.out.println("Term is not present in Lookup file: " + term);
			}
		}
		
		System.out.println("Jelineck-DOC-SIZE" + docScores.size());
		System.out.println("RANKING DOCUMENT COMPLETE");

		Map<String,Double> sortedScore = OkapiUtil.sortByComparator(docScores);

		System.out.println("SORTING DOCUMENT COMPLETE");
		//removed
		try {
			JelineckUtil.dumpResultsToFile(sortedScore,queryTerms[0]);
			System.out.println("RESULTS DUMPED FOR Laplace for QUERY " + queryTerms[0]);
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
