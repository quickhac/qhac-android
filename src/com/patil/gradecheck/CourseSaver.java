package com.patil.gradecheck;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
	public void saveCourses(ArrayList<Course> courses) {
		// Save the prefs under the username
		String username = new SettingsManager(context).getLoginInfo()[0];
		SharedPreferences prefs = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("savedCourses", new Gson().toJson(courses).toString());
		editor.putLong("lastUpdated", System.currentTimeMillis());
		Log.d("GSON", new Gson().toJson(courses).toString());
		editor.commit();
	}
	
	/*
	 * Returns the cached courses
	 */
	public ArrayList<Course> getSavedCourses() {
		ArrayList<Course> courses;
		String username = new SettingsManager(context).getLoginInfo()[0];
		SharedPreferences prefs = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		String savedCourses = prefs.getString("savedCourses", "None");
		if(savedCourses.equals("None")) {
			courses = null;
		} else {
			courses = new Gson().fromJson(savedCourses, new TypeToken<List<Course>>(){}.getType());
		}
		return courses;
	}
	
	public long getLastUpdated() {
		String username = new SettingsManager(context).getLoginInfo()[0];
		SharedPreferences prefs = context.getSharedPreferences(username, Context.MODE_PRIVATE);
		return prefs.getLong("lastUpdated", System.currentTimeMillis());
	}
	
}
