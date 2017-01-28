package com.vijet.ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vijet.ir.evalmodels.FMeasure;
import com.vijet.ir.evalmodels.NDCGMeasure;
import com.vijet.ir.model.Doc;
import com.vijet.ir.model.Score;

public class TrecEval {
	/**
	 * Relevance value
	 */
	private static final int RELEVANCE = 1;
	/**
	 * Cutoffs at different values
	 */
	private static final int CUTOFF_5 = 5;
	private static final int CUTOFF_10 = 10;
	private static final int CUTOFF_20 = 20;
	private static final int CUTOFF_50 = 50;
	private static final int CUTOFF_100 = 100;
	private static final int CUTOFF_200 = 200;
	private static final int CUTOFF_500 = 500;
	private static final int CUTOFF_1000 = 1000;
	/**
	 * Used to generate the P-R curve
	 */
	private static final String QUERY_FILENAME = "query_";
	/**
	 * Map that contains mapping for QREL file
	 * {queryId -> {docId --> boolean}}
	 */
	private final Map<String,Map<String,Integer>> qrelMap = new HashMap<String, Map<String,Integer>>();
	/**
	 * Map that contains mapping for Results file
	 * {queryId -> [doc]}
	 */
	private final Map<String,List<Doc>> resultsMap = new HashMap<String, List<Doc>>();
	/**
	 * Map to store Scores of F-measure for all the queries
	 */
	private final Map<String, Score> fMeasureMap = new HashMap<String, Score>();
	/**
	 * Map to store nDCG measures for all the queries
	 */
	private final Map<String, Score> nDCGMeasureMap = new HashMap<String, Score>();
	/**
	 * Flag indicating if the file loaded is trecFile
	 */
	private final boolean isTrecFile;
	/**
	 * List for storing avgPrecision values
	 */
	private List<Double> avgPrecisionValues = new ArrayList<Double>();
	/**
	 * List for storing zPrecision values
	 */
	private List<Double> zPrecisionValues = new ArrayList<Double>();

	
	public TrecEval(boolean isTrecFile){
		this.isTrecFile = isTrecFile;
	}
	
	/**
	 * Loads the file into memory. 
	 * @param qrelFile the qrelFile 
	 * @param resultsFile the results file that will be computed based on qrelFile
	 * @throws IOException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void loadData(final String qrelFile, String resultsFile) throws IOException,ArrayIndexOutOfBoundsException{
		loadQrelMap(qrelFile);
		loadResultsMap(resultsFile);
	}
	
	/**
	 * Reads and populates the QREL map into the memory
	 */
	private void loadQrelMap(String qrelFileName) throws IOException,ArrayIndexOutOfBoundsException{
		//51 http://www.one.com 0
		BufferedReader tempReader = new BufferedReader(new FileReader(qrelFileName));
		String line = new String();
		while((line=tempReader.readLine())!=null){
			String[] qrelData = line.split(" ");
			if(qrelMap.containsKey(qrelData[0].trim())){
				//qrelMap.get(qrelData[0]).put(qrelData[1].trim(), Integer.parseInt(qrelData[2].trim()));
				if(isTrecFile){
					qrelMap.get(qrelData[0]).put(qrelData[1].trim(), Integer.parseInt(qrelData[2].trim()));
				}else{
					qrelMap.get(qrelData[0]).put(qrelData[1].trim(), (int)Math.round(Double.valueOf(qrelData[2])));
				}
			}else{
				Map<String,Integer> tempMap = new HashMap<String, Integer>();
				if(isTrecFile){
					tempMap.put(qrelData[1], Integer.parseInt(qrelData[2]));
				}else{
					tempMap.put(qrelData[1], (int)Math.round(Double.valueOf(qrelData[2])));
				}
				//tempMap.put(qrelData[1], Integer.parseInt(qrelData[2]));
				qrelMap.put(qrelData[0], tempMap);
			}
		}
		tempReader.close();
		System.out.println("*********** QREL FILE LOADED SUCCESSFULLY *******************");
	}
	
