package com.vijet.ir.model;

public class DocScore {
	private String docId;
	private double score;
	
	
	
	public DocScore(String docId) {
		super();
		this.docId = docId;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	
}
