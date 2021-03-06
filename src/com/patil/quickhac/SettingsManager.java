package com.patil.quickhac;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

public class SettingsManager {

	/*
	 * Helper class for storing, retrieving, and erasing settings. login info is
	 * saved under a SharedPreferences file titled: [username]%[id] ex:
	 * vpatil%2096730
	 */

	SharedPreferences defaultPrefs;
	Context context;

	public SettingsManager(Context context) {
		this.context = context;
		defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	private void saveLoginInfo(String user, String pass, String id,
			String district) {
		String fileName = user + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putString("user", user);
		edit.putString("pass", pass);
		edit.putString("id", id);
		edit.putString("district", district);
		edit.commit();
	}

	public String[] getLoginInfo(String selectedStudent) {
		SharedPreferences prefs = context.getSharedPreferences(selectedStudent,
				Context.MODE_PRIVATE);
		String[] credentials = new String[4];
		credentials[0] = prefs.getString("user", null);
		credentials[1] = prefs.getString("pass", null);
		credentials[2] = prefs.getString("id", null);
		credentials[3] = prefs.getString("district", null);
		return credentials;
	}
	
	public int getAlarmPollInterval() {
		return defaultPrefs.getInt("pref_gradePollingInterval", 30);
	}
	
	public int getGradeLowerTrigger() {
		return defaultPrefs.getInt("pref_showNotificationsLowGrade", 0);
	}
	
	/*
	 * Returns if the user wants to have grade highlighting
	 */
	public boolean isGradeColorHighlightEnabled() {
		return defaultPrefs.getBoolean("pref_showGradeColor", true);
	}
	
	/*
	 * Returns if the user wants to receieve notifications
	 */
	public boolean isShowNotificationPreferenceEnabled() {
		return defaultPrefs.getBoolean("pref_showNotifications", true);
	}
	
	/*
	 * Returns if it's the first time the user has seen the overview screen
	 */
	public boolean isFirstTimeOverview() {
		return defaultPrefs.getBoolean("firstOverview", true);
	}
	
	/*
	 * Returns if it's the first time the user has seen the navigation drawer
	 */
	public boolean isFirstTimeDrawer() {
		return defaultPrefs.getBoolean("firstDrawer", true);
	}
	
	/*
	 * Returns if it's the first time the user has seen the cycle page
	 */
	public boolean isFirstTimeCycle() {
		return defaultPrefs.getBoolean("firstCycle", true);
	}
	
	/*
	 * Sets the first time overview
	 */
	public void setFirstTimeOverview(boolean first) {
		Editor edit = defaultPrefs.edit();
		edit.putBoolean("firstOverview", first);
		edit.commit();
	}
	
	/*
	 * Sets the first time drawer
	 */
	public void setFirstTimeDrawer(boolean first) {
		Editor edit = defaultPrefs.edit();
		edit.putBoolean("firstDrawer", first);
		edit.commit();
	}
	
	/*
	 * Sets the first time cycle view
	 */
	public void setFirstTimeCycle(boolean first) {
		Editor edit = defaultPrefs.edit();
		edit.putBoolean("firstCycle", first);
		edit.commit();
	}
	
	
	// Saves the selected student
	public void saveSelectedStudent(String username, String id) {
		String name;
		if (username != null && id != null) {
			name = username + "%" + id;
		} else {
			name = null;
		}
		Editor edit = defaultPrefs.edit();
		edit.putString("selectedStudent", name);
		edit.commit();
	}

	// Returns the student that should be loaded on default.
	public String getSelectedStudent() {
		String selectedStudent = defaultPrefs
				.getString("selectedStudent", null);
		return selectedStudent;
	}

	public String[] getStudentList() {
		Set<String> studentListSet = defaultPrefs.getStringSet("studentList",
				null);
		String[] studentList;
		if (studentListSet != null) {
			studentList = studentListSet.toArray(new String[0]);
		} else {
			studentList = null;
		}
		return studentList;
	}
	
	/*
	 * Erases student list
	 */
	public void eraseStudentList() {
		Editor edit = defaultPrefs.edit();
		edit.remove("studentList");
		edit.commit();
	}

	/*
	 * Saves a studentlist
	 */
	public void saveStudentList(String[] studentList) {
		Set<String> studentSet;
		if (studentList != null) {
			studentSet = new HashSet<String>(Arrays.asList(studentList));
		} else {
			studentSet = null;
		}
		Editor edit = defaultPrefs.edit();
		edit.putStringSet("studentList", studentSet);
		edit.commit();
	}

	/*
	 * Adds student to studentlist (and subsequently sharedpreferences) Students
	 * saved with [username]%[id]
	 */
	public void addStudent(String username, String password, String id,
			String district) {
		// Save the student
		saveLoginInfo(username, password, id, district);
		String fileName = username + "%" + id;
		// Update and save the student list
		String[] studentList = getStudentList();
		if (studentList != null) {
			String[] newList = new String[studentList.length + 1];
			for (int i = 0; i < studentList.length; i++) {
				newList[i] = studentList[i];
			}
			newList[newList.length - 1] = username + "%" + id;
			saveStudentList(newList);
		} else {
			String[] newList = new String[] { fileName };
			saveStudentList(newList);
		}
		// Set the current student
		saveSelectedStudent(username, id);
	}

	/*
	 * Removes student from sharedpreferences.
	 */
	public void removeStudent(String username, String id) {
		String[] studentList = getStudentList();
		// erase current credentials
		eraseCredentials(username, id);
		if (studentList != null) {
			if (studentList.length > 0) {
				if (studentList.length > 1) {
					// there's another student to set as default
					String removeName = username + "%" + id;
					// remove the username thing from the studentlist by adding
					// to a
					// new
					// list
					String[] updatedList = new String[studentList.length - 1];
					int d = 0;
					for (int i = 0; i < studentList.length; i++) {
						if (!removeName.equals(studentList[i])) {
							updatedList[d] = studentList[i];
							d++;
						}
					}
					saveStudentList(updatedList);
					// set another student as default
					String newName = updatedList[0];
					saveSelectedStudent(newName.split("%")[0],
							newName.split("%")[1]);
				} else {
					// There's no other students in the studentlist, save
					// everything
					// as null so we'll be prompted to login next time.
					saveStudentList(null);
					saveSelectedStudent(null, null);
				}

			} else {
				// Nothing to remove, no idea when this would happen

			}
		}
	}

	public void eraseCredentials(String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putString("user", "");
		edit.putString("pass", "");
		edit.putString("id", "");
		edit.putString("district", "");
		edit.commit();
	}
	
	public int getSavedVersion() {
		return defaultPrefs.getInt("savedVersion", 0);
	}

	public boolean justUpdated() {
		// Get the current version of the app
		PackageInfo pInfo = null;
		try {
			pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pInfo != null) {
			int currentVersion = pInfo.versionCode;
			int savedVersion = defaultPrefs.getInt("savedVersion", 0);
			// If the version saved is different from the version we currently
			// have
			if (savedVersion != currentVersion) {
				// we just updated
				return true;
			}
		}
		return false;
	}

	public void saveCurrentVersion(int version) {
		Editor edit = defaultPrefs.edit();
		edit.putInt("savedVersion", version);
		edit.commit();
	}

	public void saveLastLogin(String username, String id, long millis) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putLong("lastLogin", millis);
		edit.commit();
	}

	public long getLastLogin(String username, String id) {
		String fileName = username + "%" + id;
		SharedPreferences prefs = context.getSharedPreferences(fileName,
				Context.MODE_PRIVATE);
		return prefs.getLong("lastLogin", 0);
	}
}
