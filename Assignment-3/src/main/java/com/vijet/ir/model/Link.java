package com.vijet.ir.model;

import java.net.URI;
import java.net.URISyntaxException;

public class Link {
	private String rawUrl;
	private String canonicalizedurl;
	private Long waitingTime;
	private String urlAuthority;
	private int isRelevant;
	private int depth;
	
	public Link(String url, String canonicalizedurl) {
		super();
		this.rawUrl = url;
		this.canonicalizedurl = canonicalizedurl;
		try {
			urlAuthority = (new URI(url)).getAuthority();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public String getRawUrl() {
		return rawUrl;
	}
	public void setRawUrl(String url) {
		this.rawUrl = url;
	}
	public String getCanonicalizedurl() {
		return canonicalizedurl;
	}
	public void setCanonicalizedurl(String canonicalizedurl) {
		this.canonicalizedurl = canonicalizedurl;
	}
	public Long getWaitingTime() {
		return waitingTime;
	}
	public void setWaitingTime(Long waitingTime) {
		this.waitingTime = waitingTime;
	}
	
	public String getUrlAuthority() {
		return urlAuthority;
	}
	
	public int getIsRelevant() {
		return isRelevant;
	}

	public void setIsRelevant(int isRelevant) {
		this.isRelevant = isRelevant;
	}
	
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	@Override
	public String toString() {
		return "Link [rawUrl=" + rawUrl + ", canonicalizedurl="
				+ canonicalizedurl + ", waitingTime=" + waitingTime + "]";
	}
	
}
