package com.vijet.ir.model;

public class Mail {
	private transient String id;
	private String text;
	private String spamLabel;
	private String fileName;
	private String split;
	
	public Mail(String id, String text, String spamLabel, String fileName, String split) {
		super();
		this.id = id;
		this.text = text;
		this.spamLabel = spamLabel;
		this.fileName = fileName;
		this.split = split;
	}
	
	public String getId() {
		return id;
	}
	public String getText() {
		return text;
	}
	public String getSpamLabel() {
		return spamLabel;
	}
	public String getFileName() {
		return fileName;
	}
	public String getSplit() {
		return split;
	}
	
}
