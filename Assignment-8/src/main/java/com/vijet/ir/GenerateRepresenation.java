package com.vijet.ir;

import com.vijet.ir.filterclient.GenerateMatrix;

public class GenerateRepresenation {
	public static void main(String[] args) throws Exception {
		final String FOLDER_PATH = "ap89_collection";
		GenerateMatrix matrix = new GenerateMatrix();
		matrix.loadFiles(FOLDER_PATH);
		matrix.generateRepresentation();
	}
}
