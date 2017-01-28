package com.vijet.ir.ads;

public class AdsUtil {
	private static AdsFilter filter = new AdsFilter();
	public static final boolean isAdUrl(final String url){
		return filter.isAdUrl(url);
	}
}
