package com.vijet.ir;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class QueryExecution {

	public static void main(String[] args) throws UnknownHostException {

		String query = "Document will discuss allegations or measures being taken against corrupt public officials of any governmental jurisdiction worldwide";

		Client client = TransportClient
				.builder()
				.build()
				.addTransportAddress(
						new InetSocketTransportAddress(InetAddress
								.getByName("127.0.0.1"), 9300));

		SearchResponse response = null;

		try {
			response = client.prepareSearch("ap_dataset").setTypes("document")
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(QueryBuilders.queryStringQuery(query))
					// Query
					.setFrom(0).setSize(5)
					.setExplain(true).execute()
					.actionGet();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		for (SearchHit hit : response.getHits()) {
			System.out.println("******************************");
			System.out.println(hit.getSourceAsString());
			System.out.println("#########################");
	        //System.out.println(hit.explanation().toHtml());
	    }
	}
}