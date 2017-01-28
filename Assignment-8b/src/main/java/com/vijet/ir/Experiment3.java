package com.vijet.ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vijet.ir.model.QueryDoc;

public class Experiment3 {
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));
		
		Set<Integer> que = new HashSet<Integer>();
		Set<String> combinations = new HashSet<String>();
		que.add(85);que.add(59);que.add(56);que.add(71);que.add(64);
		que.add(62);que.add(93);que.add(99);que.add(58);que.add(77);
		que.add(54);que.add(87);que.add(94);que.add(100);que.add(89);
		que.add(61);que.add(95);que.add(68);que.add(57);que.add(97);
		que.add(98);que.add(60);que.add(80);que.add(63);que.add(91);
		
		
		String line = new String();
//		Set<QueryDoc> set = new HashSet<QueryDoc>();
		List<String> set = new ArrayList<String>();
		while((line=reader.readLine())!=null){
			String[] values = line.split(" ");
			if(que.contains(Integer.valueOf(values[0])) && values[3].equals("1")){
				//set.add(new QueryDoc(values[2],values[0]));
				set.add(values[0]+":"+values[2]);
			}
		}
		System.out.println(set.size());

		for (int i = 0 ; i < set.size();i++) {
			for (int j=i+1;j < set.size();j++) {
				combinations.add(set.get(i)+"&&"+set.get(j));
			}
		}
		System.out.println(combinations.size());
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("OutPut/QueryCombinations.txt"));
		for (String combination : combinations) {
			writer.write(combination+System.lineSeparator());
		}
		writer.flush();
		writer.close();
		
	}
}
