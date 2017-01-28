package com.vijet.ir.models;

public class TFIDF {
	
	public static double tfidfScore(double okapiScore, long NO_OF_DOC_CORPUS, long docFre){
		if(0==docFre){
			throw new IllegalStateException();
		}
		double tfScore =okapiScore * Math.log10(NO_OF_DOC_CORPUS/docFre);
		return  tfScore * 100;
	}
}
