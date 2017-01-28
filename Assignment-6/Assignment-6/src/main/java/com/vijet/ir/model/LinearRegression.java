package com.vijet.ir.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * LinearRegression Engine. This class is responcible for running the LR over the dataset. 
 * It reads the values(BM25,Laplace, Jelinek, Tfidf,OkapiTf) and it will generate the model using
 * the training dataset. It will predict the scores for the testing dataset and will produce the results. 
 * The results are verified with the QREL file to get the average precision.
 * @author Viji
 */
public class LinearRegression {
	/**
	 * Adhoc File
	 */
	private final String ADHOC_RESULTS_FILE_NAME = "qrels.adhoc.51-100.AP89.txt";
	/**
	 * Scores 
	 */
	private final String BM25_RESULTS_FILE_NAME = "scores/OkapiBM25Results.txt";
	private final String JELINEK_RESULTS_FILE_NAME = "scores/UnigramLmJelinekMercerResults.txt";
	private final String LAPLACE_RESULTS_FILE_NAME = "scores/UnigramLmLaplaceSmoothingResults.txt";
	private final String OKAPI_RESULTS_FILE_NAME = "scores/OkapiTfResults.txt";
	private final String TFIDF_RESULTS_FILE_NAME = "scores/TfIdfResults.txt";
	/**
	 * Model file
	 */
	private final static String MODEL_FILE_NAME = "/output/model.txt";
	
	/**
	 * Output file
	 */
	private final String MATRIX_FILE_NAME = "output/matrixFile.txt";
	private final String TESTING_TRAINING_QUERIES ="testingTrainingQueries";
	private final String RESULT_OF_TRAINING_QUERIES ="ResultOfTrainingQueries.txt";
	/**
	 * Maintains set of Training Queries
	 */
	private final Set<String> trainingQueries = new HashSet<String>();
	/**
	 * Maintains set of Testing Queries
	 */
	private final Set<String> testingQueries = new HashSet<String>();
	/**
	 * Map that contains Scores(QueryId->(docId->Score))
	 */
	private final Map<String,Map<String,Double>> bm25Scores = new LinkedHashMap<String, Map<String,Double>>();
	private final Map<String,Map<String,Double>> jelinekScores = new LinkedHashMap<String, Map<String,Double>>();
	private final Map<String,Map<String,Double>> laplaceScores = new LinkedHashMap<String, Map<String,Double>>();
	private final Map<String,Map<String,Double>> okapiScores = new LinkedHashMap<String, Map<String,Double>>();
	private final Map<String,Map<String,Double>> tfidfScores = new LinkedHashMap<String, Map<String,Double>>();
	/**
	 * Map that contains min score for each query
	 */
	private final Map<String,Double> bm25MinScore = new HashMap<String, Double>();
	private final Map<String,Double> jelinekMinScore = new HashMap<String, Double>();
	private final Map<String,Double> laplaceMinScore = new HashMap<String, Double>();
	private final Map<String,Double> okapiMinScore = new HashMap<String, Double>();
	private final Map<String,Double> tfidfMinScore = new HashMap<String, Double>();
	/**
	 * Co-efficients for different scores
	 */
	private static double bm25coEfficient, jelinekCoefficient, laplaceCoefficient, okapiCoefficient, tfidfCoefficient;
	static{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(MODEL_FILE_NAME));
			int count =1;
			while(count<=5){
				++count;
				reader.readLine();
			}
			bm25coEfficient = Double.valueOf(reader.readLine().trim());
			jelinekCoefficient = Double.valueOf(reader.readLine().trim());
			laplaceCoefficient = Double.valueOf(reader.readLine().trim());
			okapiCoefficient = Double.valueOf(reader.readLine().trim());
			tfidfCoefficient = Double.valueOf(reader.readLine().trim());
			
