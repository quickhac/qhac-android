package com.patil.quickhac;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("BackgroundGrades", "Boot receiver started, scheduling alarms");
		// Schedule alarms
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0,
				new Intent(context, AlarmReceiver.class), 0);

		// use inexact repeating which is easier on battery (system can phase
		// events and not wake at exact times)
		manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				Constants.ALARM_TRIGGER_AT_TIME, Constants.ALARM_INTERVAL,
				alarmIntent);
		Log.d("BackgroundGrades", "created alarms from boot");
	}

}
