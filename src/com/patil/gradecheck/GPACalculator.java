package com.patil.gradecheck;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class GPACalculator {
	final int DEFAULT_GPA_PRECISION = 4;
	ArrayList<Course> courses;
	Context context;

	public GPACalculator(Context context, ArrayList<Course> courses) {
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

	public double calculateCategoryAverage(Category category) {
		double add = 0;
		double divide = 0;
		ArrayList<Assignment> assignments = category.assignments;
		for (int i = 0; i < assignments.size(); i++) {
			Assignment assignment = assignments.get(i);
			if ((assignment.ptsPossible == 100 || assignment.ptsPossible == -2 || assignment.ptsPossible == -1)
					&& assignment.ptsEarned >= 0) {
				add += assignment.ptsEarned;
			} else {
				add += ((double) assignment.ptsEarned / (double) assignment.ptsPossible) * 100;
			}
			divide += 1;
		}
		double average = add / divide;
		// Round to 3 decimal places
		average = round(average, 3);
		return average;
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	public double calculateGPA() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Set<String> grades = sharedPref.getStringSet("pref_weightedClasses",
				null);
		ArrayList<String> selectedClasses = new ArrayList<String>();
		if (grades != null) {
			String[] selecteds = grades.toArray(new String[grades.size()]);
			if (selecteds != null) {
				for (int i = 0; i < selecteds.length; i++) {
					if (selecteds[i] != null) {
						selectedClasses.add(selecteds[i]);
					}
				}
			}
		}

		double add = 0;
		double divide = 0;
		for (int i = 0; i < courses.size(); i++) {
			Course course = courses.get(i);
			double grade = calculateYearAverage(course);
			boolean weighted = false;
			for (int d = 0; d < selectedClasses.size(); d++) {
				if (selectedClasses.get(d).equals(course.title)) {
					weighted = true;
				}
			}
			if (grade > 0) {

				double gradePoint = 0;
				if (weighted) {
					if (new SettingsManager(context).getLoginInfo()[3]
							.equals("Austin")) {
						gradePoint = convertGradePoint(grade, 1);
					} else if (new SettingsManager(context).getLoginInfo()[3]
							.equals("RoundRock")) {
						gradePoint = convertGradePoint(grade, 2);
					} else {
						gradePoint = convertGradePoint(grade, 1);
					}

				} else {
					gradePoint = convertGradePoint(grade, 0);
				}
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
		double divide = 0;
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
