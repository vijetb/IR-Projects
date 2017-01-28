package com.vijet.ir.pagerank;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vijet.ir.util.Util;

/**
 * Class that computes the PageRank for a set of Links!
 * @author Viji
 **/
public final class PageRankUtil {
	public final void computePageRank(Map<String,Double> PRScoreMap, 
			Map<String,Long> outlinksMap, 
			Map<String,Set<String>> inlinksMap,
			Set<String> sinkNodesSet,
			String DUMP_FILE_NAME){
		final int MAX_NUMBER_OF_ITERATIONS = 5;
		final long N = PRScoreMap.size();
		final double d = 0.85;

		int count=1;
		int perplexityCount = 0;
		double oldConvergenceScore = computeConvergenceScore(PRScoreMap);

		while(perplexityCount <= MAX_NUMBER_OF_ITERATIONS){
			double sinkPR = 0.0;
			for (String sinkNode : sinkNodesSet) {
				sinkPR += PRScoreMap.get(sinkNode);
			}

			Map<String,Double> tempPRScores = new HashMap<String, Double>();
			Set<String> keys = PRScoreMap.keySet();
			for (String docId : keys) {
				double newScore = (1.0 - d)/(double)N;
				newScore += d * sinkPR / (double)N;

				Set<String> inlinks  = inlinksMap.get(docId);
				if(inlinks!=null && !inlinks.isEmpty()){
					for(String inlinkId : inlinks){
						if(outlinksMap.containsKey(inlinkId) && PRScoreMap.containsKey(inlinkId))
							newScore += d * PRScoreMap.get(inlinkId) / (double) outlinksMap.get(inlinkId);
					}
				}
				tempPRScores.put(docId, newScore);
			}

			Iterator<Map.Entry<String, Double>> iter = PRScoreMap.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String,Double> entry = iter.next();
				entry.setValue(tempPRScores.get(entry.getKey()));
			}

			double newConvergenceScore = computeConvergenceScore(PRScoreMap);
			if(scoreConverge(oldConvergenceScore,newConvergenceScore)){
				++perplexityCount;
			}
			System.out.println(++count + " Perplexity Score: " + newConvergenceScore);
			oldConvergenceScore = newConvergenceScore;
		}

		// SORT THE SCORES AND PRINT THE FIRST 500 scores.
		Map<String, Double> sortedDocs =  Util.sortByComparator(PRScoreMap);

		try {
			Util.dumpScoresToFile(sortedDocs,DUMP_FILE_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Compute convergence score of the page rank scores using shannon Entropy
	 * @param pRScoreMap Map containing the pagerank values
	 * @return
	 */
	private double computeConvergenceScore(Map<String, Double> pRScoreMap) {
		double perplexityScore = 0.0;
		for (Double value : pRScoreMap.values()) {
			perplexityScore += value * Math.log(value)/Math.log(2);
		}
		return Math.pow(2, -1 * perplexityScore);
	}
	/**
	 * Checks if the scores converges. Scores converge iff, the units digits of previous and current
	 * iteration values are same.
	 * @param oldScore old convergence score
	 * @param newScore new convergence score.
	 */
	private boolean scoreConverge(double oldScore, double newScore){
		return ((int)Math.floor(oldScore) % 10 == (int)Math.floor(newScore) % 10) && ((int)oldScore == (int)newScore) ;
	}
}
