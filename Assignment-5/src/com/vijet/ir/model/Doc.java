package com.vijet.ir.model;

/**
 * Bean class for storing Doc information. The class implements
 * Comparable as its required to sort the documents according to 
 * score.
 * @author Vijet
 */
public class Doc implements Comparable<Doc> {
	/**
	 * Id of the document
	 */
	private String docId;
	/**
	 * Precision value for the document. Will be populated after
	 * the documents will be loaded
	 */
	private double precision;
	/**
	 * Recall value for the document. Will be populated after
	 * the documents will be loaded
	 */
	private double recall;
	/**
	 * Relevance of the document wrt to the QREL results
	 */
	private int relevance;
	/**
	 * Score associated with the document(Required for ranking)
	 */
	private double score;
	
	private int rank;
	
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getRecall() {
		return recall;
	}
	public void setRecall(double recall) {
		this.recall = recall;
	}
	public int getRelevance() {
		return relevance;
	}
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	@Override
	public String toString() {
		return "Doc [docId=" + docId + ", precision=" + precision + ", recall="
				+ recall + ", relevance=" + relevance + ", score=" + score
				+ "]";
	}
	
	@Override
	public int compareTo(Doc o) {
		return Double.compare(o.getScore(), this.getScore());
		//return o.getRank()-this.getRank();
	}
	
	
}
