package com.vijet.ir.regression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.elasticsearch.watcher.FileWatcher;
import org.json.JSONObject;

import com.vijet.ir.elasticsearchclient.ITermVector;

public class MatrixGenerator {
	
	private static final String FEATURE_FILE_NAME = "featureMapping.txt";
//	private static final String FEATURE_FILE_NAME = "spam_words.txt";
//	private static final String FEATURE_FILE_NAME = "SparseFeaturesMapping.txt";

	private static final String SPAM_MAILS_MAPPING = "mappings/index";
	private static final String TESTING_IDS_MAPPING = "mappings/testingIdsMapper.txt";
	private static final String TRAINING_IDS_MAPPING = "mappings/trainingIdsMapper.txt";

	private final ITermVector client;
	private final Map<String,String> mailsSpamMapping = new HashMap<String, String>();
	private final Map<String,String> testingIdsMapping = new LinkedHashMap<String, String>();
	private final Map<String,String> trainingIdsMapping = new LinkedHashMap<String, String>();
	private final Map<String,String> featureMapping = new LinkedHashMap<String, String>();
	
	int count = 1;
	public MatrixGenerator(ITermVector termVectorClient){
		loadFiles();
		client = termVectorClient;
	}
	private void loadFiles(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(SPAM_MAILS_MAPPING));
			String line = new String();
			while((line=reader.readLine())!=null){
				String[] values = line.split(" ");
				mailsSpamMapping.put(values[1].trim(), values[0].trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File testingIdsMapper = new File(TESTING_IDS_MAPPING);
		File trainingIdsMapper = new File(TRAINING_IDS_MAPPING);
		if(testingIdsMapper.exists() && trainingIdsMapper.exists()){
			loadDataHelper(testingIdsMapper,testingIdsMapping);
			loadDataHelper(trainingIdsMapper,trainingIdsMapping);
		}
		//featureMapping
		try {
			BufferedReader reader = new BufferedReader(new FileReader(FEATURE_FILE_NAME));
			String line = new String();
			while((line=reader.readLine())!=null){
				String[] values = line.split("-");
				try{
				featureMapping.put(values[0].trim(), values[1].trim());
				}catch(Exception e){
					System.out.println(line);
					e.printStackTrace();
					System.exit(1);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private void loadDataHelper(File fileName, Map<String,String> map){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = new String();
			while((line=reader.readLine())!=null){
				String[] values = line.split(" ");
				map.put(values[0].trim(), values[1].trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void generateMatrix(){
		try {
			generateTrainingFeatureMatrix();
			generateTestingFeatureMatrix();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateTrainingFeatureMatrix() throws IOException {
		BufferedWriter featureMatrixWriter = new BufferedWriter(new FileWriter("trainingFeatureMatrixData.txt"));
		for (String id : trainingIdsMapping.keySet()) {
			String termVectorString = client.getTermVector(id);
			writeToFile(mailsSpamMapping.get(trainingIdsMapping.get(id)),termVectorString,featureMatrixWriter);
			++count;
			System.out.println(count);
		}
		featureMatrixWriter.flush();
		featureMatrixWriter.close();
	}
	
	private void writeToFile(String isSpam, String termVectorString, BufferedWriter featureMatrixWriter) throws IOException {
		String isSpamValue = isSpam.equals("spam")?"0":"1";
		Map<Integer,Integer> featureMap = new TreeMap<Integer, Integer>();
		JSONObject jsonObject = new JSONObject(termVectorString);
		for (String featureName : featureMapping.keySet()) {
			if(jsonObject.has(featureName)){
				featureMap.put(Integer.valueOf(featureMapping.get(featureName)), jsonObject.getJSONObject(featureName).getInt("term_freq"));
				//featureMatrixWriter.write(" "+featureMapping.get(featureName)+":"+jsonObject.getJSONObject(featureName).getInt("term_freq"));
			}
		}
		featureMatrixWriter.write(isSpamValue+" ");
		for (int feature : featureMap.keySet()) {
			featureMatrixWriter.write(" "+feature+":"+featureMap.get(feature));
		}
		featureMatrixWriter.write(System.lineSeparator());
	}
	
	private void generateTestingFeatureMatrix() throws IOException  {
		BufferedWriter featureMatrixWriter = new BufferedWriter(new FileWriter("testingFeatureMatrixData.txt"));
		for (String id : testingIdsMapping.keySet()) {
			String termVectorString = client.getTermVector(id);
			writeToFile(mailsSpamMapping.get(testingIdsMapping.get(id)),termVectorString,featureMatrixWriter);
			++count;
			System.out.println(count);
		}
		featureMatrixWriter.flush();
		featureMatrixWriter.close();
	}
}
