package com.vijet.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Experiment {
	public static void main(String[] args) throws IOException {
		Map<String,String> map = new TreeMap<String, String>();
		map.put("4", "4.0");
		map.put("5", "5.0");
		map.put("1", "1.0");
		map.put("3", "3.0");
		map.put("89", "89.0");
		
		for (String string : map.keySet()) {
			System.out.println(string);
		}
	}
}
