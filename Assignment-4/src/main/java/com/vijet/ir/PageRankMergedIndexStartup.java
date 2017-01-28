package com.vijet.ir;

import java.io.IOException;

import com.vijet.ir.loadfiles.PageRankMergedIndex;
/**
 * This class is used to run the pagerank on the merged index.
 * @author Viji
 */
public class PageRankMergedIndexStartup {
	public static void main(String[] args) {
		PageRankMergedIndex PRMergedIndex = new PageRankMergedIndex();
		try {
			PRMergedIndex.loadDataFromFiles();
			PRMergedIndex.init();
		} catch (IOException e) {
			System.out.println("Unable to configure ESClient");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
