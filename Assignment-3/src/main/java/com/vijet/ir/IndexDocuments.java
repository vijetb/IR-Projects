package com.vijet.ir;

import com.vijet.ir.esclient.ESClient;

public class IndexDocuments {

	public static void main(String[] args) {
		ESClient esClient = new ESClient();
		try {
			esClient.configure();
			esClient.indexDocuements();
			//esClient.testing();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
