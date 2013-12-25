package com.patil.gradecheck;

import java.util.ArrayList;

public class Category {
	/*
	 * A category of grades (ex. Exam, Homework, Daily)
	 */
	
	String title;
	double weight;
	double average;
	ArrayList<Assignment> assignments;
	
	public Category(String title, double weight, double average,
			ArrayList<Assignment> assignments) {
		this.title = title;
		this.weight = weight;
		this.average = average;
		this.assignments = assignments;
	}
	
	

}
