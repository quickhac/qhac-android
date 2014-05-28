package com.patil.quickhac;

import java.util.ArrayList;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.quickhac.common.GradeParser;
import com.quickhac.common.GradeRetriever;
import com.quickhac.common.data.Course;
import com.quickhac.common.data.Cycle;
import com.quickhac.common.data.GradeValue;
import com.quickhac.common.data.Semester;
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
		String status = Constants.UNKNOWN_ERROR;
		if (school.equals(Constants.AUSTIN)) {
			district = new Austin();
			status = scrape(user, pass, id, district);
		} else if (school.equals(Constants.ROUNDROCK)) {
			district = new RoundRock();
			status = scrape(user, pass, id, district);
		}
		if (status != Constants.UNKNOWN_ERROR
				&& status != Constants.INVALID_LOGIN) {
			if (courseList != null) {
				// Check if any grades have changed, if they have, send a
				// notification
				Course[] savedCourses = saver.getSavedCourses(user, id);
				if (savedCourses != null) {
					Log.d("BackgroundGrades", "savedcourses not null");
					// Look for differences
					ArrayList<GradeChange> gradeLowers = getGradeLowers(
							savedCourses, courseList);
					ArrayList<GradeChange> gradeChanges = getGradeChanges(
							savedCourses, courseList);

					if (gradeLowers.size() > 0) {
						makeGradeLowerNotification(gradeLowers, user, id);
					} else if (gradeChanges.size() > 0) {
						makeGradeChangeNotification(gradeChanges, user, id);
					}
				}
				// save the new courselist
				saver.saveCourses(courseList, user, id);
				Log.d("BackgroundGrades", "successfully updated grades");
			}
		}
	}

	public String generateMessageText(ArrayList<GradeChange> changes) {
		String message = "";
		for (int i = 0; i < changes.size(); i++) {
			if (i != changes.size() - 1) {
				message += changes.get(i).toString() + "\n";
			} else {
				message += changes.get(i).toString();
			}
		}
		return message;
	}

	public void makeGradeLowerNotification(ArrayList<GradeChange> changes,
			String username, String id) {
		int gradeLowerTrigger = manager.getGradeLowerTrigger();
		for (int i = 0; i < changes.size(); i++) {
			GradeChange change = changes.get(i);
			Log.d("BackgroundGrades", "making grade lower notifications");
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this)
					.setSmallIcon(R.drawable.ic_notification)
					.setContentTitle("Grade dropped below " + gradeLowerTrigger);
			mBuilder.setContentText("User " + username + " - " + id);
			Uri alarmSound = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
			mBuilder.setSound(alarmSound);
			mBuilder.setAutoCancel(true);
			mBuilder.setStyle(new NotificationCompat.BigTextStyle()
					.bigText("Your grade in " + change.title
							+ " has dropped below a " + gradeLowerTrigger));
			// Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(this, MainActivity.class);

			// The stack builder object will contain an artificial back stack
			// for
			// the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out
			// of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(MainActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
					0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			Notification not = mBuilder.build();
			not.defaults = Notification.DEFAULT_ALL;
			// mId allows you to update the notification later on.
			mNotificationManager.notify((int) Math.random() * 100000, not);
		}
	}

	public void makeGradeChangeNotification(ArrayList<GradeChange> changes,
			String username, String id) {
		Log.d("BackgroundGrades", "making notifications");
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_notification).setContentTitle(
				"Grades changed in " + changes.size() + " courses");
		mBuilder.setContentText("User " + username + " - " + id);
		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
		mBuilder.setSound(alarmSound);
		mBuilder.setAutoCancel(true);
		mBuilder.setStyle(new NotificationCompat.BigTextStyle()
				.bigText(generateMessageText(changes)));
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);

		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification not = mBuilder.build();
		not.defaults = Notification.DEFAULT_ALL;
		// mId allows you to update the notification later on.
		mNotificationManager.notify((int) Math.random() * 100000, not);
	}

	public ArrayList<GradeChange> getGradeLowers(Course[] oldCourses,
			Course[] newCourses) {
		int gradeLowerTrigger = manager.getGradeLowerTrigger();
		Log.d("gradelower", String.valueOf(gradeLowerTrigger));
		ArrayList<GradeChange> gradeLowers = new ArrayList<GradeChange>();
		for (int courseIndex = 0; courseIndex < oldCourses.length; courseIndex++) {
			Course course = oldCourses[courseIndex];
			for (int semIndex = 0; semIndex < course.semesters.length; semIndex++) {
				Semester semester = course.semesters[semIndex];
				for (int cycleIndex = 0; cycleIndex < semester.cycles.length; cycleIndex++) {
					Cycle savedCycle = semester.cycles[cycleIndex];
					Cycle newCycle = newCourses[courseIndex].semesters[semIndex].cycles[cycleIndex];
					if (savedCycle.average != null && newCycle.average != null) {
						if (!savedCycle.average.toString().equals(
								newCycle.average.toString())) {

							if (savedCycle.average.type == GradeValue.TYPE_INTEGER
									&& newCycle.average.type == GradeValue.TYPE_INTEGER) {
								if (savedCycle.average.value >= gradeLowerTrigger
										&& newCycle.average.value < gradeLowerTrigger) {
									gradeLowers.add(new GradeChange(
											course.title, savedCycle.average
													.toString(),
											newCycle.average.toString(), false,
											savedCycle.average,
											newCycle.average));
								}

							}

						}
					}
				}
			}
		}
		return gradeLowers;
	}

	public ArrayList<GradeChange> getGradeChanges(Course[] oldCourses,
			Course[] newCourses) {
		ArrayList<GradeChange> gradeChanges = new ArrayList<GradeChange>();
		for (int courseIndex = 0; courseIndex < oldCourses.length; courseIndex++) {
			Course course = oldCourses[courseIndex];
			for (int semIndex = 0; semIndex < course.semesters.length; semIndex++) {
				Semester semester = course.semesters[semIndex];
				for (int cycleIndex = 0; cycleIndex < semester.cycles.length; cycleIndex++) {
					Cycle savedCycle = semester.cycles[cycleIndex];
					Cycle newCycle = newCourses[courseIndex].semesters[semIndex].cycles[cycleIndex];
					if (savedCycle.average != null && newCycle.average != null) {
						if (!savedCycle.average.toString().equals(
								newCycle.average.toString())) {
							gradeChanges.add(new GradeChange(course.title,
									savedCycle.average.toString(),
									newCycle.average.toString(), false,
									savedCycle.average, newCycle.average));
						}
					} else if (savedCycle.average == null
							&& newCycle.average != null) {
						gradeChanges.add(new GradeChange(course.title, "",
								newCycle.average.toString(), true, null,
								newCycle.average));
					} else if (savedCycle.average != null
							&& newCycle.average == null) {
						// How is this even possible grades somehow got deleted
					} else if (savedCycle.average == null
							&& newCycle.average == null) {
						// Staying nonexistent is not a change
					}
				}
			}
		}
		return gradeChanges;
	}

	public String scrape(final String username, final String password,
			final String id, GradeSpeedDistrict district) {
		retriever = new GradeRetriever(district);
		parser = new GradeParser(district);
		courseList = null;
		status = Constants.INVALID_LOGIN;
		Log.d("BackgroundGrades", "doing grade pull for " + username + " " + id);
		final XHR.ResponseHandler getAveragesHandler = new XHR.ResponseHandler() {

			@Override
			public void onSuccess(String response) {
				if (status != Constants.UNKNOWN_ERROR
						&& status != Constants.INVALID_LOGIN) {
					saver.saveLatestResponse(response, username, id);
					setStatus(Constants.SUCCESSFUL_LOGIN);
					setCourses(parser.parseAverages(response));
					Log.d("BackgroundGrades", "successful setting of courses");
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
					retriever.getAverages(getAveragesHandler);
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
				retriever.disambiguate(id, state, disambiguateHandler);
			}

			@Override
			public void onFailure(Exception e) {
				setStatus(Constants.INVALID_LOGIN);
			}

			@Override
			public void onDoesNotRequireDisambiguation(String response) {
				setStatus(Constants.SUCCESSFUL_LOGIN);
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