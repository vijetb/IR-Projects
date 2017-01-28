package com.vijet.ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OffsetFilesGenerator {
	public static void main(String[] args) throws IOException {
		
//		BufferedReader bfrOutlinkFile = new BufferedReader(new FileReader(new File("Results/VAN_outlinks.txt")));
//		BufferedWriter bfwOutlinkOffsetFile = new BufferedWriter(new FileWriter(new File("Results/VAN_outlinks_catlog.txt")));
//		long offset = 0;
//		String tempString = null;
//		while((tempString=bfrOutlinkFile.readLine())!=null){
//			bfwOutlinkOffsetFile.write(tempString.split("=")[0].trim()+"="+offset+System.lineSeparator());
//			offset = offset+tempString.length()+1;
//		}
//		bfwOutlinkOffsetFile.flush();
//		bfwOutlinkOffsetFile.close();
//		bfrOutlinkFile.close();
		System.out.println("***** OUTLINKS OFFSET FILE GENERATED SUCCESSFULLY*****");
		testing();
	}
	
	public static void testing() throws IOException{
		Map<String,String> tempMap = new HashMap<String,String>();
		BufferedReader bfrOutlinkFile = new BufferedReader(new FileReader(new File("Results/VAN_outlinks.txt")));
		String tempString = null;
		while((tempString=bfrOutlinkFile.readLine())!=null){
			String[] values = tempString.split("=");
			tempMap.put(values[0], tempString);
		}
		
		System.out.println("OUTLINKS-> " + tempMap.size());
		System.out.println("****");
		Map<String,String> tempMapInlinks = new HashMap<String,String>();
		BufferedReader bfrInlinkFile = new BufferedReader(new FileReader(new File("Results/VAN_inlinks.txt")));
		String tempString1 = null;
		while((tempString1=bfrInlinkFile.readLine())!=null){
			String[] values = tempString1.split("=");
			tempMapInlinks.put(values[0], tempString1);
		}
		
		System.out.println("InLINKS-> " + tempMapInlinks.size());
		
	}
}
