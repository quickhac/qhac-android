package com.patil.quickhac;

import java.util.Random;

import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.quickhac.common.data.Course;

public class SettingsActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
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

		PreferenceCategory notificationCategory = (PreferenceCategory) findPreference("category_notifications");
		
		// Add the slider dialog preference for polling interval
		SeekBarPreference sb = new SeekBarPreference(this, null);
		sb.setTitle("Choose polling interval");
		sb.setKey("pref_gradePollingInterval");
		notificationCategory.addPreference(sb);
		
		// Set the inspirational quote
		Preference quotePref = findPreference("pref_quote_pref");
		String[] quoteArray = getResources().getStringArray(R.array.quotes_array);
		Random random = new Random();
		int randIndex = random.nextInt(quoteArray.length);
		quotePref.setSummary(quoteArray[randIndex]);
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