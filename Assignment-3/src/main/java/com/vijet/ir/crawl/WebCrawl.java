package com.vijet.ir.crawl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tika.language.LanguageIdentifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vijet.ir.ads.AdsUtil;
import com.vijet.ir.exceptions.CannotCannonicalizeURLException;
import com.vijet.ir.model.DOC;
import com.vijet.ir.model.Link;
import com.vijet.ir.robot.RobotUtil;

public class WebCrawl {
	private final int NO_OF_DOCUMENTS_PER_FILE = 500;
	private final int TOTAL_NO_OF_DOCUMENTS = 100;
	private final int MAX_DOCUMENTS = 20000;
	private Set<String> visitedLinks = new LinkedHashSet<String>();
	private Map<String,Set<String>> docInlinkSet = new HashMap<String, Set<String>>();

	private long docCount = 0;
	private int fileCount;
	private BufferedWriter bfwForInlinks = null;
	private BufferedWriter bfwForOutlinks = null;
	private BufferedWriter bfwForDoc = null;
	private LinkQueue queue = null;
		
	private Map<String,Long> timeKeeper = new HashMap<String, Long>();
	private final int MAX_BRITANNICA_COUNT = 4000;
	private int britannicaCount = 0;
	//debugging
	private int documentCount = 0;
	public WebCrawl(){
		queue = new LinkQueue(docInlinkSet);
		try {
			bfwForDoc = new BufferedWriter(new FileWriter("Results/VAN_"+fileCount+".txt"));
			bfwForOutlinks = new BufferedWriter(new FileWriter("Results/VAN_outlinks.txt"));
			bfwForInlinks = new BufferedWriter(new FileWriter("Results/VAN_inlinks.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void performCrawl() throws IOException{
		crawlSeeds();
		
		while(queue.hasLinks()){
			List<Link> linksSet = queue.dequeue();
			for (Link link : linksSet) {
				
				if(!visitedLinks.contains(link.getCanonicalizedurl())){
					long currentTime = System.currentTimeMillis();
					if(timeKeeper.containsKey(link.getUrlAuthority())){
						long sleepTime = currentTime - timeKeeper.get(link.getUrlAuthority());
							if(sleepTime<1000){
								try {
										Thread.sleep(sleepTime);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
						}
					}
					timeKeeper.put(link.getUrlAuthority(),currentTime);
					crawlUrl(link);
				}
			}
		}
		
	}

	private void crawlSeeds() {
		String seed0 = "http://www.britannica.com/event/Battle-of-Midway";
		String seed1 = "http://www.britannica.com/biography/Joseph-W-Stilwell";
		String seed2 = "http://www.britannica.com/event/Bataan-Death-March";
		
		String seed3 = "http://www.history.com/topics/world-war-ii";
		String seed4 = "http://en.wikipedia.org/wiki/World_War_II";
		String seed5 = "http://en.wikipedia.org/wiki/List_of_World_War_II_battles_involving_the_United_States";
		String seed6 = "http://en.wikipedia.org/wiki/Military_history_of_the_United_States_during_World_War_II";
		
		Link link0 = new Link(seed0,canonocalizedURL(seed0));link0.setDepth(0);
		Link link1 = new Link(seed1,canonocalizedURL(seed1));link1.setDepth(0);
		Link link2 = new Link(seed2,canonocalizedURL(seed2));link2.setDepth(0);
		Link link3 = new Link(seed3,seed3);link3.setDepth(0);
		Link link4 = new Link(seed4,seed4);link4.setDepth(0);		
		Link link5 = new Link(seed5,seed5);link5.setDepth(0);
		Link link6 = new Link(seed6,seed6);link6.setDepth(0);
		
		crawlUrl(link0);
		crawlUrl(link1);
		crawlUrl(link2);
		crawlUrl(link3);
		crawlUrl(link4);
		crawlUrl(link5);
		crawlUrl(link6);
		
	}

	private void crawlUrl(Link url){
		try {
			crawl(url.getRawUrl(),url.getCanonicalizedurl(),url.getDepth());
		} catch (Exception e) {
			System.out.println("Unable to crawl: " + url.getRawUrl());
			e.printStackTrace();
		}
		visitedLinks.add(url.getCanonicalizedurl());
	}

	private String canonocalizedURL(String url) {
		if(url == null || url.trim().isEmpty()){
			throw new CannotCannonicalizeURLException("Tried to cannonicalize null/empty url");
		}
		try {
			StringBuilder builder = new StringBuilder(); 		// HTTP://www.Example.com/SomeFile.html#123312
			URL tempUrl = new URL(url.split("#")[0]);	 		// HTTP://www.Example.com/SomeFile.html
			builder.append(tempUrl.getProtocol().toLowerCase()+"://");
			builder.append(tempUrl.getAuthority().toLowerCase());
			if(tempUrl.getPort() != -1 && tempUrl.getProtocol().equalsIgnoreCase("http") && tempUrl.getPort()!=80){
				builder.append(":"+ tempUrl.getPort());
			}else if(tempUrl.getPort() != -1 && tempUrl.getProtocol().equalsIgnoreCase("https") && tempUrl.getPort()!=443){
				builder.append(":"+ tempUrl.getPort());
			}
			builder.append(tempUrl.getPath().replaceAll("//", "/"));
			return builder.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch(Exception e){
			throw new CannotCannonicalizeURLException("Unable to cannonicalize: " + url);
		}
		throw new CannotCannonicalizeURLException("Error in cannonicalizing url");
	}

	private boolean validateUrl(String canonicalizedUrl, String rawURL) {
		// Check if the url is visited
		// Check if its an Ad url
		// Check if robots.txt allows to use this url
		return !isURLVisited(canonicalizedUrl,rawURL) && !AdsUtil.isAdUrl(rawURL) && RobotUtil.canURLBeCrawled(rawURL);
	}
	
	private boolean isURLVisited(String canonicalizedUrl, String rawURL){
		if(visitedLinks.contains(canonicalizedUrl)){
			//update the inlink count
			if(docInlinkSet.containsKey(canonicalizedUrl)){
				docInlinkSet.get(canonicalizedUrl).add(canonicalizedUrl);
			}else{
				Set<String> tempSet = new HashSet<String>();
				tempSet.add(canonicalizedUrl);
				docInlinkSet.put(canonicalizedUrl, tempSet);
			}
			return true;
		}
		return false;
	}

	private void crawl(final String url, final String canonicalizedURL, final int depth) throws Exception {
		if(url.contains("britannica")){
			if(britannicaCount<=MAX_BRITANNICA_COUNT){
				++britannicaCount;
			}else{
				return;
			}
		}
		
		Document mainDoc = Jsoup.connect(url).get();
		
		cleanDocument(mainDoc,"div#mw-head");
		cleanDocument(mainDoc,"div#toc");
		cleanDocument(mainDoc,"div#mw-panel");
		cleanDocument(mainDoc,"div#catlinks");
		cleanDocument(mainDoc,"div#footer");
		
		Elements elements = mainDoc.getElementsByTag("a");
		Iterator<Element> elemIter = elements.iterator();
		Set<String> tempOutlinksSet = new HashSet<String>();
		System.out.println(documentCount + " URL: " + url);

		StringBuilder builder = new StringBuilder(mainDoc.body().text().toLowerCase());
		if(!checkLangOfPage(mainDoc.body().text().toLowerCase())){
			return;
		}
		int isRelevant = ((builder.toString().contains("war")==true) || (builder.toString().contains("battle")==true))? 1 : 0 ;
		
		Set<String> tempCanonicalizedURL = new HashSet<String>();
		while(elemIter.hasNext()){
			try{
				Element elem = elemIter.next();
				//System.out.println(elem.attr("abs:href"));
				Link link = new Link(elem.attr("abs:href"), canonocalizedURL(elem.attr("abs:href")));
				
				if(tempCanonicalizedURL.contains(link.getCanonicalizedurl())){
					continue;
				}else{
					tempCanonicalizedURL.add(link.getCanonicalizedurl());
				}
					
				if(validateUrl(link.getRawUrl(),link.getCanonicalizedurl())){
					link.setDepth(depth+1);
					link.setIsRelevant(isRelevant);
					queue.enqueue(link);
					tempOutlinksSet.add(link.getCanonicalizedurl());
					
					if(docInlinkSet.containsKey(link.getCanonicalizedurl())){
						docInlinkSet.get(link.getCanonicalizedurl()).add(canonicalizedURL);
					}else{
						Set<String> tempSet = new HashSet<String>();
						tempSet.add(canonicalizedURL);
						docInlinkSet.put(link.getCanonicalizedurl(), tempSet);
					}
				}
			}catch(CannotCannonicalizeURLException e){
				//System.out.println(e.getMessage());
			}
		}
		System.out.println("SET: " + tempOutlinksSet.size());

		DOC doc = new DOC();
		doc.setDOCNO(canonicalizedURL);
		doc.setTEXT(mainDoc.body().text());
		doc.setOutlinks(tempOutlinksSet);
		doc.setUrl(url);
		doc.setDEPTH(depth);
		try{
			doc.setHEAD(mainDoc.title());
		}catch(Exception e){
			doc.setHEAD(null);
		}
		printDocToFile(doc);
	}
	
	private boolean checkLangOfPage(final String docText) {
		try{
			return new LanguageIdentifier(docText).getLanguage().equalsIgnoreCase("en");
		}catch(Exception e){
			return false;
		}
	}


	private void cleanDocument(Document doc, String tag){
		try{
			doc.select(tag).first().remove();
		}catch(Exception e){
			// Possibly tag is not there!
		}
	}
	
	private boolean isMaxLinksForDomainNotReached(final String url){
		return url.contains("britannica") && britannicaCount<=MAX_BRITANNICA_COUNT;
	}

	private void printDocToFile(DOC doc) throws IOException {
		if(docCount == NO_OF_DOCUMENTS_PER_FILE){
			++fileCount;
			bfwForDoc.flush();
			bfwForDoc.close();
			bfwForDoc = new BufferedWriter(new FileWriter("Results/VAN_"+ fileCount + ".txt"));
			docCount =0;
		}
		bfwForDoc.write(getDOCasString(doc));
		bfwForDoc.write(System.lineSeparator());
		++docCount;
		++documentCount;
		//TODO: Write outlinkFile onceforall
		//writeToOutlinkFile(doc);
		if(documentCount == MAX_DOCUMENTS){
			writeToInlinkFile();
			System.exit(1);
		}
	}


	private void writeToOutlinkFile(DOC doc) throws IOException {
		bfwForOutlinks.write(doc.getDOCNO()+"="+getLinksAsString(doc.getOutlinks())+System.lineSeparator());
	}
	
	private void writeToInlinkFile() throws IOException {
		Iterator<Map.Entry<String, Set<String>>> setIter = docInlinkSet.entrySet().iterator();
		while(setIter.hasNext()){
			Map.Entry<String, Set<String>> entry = setIter.next();
			bfwForInlinks.write(entry.getKey()+"="+getLinksAsString(entry.getValue())+System.lineSeparator());
		}
	}


	private String getDOCasString(DOC doc) {
		StringBuilder builder = new StringBuilder();
		builder.append("<DOC>"+System.lineSeparator());
		builder.append("<DOCNO>"+doc.getDOCNO()+"</DOCNO>"+System.lineSeparator());
		builder.append("<URL>"+doc.getUrl()+"</URL>"+System.lineSeparator());
		builder.append("<TEXT>"+doc.getTEXT()+"</TEXT>"+System.lineSeparator());
		builder.append("<HEAD>"+doc.getHEAD()+"</HEAD>"+System.lineSeparator());
		builder.append("<DEPTH>"+doc.getDEPTH()+"</DEPTH>"+System.lineSeparator());
		//builder.append("<INLINKS>"+getLinksAsString(doc.getInlinks())+"</INLINKS>"+System.lineSeparator());
		builder.append("<OUTLINKS>"+getLinksAsString(doc.getOutlinks())+"</OUTLINKS>"+System.lineSeparator());
		builder.append("</DOC>");
		return builder.toString();
	}

	private String getLinksAsString(Set<String> inlinks) {
		StringBuilder builder = new StringBuilder();
		for (String link : inlinks) {
			builder.append(link+" ");
		}
		return builder.toString().trim();
	}

	
	public void shutdown(){
		try {
			bfwForDoc.flush();
			bfwForDoc.close();
			
			bfwForInlinks.flush();
			bfwForInlinks.close();

			bfwForOutlinks.flush();
			bfwForOutlinks.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
