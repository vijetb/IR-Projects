package com.vijet.ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vijet.ir.model.QueryDoc;

public class Experiment4 {
	public static void main(String[] args) throws IOException {
		Map<String,String> docClusterMapping = new HashMap<String,String>();
		BufferedReader reader = new BufferedReader(new FileReader("visual.arff"));
		String line = new String();
		while((line=reader.readLine())!= null){
			String[] val = line.split(",");
			docClusterMapping.put(val[1], val[val.length-1]);
		}
		reader.close();
		System.out.println("VALUES-FETCHED.....");
		int[][] confusionMatrix = new int[2][2];
		
		reader = new BufferedReader(new FileReader("OutPut/QueryCombinations.txt"));
		while((line=reader.readLine())!= null){
			String[] val = line.split("&&");
			String query1 = val[0].split(":")[0];
			String docId1 = val[0].split(":")[1];
			String query2 = val[1].split(":")[0];
			String docId2 = val[1].split(":")[1];
			
			if(query1.equals(query2)){
				if(docClusterMapping.get(docId1).equals(docClusterMapping.get(docId2))){
					confusionMatrix[0][0] = confusionMatrix[0][0] + 1;
				}else{
					confusionMatrix[0][1] = confusionMatrix[0][1] + 1;
				}
			}else{
				if(docClusterMapping.get(docId1).equals(docClusterMapping.get(docId2))){
					confusionMatrix[1][0] = confusionMatrix[1][0] + 1;
				}else{
					confusionMatrix[1][1] = confusionMatrix[1][1] + 1;
				}
			}
		}
		reader.close();
		
		for(int i=0;i<2;i++){
			for(int j=0;j<2;j++){
				System.out.print(confusionMatrix[i][j]+" ");
			}
			System.out.println();
		}
	}
}
