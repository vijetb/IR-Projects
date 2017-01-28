package com.vijet.ir;

import java.util.Scanner;

import com.vijet.ir.filterclient.FilterClient;

public class StartUp {

	public static void main(String[] args) throws Exception {
		final String FOLDER_PATH = "ap89_collection";
		
		FilterClient client = new FilterClient();
		client.loadFiles(FOLDER_PATH);
		client.generateWordsMapping();
		client.generateTopics();
		client.generateTopicDocDistribution();
		client.generateMatrix();
//		Scanner scanner = new Scanner(System.in);
//		do{
//			System.out.println("ENTER YES TO GENERATE THE TOPICS!");
//		}while(!scanner.next().equalsIgnoreCase("YES"));
//		client.generateTopics();
	}
	

}
