package com.patil.quickhac;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.quickhac.common.GradeParser;
import com.quickhac.common.GradeRetriever;
import com.quickhac.common.data.Course;
import com.quickhac.common.data.StudentInfo;
import com.quickhac.common.districts.GradeSpeedDistrict;
import com.quickhac.common.districts.impl.Austin;
import com.quickhac.common.districts.impl.RoundRock;
import com.quickhac.common.http.ASPNETPageState;
import com.quickhac.common.http.XHR;

// Background service that automatically fetches latest grades
public class ScrapeService extends IntentService {
	Utils utils;

	public ScrapeService() {
		super("Grade Fetch Service");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("BackgroundGrades",
				"ScrapeService invoked, will check for new grades");
		Log.d("BackgroundGrades", "starting grade loading");
		manager = new SettingsManager(this);
		saver = new CourseSaver(this);
		utils = new Utils(this);
		// if there's internet available, load new course data
		if (utils.isNetworkAvailable()) {
			updateCourses();
		}
	}

	SettingsManager manager;
	CourseSaver saver;
	String status;
	GradeRetriever retriever;
	GradeParser parser;
	Course[] courseList;

	public void updateCourses() {
		String[] studentList = manager.getStudentList();
		if (studentList != null && studentList.length > 0) {
			// for each student in studentlist, fetch latest grades
			for (int i = 0; i < studentList.length; i++) {
				String id = studentList[i];
				String[] credentials = manager.getLoginInfo(id);
				getNewCourseInfo(credentials);
			}
		}
	}

	public void getNewCourseInfo(String[] credentials) {
		GradeSpeedDistrict district;
		String user = credentials[0];
		String pass = credentials[1];
		String id = credentials[2];
		String school = credentials[3];
		String status = "UNKNOWN_ERROR";
		if (school.equals("Austin")) {
			district = new Austin();
			status = scrape(user, pass, id, district);
		} else if (school.equals("RoundRock")) {
			district = new RoundRock();
			status = scrape(user, pass, id, district);
		}
		if (status != "UNKNOWN_ERROR" && status != "INVALID_LOGIN") {
			if (courseList != null) {
				// save the courselist
				saver.saveCourses(courseList, user, id);
				Log.d("BackgroundGrades", "successfully updated grades");
			}
		}
	}

	public String scrape(final String username, final String password,
			final String id, GradeSpeedDistrict district) {
		retriever = new GradeRetriever(district);
		parser = new GradeParser(district);
		courseList = null;
		status = "INVALID_LOGIN";
		Log.d("BackgroundGrades", "doing grade pull for " + username + " " + id);
		final XHR.ResponseHandler getAveragesHandler = new XHR.ResponseHandler() {

			@Override
			public void onSuccess(String response) {
				if (status != "UNKNOWN_ERROR" && status != "INVALID_LOGIN") {
					saver.saveLatestResponse(status, username, id);
					setStatus("SUCCESS");
					setCourses(parser.parseAverages(response));
					Log.d("BackgroundGrades", "successful setting of courses");
				}
			}

			@Override
			public void onFailure(Exception e) {
				setStatus("UNKNOWN_ERROR");
			}
		};

		final XHR.ResponseHandler disambiguateHandler = new XHR.ResponseHandler() {

			@Override
			public void onSuccess(String response) {
				if (status != "UNKNOWN_ERROR" && status != "INVALID_LOGIN") {
					setStatus("SUCCESS");
					retriever.getAverages(getAveragesHandler);
				}
			}

			@Override
			public void onFailure(Exception e) {
				setStatus("UNKNOWN_ERROR");
			}
		};

		final GradeRetriever.LoginResponseHandler loginHandler = new GradeRetriever.LoginResponseHandler() {

			@Override
			public void onRequiresDisambiguation(String response,
					StudentInfo[] students, ASPNETPageState state) {
				setStatus("SUCCESS");
				retriever.disambiguate(id, state, disambiguateHandler);
			}

			@Override
			public void onFailure(Exception e) {
				setStatus("INVALID_LOGIN");
			}

			@Override
			public void onDoesNotRequireDisambiguation(String response) {
				setStatus("SUCCESS");
				retriever.getAverages(getAveragesHandler);
			}
		};

		retriever.login(username, password, loginHandler);
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setCourses(Course[] courses) {
		courseList = courses;
	}

}
