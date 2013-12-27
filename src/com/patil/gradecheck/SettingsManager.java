package com.patil.gradecheck;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SettingsManager {
	
	/*
	 * Helper class for storing, retrieving, and erasing settings.
	 */
	
	Context context;
	public SettingsManager(Context context) {
		this.context = context;
	}
	
	public void saveLoginInfo(String user, String pass, String id, String district) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		edit.putString("user", user);
		edit.putString("pass", pass);
		edit.putString("id", id);
		edit.putString("district", district);
		edit.commit();
	}
	
	public String[] getLoginInfo() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String[] credentials = new String[4];
		String user = prefs.getString("user", "");
		String pass = prefs.getString("pass", "");
		String district = prefs.getString("district", "");
		String id = prefs.getString("id", "");
		credentials[0] = user;
		credentials[1] = pass;
		credentials[2] = id;
		credentials[3] = district;
		return credentials;
	}
	
	public void eraseLoginInfo() {
		saveLoginInfo("", "", "", "");
	}
}