	/**
	 * Loads the Results file and populates the resultsMap
	 */
	private void loadResultsMap(String resultsFileName) throws IOException,ArrayIndexOutOfBoundsException{
		//51 http://www.one.com
		BufferedReader tempReader = new BufferedReader(new FileReader(resultsFileName));
		String line = new String();
		while((line=tempReader.readLine())!=null){
			String[] resultsData = line.split(" ");
			Doc doc = new Doc();
			doc.setDocId(resultsData[1].trim());
			doc.setScore(Double.parseDouble(resultsData[3].trim()));
			doc.setRank(Integer.parseInt(resultsData[2].trim()));
			if(qrelMap.containsKey(resultsData[0].trim()) && qrelMap.get(resultsData[0].trim()).containsKey(resultsData[1].trim())){
				doc.setRelevance(qrelMap.get(resultsData[0].trim()).get(resultsData[1].trim()));
			}else{
				doc.setRelevance(0);
			}
			if(resultsMap.containsKey(resultsData[0].trim())){
				resultsMap.get(resultsData[0].trim()).add(doc);
			}else{
				List<Doc> tempList = new ArrayList<Doc>();
				tempList.add(doc);
				resultsMap.put(resultsData[0].trim(), tempList);
			}
		}
		tempReader.close();
		System.out.println("*********** RESULTS FILE LOADED SUCCESSFULLY *******************");
		//sort all the docs
		for(String query: resultsMap.keySet()){
			List<Doc> tempList = resultsMap.get(query);
			Collections.sort(tempList);
			
			List<Doc> temp = new ArrayList<Doc>();
			for(int i = 0;i< 1000; i++){
				temp.add(tempList.get(i));
			}
			resultsMap.get(query).clear();
			resultsMap.get(query).addAll(temp);
			
//			Collections.sort(resultsMap.get(query));
		}
		
	}
	
	/**
	 * Generate the Precision and Recall values
	 */
	public void generateValues(){
		for (String query : resultsMap.keySet()) {
			int total_number_of_relevant = getTotalNumberOfRelevantDocs(qrelMap.get(query));
			populatePrecAndRecallValues(resultsMap.get(query),total_number_of_relevant);
		}
		System.out.println("******* PRECISION AND RECALL VALUES GENERATED SUCCESSFULLY ************");
	}

	/**
	 * Compute Precision and Recall values for the list of Docs
	 */
	private void populatePrecAndRecallValues(List<Doc> list, int TOTAL_RELEVANT_DOCS) {
		int relevanceCount = 0;
		for (int i = 0; i < list.size(); i++) {
			Doc tempDoc = list.get(i);
			if(tempDoc.getRelevance() == RELEVANCE){
				++relevanceCount;
			}
			tempDoc.setRecall((double)relevanceCount/TOTAL_RELEVANT_DOCS);
			tempDoc.setPrecision((double)relevanceCount/(i+1));
		}
	}

	/**
	 * Returns the no of Relevant docs in the given map
	 */
	private int getTotalNumberOfRelevantDocs(Map<String, Integer> map) {
		int totalReleventCount = 0;
		for (int relevanceValue : map.values()) {
			totalReleventCount = totalReleventCount + relevanceValue;
		}
		return totalReleventCount;
	}
	
