package com.patil.quickhac;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	/*
	 * * Schedules a periodic alarm to periodically notify the user of new grades.
	 * These alarms are wiped when the device reboots, which is the reason for
	 * the BootReceiver class which resets alarms.
	 */
	public void makeAlarms() {
		Log.d("BackgroundGrades", "Boot receiver started, scheduling alarms");
		// Schedule alarms
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0,
				new Intent(context, AlarmReceiver.class), 0);

		// Cancel any existing alarms
		manager.cancel(alarmIntent);

		// Get the polling interval
		int intervalMinutes = settingsManager.getAlarmPollInterval();
		int interval = intervalMinutes * 60000;

		// use inexact repeating which is easier on battery (system can
		// phase
		// events and not wake at exact times)
		manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				Constants.GRADE_PULL_TRIGGER_AT_TIME, interval, alarmIntent);
	}
}
