package com.vijet.ir.models;

import java.util.Map;

import com.vijet.ir.model.Document;


public final class UnigramLaplace {

	static final long TOTAL_UNIQUE_TERMS = 178081;
	public static double lapaceSmoothingScore(long termFreq, long docLength){
		//return Math.log10(Double.valueOf(termFreq+1.0)/(docLength + TOTAL_UNIQUE_TERMS));
		return Double.valueOf(termFreq+1.0)/(docLength + TOTAL_UNIQUE_TERMS);
	}
	
	public static double penalizedScore(long doclength, int freq){
		//return Math.log10(1.0/(doclength + TOTAL_UNIQUE_TERMS)) * freq;
		return 1.0/(doclength + TOTAL_UNIQUE_TERMS) * freq;
	}
}
