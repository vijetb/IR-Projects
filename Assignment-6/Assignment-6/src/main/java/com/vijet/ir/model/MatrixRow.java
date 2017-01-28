package com.vijet.ir.model;

/**
 * Feature matrix Row
 */
public class MatrixRow {
	private String queryId;
	private String docId;
	private String rowId;
	private double tfidfScore;
	private double okapiScore;
	private double bm25Score;
	private double jelinekScore;
	private double laplaceScore;
	private int docLength;
	private double proximitySearchScore;
	
	private int relevance;
	
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public double getTfidfScore() {
		return tfidfScore;
	}
	public void setTfidfScore(double tfidfScore) {
		this.tfidfScore = tfidfScore;
	}
	public double getOkapiScore() {
		return okapiScore;
	}
	public void setOkapiScore(double okapiScore) {
		this.okapiScore = okapiScore;
	}
	public double getBm25Score() {
		return bm25Score;
	}
	public void setBm25Score(double bm25Score) {
		this.bm25Score = bm25Score;
	}
	public double getJelinekScore() {
		return jelinekScore;
	}
	public void setJelinekScore(double jelinekScore) {
		this.jelinekScore = jelinekScore;
	}
	public double getLaplaceScore() {
		return laplaceScore;
	}
	public void setLaplaceScore(double laplaceScore) {
		this.laplaceScore = laplaceScore;
	}
	public int getDocLength() {
		return docLength;
	}
	public void setDocLength(int docLength) {
		this.docLength = docLength;
	}
	public double getProximitySearchScore() {
		return proximitySearchScore;
	}
	public void setProximitySearchScore(double proximitySearchScore) {
		this.proximitySearchScore = proximitySearchScore;
	}
	public int getRelevance() {
		return relevance;
	}
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}
	public String getQueryId() {
		return queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	
}
