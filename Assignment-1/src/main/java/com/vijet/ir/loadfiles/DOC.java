package com.vijet.ir.model;

import com.google.gson.annotations.SerializedName;



public class DOC {
	@SerializedName("docno")
	private String DOCNO;
	private transient String FILEID;
	private transient String FIRST;
	private transient String SECOND;
	private transient String HEAD;
	private transient String BYLINE;
	private transient String DATELINE;
	@SerializedName("text")
	private String TEXT;
	
	public String getDOCNO() {
		return DOCNO;
	}
	
	public void setDOCNO(String dOCNO) {
		DOCNO = updateText(DOCNO,dOCNO);
	}
	
	public String getFILEID() {
		return FILEID;
	}
	public void setFILEID(String fILEID) {
		FILEID = updateText(FILEID,fILEID);
	}
	
	public String getFIRST() {
		return FIRST;
	}
	public void setFIRST(String fIRST) {
		FIRST = updateText(FIRST,fIRST);
	}
	
	public String getSECOND() {
		return SECOND;
	}
	public void setSECOND(String sECOND) {
		SECOND = updateText(SECOND,sECOND);
	}
	
	public String getHEAD() {
		return HEAD;
	}
	public void setHEAD(String hEAD) {
		HEAD = updateText(HEAD,hEAD);
	}
	
	public String getBYLINE() {
		return BYLINE;
	}
	public void setBYLINE(String bYLINE) {
		BYLINE = updateText(BYLINE,bYLINE);
	}
	
	public String getDATELINE() {
		return DATELINE;
	}
	public void setDATELINE(String dATELINE) {
		DATELINE = updateText(DATELINE,dATELINE);
	}

	public String getTEXT() {
		return TEXT;
	}
	public void setTEXT(String tEXT) {
		TEXT = updateText(TEXT,tEXT);
	}
	
	private String updateText(String existingText, String newText){
		if(null==existingText){
			return newText;
		}
		return existingText + " " + newText;
	}
	
	@Override
	public String toString() {
		return "DOC [DOCNO=" + DOCNO + ", FILEID=" + FILEID + ", FIRST="
				+ FIRST + ", SECOND=" + SECOND + ", HEAD=" + HEAD + ", BYLINE="
				+ BYLINE + ", DATELINE=" + DATELINE + ", TEXT=" + TEXT + "]";
	}
	
	
}

