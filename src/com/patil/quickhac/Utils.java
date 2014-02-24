package com.patil.quickhac;

import com.quickhac.common.data.Course;
import com.quickhac.common.data.Cycle;
import com.quickhac.common.data.GradeValue;
import com.quickhac.common.data.Semester;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

public class Utils {
	Context context;
	SettingsManager settingsManager;

	public Utils(Context context) {
		this.context = context;
		settingsManager = new SettingsManager(context);
	}

	/*
	 * Helper method to check if internet is available
	 */
	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/*
	 * * Schedules a periodic alarm to periodically notify the user of new
	 * grades. These alarms are wiped when the device reboots, which is the
	 * reason for the BootReceiver class which resets alarms.
	 */
	public void makeAlarms() {
		// Schedule alarms
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0,
				new Intent(context, AlarmReceiver.class), 0);

		// Cancel any existing alarms
		manager.cancel(alarmIntent);

		// Get the polling interval
		int intervalMinutes = settingsManager.getAlarmPollInterval();
		int interval = (intervalMinutes * 60000) + 1000;

		// use inexact repeating which is easier on battery (system can
		// phase
		// events and not wake at exact times)
		manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				Constants.GRADE_PULL_TRIGGER_AT_TIME, interval, alarmIntent);
	}

	// Helper method to make sure there aren't any letter grades
	public boolean isLetterGradesInCourses(Course[] courses) {
		for (int courseIndex = 0; courseIndex < courses.length; courseIndex++) {
			Course course = courses[courseIndex];
			for (int semesterIndex = 0; semesterIndex < course.semesters.length; semesterIndex++) {
				Semester semester = course.semesters[semesterIndex];
				if (semester != null) {
					for (int cycleIndex = 0; cycleIndex < semester.cycles.length; cycleIndex++) {
						Cycle cycle = semester.cycles[cycleIndex];
						if (cycle != null && cycle.average != null) {
							if (cycle.average.type == GradeValue.TYPE_LETTER) {
								return true;
							}
						}
					}
					if (semester.average != null) {
						if (semester.average.type == GradeValue.TYPE_LETTER) {
							return true;
						}
					}
					if (semester.examGrade != null) {
						if (semester.examGrade.type == GradeValue.TYPE_LETTER) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	

	/*
	 * Checks if the activity contains a flag that says to refresh
	 */
	public boolean checkIfStartFromRefresh(Bundle extras) {
		if (extras != null) {
			return extras.getBoolean(Constants.REFRESH_INTENT);
		} else {
			return false;
		}
	}
}
