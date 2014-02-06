package com.patil.quickhac;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	SettingsManager settingsManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		settingsManager = new SettingsManager(context);
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
