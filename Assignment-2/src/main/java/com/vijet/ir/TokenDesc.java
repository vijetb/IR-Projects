package com.vijet.ir;

import java.util.ArrayList;
import java.util.List;

public class TokenDesc implements Comparable<TokenDesc> {
	private String docId;
	private int count;
	private String term;
	private String positions;
	
	public TokenDesc(){
	
	}
	
	public TokenDesc(String docId, int count) {
		super();
		this.docId = docId;
		this.count = count;
	}
	
	public TokenDesc(String docId, int count, String term) {
		super();
		this.docId = docId;
		this.count = count;
		this.term = term;
	}
	
	public TokenDesc(String docId, int count, String term, String position) {
		super();
		this.docId = docId;
		this.count = count;
		this.term = term;
		this.positions = position;
	}
	

	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public String getPositions() {
		return positions;
	}

	public void setPositions(String positions) {
		this.positions = positions;
	}

	@Override
	public int compareTo(TokenDesc o) {
		// TODO Auto-generated method stub
		return o.count-this.count;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}


	
}
