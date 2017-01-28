package com.vijet.ir.models;


public final class OkapiTF {
		
	public static double okapiScore(long termFreq, long lenOfDoc, long totalDocs){
		//double avg_len_doc = Double.valueOf(lenOfDoc)/totalDocs;
		double okapiScore = Double.valueOf(termFreq)/(termFreq + 0.5 + 1.5 * lenOfDoc / 247);
		return okapiScore;
	}
	
//	public static double okapiScore(long temFreq, long docLen){
//		double okapiScore = Double.valueOf(temFreq)/(temFreq + 0.5 + 1.5 * docLen / 247);
//		return okapiScore;
//	}
	
	public static double okapiScore(long temFreq, long docLen){
		double okapiScore = Double.valueOf(temFreq)/(temFreq + 0.5 + 1.5 * docLen / 247.0);	
		return okapiScore;
	}
}
