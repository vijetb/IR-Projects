package com.vijet.ir.esclient;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.vijet.ir.hubsauths.HubsAndAuthorities;

/**
 * Class that acts as a ElasticSearch Client to fetch the documents from the index
 * @author Viji
 */
public class ESClient {
	/**
	 * Elastic search Properties keys. (Values are configured in the config file)
	 */
	private final String ELASTICSERVER_HOSTNAME_KEY = "elasticserver.hostname";
	private final String ELASTICSERVER_PORT_KEY = "elasticserver.port";
	private final String INDEX_NAME = "van_dataset";
	/**
	 * File name that stores all the inlinks
	 */
	private final String MERGED_INDEX_INLINKS = "mergedIndexInlinks.txt";
	/**
	 * Topical query for hubs and authorities.
	 */
	private final String QUERY = "world war II";
	/**
	 * Max number of inlinks
	 */
	private final int INLINK_CAP = 50;
	/**
	 * ES Client
	 */
	private Client client = null;
	/**
	 * Set maintaining all the links(crawledLinks + outlinks + cappedInlinks)
	 */
	private Set<String> baseSet = new HashSet<String>();
	/**
	 * Map that stores the hub scores for all the links of Baseset.
	 */
	private Map<String,Double> hubScore = new HashMap<String,Double>();
	/**
	 * Map that stores the authority scores for all the links of Baseset.
	 */
	private Map<String,Double> authorityScore = new HashMap<String,Double>();
	/**
	 * Map that stores the set of in links for the links of Rootset.
	 */
	private Map<String, Set<String>> inlinks = new HashMap<String, Set<String>>();
	/**
	 * Map that stores the set of out links for the links of Rootset.
	 */
	private Map<String, Set<String>> outlinks = new HashMap<String, Set<String>>();
	
	/**
	 * Configures the Elastic search client. Should be invoked before invoking any other client
	 * method.
	 * @throws IOException
	 */
	public void configure() throws IOException{
		Properties props = new Properties();
		props.load(ESClient.class.getClassLoader().getResourceAsStream("elasticserver.config"));
		Settings settings = Settings.builder().put(props).build();
		client = TransportClient.builder().settings(settings).build().
				addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(props.getProperty(ELASTICSERVER_HOSTNAME_KEY)), Integer.parseInt(props.getProperty(ELASTICSERVER_PORT_KEY))));
		}
	
	/**
	 * Load all the documents from the index and write to the file.
	 */
	public void loadAllDocumentsFromIndex(){
		BufferedWriter bwInlinks  = null;
		try {
			bwInlinks = new BufferedWriter(new FileWriter(MERGED_INDEX_INLINKS));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		QueryBuilder qb = QueryBuilders.matchAllQuery();

		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME)
				.setScroll(new TimeValue(60000))
				.setQuery(qb)
				.setSize(100)
				.execute().actionGet(); //100 hits per shard will be returned for each scroll
		//Scroll until no hits are returned
		while (true) {

			for (SearchHit hit : scrollResp.getHits().getHits()) {
				List inlinksList = (List) hit.getSource().get("in_links");
				StringBuilder builder = new StringBuilder();
				for (Object object : inlinksList) {
					builder.append(String.valueOf(object)+" ");
				}
				try {
					bwInlinks.write(hit.getId()+" "+builder.toString().trim()+System.lineSeparator());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
			
			if (scrollResp.getHits().getHits().length == 0) {
				break;
			}
		}
		try {
			bwInlinks.flush();
			bwInlinks.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("************** MERGED INDEX INLINKS CREATED SUCCESSFULLY *********");
	}
	
	/**
	 * Fetches the document from the ElasticSerach.
	 * This method fetches the top 1000 links from the ES that match the query term {@link QUERY}.
	 * For each of the links, fetch all the out links and update the base set. Fetch all
	 * the in links and update the base set (links expansion). Cap the number of in links to INLINK_COUNT.
	 * @see {@linkplain com.vijet.ir.hubsauths.HubsAndAuthorities.java}
	 */
	public void fetchDocuments(){
		QueryBuilder qb = QueryBuilders.queryStringQuery(QUERY);

		SearchResponse scrollResp = client.prepareSearch(INDEX_NAME)
				.setScroll(new TimeValue(60000))
				.setQuery(qb)
				.setSize(1000)
				.execute().actionGet(); 
		
		System.out.println("NO-OF-DOCUMENTS-FETCHED->" + scrollResp.getHits().getHits().length);

		for (SearchHit hit : scrollResp.getHits().getHits()) {
			baseSet.add(hit.getId());
			List tempOutLinks = (List) hit.getSource().get("out_links");
			List tempInLinks = (List) hit.getSource().get("in_links");
			// For outlinks add all the outlinks
			baseSet.addAll(tempOutLinks);
			// For Inlinks! If inlinks size is less than 50, add all! else add top 50;
			int maxLength = (tempInLinks.size()>INLINK_CAP)? INLINK_CAP: tempInLinks.size();
			baseSet.addAll(tempInLinks.subList(0, maxLength));
			// gather inlinks/ outlinks for hitId
			inlinks.put(hit.getId(), new HashSet<String>(tempInLinks));
			outlinks.put(hit.getId(), new HashSet<String>(tempOutLinks));
		}
		
		
		// init all the links of the base set to the default hub/ auth score.
		for (String link : baseSet) {
			hubScore.put(link, 1.0);
			authorityScore.put(link, 1.0);
		}
		
		System.out.println("******** TOTAL SIZE OF BASE SET ******" + baseSet.size());
		
		HubsAndAuthorities hubsAndAuthorities = new HubsAndAuthorities(baseSet,hubScore, authorityScore,
				inlinks, outlinks);
		hubsAndAuthorities.rank();
	}


}
