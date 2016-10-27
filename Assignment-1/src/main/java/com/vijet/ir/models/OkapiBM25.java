package com.vijet.ir.models;


public final class OkapiBM25 {

	public static double okapiBM25Score(long totalDocs, long docFreq, long termFreq, long docLength){
		double k1=1.2, k2=100, b=0.4;
		
		double avg_len_doc = 247;

		double term1 = Math.log10(Double.valueOf((totalDocs+0.5))/(docFreq+0.5));
		double term2 = Double.valueOf(termFreq + k1 * termFreq)/(termFreq+k1*((1-b)+b*Double.valueOf(docLength)/avg_len_doc));
		double term3 = (1+ k2*1)/(1+k2);

		return term1*term2*term3;
	}
}
