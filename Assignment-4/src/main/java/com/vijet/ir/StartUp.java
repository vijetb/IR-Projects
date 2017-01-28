package com.vijet.ir;

import java.io.IOException;

import com.vijet.ir.esclient.ESClient;
/**
 * This class is used to create the mergedIndex in links file required for Page rank of merged index.
 * @author Viji
 */
public class StartUp {
	public static void main(String[] args) {
		ESClient esClient = new ESClient();
		try {
			esClient.configure();
			esClient.loadAllDocumentsFromIndex();
		} catch (IOException e) {
			System.out.println("Unable to configure ESClient");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
