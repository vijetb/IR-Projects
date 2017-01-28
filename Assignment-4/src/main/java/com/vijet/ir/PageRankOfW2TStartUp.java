package com.vijet.ir;

import java.io.IOException;

import com.vijet.ir.loadfiles.PageRankWT2g;
/**
 * Startup class for page ranking W2Tg dataset
 * @author Viji
 */
public class PageRankOfW2TStartUp {
	public static void main(String[] args) {
		PageRankWT2g loadFiles = new PageRankWT2g();
		try {
			loadFiles.loadDataFromFiles();
			loadFiles.init();
		} catch (IOException e) {
			System.out.println("Unable to configure ESClient");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
