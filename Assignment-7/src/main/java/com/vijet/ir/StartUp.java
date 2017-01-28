package com.vijet.ir;

import com.vijet.ir.elasticsearchclient.ESClient;
import com.vijet.ir.regression.MatrixGenerator;


public class StartUp {

	public static void main(String[] args) throws Exception {
		final String FOLDER_PATH = "data";

		ESClient elasticServerClient = new ESClient();
		try {
			elasticServerClient.configure();
		} catch (Exception  e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Client is successfully connected to Elasticserver!!!");
		elasticServerClient.loadFiles();
//		elasticServerClient.buildSparseFeatureMatrix();
//		elasticServerClient.indexFiles(FOLDER_PATH);
		MatrixGenerator generator = new MatrixGenerator(elasticServerClient);
		generator.generateMatrix();
		elasticServerClient.closeConnection();
	}
	

}
