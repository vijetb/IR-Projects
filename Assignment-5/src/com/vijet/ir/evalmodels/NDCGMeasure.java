package com.vijet.ir.evalmodels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vijet.ir.model.Doc;

/**
 * Util class for computing the NDCG Measure.
 * @author Viji
 */
public final class NDCGMeasure {
	/**
	 * Computes the ndcg score at a particular cutoff
	 */
	public static final double nDCGScore(final List<Doc> docs,  int cutoff, String queryNo){
		if(docs.size()<cutoff){
			return 0.0;
		}
		List<Doc> sortedDocs  = new ArrayList<Doc>();
		List<Doc> unsortedCutoffDocs = new ArrayList<Doc>();
		for(int i =0 ; i < cutoff; i++){
			unsortedCutoffDocs.add(docs.get(i));	
		}
		
		for(int i = 0; i < docs.size();i++){
			sortedDocs.add(docs.get(i));
		}
		
		double unsorted_dcgScore =  dcgScore(unsortedCutoffDocs, queryNo);
		if(unsorted_dcgScore==0.0)
			return 0.0;
		Collections.sort(sortedDocs, new Comparator<Doc>() {

			@Override
			public int compare(Doc o1, Doc o2) {
				return o2.getRelevance()-o1.getRelevance();
			}
		});
		
		double sorted_dcgScore = dcgScore(sortedDocs,queryNo);		
		return unsorted_dcgScore/sorted_dcgScore;
	}
	
	/**
	 * Compute the ndcg value for the list of Documents
	 * @param docs the list for which ndcg values will be computed. This list is already at
	 * certain cutoff
	 * @return ndcg values for the given list
	 */
	private static final double dcgScore(final List<Doc> docs, String queryNo){
		double dcgScore = 0.0;

		if(docs.isEmpty()){
			return dcgScore;
		}
		dcgScore = docs.get(0).getRelevance();
		for(int i = 1; i < docs.size();i++){
			double temp = Math.log(i+1)/Math.log(2.0);
			dcgScore = dcgScore + (docs.get(i).getRelevance()/temp);
		}
		return dcgScore;
	}
}
