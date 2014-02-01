package com.patil.quickhac;

import android.app.AlarmManager;
import android.os.SystemClock;

public class Constants {
	// Has list of many constants used consistently throughout the app.
	// How often new grades are pulled (defaults to 1 hour)
	public static final long ALARM_INTERVAL = 10000;
	// Time delay until alarm first goes off
	public static final long ALARM_TRIGGER_AT_TIME = SystemClock.elapsedRealtime() + 30000;
	
}
