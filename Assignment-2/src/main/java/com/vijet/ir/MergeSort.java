package com.vijet.ir;

import java.util.ArrayList;
import java.util.List;

public class MergeSort {
	
	
	public static void MergeSort(List<TokenDesc> a, int low, int high){
		if(low<high){
			int mid = (low+high)/2;
			MergeSort(a,low,mid);
			MergeSort(a,mid+1,high);
			Merge(a,low,mid,high);
		}
	}
	
	public static void Merge(List<TokenDesc> a, int low, int mid, int high){
//			int[] tempArray = new int[high+1];
//			
//			for(int i = low; i <= high; i++){
//				tempArray[i] = a[i];
//			}
			List<TokenDesc> tempArray = new ArrayList<TokenDesc>();
			tempArray.addAll(a);
			
			int i = low;
			int j = mid+1;
			int count = low;
			
			while(i<=mid && j<=high){
				if(tempArray.get(j).getCount() < tempArray.get(i).getCount()){
					a.set(count, tempArray.get(i));
					//a[count] = tempArray[i];
					i++;
				}else{
					
					
					a.set(count, tempArray.get(j));
					//a[count] = tempArray[j];
					j++;
				}
				count++;
			}
			
			while(i<=mid){
				a.set(count, tempArray.get(i));
				//a[count] = tempArray[i];
				i++;
				count++;
			}
	}
	
	public static void main(String[] args) {
		TokenDesc t1 = new TokenDesc("1", 5);
		TokenDesc t2 = new TokenDesc("2", 45);
		TokenDesc t3 = new TokenDesc("3", 53);
		TokenDesc t4 = new TokenDesc("4", 2);
		TokenDesc t5 = new TokenDesc("5", 98);
		
		List<TokenDesc> list = new ArrayList<TokenDesc>();
		list.add(t1);
		list.add(t2);
		list.add(t3);
		list.add(t4);
		list.add(t5);

		//int[] unsortedArray = new int[]{45,23,11,89,77,98,4,28,65,43};
//		for(int i = 0 ; i < unsortedArray.length; i++){
//			System.out.print(unsortedArray[i]+" ");
//		}
		MergeSort(list, 0, list.size()-1);
		//System.out.println();
		
		for (TokenDesc tokenDesc : list) {
			System.out.println(tokenDesc.getCount());
		}
		//System.out.println(list);
//		for(int i = 0 ; i < unsortedArray.length; i++){
//			System.out.print(unsortedArray[i]+" ");
//		}
	}
}
