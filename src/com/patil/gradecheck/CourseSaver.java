package com.patil.gradecheck;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

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
	public void saveCourses(Course[] courses) {
		// Save the prefs under the username
		String username = new SettingsManager(context).getLoginInfo()[0];
		SharedPreferences prefs = context.getSharedPreferences(username,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("savedCourses", new Gson().toJson(courses));
		editor.putLong("lastUpdated", System.currentTimeMillis());
		editor.commit();
	}

	/*
	 * Returns the cached courses
	 */
	public Course[] getSavedCourses() {
		Course[] courses;
		String username = new SettingsManager(context).getLoginInfo()[0];
		SharedPreferences prefs = context.getSharedPreferences(username,
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

	public long getLastUpdated() {
		String username = new SettingsManager(context).getLoginInfo()[0];
		SharedPreferences prefs = context.getSharedPreferences(username,
				Context.MODE_PRIVATE);
		return prefs.getLong("lastUpdated", System.currentTimeMillis());
	}

}
