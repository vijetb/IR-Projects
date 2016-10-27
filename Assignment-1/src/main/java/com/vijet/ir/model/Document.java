package com.vijet.ir.model;

import java.util.ArrayList;
import java.util.List;

public class Document {
	private String docId;
	private long length;
	private long docFreq;
	private long termFreq;
	private String term;
	
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	public long getDocFreq() {
		return docFreq;
	}
	public void setDocFreq(long docFreq) {
		this.docFreq = docFreq;
	}
	public long getTermFreq() {
		return termFreq;
	}
	public void setTermFreq(long termFreq) {
		this.termFreq = termFreq;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (docFreq ^ (docFreq >>> 32));
		result = prime * result + ((docId == null) ? 0 : docId.hashCode());
		result = prime * result + (int) (length ^ (length >>> 32));
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		result = prime * result + (int) (termFreq ^ (termFreq >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Document other = (Document) obj;
		if (docFreq != other.docFreq)
			return false;
		if (docId == null) {
			if (other.docId != null)
				return false;
		} else if (!docId.equals(other.docId))
			return false;
		if (length != other.length)
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		if (termFreq != other.termFreq)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Document [docId=" + docId + ", length=" + length + ", docFreq="
				+ docFreq + ", termFreq=" + termFreq + ", term=" + term + "]";
	}
	
	

	
}
