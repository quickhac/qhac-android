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
		/*// Create a Set<String> with list items that should be selected
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean[] classesSelected = new boolean[courses.size()];
		for (int i = 0; i < classesSelected.length; i++) {
			classesSelected[i] = sharedPref.getBoolean(courses.get(i).title,
					false);
		}

		final String[] sets = new String[classesSelected.length];
		for (int i = 0; i < classesSelected.length; i++) {
			if (classesSelected[i]) {
				sets[i] = courses.get(i).title;
			}
		}

		Set<String> mySet = new HashSet<String>();
		Collections.addAll(mySet, sets);

		// Add the set
		listPreference.setValues(mySet);

		// Listen for changes, I'm not sure if this is how it's meant to work,
		// but it does :/
		/*listPreference
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object o) {

						HashSet hashSet = (HashSet) o;
						Iterator stringIterator = hashSet.iterator();
						boolean[] states = new boolean[sets.length];
						String prefString;

						while (stringIterator.hasNext()) {

							prefString = (String) stringIterator.next();
							if (prefString != null) {
								for (int i = 0; i < sets.length; i++) {
									if (prefString.equals(sets[i])) {
										states[i] = true;
										Log.d("Weighted", "something was true");
									}
								}
							}
						}

						Editor edit = PreferenceManager
								.getDefaultSharedPreferences(
										SettingsActivity.this).edit();
						for (int i = 0; i < sets.length; i++) {
							edit.putBoolean(courses.get(i).title, states[i]);

						}
						edit.commit();

						return true;
					}
				});*/
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
