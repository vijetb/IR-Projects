package com.vijet.ir.util;

public class CleanText {
	public static final String cleanText(final String uncleanedText){
//		return uncleanedText.replaceAll("[-=/]", " ")
//				.replaceAll("\\.()", "")
//				.replaceAll("\\(", "")
//				.replaceAll("\\)", "")
//				.replaceAll("[$'._`?{}~]", "")
//				.replaceAll("[0-9]", "")
//				.replaceAll("[\":\"+()<>!,;\"]", "");
		
		return uncleanedText.replaceAll("[^a-zA-Z]+", " ");
	}
	
	
	public static void main(String[] args) {
		String str = "the quick! brown-fox don't jumped`` over lazy... ";
		String cleantStr = cleanText(str);
		System.out.println(cleantStr);
	}
}
