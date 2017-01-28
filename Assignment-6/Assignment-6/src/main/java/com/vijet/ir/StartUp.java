package com.vijet.ir;

import java.io.IOException;

import com.vijet.ir.model.LinearRegression;

/**
 * StartUp class for the Regression. It will load the data, generate the feature matrix,
 * run it against the test queries and output the results.
 * @author Vijet Badigannavar
 */
public class StartUp {
	public static void main(String[] args) throws IOException {
		LinearRegression regression = new LinearRegression();
		regression.loadData();
		regression.generateMatrix();
		regression.testingQueries();
		regression.testOnTrainingQueries();
	}
}
