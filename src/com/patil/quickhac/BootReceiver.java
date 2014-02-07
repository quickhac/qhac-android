package com.patil.quickhac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	Utils utils;

	@Override
	public void onReceive(Context context, Intent intent) {
		utils = new Utils(context);
		utils.makeAlarms();
	}

}
