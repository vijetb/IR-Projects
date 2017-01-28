package com.vijet.ir;

import java.io.IOException;

import com.vijet.ir.crawl.WebCrawl;


public class StartUp {
	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		WebCrawl webCrawl = new WebCrawl();
		try {
			webCrawl.performCrawl();
		} catch (IOException e) {
			e.printStackTrace();
		}
		webCrawl.shutdown();
		System.out.println(System.currentTimeMillis()-t1);
	}
}
