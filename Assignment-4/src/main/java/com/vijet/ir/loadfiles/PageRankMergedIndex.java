package com.vijet.ir.loadfiles;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.vijet.ir.pagerank.PageRankUtil;

public class PageRankMergedIndex {
	/**
	 * File name that stores all the inlinks
	 */
	private final String MERGED_INDEX_INLINKS = "mergedIndexInlinks.txt";
	/**
	 * File name that stores page rank values and the scores  
	 */
	private final String DUMP_FILE_NAME ="Question1Solutions.txt";
	/**
	 * Map that stores all the page rank values
	 */
	private Map<String,Double> PRScoreMap = new HashMap<String, Double>(50000);
	/**
	 * Map that stores the generated outlinks
	 */
	private Map<String,Set<String>> outlinksMapNew = new HashMap<String, Set<String>>(50000);
	/**
	 * Map that stores the outlinks count
	 */
	private Map<String,Long> outlinksMap = new HashMap<String, Long>(50000);
	/**
	 * Map that stores all the inlinks
	 */
	private Map<String,Set<String>> inlinksMap = new HashMap<String, Set<String>>(50000);
	/**
	 * Set that stores the sink nodes.
	 */
	private Set<String> sinkNodesSet = new HashSet<String>();
	/**
	 * Pagerank class
	 */
	final PageRankUtil pageRankUtil = new PageRankUtil();

	/**
	 * Method that loads the data from the file and generates the out links and 
	 * populates the in links for that file.
	 * @throws IOException
	 */
	public void loadDataFromFiles() throws IOException{
		BufferedReader bwInlinks = null;
		try {
			bwInlinks = new BufferedReader(new FileReader(MERGED_INDEX_INLINKS));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String str = new String();
		while((str=bwInlinks.readLine())!=null){
			String[] values = str.split(" ");
			if(values.length>1){
				//update the inlinks map
				for(int i = 1 ; i < values.length;i++){
					//update inlinks
					if(inlinksMap.containsKey(values[0])){
						inlinksMap.get(values[0]).add(values[i]);
					}else{
						Set<String> set = new HashSet<String>();
						set.add(values[i]);
						inlinksMap.put(values[0], set);
					}
					//update the inlinkmap
					PRScoreMap.put(values[i], null);

					//update outlinks
					if(outlinksMapNew.containsKey(values[i])){
						outlinksMapNew.get(values[i]).add(values[0]);
					}else{
						Set<String> set = new HashSet<String>();
						set.add(values[0]);
						outlinksMapNew.put(values[i], set);
					}
				}
			}else{
				if(!inlinksMap.containsKey(values[0])){
					inlinksMap.put(values[0], new HashSet<String>());
				}
			}

			PRScoreMap.put(values[0], null);
		}
	}

	/**
	 * Initialize page rank to the default value. From the outlinkNew map compute the 
	 * out link map that contain only the count.
	 */
	public void init(){
		final long N = PRScoreMap.size();
		// initialize the score of all the values.
		Iterator<Map.Entry<String,Double>> iter = PRScoreMap.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,Double> entry = iter.next();
			entry.setValue(1.0/N);
		}
		// convert the outlinkmapnew to outlinks map that contains the count
		Iterator<Map.Entry<String,Set<String>>> outlinkMapCountIter = outlinksMapNew.entrySet().iterator();
		while(outlinkMapCountIter.hasNext()){
			Map.Entry<String,Set<String>> entry = outlinkMapCountIter.next();
			outlinksMap.put(entry.getKey(),Long.valueOf(entry.getValue().size()));
		}
		//update the sink nodes
		sinkNodesSet.addAll(inlinksMap.keySet());
		sinkNodesSet.removeAll(outlinksMap.keySet());
		
		System.out.println("***** PAGE RANK STARTED *****");
		pageRankUtil.computePageRank(PRScoreMap, outlinksMap, inlinksMap, sinkNodesSet,DUMP_FILE_NAME);
		System.out.println("***** PAGE RANK ENDED *****");
	}
}
