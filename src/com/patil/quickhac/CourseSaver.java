package com.patil.quickhac;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quickhac.common.data.Course;

public class CourseSaver {
	/*
	 * Helper class for cacheing and decacheing courses for offline availability
	 */
	Context context;

	public CourseSaver(Context context) {
		this.context = context;
	}

	/*
	 * Saves courses to cache
	 */
	public void saveCourses(Course[] courses, String username, String id) {
		// Save the prefs under the username
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("savedCourses", new Gson().toJson(courses));
		editor.putLong("lastUpdated", System.currentTimeMillis());
		editor.commit();
	}

	public void eraseCourses(String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.remove("savedCourses");
		editor.remove("lastUpdated");
		editor.commit();
	}

	/*
	 * Saves GPA value
	 */
	public void saveWeightedGPA(double GPA, String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putFloat("weightedGPA", (float) GPA);
		editor.commit();
	}

	/*
	 * Returns saved GPA
	 */
	public double getWeightedGPA(String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		float gpa = prefs.getFloat("weightedGPA", 0);
		return (double) gpa;
	}

	/*
	 * Saves GPA value
	 */
	public void saveUnweightedGPA(double GPA, String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putFloat("unweightedGPA", (float) GPA);
		editor.commit();
	}

	/*
	 * Returns saved GPA
	 */
	public double getUnweightedGPA(String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		float gpa = prefs.getFloat("unweightedGPA", 0);
		return (double) gpa;
	}
	
	public void eraseWeightedGPA(String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.remove("weightedGPA");
		editor.commit();
	}
	
	public void eraseUnweightedGPA(String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.remove("unweightedGPA");
		editor.commit();
	}

	/*
	 * Returns the cached courses
	 */
	public Course[] getSavedCourses(String username, String id) {
		Course[] courses;
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		String savedCourses = prefs.getString("savedCourses", "None");
		if (savedCourses.equals("None")) {
			courses = null;
		} else {
			courses = new Gson().fromJson(savedCourses,
					new TypeToken<Course[]>() {
					}.getType());
		}
		return courses;
	}

	public void saveLatestResponse(String response, String username, String id) {
		// Save the prefs under the username
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("savedResponse", response);
		editor.commit();
		Log.d("BackgroundGrades", "saved response ");
	}

	public String getLatestResponse(String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		String savedResponse = prefs.getString("savedResponse", null);
		Log.d("BackgroundGrades", "got saved response ");
		if (savedResponse == null) {
			Log.d("BackgroundGrades", "saved response is null");
		}
		return savedResponse;
	}

	public long getLastUpdated(String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		return prefs.getLong("lastUpdated", System.currentTimeMillis());
	}

}
