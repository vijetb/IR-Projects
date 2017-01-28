package com.vijet.ir;

import java.io.IOException;

import com.vijet.ir.esclient.ESClient;
/**
 * StartUp class for Assignment-4: Hubs and Authorities.
 * This assignment fetches the top 1000 docs from the ES and ranks those pages 
 * using Hubs and Authority Algorithm.
 * @author Vijet Badigannavar
 */
public class HubsAndAuthoritiesStartUp {
	public static void main(String[] args) {
		ESClient esClient = new ESClient();
		try {
			esClient.configure();
			esClient.fetchDocuments();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
