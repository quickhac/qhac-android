package com.patil.quickhac;

import com.patil.quickhac.R;
import com.quickhac.common.data.Course;

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

		MultiSelectListPreference weightedPreference = (MultiSelectListPreference) findPreference("pref_weightedClasses");
		
		MultiSelectListPreference excludedPreference = (MultiSelectListPreference) findPreference("pref_excludedClasses");

		Course[] courses = MainActivity.courses;

		CharSequence[] classes = new CharSequence[courses.length];
		for (int i = 0; i < courses.length; i++) {
			classes[i] = courses[i].title;
		}
		weightedPreference.setEntryValues(classes);
		weightedPreference.setEntries(classes);
		excludedPreference.setEntryValues(classes);
		excludedPreference.setEntries(classes);
		
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