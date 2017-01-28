package com.vijet.ir;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tika.detect.NameDetector;
import org.apache.tika.language.LanguageIdentifier;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vijet.ir.exceptions.CannotCannonicalizeURLException;
import com.vijet.ir.model.Link;
import com.vijet.ir.robot.RobotUtil;

public class Expriment {

	public static void main(String[] args) throws Exception {
		String url = "http://nsarchive.gwu.edu/nukevault/ebb401/";

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		// add request header
		HttpResponse response = client.execute(request);

//		System.out.println(response.getAllHeaders());
		Header[] headers = response.getAllHeaders();
		for (Header header : headers) {
			System.out.println(header.toString());
			//System.out.println(header.getName()+" "+ header.getValue());
		}
		System.out.println("Response Code : " 
	                + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		System.out.println(result.toString());
	}
}
