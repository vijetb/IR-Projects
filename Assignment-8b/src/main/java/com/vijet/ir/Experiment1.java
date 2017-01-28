package com.vijet.ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdk.jfr.events.FileWriteEvent;

import com.vijet.ir.model.DOC;

public class Experiment1 {
	public static void main(String[] args) throws IOException {
		for(int i=0; i < 200;i++){
			System.out.println("@attribute TOPIC-"+i+" numeric");
		}
	}
}
