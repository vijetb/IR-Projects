package com.vijet.ir.model;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;



public class DOC {
	@SerializedName("docno")
	private String DOCNO;
	@SerializedName("text")
	private String TEXT;
	@SerializedName("title")
	private String HEAD;
	@SerializedName("url")
	private String url;
	@SerializedName("in_links")
	private Set<String> inlinks = new HashSet<String>();
	@SerializedName("out_links")
	private Set<String> outlinks = new HashSet<String>();
	@SerializedName("depth")
	private Integer DEPTH;
	@SerializedName("author")
	private Set<String> authors = new HashSet<String>();
	@SerializedName("HTTPheader")
	private String httpHeader;
	@SerializedName("html_Source")
	private String htmlSource;
	
	public String getDOCNO() {
		return DOCNO;
	}
	public void setDOCNO(String dOCNO) {
		DOCNO = dOCNO;
	}
	public String getTEXT() {
		return TEXT;
	}
	public void setTEXT(String tEXT) {
		TEXT = tEXT;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Set<String> getInlinks() {
		return inlinks;
	}
	public void setInlinks(Set<String> inlinks) {
		this.inlinks = inlinks;
	}
	public Set<String> getOutlinks() {
		return outlinks;
	}
	public void setOutlinks(Set<String> outlinks) {
		this.outlinks = outlinks;
	}
	public String getHEAD() {
		return HEAD;
	}
	public void setHEAD(String hEAD) {
		HEAD = hEAD;
	}
	public Integer getDEPTH() {
		return DEPTH;
	}
	public void setDEPTH(Integer dEPTH) {
		DEPTH = dEPTH;
	}
	public Set<String> getAuthors() {
		return authors;
	}
	public void setAuthors(Set<String> authors) {
		this.authors = authors;
	}
	
	public String getHttpHeader() {
		return httpHeader;
	}
	public void setHttpHeader(String httpHeader) {
		this.httpHeader = httpHeader;
	}
	public String getHtmlSource() {
		return htmlSource;
	}
	public void setHtmlSource(String htmlSource) {
		this.htmlSource = htmlSource;
	}
	
	@Override
	public String toString() {
		return "DOC [DOCNO=" + DOCNO + ", TEXT=" + TEXT + ", HEAD=" + HEAD
				+ ", url=" + url + ", inlinks=" + inlinks + ", outlinks="
				+ outlinks + ", DEPTH=" + DEPTH + ", authors=" + authors
				+ ", httpHeader=" + httpHeader + ", htmlSource=" + htmlSource
				+ "]";
	}
	
}

