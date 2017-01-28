package com.vijet.ir.hubsauths;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.vijet.ir.util.Util;
/**
 * Class that ranks the pages using HubsAndAuthorities Algorithm.
 * @author Vijet Badigannavar
 */
public class HubsAndAuthorities {
	/**
	 * File name for Hub scores
	 */
	private final String HUBS_SCORE_FILE_NAME = "hubScores.txt";
	/**
	 * File name for authority scores
	 */
	private final String AUTH_SCORE_FILE_NAME = "authScores.txt";
	/**
	 * Set maintaining all the links(crawledLinks + outlinks + cappedInlinks)
	 */
	private final Set<String> baseSet;
	/**
	 * Map that stores the hub scores for all the links of Baseset.
	 */
	private final Map<String, Double> hubScore;
	/**
	 * Map that stores the authority scores for all the links of Baseset.
	 */
	private final Map<String, Double> authorityScore;
	/**
	 * Map that stores the set of in links for the links of Rootset.
	 */
	private final Map<String, Set<String>> inlinks;
	/**
	 * Map that stores the set of out links for the links of Rootset.
	 */
	private final Map<String, Set<String>> outlinks;
	/**
	 * Map that is used to temporary store the hub scores of all the links of the base set. 
	 */
	private final Map<String, Double> tempHubScore;
	/**
	 * Map that is used to temporary store the Authority scores of all the links of the base set. 
	 */
	private final Map<String, Double> tempAuthorityScore;
	/**
	 * Flag to indicate the termination of the algorithm.
	 */
	private boolean convergedFlag = false;

	
	public HubsAndAuthorities(Set<String> baseSet, Map<String, Double> hubScore, Map<String, Double> authorityScore,
			Map<String, Set<String>> inlinks, Map<String, Set<String>> outlinks) {
		this.baseSet = baseSet;
		this.hubScore = hubScore;
		this.authorityScore = authorityScore;
		this.inlinks = inlinks;
		this.outlinks = outlinks;
		this.tempHubScore = new HashMap<String, Double>(hubScore.size()); 
		this.tempAuthorityScore = new HashMap<String, Double>(authorityScore.size()); 
	}

	/**
	 * This method ranks the pages. For each iteration until, the hub scores and authority scores converges, 
	 * update the hub score and authority score of each of the links of the base set and normalize them.
	 * Sort the results and print them to file.
	 */
	public void rank() {
		int iterCount = 0;
			while(!convergedFlag){
				System.out.println("ITERATION: " + ++iterCount);
				tempHubScore.clear();
				tempAuthorityScore.clear();
				
				for (String link : baseSet) {
					//get the sum of hub scores of all the pages that point to it
					double authScore = getHubScores(inlinks.get(link));
					// get the sum of authority scores of all the pages that it is pointing to
					double tHubScore = getAuthScores(outlinks.get(link));
					tempHubScore.put(link, tHubScore);
					tempAuthorityScore.put(link, authScore);
				}

				normalizeHubAuthScores(tempHubScore);
				normalizeHubAuthScores(tempAuthorityScore);
				convergedFlag = hasAlgorithmConverged(tempHubScore,tempAuthorityScore);
				
				hubScore.clear();
				hubScore.putAll(tempHubScore);
				
				authorityScore.clear();
				authorityScore.putAll(tempAuthorityScore);
			}
			
			Map<String, Double> sortedHubs = Util.sortByComparator(hubScore);
			Map<String, Double> sortedAuths = Util.sortByComparator(authorityScore);
			
			try {
				Util.dumpScoresToFile(sortedHubs, HUBS_SCORE_FILE_NAME);
				Util.dumpScoresToFile(sortedAuths, AUTH_SCORE_FILE_NAME);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Normalize the map so that all the values lie between 0 to 1.
	 * Take the sum of all the values and divide each value by that sum!
	 * @param map -  hubScore or authScore or tempHubScore or tempAuthScore
	 */
	private void normalizeHubAuthScores(Map<String, Double> map){
		double temp = 0.0;
		for (Double score : map.values()) {
			temp += score;
		};
		Iterator<Map.Entry<String, Double>> iter = map.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,Double> entry = iter.next();
			map.put(entry.getKey(), entry.getValue()/temp);
		}
	}

	/**
	 * Returns if the algorithm is converged. Algorithm converges when old value and new value differ by EPISON(10^5)
	 * for each of the scores of the links in the base set.
	 * @param tempHubScore - Map containing the new hub scores
	 * @param tempAuthorityScore -  Map containing the new authority scores
	 * @return true if each of the values differ by EPISLON or false otherwise
	 */
	private boolean hasAlgorithmConverged(Map<String, Double> tempHubScore, Map<String, Double> tempAuthorityScore) {
		final double EPSILON = 0.00001;
		
		for (String link : authorityScore.keySet()) {
			if(Math.abs(hubScore.get(link)-tempHubScore.get(link))>EPSILON){
				return false;
			}
			if(Math.abs(authorityScore.get(link)-tempAuthorityScore.get(link))>EPSILON){
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the sum of authority scores of all the out links. Returns 0.0 if out links is null or empty.
	 */
	private Double getAuthScores(Set<String> outlinks) {
		if(outlinks == null || outlinks.isEmpty())
			return 0.0;
		double tempAuthScore = 0.0;
		for (String outlink : outlinks) {
			if(authorityScore.containsKey(outlink)){
				tempAuthScore+=authorityScore.get(outlink);
			}
		}
		return tempAuthScore;
	}
	/**
	 * Returns the sum of hub scores of all the in links. Returns 0.0 if out links is null or empty.
	 */
	private double getHubScores(Set<String> inlinks) {
		if(inlinks == null || inlinks.isEmpty())
			return 0.0;
		double tempAuthScore = 0.0;
		for (String inlink : inlinks) {
			if(hubScore.containsKey(inlink)){
				tempAuthScore+=hubScore.get(inlink);
			}
		}
		return tempAuthScore;
	}
	
}
