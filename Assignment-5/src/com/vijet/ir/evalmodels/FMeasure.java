package com.vijet.ir.evalmodels;

import java.util.List;

import com.vijet.ir.model.Doc;
/**
 * Util class for computing the F Measure.
 * @author Viji
 */
public final class FMeasure {
	/**
	 * Computes the f-measure at a specified cut-off
	 */
	public static final double fMeasureScore(final List<Doc> listOfDoc, final int cutoff){
		if(listOfDoc.size()<cutoff){
			return 0.0;
		}
		Doc tempDoc = listOfDoc.get(cutoff-1);
		if(tempDoc.getPrecision()!=0.0 && tempDoc.getRecall()!=0.0)
			return 2.0 * tempDoc.getPrecision() * tempDoc.getRecall()/(tempDoc.getPrecision()+tempDoc.getRecall());
		return 0.0;
	}
}
