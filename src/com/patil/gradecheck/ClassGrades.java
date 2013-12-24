package com.patil.gradecheck;

public class ClassGrades {
	/*
	 * Container for categories of assignments.
	 */
	
	String title;
	String period;
	int sixWeekIndex;
	int average;
	Category[] categories;
	
	public ClassGrades(String title, String period, int sixWeekIndex, int average, Category[] categories) {
		this.title = title;
		this.period = period;
		this.sixWeekIndex = sixWeekIndex;
		this.average = average;
		this.categories = categories;
	}
	
}
