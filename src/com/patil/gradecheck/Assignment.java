package com.patil.gradecheck;

public class Assignment {
	/*
	 * An individual assignment.
	 */
	
	String title;
	String dateDue;
	String dateAssigned;
	int ptsEarned;
	int ptsPossible;
	double weight;
	String note;
	boolean extraCredit;
	
	public Assignment(String title, String dateAssigned, String dateDue,
			int ptsEarned, int ptsPossible, double weight, String note,
			boolean extraCredit) {
		this.title = title;
		this.dateDue = dateDue;
		this.dateAssigned = dateAssigned;
		this.ptsEarned = ptsEarned;
		this.ptsPossible = ptsPossible;
		this.weight = weight;
		this.note = note;
		this.extraCredit = extraCredit;
	}
	
}
