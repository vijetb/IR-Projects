package com.vijet.ir.model;

public class QueryScore {
	private double okapiScore;
	private double tf_idfScore;
	private double bm25Score;
	private double laplaceScore;
	private double jelinekMercerScore;
	private String termVectorDesc;
	private DOC doc;
	
	public DOC getDoc() {
		return doc;
	}
	public void setDoc(DOC doc) {
		this.doc = doc;
	}
	public String getTermVectorDesc() {
		return termVectorDesc;
	}
	public void setTermVectorDesc(String termVectorDesc) {
		this.termVectorDesc = termVectorDesc;
	}
	
	public double getOkapiScore() {
		return okapiScore;
	}
	public void setOkapiScore(double okapiScore) {
		this.okapiScore = okapiScore;
	}
	public double getTf_idfScore() {
		return tf_idfScore;
	}
	public void setTf_idfScore(double tf_idfScore) {
		this.tf_idfScore = tf_idfScore;
	}
	public double getBm25Score() {
		return bm25Score;
	}
	public void setBm25Score(double bm25Score) {
		this.bm25Score = bm25Score;
	}
	public double getLaplaceScore() {
		return laplaceScore;
	}
	public void setLaplaceScore(double laplaceScore) {
		this.laplaceScore = laplaceScore;
	}
	public double getJelinekMercerScore() {
		return jelinekMercerScore;
	}
	public void setJelinekMercerScore(double jelinekMercerScore) {
		this.jelinekMercerScore = jelinekMercerScore;
	}
	
	@Override
	public String toString() {
		return "QueryScore [okapiScore=" + okapiScore + ", tf_idfScore="
				+ tf_idfScore + ", bm25Score=" + bm25Score + ", laplaceScore="
				+ laplaceScore + ", jelinekMercerScore=" + jelinekMercerScore
				+ ", termVectorDesc=" + termVectorDesc + ", doc=" + doc + "]";
	}
	
	
	
}
