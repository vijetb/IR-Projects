package com.vijet.ir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProximitySearch {

	static int min, max;
	public static void main(String[] args) {
		List<Integer> list1 = new ArrayList<Integer>();
		list1.add(0);
		list1.add(5);
		list1.add(10);
		list1.add(15);
		
		List<Integer> list2 = new ArrayList<Integer>();
		list2.add(1);
		list2.add(3);
		list2.add(6);
		list2.add(9);
		
		List<Integer> list3 = new ArrayList<Integer>();
		list3.add(4);
		list3.add(8);
		list3.add(16);
		list3.add(21);
		
		List<List<Integer>> lists = new ArrayList<List<Integer>>();
		lists.add(list1);
		lists.add(list2);
		lists.add(list3);
		
		long maxList = 0;
		for (List<Integer> list : lists) {
			if(maxList<list.size()){
				maxList = list.size();
			}
		}
		int docScore = Integer.MAX_VALUE;
		int[] scores = new int[lists.size()];
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		for (int i = 0; i < lists.size()*maxList; i++){
			
			map.clear();
			for(int j = 0 ; j < lists.size(); j++){
				scores[j] = lists.get(j).get(0);
				map.put(scores[j], j);
			}
			
			bubbleSort(scores);
			int tempScore = scores[scores.length-1]-scores[0];
			if(tempScore < docScore){
				docScore = tempScore+1;
			}
			
			boolean isDone = true;
			for (int p = 0 ; p < lists.size() ; p++) {
				isDone = isDone && (lists.get(p).size() == 1);
			}
			if(isDone){
				break;
			}
			
			for(int y = 0 ; y< lists.size();y++){
				
				int arrPos = map.get(scores[y]);
				if(lists.get(arrPos).size()==1){
					continue;
				}else{
					lists.get(arrPos).remove(0);
					break;
				}
			}
				
		}
		
		
		System.out.println("Score: "+ docScore);
		
		
	}
	
	public static int scoreForDocument(List<List<Integer>> lists) throws Exception{
		long maxList = 0;
		for (List<Integer> list : lists) {
			if(maxList<list.size()){
				maxList = list.size();
			}
		}
		int docScore = Integer.MAX_VALUE;
		int[] scores = new int[lists.size()];
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		for (int i = 0; i < lists.size()*maxList; i++){
			
			map.clear();
			for(int j = 0 ; j < lists.size(); j++){
				scores[j] = lists.get(j).get(0);
				map.put(scores[j], j);
			}
			
			bubbleSort(scores);
			int tempScore = scores[scores.length-1]-scores[0];
			if(tempScore < docScore){
				docScore = tempScore+1;
			}
			
			boolean isDone = true;
			for (int p = 0 ; p < lists.size() ; p++) {
				isDone = isDone && (lists.get(p).size() == 1);
			}
			if(isDone){
				return docScore;
			}
			
			for(int y = 0 ; y< lists.size();y++){
				
				int arrPos = map.get(scores[y]);
				if(lists.get(arrPos).size()==1){
					continue;
				}else{
					lists.get(arrPos).remove(0);
					break;
				}
			}
				
		}
	
		throw new Exception("Malfunctioning");
	}

	public static void bubbleSort(int[] a){
		for(int i = 0 ; i < a.length; i++){
			boolean swap = false;
			for(int j=0; j < (a.length-i-1); j++ ){
				if(a[j] > a[j+1]){
					int temp = a[j];
					a[j] = a[j+1];
					a[j+1] = temp;
					swap =true;
				}
			}
			if(!swap){
				break;
			}
		}
	}
		

}
