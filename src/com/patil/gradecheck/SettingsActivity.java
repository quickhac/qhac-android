package com.patil.gradecheck;

import java.util.ArrayList;

import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("Settings");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.layout.activity_settings);

		MultiSelectListPreference listPreference = (MultiSelectListPreference) findPreference("pref_weightedClasses");
		listPreference.setDialogTitle("Choose weighted classes");

		final ArrayList<Course> courses = MainActivity.courses;

		CharSequence[] classes = new CharSequence[courses.size()];
		for (int i = 0; i < courses.size(); i++) {
			classes[i] = courses.get(i).title;
		}
		listPreference.setEntryValues(classes);
		listPreference.setEntries(classes);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_OK, null);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	setResult(RESULT_OK, null);
			finish();
			return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}