package com.vijet.ir.esclient;

import java.util.List;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.client.Client;

import com.google.gson.Gson;
import com.vijet.ir.constants.Constants;
import com.vijet.ir.model.DOC;

public class IndexClientThread implements Runnable {
	
	private List<DOC> listOfDocs = null;
	private Client esClient;
	private Gson gson;
	public IndexClientThread(List<DOC> docs, Client client) {
		this.listOfDocs = docs;
		this.esClient = client;
		gson = new Gson();
	}
	
	@Override
	public void run() {
		if(listOfDocs!=null){
			for (DOC doc : listOfDocs) {
				if(doc!=null){
					//obtain the handler to elasticsearch
					QueryBuilder qb = QueryBuilders.matchQuery("_id",doc.getDOCNO());
					SearchResponse scrollResp = esClient.prepareSearch(Constants.INDEX_NAME)
							.setQuery(qb).execute().actionGet();
					
					if(scrollResp.getHits().getHits().length > 1){
						System.out.println("DUPLICATE RECORD FOUND! " + doc.getDOCNO());
						continue;
					}else if(scrollResp.getHits().getHits().length == 1){
						SearchHit hit = scrollResp.getHits().getHits()[0];
						List tempList = (List) hit.getSource().get("in_links");
						if(tempList!=null){
							doc.getInlinks().addAll(tempList);
						}
						tempList = (List) hit.getSource().get("out_links");
						if(tempList!=null){
							doc.getOutlinks().addAll(tempList);
						}
						tempList = (List) hit.getSource().get("authors");
						if(tempList!=null){
							doc.getAuthors().addAll(tempList);
						}
					}
						// NOW Index the updated document!
						try{
							IndexResponse indexResponse = esClient.prepareIndex(Constants.INDEX_NAME, Constants.INDEX_TYPE)
									.setId(doc.getDOCNO())
									.setSource(gson.toJson(doc)).get();
							
						}catch(Exception e){
							e.printStackTrace();
							System.out.println("*** UNABLE TO INDEX: "+ doc.getDOCNO()+" **********");
						}
						
					}
				}
			Constants.NO_OF_DOCS_INDEXED +=listOfDocs.size();
			System.out.println("*** TOTAL FILES INDEXED ****** : " + Constants.NO_OF_DOCS_INDEXED);
			}
		}
		
	}