			System.out.println("BM25-CO-EFFICIENT: " + bm25coEfficient);
			System.out.println("JELINEK-CO-EFFICIENT: " + jelinekCoefficient);
			System.out.println("LAPLACE-CO-EFFICIENT: " + laplaceCoefficient);
			System.out.println("OKAPI-CO-EFFICIENT: " + okapiCoefficient);
			System.out.println("TFIDF-CO-EFFICIENT: " + tfidfCoefficient);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads all scores into memory. 
	 * @throws IOException
	 */
	public void loadData() throws IOException{
		loadTrainingTestingQueries();
		loadFeatureData(bm25Scores,BM25_RESULTS_FILE_NAME,bm25MinScore);
		loadFeatureData(jelinekScores,JELINEK_RESULTS_FILE_NAME,jelinekMinScore);
		loadFeatureData(laplaceScores,LAPLACE_RESULTS_FILE_NAME,laplaceMinScore);
		loadFeatureData(okapiScores,OKAPI_RESULTS_FILE_NAME,okapiMinScore);
		loadFeatureData(tfidfScores,TFIDF_RESULTS_FILE_NAME,tfidfMinScore);
	}
	
	/**
	 * Load training and testing queries.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadTrainingTestingQueries() throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileReader(TESTING_TRAINING_QUERIES));
		for (String queryId : properties.get("trainingQueries").toString().split(",")) {
			trainingQueries.add(queryId.trim());
		}
		for (String queryId : properties.get("testingQueries").toString().split(",")) {
			testingQueries.add(queryId.trim());
		}
	}

	/**
	 * Loads the feature matrix into memory
	 */
	private void loadFeatureData(Map<String, Map<String, Double>> featureMap,String dataFile,Map<String,Double> minScoreMap ) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String line = new String();
		while((line = reader.readLine())!=null){
			String[] featureData = line.split(" "); //99 Q0 AP890420-0084 1 8.762818484120952 Sample
			if(line.trim().length()==0)continue;
			if(featureMap.containsKey(featureData[0].trim())){
				featureMap.get(featureData[0].trim()).put(featureData[2].trim(), Double.valueOf(featureData[4].trim()));
			}else{
				Map<String,Double> tempMap = new LinkedHashMap<String, Double>();
				tempMap.put(featureData[2].trim(), Double.valueOf(featureData[4].trim()));
				featureMap.put(featureData[0].trim(),tempMap);
			}
			
			if(minScoreMap.containsKey(featureData[0].trim())){
				double value = minScoreMap.get(featureData[0].trim());
				if(Double.valueOf(featureData[4].trim()) < value){
					minScoreMap.put(featureData[0].trim(), Double.valueOf(featureData[4].trim()));
				}
			}else{
				minScoreMap.put(featureData[0].trim(), Double.valueOf(featureData[4].trim()));
			}
		}
		reader.close();
	}

	/**
	 * Generates feature Matrix.
	 * @throws IOException
	 */
	public void generateMatrix() throws IOException{
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(MATRIX_FILE_NAME));
		BufferedReader bufferedReader = new BufferedReader(new FileReader(ADHOC_RESULTS_FILE_NAME));
		
		String line = new String();
		while((line=bufferedReader.readLine())!=null){
			String[] qrelDetails =  line.split(" "); //51 0 AP890104-0259 0
			if(trainingQueries.contains(qrelDetails[0].trim())){
				writeToOutputFile(qrelDetails[0],qrelDetails[2],qrelDetails[3],bufferedWriter);
			}
		}
		bufferedReader.close();
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	/**
	 * Outputs the results
	 */
	private void writeToOutputFile(String queryId, String docId, String relevance, BufferedWriter bufferedWriter) throws IOException {		
		Double bm25Score = getFeatureScore(bm25Scores,queryId,docId,bm25MinScore);
		Double jelinekScore = getFeatureScore(jelinekScores,queryId,docId,jelinekMinScore);
		Double laplaceScore = getFeatureScore(laplaceScores,queryId,docId,laplaceMinScore);
		Double okapiScore = getFeatureScore(okapiScores,queryId,docId,okapiMinScore);
		Double tfidfScore = getFeatureScore(tfidfScores,queryId,docId,tfidfMinScore);

		bufferedWriter.write(relevance+" 1:"+bm25Score+" 2:"+jelinekScore+" 3:"+laplaceScore+" 4:"+okapiScore+" 5:"+tfidfScore+System.lineSeparator());
		//FOR WEKA
		//bufferedWriter.write(bm25Score+","+jelinekScore+","+laplaceScore+","+okapiScore+","+proxScore+","+tfidfScore+","+relevance+System.lineSeparator());
	}
	
	/**
	 * Retrives the score of a feature if exists, otherwise returns the min score associated with
	 * that QueryId
	 */
	private double getFeatureScore(Map<String,Map<String,Double>> featureMap, String queryId, String docId,Map<String,Double> minScoreMap){
		double score = 0.0;
		try{
			score = featureMap.get(queryId).get(docId);
		}catch(Exception e){
			score = minScoreMap.get(queryId);
		}
		return score;
	}
	
	/**
	 * Testing Queries
	 */
	public void testingQueries() throws IOException{
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("TestingQueries.txt"));
		
		for (String queryId : testingQueries) {
			Set<String> docIds = new HashSet<String>();
			docIds.addAll(bm25Scores.get(queryId).keySet());
			docIds.addAll(jelinekScores.get(queryId).keySet());
			docIds.addAll(laplaceScores.get(queryId).keySet());
			docIds.addAll(okapiScores.get(queryId).keySet());
			docIds.addAll(tfidfScores.get(queryId).keySet());
			
			List<Score> list = new ArrayList<Score>();
			for (String docId : docIds) {
				double scoreValue = writeResultForTestingQuery(queryId,docId);
				Score score = new Score(docId,scoreValue,queryId);
				list.add(score);
			}
			Collections.sort(list);
			int count =1;
			for (Score score : list) {
				bufferedWriter.write(score.getQueryId()+" "+ "Q0" + " " + score.getDocId()+" "+ count + " " + score.getScore()+" " +"Sample"+System.lineSeparator());
				++count;
			}
			
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	/**
	 * Output the result for the Testing Query
	 */
	private double writeResultForTestingQuery(String queryId, String docId) throws IOException {
		Double bm25Score = getFeatureScore(bm25Scores,queryId,docId,bm25MinScore);
		Double jelinekScore = getFeatureScore(jelinekScores,queryId,docId,jelinekMinScore);
		Double laplaceScore = getFeatureScore(laplaceScores,queryId,docId,laplaceMinScore);
		Double okapiScore = getFeatureScore(okapiScores,queryId,docId,okapiMinScore);
		Double tfidfScore = getFeatureScore(tfidfScores,queryId,docId,tfidfMinScore);
		return getRegrssionScore(bm25Score,jelinekScore,laplaceScore,okapiScore,null,tfidfScore);
	}
	
	/**
	 * Generate the score for a particular Query
	 */
	private double getRegrssionScore(Double bm25Score,Double jelinekScore, Double laplaceScore,
			Double okapiScore,Double proxScore,Double tfidfScore){ 
		return  bm25coEfficient * bm25Score 
				+ jelinekCoefficient  * jelinekScore
				+ laplaceCoefficient * laplaceScore
				+ okapiCoefficient *  okapiScore
				+ tfidfCoefficient   * tfidfScore;
	}

	/**
	 * Tests on Training Queries
	 * @throws IOException
	 */
	public void testOnTrainingQueries() throws IOException {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(RESULT_OF_TRAINING_QUERIES));
		BufferedReader bufferedReader = new BufferedReader(new FileReader(ADHOC_RESULTS_FILE_NAME));
		
		String line = new String();
		while((line=bufferedReader.readLine())!=null){
			String[] qrelDetails =  line.split(" "); //51 0 AP890104-0259 0
			if(trainingQueries.contains(qrelDetails[0].trim())){
				writeToResultsFile(qrelDetails[0],qrelDetails[2],bufferedWriter);
			}
		}
		bufferedReader.close();
		bufferedWriter.flush();
		bufferedWriter.close();
	}
	
	/**
	 * Write to the Results File
	 */
	private void writeToResultsFile(String queryId, String docId,BufferedWriter bufferedWriter) throws IOException {
		bufferedWriter.write(queryId+" "+ "Q0" + " " + docId+" "+ 1 + " " + writeResultForTestingQuery(queryId,docId)+" " +"Sample"+System.lineSeparator());
	}
}













