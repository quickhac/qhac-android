package com.patil.gradecheck;

import java.util.ArrayList;

public class CycleGrades {
	/*
	 * Container for categories of assignments.
	 */
	
	String title;
	int average;
	ArrayList<Category> categories;
	
	public CycleGrades(String title, int average, ArrayList<Category> categories) {
		this.title = title;
		this.average = average;
		this.categories = categories;
	}
	
}
