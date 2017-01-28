package com.vijet.ir.model;

/**
 * Bean class that is needed to store the scores at different levels
 * required for f-Measure and nDCG Measure
 * @author Viji
 *
 */
public class Score {
	/**
	 * Score at 5 cutoff
	 */
	private double five_cutoff;
	/**
	 * Score at 10 cutoff
	 */
	private double ten_cutoff;
	/**
	 * Score at 20 cutoff
	 */
	private double twenty_cutoff;
	/**
	 * Score at 50 cutoff
	 */
	private double fifty_cutoff;
	/**
	 * Score at 100 cutoff
	 */
	private double hundred_cutoff;
	/**
	 * Score at 200 cutoff
	 */
	private double twoHundred_cutoff;
	/**
	 * Score at 500 cutoff
	 */
	private double fiveHundred_cutoff;
	/**
	 * Score at 1000 cutoff
	 */
	private double thousand_cutoff;
	
	public double getFive_cutoff() {
		return five_cutoff;
	}
	public void setFive_cutoff(double five_cutoff) {
		this.five_cutoff = five_cutoff;
	}
	public double getTen_cutoff() {
		return ten_cutoff;
	}
	public void setTen_cutoff(double ten_cutoff) {
		this.ten_cutoff = ten_cutoff;
	}
	public double getTwenty_cutoff() {
		return twenty_cutoff;
	}
	public void setTwenty_cutoff(double twenty_cutoff) {
		this.twenty_cutoff = twenty_cutoff;
	}
	public double getFifty_cutoff() {
		return fifty_cutoff;
	}
	public void setFifty_cutoff(double fifty_cutoff) {
		this.fifty_cutoff = fifty_cutoff;
	}
	public double getHundred_cutoff() {
		return hundred_cutoff;
	}
	public void setHundred_cutoff(double hundred_cutoff) {
		this.hundred_cutoff = hundred_cutoff;
	}
	
	public double getTwoHundred_cutoff() {
		return twoHundred_cutoff;
	}
	public void setTwoHundred_cutoff(double twoHundred_cutoff) {
		this.twoHundred_cutoff = twoHundred_cutoff;
	}
	public double getFiveHundred_cutoff() {
		return fiveHundred_cutoff;
	}
	public void setFiveHundred_cutoff(double fiveHundred_cutoff) {
		this.fiveHundred_cutoff = fiveHundred_cutoff;
	}
	public double getThousand_cutoff() {
		return thousand_cutoff;
	}
	public void setThousand_cutoff(double thousand_cutoff) {
		this.thousand_cutoff = thousand_cutoff;
	}
	
}
