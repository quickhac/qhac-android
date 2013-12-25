package com.patil.gradecheck;

public class Course {
	/*
	 * An object that stores general information about each course. Can hold an array of ClassGrades, or individual six weeks assignments.
	 */
	
	String title;
	String teacherName;
	String teacherEmail;
	int[] sixWeeksAverages;
	int[] examGrades;
	int[] semesterAverages;
	String[] gradeLinks;
	
	CycleGrades[] sixWeekGrades;
	
	public Course(String title, String teacherName, String teacherEmail, int[] sixWeeksAverages, int[] examGrades, int[] semesterAverages, String[] gradeLinks) {
		this.title = title;
		this.teacherName = teacherName;
		this.teacherEmail = teacherEmail;
		this.sixWeeksAverages = sixWeeksAverages;
		this.examGrades = examGrades;
		this.semesterAverages = semesterAverages;
		this.gradeLinks = gradeLinks;
	}
}
