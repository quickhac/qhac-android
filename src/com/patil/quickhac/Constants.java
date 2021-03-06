package com.patil.quickhac;

import android.app.AlarmManager;
import android.os.SystemClock;

// Has list of many constants used consistently throughout the app.
public class Constants {
	
	// Number of "header" sections that aren't courses (ex. "Overview", "Latest Grades", etc)
	public static final int HEADER_SECTIONS = 1;

	// How often new grades are pulled (defaults to 1 hour)
	public static final long GRADE_PULL_INTERVAL = AlarmManager.INTERVAL_HALF_HOUR;

	// Time delay until alarm first goes off
	public static final long GRADE_PULL_TRIGGER_AT_TIME = SystemClock
			.elapsedRealtime() + 30000;

	// Length of time that saved grades don't need to be refreshed
	public static final long GRADE_LENGTH = AlarmManager.INTERVAL_HALF_HOUR;

	// Length of time that it is assumed login timeout takes (10 minutes)
	public static final long LOGIN_TIMEOUT = 600000;

	// String that represents an invalid login
	public static final String INVALID_LOGIN = "INVALID_LOGIN";

	// String that represents an unknown error
	public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

	// String that represents successful login
	public static final String SUCCESSFUL_LOGIN = "SUCCESSFUL_LOGIN";

	// Data that says if the intent is to refresh grades
	public static final String REFRESH_INTENT = "refresh";

	// Names of districts
	public static final String AUSTIN = "Austin";
	public static final String ROUNDROCK = "RoundRock";

}
