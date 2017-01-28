package com.vijet.ir;

import java.io.IOException;

import com.vijet.ir.results.GenerateResults;
/**
 * Start up class to generate the intermediate results from combinedQREL file to 
 * the format requrired to run TrecEval
 * @author Vijet
 */
public class GenerateCombinedResultsStartUp {

	public static void main(String[] args) {
		GenerateResults results = new GenerateResults();
		try {
			results.generateCombinedResults("QrelFileMerged.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
