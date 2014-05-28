package com.patil.quickhac;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class AndroidApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		 Parse.initialize(this, "zdfmsikcIagv4CYaNCmvLTB9SNiyaDP9N8niPmsq", "DElV0JAfs6o8Mijq1A1cAl4siwiGugQKVfHfpLPD");
			PushService.setDefaultPushCallback(this, MainActivity.class);
			ParseInstallation.getCurrentInstallation().saveInBackground();
	}
}
