package com.vijet.ir;

import java.io.IOException;
/**
 * Start up class for running all the measures
 * @author Viji
 * @modified Last minute fix given to handle both the treceval for ES index and 
 * actual trec files
 */
public class QREL {
	public static void main(String[] args) {
		boolean isTrecFile = true;
		TrecEval trecEval = new TrecEval(isTrecFile);
		try {
			if(isTrecFile){
				trecEval.loadData("qrels.adhoc.51-100.AP89.txt", "Trec-Text-HW5.txt");
			}
			else{
				trecEval.loadData("qrelFileBoolean.txt", "RankFile.txt");
			}
			//generate the precision and recall values
			trecEval.generateValues();
			//generate all the measure values
			trecEval.generateAllScores();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
