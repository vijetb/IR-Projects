package com.vijet.ir.util;

import org.tartarus.snowball.ext.PorterStemmer;

public class StemUtil {
	public static String getStemOfWord(String input){

		PorterStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(input);
		stemmer.stem();
		return stemmer.getCurrent();


	}
}
