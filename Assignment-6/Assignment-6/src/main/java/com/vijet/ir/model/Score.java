package com.vijet.ir.model;

/**
 * Score for a particular document
 */
public class Score implements Comparable<Score> {
	private String queryId;
	private String docId;
	private double score;
	
	public Score(String docId, double score, String queryId){
		this.docId = docId;
		this.score = score;
		this.queryId = queryId;
	}

	public String getQueryId() {
		return queryId;
	}

	public String getDocId() {
		return docId;
	}

	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(Score o) {
		return Double.compare(o.getScore(), this.getScore());
	}
	
	
	
}
