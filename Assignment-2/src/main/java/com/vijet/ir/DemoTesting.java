package com.vijet.ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vijet.ir.util.StemUtil;

public class DemoTesting {
	private static final File DEMO_WORDS_LIST_FILE = new File("in.0");
	private static final File FINAL_INVERTED_INDEX_FILE = new File("finalInvertedIndex.txt");
	private static final File FINAL_CATALOG_FILE = new File("finalcatalogfile.txt");
	private static final File DEMO_OUTPUT_FILE = new File("demo_output_file.txt");
	
	public static void main(String[] args) throws IOException {
		//Read catalog file into Memory;
		Map<String,Long> catalogMap = new HashMap<String,Long>();
		BufferedReader br = new BufferedReader(new FileReader(FINAL_CATALOG_FILE));
		String catalogTerm = new String();
		while((catalogTerm=br.readLine())!=null){
			String[] temp = catalogTerm.split(" ");
			catalogMap.put(temp[0], Long.valueOf(temp[1]));
		}
		br.close();
		
		// Open InvertedFile
		RandomAccessFile raf = new RandomAccessFile(FINAL_INVERTED_INDEX_FILE, "r");
		
		//Open the output file
		BufferedWriter bw = new BufferedWriter(new FileWriter(DEMO_OUTPUT_FILE));
		
		// process the demo file
		BufferedReader brDemo = new BufferedReader(new FileReader(DEMO_WORDS_LIST_FILE));
		String term = null;
		while((term=brDemo.readLine())!=null){
			
			String demoTerm = StemUtil.getStemOfWord(term);
			if(catalogMap.containsKey(demoTerm)){
				long offset = catalogMap.get(demoTerm);
				raf.seek(offset);
				StringBuilder demoTermInvertedEntry = new StringBuilder(raf.readLine());
				List<TokenDesc> tokenList = getTokenDescList(demoTermInvertedEntry.toString());
				long documentFreq = tokenList.size();
				long termFreq = getTermFreqFromList(tokenList);
				bw.write(term+" "+ documentFreq +" " + termFreq +System.lineSeparator());
			}else{
				System.out.println("Catalog doesnot contain the term : " + demoTerm);
				bw.write(term+" 0 0 " +System.lineSeparator());
			}
		}
		
		brDemo.close();
		bw.flush();
		bw.close();
		raf.close();
	}
	
	private static long getTermFreqFromList(List<TokenDesc> tokenList) {
		long count = 0;
		for (TokenDesc tokenDesc : tokenList) {
			count+=tokenDesc.getCount();
		}
		return count;
	}

	private static List<TokenDesc> getTokenDescList(String tokenDescString) {
		List<TokenDesc> temp = new ArrayList<TokenDesc>();
		String[] tokenDescList = tokenDescString.split("=")[1].split("~");
		for (String tokenDesc : tokenDescList) {//12345#3!12-12-12
			String[] tokenData = tokenDesc.trim().split("#");			
			
			String[] tokenDescData = tokenData[1].split("!");
			try{
				temp.add(new TokenDesc(tokenData[0], tokenData[1].split("-").length, null, tokenData[1]));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return temp;
	}

}
