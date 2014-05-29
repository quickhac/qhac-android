package com.patil.quickhac;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.ocpsoft.prettytime.PrettyTime;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.ActionViewTarget;
import com.fima.cardsui.objects.Card;
import com.fima.cardsui.views.CardUI;
import com.parse.ParseInstallation;
import com.parse.PushService;
import com.quickhac.common.GPACalc;
import com.quickhac.common.GradeCalc;
import com.quickhac.common.GradeParser;
import com.quickhac.common.GradeRetriever;
import com.quickhac.common.data.Category;
import com.quickhac.common.data.ClassGrades;
import com.quickhac.common.data.Course;
import com.quickhac.common.data.Semester;
import com.quickhac.common.data.StudentInfo;
import com.quickhac.common.districts.GradeSpeedDistrict;
import com.quickhac.common.districts.impl.Austin;
import com.quickhac.common.districts.impl.RoundRock;
import com.quickhac.common.http.ASPNETPageState;
import com.quickhac.common.http.XHR;
import com.quickhac.common.util.Numeric;

public class MainActivity extends FragmentActivity implements
		OnItemSelectedListener {

	// Courses references throughout the class
	public static Course[] courses;
	// ClassGrades which contain assignments
	static ArrayList<ArrayList<ClassGrades>> classGradesList;

	// ActionBar menu
	Menu menu;
	// List view inside navigation drawer
	ListView drawerList;
	// Layout for navigation drawer
	DrawerLayout drawerLayout;
	// Makes sure drawer closes smoothly
	Handler drawerHandler = new Handler();
	// Drawer toggle button in upper left corner
	ActionBarDrawerToggle drawerToggle;
	// The last position the navigation drawer was set to
	int drawerPosition;
	// The last title the navigation drawer was set to
	String drawerTitle;

	// For signing in. Only shows up when no user is logged in
	Button signInButton;
	// Shows "Updated ___ seconds ago", etc
	TextView lastUpdatedText;
	// The drawer object
	LinearLayout drawer;
	// Allows for selecting between different accounts
	Spinner studentSpinner;

	// ListView containing all course cards
	CardUI courseCardListView;

	// Qhac-common networking stuff
	GradeParser parser;
	GradeRetriever retriever;
	GradeSpeedDistrict gradeSpeedDistrict;

	// Helper utility objects
	SettingsManager settingsManager;
	CourseSaver saver;
	ColorGenerator colorGenerator;
	Utils utils;
	SharedPreferences defaultPrefs;

	// Current credentials. Set when user logs in or app loads
	String currentUsername;
	String currentId;
	String currentDistrict;

	// List of student accounts
	String[] studentList;

	int addStudentIndex;

	// Avoids firing off onItemSelected() when app launches, avoiding infinite
	// loops
	int initializationStudentSpinnerCounter;
	// The position of the currently selected student
	int currentStudentSelectedPosition;

	// Makes sure grades aren't loaded repeatedly
	boolean alreadyLoadedGrades = false;

	// If user is currently logged in
	boolean loggedIn;

	// If started from refresh
	boolean startedFromRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setTitle(R.string.action_overview);
		createUtilities();
		initializeVariables();
		assignViewIds();
		makeDrawer();
	}

	@Override
	public void setTitle(CharSequence title) {
		drawerTitle = (String) title;
		getActionBar().setTitle(title);
	}

	@Override
	public void onItemSelected(AdapterView<?> parentView,
			View selectedItemView, int position, long id) {
		if (initializationStudentSpinnerCounter > 0) {
			if (position == addStudentIndex) {
				drawerLayout.closeDrawer(drawer);
				startLogin();
			} else {
				settingsManager.saveSelectedStudent(
						studentList[position].split("%")[0],
						studentList[position].split("%")[1]);
				restartActivity();
			}
		}
		initializationStudentSpinnerCounter++;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar drawer toggle has been tapped
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case (R.id.action_refresh):
			restartActivityForRefresh();
			break;
		case (R.id.action_signout):
			showSignOutDialog();
			break;
		case (R.id.action_about):
			AboutDialog about = new AboutDialog(this);
			about.setTitle("QuickHAC for Android");
			about.show();
			break;
		case R.id.action_addStudent:
			startLogin();
			break;
		case (R.id.action_settings):
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, 1);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/*
	 * Displays the dialog to sign out. If user selects yes, the student is
	 * removed.
	 */
	public void showSignOutDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.dialog_signout_title);
		dialog.setMessage(R.string.dialog_signout_message);
		dialog.setPositiveButton(R.string.dialog_yes,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						settingsManager.removeStudent(currentUsername,
								currentId);
						dialog.dismiss();
						restartActivity();
					}
				});
		dialog.setNegativeButton(R.string.dialog_no,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		dialog.create().show();
	}

	@Override
	public void onBackPressed() {
		if (drawerPosition != 0) {
			// Go to Overview fragment
			selectItem(0);
		} else {
			// Close app
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Check if activity was opened from settings
		if (resultCode == RESULT_OK) {
			// Make alarms with new polling interval settings
			utils.makeAlarms();
			// Refresh grades in case grade settings changed
			restartActivityForRefresh();
		}
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = drawerLayout.isDrawerOpen(drawer);
		if (drawerOpen) {
			// TODO: Hide actionbar items

			// First time tutorial
			if (settingsManager.isFirstTimeDrawer()) {
				if (studentList != null) {
					if (studentList.length > 0) {
						ActionViewTarget target = new ActionViewTarget(this,
								ActionViewTarget.Type.HOME);
						ShowcaseView sv = ShowcaseView.insertShowcaseView(
								target, this, R.string.showcase_student,
								R.string.showcase_student_description);
						sv.setShowcaseIndicatorScale(1.75f);
						settingsManager.setFirstTimeDrawer(false);
					}
				}
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		// SettingsManager initialized here instead of makeUtilities() because
		// it's needed to check for wiping credentials/checking first time
		// overview below
		settingsManager = new SettingsManager(this);

		// Erase credentials if the current version is much older than last
		// saved version to avoid data storage failure when jumping updates
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		int currentVersion = 0;
		if (pInfo != null) {
			currentVersion = pInfo.versionCode;
		}
		if (currentVersion - settingsManager.getSavedVersion() > 0) {
			Log.d("Update", "Wiping data");
			studentList = settingsManager.getStudentList();
			if (studentList != null) {
				// Erase student info and saved grades
				for (int i = 0; i < studentList.length; i++) {
					String user = studentList[i].split("%")[0];
					String id = studentList[i].split("%")[1];
					settingsManager.eraseCredentials(user, id);
					saver.eraseCourses(user, id);
					saver.eraseWeightedGPA(user, id);
					saver.eraseUnweightedGPA(user, id);
				}
				// Erase student list
				settingsManager.eraseStudentList();
			}
		}

		if (pInfo != null) {
			settingsManager.saveCurrentVersion(currentVersion);
		}
		if (!alreadyLoadedGrades) {
			startDisplayingGrades();
			alreadyLoadedGrades = true;
		}
		if (settingsManager.isFirstTimeOverview()) {
			if (studentList != null) {
				if (studentList.length > 0) {
					ActionViewTarget target = new ActionViewTarget(this,
							ActionViewTarget.Type.TITLE);
					ShowcaseView sv = ShowcaseView.insertShowcaseView(target,
							this, R.string.showcase_navigation,
							R.string.showcase_navigation_description);
					settingsManager.setFirstTimeOverview(false);
				}
			}
		}
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	public void startDisplayingGrades() {
		String selectedStudent = settingsManager.getSelectedStudent();
		String[] students = settingsManager.getStudentList();
		// Check for if there's a student to load
		if (selectedStudent != null && students != null) {
			studentList = students;
			String[] credentials = settingsManager
					.getLoginInfo(selectedStudent);

			// Check for valid login info
			if (credentials[0] != null && credentials[1] != null
					&& credentials[2] != null && credentials[3] != null) {
				currentUsername = credentials[0];
				currentId = credentials[2];
				currentDistrict = credentials[3];

				// See if there are any saved courses
				Course[] savedCourses = saver.getSavedCourses(credentials[0],
						credentials[2]);
				if (utils.isNetworkAvailable() && savedCourses != null) {
					long timeSinceLastUpdated = System.currentTimeMillis()
							- saver.getLastUpdated(credentials[0],
									credentials[2]);
					// Check to see if it's been less than 30 min since grades
					// were loaded. If it has AND we haven't started from a
					// refresh, don't bother updating grades. If
					// either isn't true, get new grades.
					if (timeSinceLastUpdated < Constants.GRADE_LENGTH
							&& !startedFromRefresh) {
						// Don't bother getting new grades
						displayLastUpdateTime(saver.getLastUpdated(
								credentials[0], credentials[2]));
						handleOnlineSavedCourses(savedCourses);
					} else {
						displayUpdatingTime();
						// We have saved courses, show those until grades are
						// loaded
						handleOnlineSavedCourses(savedCourses);
						// We have internet, load grades
						executeScrapeTask(credentials[0], credentials[1],
								credentials[2], credentials[3], "no", false);
					}
				} else if (utils.isNetworkAvailable() && savedCourses == null) {
					// We have internet, load grades
					executeScrapeTask(credentials[0], credentials[1],
							credentials[2], credentials[3], "no", true);
				} else {
					if (savedCourses != null) {
						// We have saved courses
						handleOfflineCourses(savedCourses);
					} else {
						// No saved courses and no way to load them
						Toast.makeText(
								this,
								"You must be connected to the internet to load grades for the first time.",
								Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				// Invalid login info, delete the student
				settingsManager.removeStudent(selectedStudent.split("%")[0],
						selectedStudent.split("%")[1]);
				restartActivity();
			}
			// Create the spinner in the nav drawer that has the list of
			// students
			makeStudentSpinner();
		} else {
			// Prompt for login
			currentUsername = "";
			currentId = "";
			currentDistrict = "";
			signInButton.setVisibility(View.VISIBLE);
			startLogin();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (settingsManager != null) {
			long lastLogin = settingsManager.getLastLogin(currentUsername,
					currentId);
			if (System.currentTimeMillis() - lastLogin > Constants.LOGIN_TIMEOUT) {
				// Login has probably timed out, so set loggedIn to false so
				// that we'll log in again
				loggedIn = false;
			}
		}
	}

	private void initializeVariables() {
		defaultPrefs = PreferenceManager
				.getDefaultSharedPreferences(MainActivity.this);
		loggedIn = false;
		drawerTitle = "Overview";
		initializationStudentSpinnerCounter = 0;
		classGradesList = new ArrayList<ArrayList<ClassGrades>>();
		startedFromRefresh = utils.checkIfStartFromRefresh(getIntent()
				.getExtras());
	}

	private void createUtilities() {
		utils = new Utils(this);
		colorGenerator = new ColorGenerator(this);
		saver = new CourseSaver(this);
	}

	private void assignViewIds() {
		signInButton = (Button) findViewById(R.id.button_signin);
		lastUpdatedText = (TextView) findViewById(R.id.lastUpdate_text);
		studentSpinner = (Spinner) findViewById(R.id.spinner_student);
		drawer = (LinearLayout) findViewById(R.id.menu);
	}

	private void handleOnlineSavedCourses(Course[] savedCourses) {
		String[] credentials = settingsManager.getLoginInfo(currentUsername
				+ "%" + currentId);
		GradeSpeedDistrict district = null;
		if (credentials[3].equals(Constants.AUSTIN)) {
			district = new Austin();
		} else if (credentials[3].equals(Constants.ROUNDROCK)) {
			district = new RoundRock();
		}
		retriever = new GradeRetriever(district);
		parser = new GradeParser(district);
		courses = savedCourses;
		for (int i = 0; i < courses.length; i++) {
			classGradesList.add(null);
		}
		setupActionBar();
		makeCourseCards(false);
	}

	private void handleOfflineCourses(Course[] savedCourses) {
		courses = savedCourses;
		long lastUpdateMillis = saver
				.getLastUpdated(currentUsername, currentId);
		displayLastUpdateTime(lastUpdateMillis);
		Toast.makeText(this,
				"No internet connection detected. Displaying saved grades.",
				Toast.LENGTH_SHORT).show();
		// Set up the classGradesList with unintialized
		// class grades
		for (int i = 0; i < courses.length; i++) {
			classGradesList.add(null);
		}
		setupActionBar();
		makeCourseCards(false);
	}

	private void displayLastUpdateTime(long lastUpdateMillis) {
		String toDisplay = "Updated ";
		PrettyTime p = new PrettyTime();
		toDisplay += p.format(new Date(lastUpdateMillis));
		lastUpdatedText.setText(toDisplay);
		lastUpdatedText.setTextColor(Color.BLACK);
	}

	private void displayUpdatingTime() {
		String toDisplay = "Loading new grades...";
		lastUpdatedText.setText(toDisplay);
		lastUpdatedText.setTextColor(getResources().getColor(
				R.color.pomegranate));
	}

	/*
	 * Starts login by showing a login dialog. Saves credentials and loads
	 * grades when user logs in.
	 */
	private void startLogin() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.dialog_login, null);
		final EditText userName = (EditText) textEntryView
				.findViewById(R.id.user);
		final EditText password = (EditText) textEntryView
				.findViewById(R.id.pass);
		final EditText studentId = (EditText) textEntryView
				.findViewById(R.id.id);
		final Spinner district = (Spinner) textEntryView
				.findViewById(R.id.district);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.districts_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		district.setAdapter(adapter);

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.dialog_login)
				.setCancelable(false)
				.setView(textEntryView)
				.setPositiveButton(R.string.dialog_login,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (userName.getText().length() > 0
										&& password.getText().length() > 0
										&& studentId.getText().length() > 0
										&& district.getSelectedItem() != null) {
									String distr = "";
									if (district.getSelectedItem().toString()
											.equals("AISD")) {
										distr = Constants.AUSTIN;
									} else if (district.getSelectedItem()
											.toString().equals("RRISD")) {
										distr = Constants.ROUNDROCK;
									}
									executeScrapeTask(userName.getText()
											.toString(), password.getText()
											.toString(), studentId.getText()
											.toString(), distr, "yes", true);

								} else {
									Toast.makeText(MainActivity.this,
											"Please fill out all info.",
											Toast.LENGTH_SHORT).show();
								}
							}
						})
				.setNegativeButton(R.string.dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								initializationStudentSpinnerCounter = 0;
								studentSpinner
										.setSelection(currentStudentSelectedPosition);
							}
						}).setMessage(R.string.dialog_login_message);
		alert.show();
	}

	/*
	 * Starts the login and scrape task.
	 * 
	 * @param user - the username
	 * 
	 * @param pass - the password
	 * 
	 * @param id - the student id
	 * 
	 * @param distr - the school district
	 * 
	 * @param firstLogOn - if it's the first time the user is logging in
	 * 
	 * @param asyncRefresh - if we need to show a dialog or if we can refresh
	 * the UI asynchronously
	 */
	private void executeScrapeTask(String user, String pass, String id,
			String distr, String firstLogOn, boolean asyncRefresh) {
		new ScrapeTask(this, asyncRefresh).execute(new String[] { user, pass,
				id, distr, firstLogOn });

	}

	private void loadCourseInfo(int course, String school) {
		if (utils.isNetworkAvailable()) {
			new CycleScrapeTask(this).execute(new String[] { String
					.valueOf(course) });
		} else {
			Toast.makeText(this, "You need internet to view assignments.",
					Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * Makes descriptions and loads in cards to display.
	 */
	private void makeCourseCards(final boolean online) {
		courseCardListView = (CardUI) findViewById(R.id.cardsview);

		courseCardListView.clearCards();
		courseCardListView.setSwipeable(false);
		courseCardListView.setPersistentDrawingCache(3);

		// Add GPA card if user has enabled
		boolean gpaPref = defaultPrefs.getBoolean("pref_showGPA", true);
		if (gpaPref) {
			makeGPACard(online);
		}

		for (int k = 0; k < courses.length; k++) {
			Course course = courses[k];
			String color = colorGenerator.getCardColor(k);
			final Card courseCard = new CourseCard(course.title, "", color,
					"#787878", false, true);
			courseCard.setData(course);

			// Set onClick
			courseCard.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = getCoursePositionFromTitle(courses,
							((CourseCard) courseCard).getCardTitle());
					// Find appropriate position
					selectItem(pos + Constants.HEADER_SECTIONS);
				}
			});
			courseCardListView.addCard(courseCard);
		}
	}

	/*
	 * Gets the position of the course from the given title and list of courses.
	 */
	private int getCoursePositionFromTitle(Course[] courses, String title) {
		int pos = 0;
		for (int e = 0; e < courses.length; e++) {
			if (courses[e].title.equals(title)) {
				pos = e;
			}
		}
		return pos;
	}

	private void makeGPACard(boolean online) {
		// make sure there aren't any letter grades for the gpa
		if (!utils.isLetterGradesInCourses(courses)) {
			double[] gpa = getGPA(online);
			String description = String.valueOf(gpa[0]) + " / "
					+ String.valueOf(gpa[1]);
			Card GPACard = new NoGradesCard("GPA", description, "#787878",
					"#787878", false, true);
			GPACard.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this,
							SettingsActivity.class);
					startActivityForResult(intent, 1);
				}
			});
			courseCardListView.addCard(GPACard);
		}
	}

	/*
	 * Returns array of weighted and unweighted GPA for displaying in GPA card.
	 */
	private double[] getGPA(boolean online) {
		if (online) {
			List<String> weightedClasses = new ArrayList<String>();
			Set<String> savedWeighted = defaultPrefs.getStringSet(
					"pref_weightedClasses", null);
			if (savedWeighted != null) {
				String[] weighted = savedWeighted
						.toArray(new String[savedWeighted.size()]);
				if (weighted != null) {
					for (int i = 0; i < weighted.length; i++) {
						weightedClasses.add(weighted[i]);
					}
				}
			}

			List<String> excludedClasses = new ArrayList<String>();
			Set<String> savedExcluded = defaultPrefs.getStringSet(
					"pref_excludedClasses", null);
			if (savedExcluded != null) {
				String[] excluded = savedExcluded
						.toArray(new String[savedExcluded.size()]);
				if (excluded != null) {
					for (int i = 0; i < excluded.length; i++) {
						excludedClasses.add(excluded[i]);
					}
				}
			}

			// remove excluded classes from list of weighed classes to calculate
			List<String> toWeighted = new ArrayList<String>();
			for (int i = 0; i < weightedClasses.size(); i++) {
				boolean excluded = false;
				for (int d = 0; d < excludedClasses.size(); d++) {
					if (d < excludedClasses.size()
							&& excludedClasses.get(d).equals(courses[i].title)) {
						excluded = true;
					}
				}
				if (!excluded) {
					toWeighted.add(weightedClasses.get(i));
				}
			}

			double weightedGPA = 0;
			double unweightedGPA = 0;
			unweightedGPA = GPACalc.unweighted(courses);
			if (currentDistrict.equals(Constants.AUSTIN)) {
				weightedGPA = GPACalc.weighted(courses, toWeighted,
						gradeSpeedDistrict.weightedGPABoost());
			} else if (currentDistrict.equals(Constants.ROUNDROCK)) {
				weightedGPA = GPACalc.weighted(courses, toWeighted,
						gradeSpeedDistrict.weightedGPABoost());
			}
			saver.saveUnweightedGPA(unweightedGPA, currentUsername, currentId);
			saver.saveWeightedGPA(weightedGPA, currentUsername, currentId);
			return new double[] { Numeric.round(unweightedGPA, 4),
					Numeric.round(weightedGPA, 4) };
		} else {
			return new double[] {
					Numeric.round(
							saver.getUnweightedGPA(currentUsername, currentId),
							4),
					Numeric.round(
							saver.getWeightedGPA(currentUsername, currentId), 4) };
		}
	}

	private void setupActionBar() {
		// Create navigation drawer of courses

		// Make array of all of the headings for the drawer
		String[] titles = new String[courses.length + Constants.HEADER_SECTIONS];
		titles[0] = "Overview";
		for (int i = 0; i < courses.length; i++) {
			titles[i + Constants.HEADER_SECTIONS] = courses[i].title;
		}
		// Set adapter
		drawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, R.id.drawer_text, titles));

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	/*
	 * Sets up some elements of slide out navigation drawer.
	 */
	private void makeDrawer() {
		drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				setTitle(drawerTitle);
				invalidateOptionsMenu();
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle("QuickHAC");
				invalidateOptionsMenu();
			}
		};

		// Set the drawer toggle as the DrawerListener
		drawerLayout.setDrawerListener(drawerToggle);
		drawerList.setOnItemClickListener(new DrawerItemClickListener());
	}

	/*
	 * Creates the spinner that contains the list of students.
	 */
	private void makeStudentSpinner() {
		String[] students = new String[studentList.length + 1];
		for (int i = 0; i < studentList.length; i++) {
			if (studentList[i].equals(currentUsername + "%" + currentId)) {
				currentStudentSelectedPosition = i;
			}
			students[i] = studentList[i].replace("%", " - ");
		}
		students[students.length - 1] = "Add student...";
		addStudentIndex = students.length - 1;
		ArrayAdapter<String> adp = new ArrayAdapter<String>(this,
				R.layout.spinner_item, students);
		studentSpinner.setAdapter(adp);
		studentSpinner.setOnItemSelectedListener(this);
		initializationStudentSpinnerCounter = 0;
		studentSpinner.setSelection(currentStudentSelectedPosition);
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		drawerPosition = position;
		drawerList.setItemChecked(position, true);
		if (position == 0) {
			Fragment fragment = new OverviewFragment();
			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getSupportFragmentManager();

			fragmentManager.beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.replace(R.id.content_frame, fragment).commit();

			setTitle(R.string.action_overview);
			drawerLayout.closeDrawer(drawer);
		} else {
			setTitle(courses[position - Constants.HEADER_SECTIONS].title);
			// Check if we already have info, otherwise load the course info
			if (classGradesList.get(position - Constants.HEADER_SECTIONS) == null) {
				drawerLayout.closeDrawer(drawer);
				loadCourseInfo(position - Constants.HEADER_SECTIONS,
						currentDistrict);
			} else {
				createFragment(position - Constants.HEADER_SECTIONS);
			}
		}
	}

	private void createFragment(int position) {
		final int pos = position;

		drawerHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				drawerLayout.closeDrawer(drawer);
			}
		}, 150);

		Fragment fragment = new ClassFragment();
		Bundle args = new Bundle();
		args.putInt(ClassFragment.INDEX, pos);
		fragment.setArguments(args);
		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.replace(R.id.content_frame, fragment).commit();

	}

	private void setRefreshActionButtonState(final boolean refreshing) {
		if (menu != null) {
			final MenuItem refreshItem = menu.findItem(R.id.action_refresh);
			if (refreshItem != null) {
				if (refreshing) {
					refreshItem
							.setActionView(R.layout.actionbar_indeterminate_progress);
				} else {
					refreshItem.setActionView(null);
				}
			}
		}
	}

	/*
	 * Restarts the activity.
	 */
	private void restartActivity() {
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	/*
	 * Restarts the activity, supplying a flag that says to refresh instead of
	 * loading saved grades when the activity restarts.
	 */
	private void restartActivityForRefresh() {
		Intent intent = getIntent();
		// Put refresh flag
		intent.putExtra(Constants.REFRESH_INTENT, true);
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	/*
	 * Detects if Sign In button has been pressed.
	 */
	public void onSignInClick(View v) {
		startLogin();
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	/*
	 * A fragment for a class. Each fragment is for a different class (ex. one
	 * for Precalc, one for History)
	 */
	public static class ClassFragment extends Fragment {
		CollectionPagerAdapter pagerAdapter;
		ViewPager viewPager;
		public static final String INDEX = "index";
		int index;

		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// Inflate the layout for this fragment
			return inflater.inflate(R.layout.fragment_class, container, false);
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.fragment_class, menu);

		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.action_jumpCycle:
				showJumpCycleDialog();
				break;
			case R.id.action_previousCycle:
				goPreviousCycle();
				break;
			case R.id.action_nextCycle:
				goNextCycle();
				break;
			}

			return super.onOptionsItemSelected(item);
		}

		public void goPreviousCycle() {
			if (viewPager.getCurrentItem() != 0) {
				viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
			}
		}

		public void goNextCycle() {
			if (viewPager.getCurrentItem() != classGradesList.get(index).size() - 1) {
				viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
			}
		}

		public void showJumpCycleDialog() {

			AlertDialog.Builder builderSingle = new AlertDialog.Builder(
					getView().getContext());
			builderSingle.setTitle("Go to cycle");
			String[] items = new String[classGradesList.get(index).size()];
			for (int i = 0; i < classGradesList.get(index).size(); i++) {
				items[i] = ("Cycle " + String.valueOf(i + 1));
			}
			builderSingle.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builderSingle.setItems(items,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							viewPager.setCurrentItem(which);
							dialog.dismiss();
						}
					});

			builderSingle.show();
		}

		public ViewPager getViewPager() {
			return viewPager;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			setHasOptionsMenu(true);
			Bundle args = getArguments();
			index = args.getInt(INDEX);

			pagerAdapter = new CollectionPagerAdapter(getFragmentManager());
			viewPager = (ViewPager) getView().findViewById(R.id.pager);
			viewPager.setAdapter(pagerAdapter);

			int latest = 0;
			ArrayList<ClassGrades> grades = classGradesList.get(index);
			if (grades != null) {
				// Set to latest cycle
				for (int i = grades.size() - 1; i >= 0; i--) {
					if (grades.get(i) != null) {
						latest = i;
						break;
					}
				}
				viewPager.setCurrentItem(latest);
			} else {
				Toast.makeText(
						getView().getContext(),
						"Something went wrong with creating the cycles. Make sure you're still connected to the internet.",
						Toast.LENGTH_SHORT).show();
			}

			SettingsManager manager = new SettingsManager(getView()
					.getContext());
			if (manager.isFirstTimeCycle()) {
				ShowcaseView sv = ShowcaseView.insertShowcaseViewWithType(
						ShowcaseView.ITEM_ACTION_ITEM, R.id.action_nextCycle,
						getActivity(), R.string.showcase_cycle,
						R.string.showcase_cycle_description,
						new ShowcaseView.ConfigOptions());
				manager.setFirstTimeCycle(false);

			}
		}

		public class CollectionPagerAdapter extends FragmentStatePagerAdapter {
			public CollectionPagerAdapter(FragmentManager fm) {
				super(fm);
			}

			@Override
			public Fragment getItem(int i) {
				Fragment fragment = new CycleFragment();
				Bundle args = new Bundle();
				args.putInt(CycleFragment.INDEX_COURSE, index);
				args.putInt(CycleFragment.INDEX_CYCLE, i);
				fragment.setArguments(args);
				return fragment;
			}

			@Override
			public int getCount() {
				return classGradesList.get(index).size();
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return "Cycle " + (position + 1);
			}
		}

		/*
		 * A fragment for a cycle. Each class has cycles. Cycles are scrolled
		 * through with swipe tabs.
		 */
		public static class CycleFragment extends Fragment {
			CardUI cardUI;
			public static final String INDEX_COURSE = "indexCourse";
			public static final String INDEX_CYCLE = "indexCycle";

			TextView title;

			int courseIndex;
			int cycleIndex;

			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				// Inflate the layout for this fragment
				return inflater.inflate(R.layout.fragment_cycle, container,
						false);
			}

			@Override
			public void onViewCreated(View view, Bundle savedInstanceState) {
				super.onViewCreated(view, savedInstanceState);
				Bundle args = getArguments();
				courseIndex = args.getInt(INDEX_COURSE);
				cycleIndex = args.getInt(INDEX_CYCLE);
				title = (TextView) getView().findViewById(R.id.title_text);
				Course course = courses[courseIndex];
				if (course != null) {
					String titleText = "";
					int semester = 0;
					int cycle = cycleIndex;
					semester = cycleIndex / (course.semesters[0].cycles.length);
					cycle = cycleIndex % (course.semesters[0].cycles.length);
					if (course.semesters[semester].cycles[cycle].average != null) {
						titleText = "Cycle "
								+ (cycleIndex + 1)
								+ " - "
								+ course.semesters[semester].cycles[cycle].average;
					} else {
						titleText = "Cycle " + (cycleIndex + 1);
					}
					title.setText(titleText);
					ArrayList<ClassGrades> gradesList = classGradesList
							.get(courseIndex);
					if (gradesList != null
							&& gradesList.get(cycleIndex) != null) {
						ClassGrades grades = gradesList.get(cycleIndex);
						Category[] categories = grades.categories;
						makeCategoryCards(courseIndex,
								course.semesters[semester], cycle, categories);
					} else {
						// create an arraylist of categories of size 0
						Category[] categories = new Category[0];
						makeCategoryCards(courseIndex,
								course.semesters[semester], cycle, categories);

					}
				} else {
					Toast.makeText(getView().getContext(),
							"An error occurred. Try refreshing.",
							Toast.LENGTH_SHORT).show();
				}
			}

			public void makeCategoryCards(int courseIndex, Semester semester,
					int cycleIndex, Category[] categories) {
				cardUI = (CardUI) getView().findViewById(R.id.cardsview);
				cardUI.setSwipeable(false);
				if (categories != null) {
					if (categories.length > 0) {
						for (int i = 0; i < categories.length; i++) {
							Category category = categories[i];
							String title = category.title;
							if (category.title.length() > 0
									&& category.assignments.length > 0) {
								String average;
								if (category.assignments != null) {
									Double av = GradeCalc
											.categoryAverage(category.assignments);
									if (av != null) {
										av = Numeric.round(av, 2);
										double aver = Numeric.round(av, 2);
										if ((int) aver == aver) {
											average = String
													.valueOf((int) aver);
										} else {
											average = String.valueOf(aver);
										}
									} else {
										average = "-";
									}
								} else {
									average = "-";
								}
								ColorGenerator gen = new ColorGenerator(
										getActivity());
								String color = gen.getCardColor(i);
								CategoryCard card = new CategoryCard(title,
										average, color, "#787878", false, false);
								card.setData(category);
								cardUI.addCard(card);
							} else if (category.assignments.length == 0) {
								// There aren't any grades for the category, so
								// create a nogrades card
								NoGradesCard card = new NoGradesCard(title,
										"No Grades :(", "#787878", "#787878",
										false, false);
								cardUI.addCard(card);
							}
						}
					}

					if (categories.length == 0) {
						// No categories, so create a nogrades card
						NoGradesCard card = new NoGradesCard("Assignments",
								"No Grades :(", "#787878", "#787878", false,
								false);
						cardUI.addCard(card);
					}
				} else {
					// No categories, so create a nogrades card
					NoGradesCard card = new NoGradesCard("Assignments",
							"No Grades :(", "#787878", "#787878", false, false);
					cardUI.addCard(card);
				}

				cardUI.refresh();
			}
		}
	}

	public static class OverviewFragment extends Fragment {
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// Inflate the layout for this fragment
			return inflater.inflate(R.layout.fragment_overview, container,
					false);
		}
	}

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

		public ScrapeTask(Context context, boolean showDialog) {
			this.context = context;
			this.showDialog = showDialog;
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
				gradeSpeedDistrict = new Austin();
			} else if (school.equals(Constants.ROUNDROCK)) {
				gradeSpeedDistrict = new RoundRock();
			}
			status = scrape(username, password, id, gradeSpeedDistrict);
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
				setRefreshActionButtonState(false);
				// Error unknown
				Toast.makeText(
						context,
						"Something went wrong. GradeSpeed servers are probably down. Try relogging or refreshing.",
						Toast.LENGTH_SHORT).show();
				signInButton.setVisibility(View.VISIBLE);
			} else if (response.equals(Constants.INVALID_LOGIN)) {
				if (showDialog) {
					dialog.dismiss();
				}

				setRefreshActionButtonState(false);
				// Wrong credentials sent
				Toast.makeText(
						context,
						"Invalid username, password, student ID, or school district.",
						Toast.LENGTH_SHORT).show();
				// Only show signinbutton if there's no other student to show
				// grades for
				if (studentList != null) {
					if (!(studentList.length > 0)) {
						signInButton.setVisibility(View.VISIBLE);
					}
				} else {
					signInButton.setVisibility(View.VISIBLE);
				}
				startLogin();
			} else if (!firstLog) {
				displayLastUpdateTime(System.currentTimeMillis() - 10);
				saver.saveCourses(courses, currentUsername, currentId);

				makeCourseCards(true);
				if (showDialog) {
					dialog.dismiss();
				}

				setupActionBar();
				setRefreshActionButtonState(false);
			} else {
				// first login
				settingsManager.addStudent(username, password, id, school);
				utils.makeAlarms();
				if (showDialog) {
					dialog.dismiss();
				}
				restartActivity();
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

			setRefreshActionButtonState(true);

		}

		public String scrape(final String username, final String password,
				final String id, GradeSpeedDistrict district) {
			retriever = new GradeRetriever(district);
			parser = new GradeParser(district);
			status = Constants.INVALID_LOGIN;

			final XHR.ResponseHandler getAveragesHandler = new XHR.ResponseHandler() {

				@Override
				public void onSuccess(String response) {
					if (status != Constants.UNKNOWN_ERROR
							&& status != Constants.INVALID_LOGIN) {
						loggedIn = true;
						saver.saveLatestResponse(response, username, id);
						courses = parser.parseAverages(response);
						// Set up the classGradesList with unintialized
						// class grades
						for (int i = 0; i < courses.length; i++) {
							classGradesList.add(null);
						}
						settingsManager.saveLastLogin(currentUsername,
								currentId, System.currentTimeMillis());
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

	}

	public class CycleScrapeTask extends AsyncTask<String, Void, String> {

		ProgressDialog dialog;
		Context context;
		int position;

		String status;

		public CycleScrapeTask(Context context) {
			this.context = context;
		}

		/*
		 * Scrapes ParentConnection remotely and returns a String of the webpage
		 * HTML.
		 * 
		 * @param[0] The course to scrape
		 * 
		 * @param[1] The school
		 * 
		 * @return A String of the webpage HTML.
		 */
		protected String doInBackground(String... information) {
			int course = Integer.valueOf(information[0]);
			position = course;
			scrape(course);
			return Constants.SUCCESSFUL_LOGIN;
		}

		protected void onPostExecute(String response) {
			dialog.dismiss();
			MainActivity.this.createFragment(position);
		}

		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage("Loading Assignments...");
			dialog.show();
		}

		public String scrape(int c) {
			// Log in if we haven't already
			if (!loggedIn) {
				final String[] credentials = settingsManager
						.getLoginInfo(currentUsername + "%" + currentId);
				final XHR.ResponseHandler getAveragesHandler = new XHR.ResponseHandler() {

					@Override
					public void onSuccess(String response) {
						if (status != Constants.UNKNOWN_ERROR
								&& status != Constants.INVALID_LOGIN) {
							saver.saveLatestResponse(response, credentials[0],
									credentials[2]);
							courses = parser.parseAverages(response);
							// Set up the classGradesList with unintialized
							// class grades
							for (int i = 0; i < courses.length; i++) {
								classGradesList.add(null);
							}
							settingsManager.saveLastLogin(currentUsername,
									currentId, System.currentTimeMillis());
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
						retriever.disambiguate(credentials[2], state,
								disambiguateHandler);
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

				retriever.login(credentials[0], credentials[1], loginHandler);
				loggedIn = true;
			}

			Course course = courses[c];
			final ArrayList<ClassGrades> gradesList = new ArrayList<ClassGrades>();
			for (int semesterIndex = 0; semesterIndex < course.semesters.length; semesterIndex++) {
				for (int cycleIndex = 0; cycleIndex < course.semesters[semesterIndex].cycles.length; cycleIndex++) {
					final String hash = course.semesters[semesterIndex].cycles[cycleIndex].urlHash;
					final int sem = semesterIndex;
					final int cy = cycleIndex;
					if (hash != null) {
						if (saver.getLatestResponse(currentUsername, currentId) != null) {
							Document doc = Jsoup.parse(saver.getLatestResponse(
									currentUsername, currentId));
							retriever.getCycle(hash, doc,
									new XHR.ResponseHandler() {

										@Override
										public void onSuccess(String response) {
											ClassGrades grades = parser
													.parseClassGrades(response,
															hash, sem, cy);
											gradesList.add(grades);
										}

										@Override
										public void onFailure(Exception e) {

										}

									});
						} else {
							gradesList.add(null);
						}
					} else {
						gradesList.add(null);
					}
				}
			}

			classGradesList.add(gradesList);
			classGradesList.set(c, gradesList);
			return Constants.SUCCESSFUL_LOGIN;
		}

		public void setStatus(String status) {
			this.status = status;
		}

	}

}