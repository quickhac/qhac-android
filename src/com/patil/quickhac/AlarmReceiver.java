package com.patil.quickhac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("BackgroundGrades", "AlarmReceiver invoked, starting scrapeservice");
		context.stopService(new Intent(context, ScrapeService.class));
		context.startService(new Intent(context, ScrapeService.class));
	}

}