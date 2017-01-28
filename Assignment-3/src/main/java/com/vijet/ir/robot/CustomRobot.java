package com.vijet.ir.robot;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class CustomRobot {
	
	private static Map<String,Boolean> visitedPages = new HashMap<String,Boolean>();
	
	
	protected boolean checkUrl(final String url){
		URL tempUrl = null;
		try{
			tempUrl = new URL(url.trim());
			if(visitedPages.containsKey(tempUrl.getAuthority())){
				return visitedPages.get(tempUrl.getAuthority());
			}
			
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(tempUrl.getProtocol()+"://");
			urlBuilder.append(tempUrl.getAuthority()+"/");
			urlBuilder.append("robots.txt");

			
			if(Jsoup.connect(urlBuilder.toString()).get().body().text().contains("User-agent: * Disallow: /")){
				visitedPages.put(tempUrl.getAuthority(), false);
				return false;
			}
			
		}catch(Exception e){
			//System.out.println("ERROR:URL: " + url);
			if(tempUrl!= null){
				visitedPages.put(tempUrl.getAuthority(), true);
			}
			return true;
		}
		visitedPages.put(tempUrl.getAuthority(), true);
		return true;
	}
	
	
	
	
	
	public static void main(String[] args) throws IOException {
		try{
			Document mainDoc = Jsoup.connect("http://www.google.com/robots.txt").get();
			StringBuilder builder = new StringBuilder(mainDoc.body().text());
			if(builder.toString().contains("User-agent: * Disallow: /")){
				System.out.println(mainDoc.body().text());
			}
			

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
