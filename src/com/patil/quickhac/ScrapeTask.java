package com.patil.quickhac;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.quickhac.common.GradeParser;
import com.quickhac.common.GradeRetriever;
import com.quickhac.common.data.StudentInfo;
import com.quickhac.common.districts.GradeSpeedDistrict;
import com.quickhac.common.districts.impl.Austin;
import com.quickhac.common.districts.impl.RoundRock;
import com.quickhac.common.http.ASPNETPageState;
import com.quickhac.common.http.XHR;

public class ScrapeTask extends AsyncTask<String, Void, String> {

	ProgressDialog dialog;
	Context context;
	String status;
	String username;
	String password;
	String id;
	String school;
	boolean firstLog;
	boolean showDialog;
	MainActivity activity;

	public ScrapeTask(Context context, MainActivity activity, boolean showDialog) {
		this.context = context;
		this.showDialog = showDialog;
		this.activity = activity;
	}

	/*
	 * Scrapes ParentConnection remotely.
	 * 
	 * @param[0] The username to log in with.
	 * 
	 * @param[1] The password to log in with.
	 * 
	 * @param[2] The student id.
	 * 
	 * @param[3] The id of the school logging in with. "Austin" or
	 * "RoundRock".
	 * 
	 * @return A String of the webpage HTML.
	 */
	protected String doInBackground(String... information) {
		username = information[0];
		password = information[1];
		id = information[2];
		school = information[3];
		String firstLogin = information[4];
		firstLog = (firstLogin.equals("yes")) ? true : false;

		String status = Constants.UNKNOWN_ERROR;
		if (school.equals(Constants.AUSTIN)) {
			activity.gradeSpeedDistrict = new Austin();
		} else if (school.equals(Constants.ROUNDROCK)) {
			activity.gradeSpeedDistrict = new RoundRock();
		}
		status = scrape(username, password, id, activity.gradeSpeedDistrict);
		return status;
	}

	protected void onPostExecute(String response) {
		handleResponse(response);
	}

	public void handleResponse(String response) {
		if (response.equals(Constants.UNKNOWN_ERROR)) {
			if (showDialog) {
				dialog.dismiss();
			}
			activity.setRefreshActionButtonState(false);
			// Error unknown
			Toast.makeText(
					context,
					"Something went wrong. GradeSpeed servers are probably down. Try relogging or refreshing.",
					Toast.LENGTH_SHORT).show();
			activity.signInButton.setVisibility(View.VISIBLE);
		} else if (response.equals(Constants.INVALID_LOGIN)) {
			if (showDialog) {
				dialog.dismiss();
			}

			activity.setRefreshActionButtonState(false);
			// Wrong credentials sent
			Toast.makeText(
					context,
					"Invalid username, password, student ID, or school district.",
					Toast.LENGTH_SHORT).show();
			// Only show signinbutton if there's no other student to show
			// grades for
			if (activity.studentList != null) {
				if (!(activity.studentList.length > 0)) {
					activity.signInButton.setVisibility(View.VISIBLE);
				}
			} else {
				activity.signInButton.setVisibility(View.VISIBLE);
			}
			activity.startLogin();
		} else if (!firstLog) {
			activity.displayLastUpdateTime(System.currentTimeMillis() - 10);
			activity.saver.saveCourses(activity.courses, activity.currentUsername, activity.currentId);

			activity.makeCourseCards(true);
			if (showDialog) {
				dialog.dismiss();
			}

			activity.setupActionBar();
			activity.setRefreshActionButtonState(false);
		} else {
			// first login
			activity.settingsManager.addStudent(username, password, id, school);
			activity.utils.makeAlarms();
			if (showDialog) {
				dialog.dismiss();
			}
			activity.restartActivity();
		}

	}

	protected void onPreExecute() {
		super.onPreExecute();
		if (showDialog) {
			dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage("Loading Grades...");
			dialog.show();
		}

		activity.setRefreshActionButtonState(true);

	}

	public String scrape(final String username, final String password,
			final String id, GradeSpeedDistrict district) {
		activity.retriever = new GradeRetriever(district);
		activity.parser = new GradeParser(district);
		status = Constants.INVALID_LOGIN;

		final XHR.ResponseHandler getAveragesHandler = new XHR.ResponseHandler() {

			@Override
			public void onSuccess(String response) {
				if (status != Constants.UNKNOWN_ERROR
						&& status != Constants.INVALID_LOGIN) {
					activity.loggedIn = true;
					activity.saver.saveLatestResponse(response, username, id);
					activity.courses = activity.parser.parseAverages(response);
					// Set up the classGradesList with unintialized
					// class grades
					for (int i = 0; i < activity.courses.length; i++) {
						activity.classGradesList.add(null);
					}
					activity.settingsManager.saveLastLogin(activity.currentUsername,
							activity.currentId, System.currentTimeMillis());
				}
			}

			@Override
			public void onFailure(Exception e) {
				setStatus(Constants.UNKNOWN_ERROR);
			}
		};

		final XHR.ResponseHandler disambiguateHandler = new XHR.ResponseHandler() {

			@Override
			public void onSuccess(String response) {
				if (status != Constants.UNKNOWN_ERROR
						&& status != Constants.INVALID_LOGIN) {
					setStatus(Constants.SUCCESSFUL_LOGIN);
					activity.retriever.getAverages(getAveragesHandler);
				}
			}

			@Override
			public void onFailure(Exception e) {
				setStatus(Constants.UNKNOWN_ERROR);
			}
		};

		final GradeRetriever.LoginResponseHandler loginHandler = new GradeRetriever.LoginResponseHandler() {

			@Override
			public void onRequiresDisambiguation(String response,
					StudentInfo[] students, ASPNETPageState state) {
				setStatus(Constants.SUCCESSFUL_LOGIN);
				activity.retriever.disambiguate(id, state, disambiguateHandler);
			}

			@Override
			public void onFailure(Exception e) {
				setStatus(Constants.INVALID_LOGIN);
			}

			@Override
			public void onDoesNotRequireDisambiguation(String response) {
				setStatus(Constants.SUCCESSFUL_LOGIN);
				activity.retriever.getAverages(getAveragesHandler);
			}
		};

		activity.retriever.login(username, password, loginHandler);
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