	/**
	 * Prints the p-r values to file(Required for scatter plot)
	 * @throws IOException
	 */
	public void printPrecisionRecallValues() throws IOException {
		int count=1;
		for (String query : resultsMap.keySet()) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(QUERY_FILENAME+ count++ +"precisionRecallValues.csv"));
			List<Doc> docs = resultsMap.get(query);
			System.out.println("QUERY: "+ query);
			for (Doc doc : docs) {
				bw.write(doc.getPrecision()+","+doc.getRecall()+System.lineSeparator());
			}
			bw.flush();
			bw.close();
		}
	}

	/**
	 * Generate all measures. It iterates over each of the queries and generates
	 * the values for Precision, recall, f-Measure, nDCG measure at cutoff-5-10-20-50-100-200-500-1000.
	 * Avg precision and Z-precision.
	 */
	public void generateAllScores() {
		
		for (String query : resultsMap.keySet()) {
			
			List<Doc> docs = resultsMap.get(query);
			int total_number_of_relevant = getTotalNumberOfRelevantDocs(qrelMap.get(query));
			Score fScore = computeFMeasure(query,docs);
			Score ndcgScore = computeNDCGMeasure(query, docs);
			double avgScore = getAvgPrecision(query,docs,total_number_of_relevant);
			double rScore = getRPrecision(query,docs,total_number_of_relevant);
			
			avgPrecisionValues.add(avgScore);
			zPrecisionValues.add(rScore);
			
			System.out.println("-----------------------------------------------------------------------------");
			System.out.println("QUERY-ID: "+query);
			System.out.println("-----------------------------------------------------------------------------");
			System.out.println("RETRIVED-DOCS: "+ docs.size());
			System.out.println("RELAVENT-DOCS: "+ total_number_of_relevant);
			System.out.println("RETRIEVED-RELAVENT-DOCS: " + getRelevantDocs(docs));
			System.out.println("-----------------------------------------------------------------------------");
			System.out.println("                  RANK-5             RANK-10             RANK-20              RANK-50            RANK-100               RANK-200           RANK-500           RANK-1000");
			if(isTrecFile){
				System.out.println("Precision:  "+ docs.get(CUTOFF_5-1).getPrecision() +"  "+ docs.get(CUTOFF_10-1).getPrecision()+"  "+docs.get(CUTOFF_20-1).getPrecision()+"  "+docs.get(CUTOFF_50-1).getPrecision()+"  "+docs.get(CUTOFF_100-1).getPrecision()+"  "+docs.get(200-1).getPrecision()+"  "+docs.get(500-1).getPrecision()+"  "+docs.get(1000-1).getPrecision());
				System.out.println("Recall:     "+ docs.get(CUTOFF_5-1).getRecall() +"  "+ docs.get(CUTOFF_10-1).getRecall()+"  "+docs.get(CUTOFF_20-1).getRecall()+"  "+docs.get(CUTOFF_50-1).getRecall()+"  "+docs.get(CUTOFF_100-1).getRecall()+"  "+docs.get(200-1).getRecall()+"  "+docs.get(500-1).getRecall()+"  "+docs.get(1000-1).getRecall());
			}else{
				System.out.println("Precision:  "+ docs.get(CUTOFF_5-1).getPrecision() +"  "+ docs.get(CUTOFF_10-1).getPrecision()+"  "+docs.get(CUTOFF_20-1).getPrecision()+"  "+docs.get(CUTOFF_50-1).getPrecision()+"  "+docs.get(CUTOFF_100-1).getPrecision());
				System.out.println("Recall:     "+ docs.get(CUTOFF_5-1).getRecall() +"  "+ docs.get(CUTOFF_10-1).getRecall()+"  "+docs.get(CUTOFF_20-1).getRecall()+"  "+docs.get(CUTOFF_50-1).getRecall()+"  "+docs.get(CUTOFF_100-1).getRecall());
			}
			System.out.println("F-1:        "+ fScore.getFive_cutoff()+"  "+ fScore.getTen_cutoff()+"  "+fScore.getTwenty_cutoff()+"  "+ fScore.getFifty_cutoff()+"  "+fScore.getHundred_cutoff()+"  "+fScore.getTwoHundred_cutoff()+"  "+fScore.getFiveHundred_cutoff()+"  "+fScore.getThousand_cutoff());
			System.out.println("nDCG:       "+ ndcgScore.getFive_cutoff()+"  "+ ndcgScore.getTen_cutoff()+"  "+ndcgScore.getTwenty_cutoff()+"  "+ ndcgScore.getFifty_cutoff()+"  "+ndcgScore.getHundred_cutoff()+"  "+ndcgScore.getTwoHundred_cutoff()+"  "+ndcgScore.getFiveHundred_cutoff()+"  "+ndcgScore.getThousand_cutoff());
			System.out.println("-----------------------------------------------------------------------------");
			System.out.println("AVG-PRECISION: " + avgScore);
			System.out.println("-----------------------------------------------------------------------------");
			System.out.println("R-PRECISION  : " + rScore);
			System.out.println("-----------------------------------------------------------------------------");

		}
		//for overall avg scores of all the queries
		double avgScore = 0.0;
		for (Double tempAvg : avgPrecisionValues) {
			avgScore = avgScore + tempAvg;
		}
		//for overall z scores of all the queries
		double zScore = 0.0;
		for (Double tempzScore : zPrecisionValues) {
			zScore = zScore + tempzScore;
		}
		//scores at 5,10,20,50,100,200,500,1000 precision for Fmeasure
		double _5Precision=0.0,_10Precision=0.0, _20Precision = 0,_50Precision= 0, _100Precision= 0,_200Precision= 0,_500Precision= 0,_1000Precision= 0;
		for (Map.Entry<String, Score> entry : fMeasureMap.entrySet()) {
			_5Precision = _5Precision + entry.getValue().getFive_cutoff();
			_10Precision = _10Precision + entry.getValue().getTen_cutoff();
			_20Precision = _20Precision + entry.getValue().getTwenty_cutoff();
			_50Precision = _50Precision + entry.getValue().getFifty_cutoff();
			_100Precision = _100Precision + entry.getValue().getHundred_cutoff();
			_200Precision = _200Precision+ entry.getValue().getTwoHundred_cutoff();
			_500Precision = _500Precision + entry.getValue().getFiveHundred_cutoff();
			_1000Precision = _1000Precision + entry.getValue().getThousand_cutoff();
		}
		
		//scores at 5,10,20,50,100,200,500,1000 precision for nDCGmeasure
		double _5PrecisionNDCG=0.0,_10PrecisionNDCG=0.0, _20PrecisionNDCG=0.0,_50PrecisionNDCG=0.0, _100PrecisionNDCG=0.0,_200PrecisionNDCG=0.0,_500PrecisionNDCG=0.0,_1000PrecisionNDCG=0.0;
		for (Map.Entry<String, Score> entry : nDCGMeasureMap.entrySet()) {
			_5PrecisionNDCG = _5PrecisionNDCG + entry.getValue().getFive_cutoff();
			_10PrecisionNDCG = _10PrecisionNDCG + entry.getValue().getTen_cutoff();
			_20PrecisionNDCG = _20PrecisionNDCG + entry.getValue().getTwenty_cutoff();
			_50PrecisionNDCG = _50PrecisionNDCG + entry.getValue().getFifty_cutoff();
			_100PrecisionNDCG = _100PrecisionNDCG + entry.getValue().getHundred_cutoff();
			_200PrecisionNDCG = _200PrecisionNDCG+ entry.getValue().getTwoHundred_cutoff();
			_500PrecisionNDCG = _500PrecisionNDCG + entry.getValue().getFiveHundred_cutoff();
			_1000PrecisionNDCG = _1000PrecisionNDCG + entry.getValue().getThousand_cutoff();
		}
		
		//scores at 5,10,20,50,100,200,500,1000 precision for results
		double _5PrecisionPrec=0.0,_10PrecisionPrec=0.0, _20PrecisionPrec=0.0,_50PrecisionPrec=0.0, _100PrecisionPrec=0.0,_200PrecisionPrec=0.0,_500PrecisionPrec=0.0,_1000PrecisionPrec=0.0;
		for (Entry<String, List<Doc>> entry : resultsMap.entrySet()) {
			_5PrecisionPrec =_5PrecisionPrec + entry.getValue().get(CUTOFF_5-1).getPrecision();
			_10PrecisionPrec =_10PrecisionPrec + entry.getValue().get(CUTOFF_10-1).getPrecision();
			_20PrecisionPrec =_20PrecisionPrec + entry.getValue().get(CUTOFF_20-1).getPrecision();
			_50PrecisionPrec =_50PrecisionPrec + entry.getValue().get(CUTOFF_50-1).getPrecision();
			_100PrecisionPrec =_100PrecisionPrec + entry.getValue().get(CUTOFF_100-1).getPrecision();
			if(entry.getValue().size()>200){
				_200PrecisionPrec =_200PrecisionPrec + entry.getValue().get(200-1).getPrecision();
				_500PrecisionPrec =_500PrecisionPrec + entry.getValue().get(500-1).getPrecision();
				_1000PrecisionPrec =_1000PrecisionPrec + entry.getValue().get(1000-1).getPrecision();
			}
		}
		
		//scores at 5,10,20,50,100,200,500,1000 precision for Recall
		double _5PrecisionRecall=0.0,_10PrecisionRecall=0.0, _20PrecisionRecall=0.0,_50PrecisionRecall=0.0, _100PrecisionRecall=0.0,_200PrecisionRecall=0.0,_500PrecisionRecall=0.0,_1000PrecisionRecall=0.0;
		for (Entry<String, List<Doc>> entry : resultsMap.entrySet()) {
			_5PrecisionRecall =_5PrecisionRecall + entry.getValue().get(CUTOFF_5-1).getRecall();
			_10PrecisionRecall =_10PrecisionRecall + entry.getValue().get(CUTOFF_10-1).getRecall();
			_20PrecisionRecall =_20PrecisionRecall + entry.getValue().get(CUTOFF_20-1).getRecall();
			_50PrecisionRecall =_50PrecisionRecall + entry.getValue().get(CUTOFF_50-1).getRecall();
			_100PrecisionRecall =_100PrecisionRecall + entry.getValue().get(CUTOFF_100-1).getRecall();
			if(entry.getValue().size()>200){
				_200PrecisionRecall =_200PrecisionRecall + entry.getValue().get(200-1).getRecall();
				_500PrecisionRecall =_500PrecisionRecall + entry.getValue().get(500-1).getRecall();
				_1000PrecisionRecall =_1000PrecisionRecall + entry.getValue().get(1000-1).getRecall();
			}
		}
		
		//print for all the values
		
		System.out.println("------------------------------ OVERALL VALUES -----------------------------");
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("                  RANK-5             RANK-10             RANK-20              RANK-50            RANK-100               RANK-200           RANK-500           RANK-1000");
		System.out.println("Precision:  "+ _5PrecisionPrec/resultsMap.entrySet().size() +"  "+ _10PrecisionPrec/resultsMap.entrySet().size() +"  "+_20PrecisionPrec/resultsMap.entrySet().size() +"  "+_50PrecisionPrec/resultsMap.entrySet().size() +"  "+_100PrecisionPrec/resultsMap.entrySet().size()+ "  "+ _200PrecisionPrec/resultsMap.entrySet().size() +"  "+ _500PrecisionPrec/resultsMap.entrySet().size()+"  "+ _1000PrecisionPrec/resultsMap.entrySet().size()  );
		System.out.println("Recall:     "+ _5PrecisionRecall/resultsMap.entrySet().size() +"  "+ _10PrecisionRecall/resultsMap.entrySet().size()+"  "+_20PrecisionRecall/resultsMap.entrySet().size()+"  "+_50PrecisionRecall/resultsMap.entrySet().size()+"  "+_100PrecisionRecall/resultsMap.entrySet().size() +"  "+ _200PrecisionRecall/resultsMap.entrySet().size()+"  "+_500PrecisionRecall/resultsMap.entrySet().size()+"  "+_1000PrecisionRecall/resultsMap.entrySet().size());
		System.out.println("F-1:        "+ _5Precision/fMeasureMap.size()+"  "+ _10Precision/fMeasureMap.size()+"  "+_20Precision/fMeasureMap.size()+"  "+ _50Precision/fMeasureMap.size()+"  "+_100Precision/fMeasureMap.size()+"  "+_200Precision/fMeasureMap.size()+"  "+_500Precision/fMeasureMap.size()+"  "+_1000Precision/fMeasureMap.size());
		System.out.println("nDCG:       "+ _5PrecisionNDCG/nDCGMeasureMap.size()+"  "+ _10PrecisionNDCG/nDCGMeasureMap.size()+"  "+_20PrecisionNDCG/nDCGMeasureMap.size()+"  "+ _50PrecisionNDCG/nDCGMeasureMap.size()+"  "+_100PrecisionNDCG/nDCGMeasureMap.size()+"  "+_200PrecisionNDCG/nDCGMeasureMap.size()+"  "+_500PrecisionNDCG/nDCGMeasureMap.size()+"  "+_1000PrecisionNDCG/nDCGMeasureMap.size());
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("AVG-PRECISION: " + avgScore/avgPrecisionValues.size());
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("R-PRECISION  : " + zScore/zPrecisionValues.size());
		System.out.println("-----------------------------------------------------------------------------");
			
	}
	
	/**
	 * retrieve all the relevant docs from the given list
	 */
	private int getRelevantDocs(List<Doc> listOfDocs){
		int count = 0;
		for (Doc doc : listOfDocs) {
			if(doc.getRelevance()==RELEVANCE){
				++count;
			}
		}
		return count;
	}
	
	/**
	 * Compute avg precision for all the docs
	 */
	private double getAvgPrecision(final String query, final List<Doc> docs, int total_number_of_relevant){
		if(qrelMap.containsKey(query)){
			double precisionSum = 0.0;
			for(int i = 0 ; i < docs.size(); i++){
				if(docs.get(i).getRelevance() == RELEVANCE)
					precisionSum = precisionSum + docs.get(i).getPrecision();
			}
			return (precisionSum/total_number_of_relevant);
		}
		return 0.0;
	}
	
	/**
	 * Computes R-precision for the docs
	 */
	private double getRPrecision(final String query, final List<Doc> docs, int total_number_of_relevant){
		if(qrelMap.containsKey(query)){
			return docs.get(total_number_of_relevant-1).getPrecision();
		}
		return 0.0;
	}
	
	/**
	 * Compute F-measure for the docs
	 */
	private Score computeFMeasure(final String query,final List<Doc> docs){
		Score score = new Score();
		score.setFive_cutoff(FMeasure.fMeasureScore(docs, CUTOFF_5));
		score.setTen_cutoff(FMeasure.fMeasureScore(docs, CUTOFF_10));
		score.setTwenty_cutoff(FMeasure.fMeasureScore(docs, CUTOFF_20));
		score.setFifty_cutoff(FMeasure.fMeasureScore(docs, CUTOFF_50));
		score.setHundred_cutoff(FMeasure.fMeasureScore(docs, CUTOFF_100));
		score.setTwoHundred_cutoff(FMeasure.fMeasureScore(docs, CUTOFF_200));
		score.setFiveHundred_cutoff(FMeasure.fMeasureScore(docs, CUTOFF_500));
		score.setThousand_cutoff(FMeasure.fMeasureScore(docs, CUTOFF_1000));
		fMeasureMap.put(query, score);
		return score;
	}
	
	/**
	 * Compute nDCG measure for the docs
	 */
	private Score computeNDCGMeasure(final String query,final List<Doc> docs){
		Score score = new Score();
		score.setFive_cutoff(NDCGMeasure.nDCGScore(resultsMap.get(query), CUTOFF_5,query));
		score.setTen_cutoff(NDCGMeasure.nDCGScore(resultsMap.get(query), CUTOFF_10,query));
		score.setTwenty_cutoff(NDCGMeasure.nDCGScore(resultsMap.get(query), CUTOFF_20,query));
		score.setFifty_cutoff(NDCGMeasure.nDCGScore(resultsMap.get(query), CUTOFF_50,query));
		score.setHundred_cutoff(NDCGMeasure.nDCGScore(resultsMap.get(query), CUTOFF_100,query));
		score.setTwoHundred_cutoff(NDCGMeasure.nDCGScore(docs, CUTOFF_200,query));
		score.setFiveHundred_cutoff(NDCGMeasure.nDCGScore(docs, CUTOFF_500,query));
		score.setThousand_cutoff(NDCGMeasure.nDCGScore(docs, CUTOFF_1000,query));
		nDCGMeasureMap.put(query, score);
		return score;
	}
	
}