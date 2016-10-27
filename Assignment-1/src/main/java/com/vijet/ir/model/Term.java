package com.vijet.ir.model;

public class Term {
	long termFrequency;
	long documentFrequency;
	
	public long getTermFrequency() {
		return termFrequency;
	}
	public void setTermFrequency(long termFrequency) {
		this.termFrequency = termFrequency;
	}
	public long getDocumentFrequency() {
		return documentFrequency;
	}
	public void setDocumentFrequency(long documentFrequency) {
		this.documentFrequency = documentFrequency;
	}
	@Override
	public String toString() {
		return "Term [termFrequency=" + termFrequency + ", documentFrequency="
				+ documentFrequency + "]";
	}
	
	
	
}
