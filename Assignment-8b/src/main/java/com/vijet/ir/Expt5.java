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

public class Expt5 {
	public static void main(String[] args) throws IOException {
		String st = "58:AP890305-0021&&59:AP890302-0079";
		String[] val = st.split("&&");
		String query1 = val[0].split(":")[0];
		String docId1 = val[0].split(":")[1];
		String query2 = val[1].split(":")[0];
		String docId2 = val[1].split(":")[1];
		System.out.println(query1);
		System.out.println(docId1);
		System.out.println(query2);
		System.out.println(docId2);

	}
}
