package com.patil.gradecheck;

public class Category {
	/*
	 * A category of grades (ex. Exam, Homework, Daily)
	 */
	
	String title;
	double weight;
	float average;
	Assignment[] assignments;
	
	public Category(String title, double weight, float average,
			Assignment[] assignments) {
		this.title = title;
		this.weight = weight;
		this.average = average;
		this.assignments = assignments;
	}
	
	

}
