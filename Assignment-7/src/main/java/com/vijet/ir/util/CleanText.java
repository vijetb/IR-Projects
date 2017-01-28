package com.vijet.ir.util;

public class CleanText {
	public static final String cleanText(final String uncleanedText){
		return uncleanedText.replaceAll("[-=/]", " ")
				.replaceAll("\\.()", "")
				.replaceAll("\\(", "")
				.replaceAll("\\)", "")
				.replaceAll("[\":\"+()<>!,;\"]", "");
	}
}
