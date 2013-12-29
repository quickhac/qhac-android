package com.patil.gradecheck;

import java.util.ArrayList;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class GPACalculator {
	final int DEFAULT_GPA_PRECISION = 4;
	String district;
	ArrayList<Course> courses;
	Context context;

	public GPACalculator(Context context, String district, ArrayList<Course> courses) {
		this.district = district;
		this.courses = courses;
		this.context = context;
	}

	public double convertGradePoint(double grade, int offset) {
		if (grade < 70) {
			return 0;
		} else {
			return Math.min((grade - 60) / 10, 4) + offset;
		}
	}

	public double calculateGPA() {
		
		
		double add = 0;
		int divide = 0;
		for (int i = 0; i < courses.size(); i++) {
			Course course = courses.get(i);
			double grade = calculateYearAverage(course);
			
			if (grade > 0) {
				double gradePoint = convertGradePoint(grade, 0);
				Log.d("GPA", "Add: " + String.valueOf(add));
				add += gradePoint;
				divide++;
			}
		}
		double GPA = add / divide;
		Log.d("GPA", String.valueOf("GPA"));
		return GPA;
	}

	public double calculateYearAverage(Course course) {
		double add = 0;
		int divide = 0;
		for (int i = 0; i < course.semesterAverages.length; i++) {
			int semesterAverage = course.semesterAverages[i];
			if (semesterAverage > 0) {
				add += semesterAverage;
				divide++;
			}
		}
		for (int i = 0; i < course.examGrades.length; i++) {
			int examGrade = course.examGrades[i];
			if (examGrade > 0) {
				add += examGrade;
				divide++;
			}
		}
		for (int i = 0; i < course.sixWeeksAverages.length; i++) {
			int sixWeeksAverage = course.sixWeeksAverages[i];
			if (sixWeeksAverage > 0) {
				add += sixWeeksAverage;
				divide++;
			}
		}

		double grade = add / divide;
		Log.d("GPA", "Grade: " + String.valueOf(grade));
		return grade;
	}
}
