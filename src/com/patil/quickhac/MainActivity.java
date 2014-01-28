package com.patil.quickhac;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.ocpsoft.prettytime.PrettyTime;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.quickhac.common.GPACalc;
import com.quickhac.common.GradeCalc;
import com.quickhac.common.GradeParser;
import com.quickhac.common.GradeRetriever;
import com.quickhac.common.data.Category;
import com.quickhac.common.data.ClassGrades;
import com.quickhac.common.data.Course;
import com.quickhac.common.data.Cycle;
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

	public static Course[] courses;
	static ArrayList<ArrayList<ClassGrades>> classGradesList;

	ListView drawerList;
	DrawerLayout drawerLayout;
	ActionBarDrawerToggle drawerToggle;
	int lastPosition;
	// Handler to make sure drawer closes smoothly
	Handler drawerHandler = new Handler();

	String currentTitle;
	String cycleResponse;

	Button signInButton;
	TextView lastUpdatedText;

	CardUI cardView;
	ColorGenerator colorGenerator;

	GradeParser parser;
	GradeRetriever retriever;
	GradeSpeedDistrict gradeSpeedDistrict;

	SettingsManager settingsManager;
	CourseSaver saver;

	LinearLayout drawer;
	Spinner studentSpinner;

	String currentUsername;
	String currentId;
	String currentDistrict;
	String[] studentList;
	int addStudentIndex;
	// Counter so that we don't fire off onItemSelected when spinner is
	// initialized
	int initializationSpinnerCounter;
	int currentStudentSelectedPosition;
	boolean alreadyLoadedGrades = false;

	Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setTitle("Overview");
		settingsManager = new SettingsManager(this);
		colorGenerator = new ColorGenerator(this);
		currentTitle = "Overview";
		signInButton = (Button) findViewById(R.id.button_signin);
		lastUpdatedText = (TextView) findViewById(R.id.lastUpdate_text);
		studentSpinner = (Spinner) findViewById(R.id.spinner_student);
		drawer = (LinearLayout) findViewById(R.id.menu);
		initializationSpinnerCounter = 0;
		classGradesList = new ArrayList<ArrayList<ClassGrades>>();
		saver = new CourseSaver(this);
		makeDrawer();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case (R.id.action_refresh):
			restartActivity();
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

	public void showSignOutDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Sign out")
				.setMessage("Are you sure you want to sign out?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								settingsManager.removeStudent(currentUsername,
										currentId);
								dialog.dismiss();
								restartActivity();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}

	@Override
	public void onBackPressed() {
		// Go to overview fragment
		if (lastPosition != 0) {
			selectItem(0);
		} else {
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Restart activity if from settings to apply settings
		if (resultCode == RESULT_OK) {
			restartActivity();
		}
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = drawerLayout.isDrawerOpen(drawer);
		if (drawerOpen) {
			if (isFirstDrawer()) {
				if (studentList != null) {
					if (studentList.length > 0) {
						ActionViewTarget target = new ActionViewTarget(this,
								ActionViewTarget.Type.HOME);
						ShowcaseView sv = ShowcaseView.insertShowcaseView(
								target, this, R.string.showcase_student,
								R.string.showcase_student_description);
						sv.setShowcaseIndicatorScale(1.75f);
						setFirstDrawer(false);
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
		if (!alreadyLoadedGrades) {
			startDisplayingGrades();
			alreadyLoadedGrades = true;
		}
		if (isFirstOverview()) {
			if (studentList != null) {
				if (studentList.length > 0) {
					ActionViewTarget target = new ActionViewTarget(this,
							ActionViewTarget.Type.TITLE);
					ShowcaseView sv = ShowcaseView.insertShowcaseView(target,
							this, R.string.showcase_navigation,
							R.string.showcase_navigation_description);
					setFirstOverview(false);
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
		if (students != null) {
			studentList = students;
		} else {
			studentList = new String[0];
		}
		if (selectedStudent != null) {
			// We have a student to load
			String[] credentials = settingsManager
					.getLoginInfo(selectedStudent);

			if (credentials[0] != null && credentials[1] != null
					&& credentials[2] != null && credentials[3] != null) {
				// Valid login info
				currentUsername = credentials[0];
				currentId = credentials[2];
				currentDistrict = credentials[3];

				// See if there are any saved courses
				Course[] savedCourses = saver.getSavedCourses(credentials[0],
						credentials[2]);
				if (isNetworkAvailable() && savedCourses != null) {
					// We have saved courses, show those until grades are loaded
					handleOnlineSavedCourses(savedCourses);
					// We have internet, load grades
					executeScrapeTask(credentials[0], credentials[1],
							credentials[2], credentials[3], "no", false);
				} else if (isNetworkAvailable() && savedCourses == null) {
					// We have internet, load grades
					executeScrapeTask(credentials[0], credentials[1],
							credentials[2], credentials[3], "no", true);
				} else {
					if (savedCourses != null) {
						// We have saved courses
						handleOfflineCourses(savedCourses);
					} else {
						// No saved courses
						Toast.makeText(
								this,
								"You must be connected to the internet to load grades for the first time.",
								Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				// Invalid login info, just delete the student
				settingsManager.removeStudent(selectedStudent.split("%")[0],
						selectedStudent.split("%")[1]);
				restartActivity();
			}
		} else {
			currentUsername = "";
			currentId = "";
			currentDistrict = "";
			signInButton.setVisibility(View.VISIBLE);
			startLogin();
		}
		makeStudentSpinner();
	}

	public void makeStudentSpinner() {
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
		initializationSpinnerCounter = 0;
		studentSpinner.setSelection(currentStudentSelectedPosition);
	}

	public void handleOnlineSavedCourses(Course[] savedCourses) {
		courses = savedCourses;
		String toDisplay = "Loading new grades...";
		lastUpdatedText.setText(toDisplay);
		lastUpdatedText.setTextColor(getResources().getColor(
				R.color.pomegranate));
		for (int i = 0; i < courses.length; i++) {
			classGradesList.add(null);
		}
		setupActionBar();
		makeCourseCards(false);
	}

	public void handleOfflineCourses(Course[] savedCourses) {
		courses = savedCourses;
		long lastUpdateMillis = saver
				.getLastUpdated(currentUsername, currentId);
		String toDisplay = "Updated ";
		PrettyTime p = new PrettyTime();
		toDisplay += p.format(new Date(lastUpdateMillis));
		lastUpdatedText.setText(toDisplay);
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

	/*
	 * Detects if Sign In button has been pressed.
	 */
	public void onSignInClick(View v) {
		startLogin();
	}

	/*
	 * Starts login by showing a login dialog. Saves credentials and loads
	 * grades when user logs in.
	 */
	public void startLogin() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.dialog_login, null);
		// text_entry is an Layout XML file containing two text field to display
		// in alert dialog
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

		alert.setTitle("Sign in")
				.setCancelable(false)
				.setView(textEntryView)
				.setPositiveButton("Login",
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
										distr = "Austin";
									} else if (district.getSelectedItem()
											.toString().equals("RRISD")) {
										distr = "RoundRock";
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
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								initializationSpinnerCounter = 0;
								studentSpinner
										.setSelection(currentStudentSelectedPosition);
							}
						}).setMessage("Use your GradeSpeed credentials.");
		alert.show();
	}

	/*
	 * user = username pass = password id = student id distr = the school
	 * district firstLogOn = if we're logging this user in for the first time
	 * asyncRefresh = if we need to show a dialog or if we can just refresh the
	 * ui asynchronously
	 */
	public void executeScrapeTask(String user, String pass, String id,
			String distr, String firstLogOn, boolean asyncRefresh) {
		new ScrapeTask(this, asyncRefresh).execute(new String[] { user, pass,
				id, distr, firstLogOn });

	}

	public void loadCourseInfo(int course, String school) {
		if (isNetworkAvailable()) {
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
	public void makeCourseCards(final boolean online) {

		cardView = (CardUI) findViewById(R.id.cardsview);

		Runnable thread = new Runnable() {

			@Override
			public void run() {
				cardView.clearCards();
				cardView.setSwipeable(false);
				// add GPA card if user has enabled
				SharedPreferences sharedPref = PreferenceManager
						.getDefaultSharedPreferences(MainActivity.this);
				boolean gpaPref = sharedPref.getBoolean("pref_showGPA", true);
				if (gpaPref) {
					double[] gpa = getGPA(online);
					String description = String.valueOf(gpa[0]) + " / "
							+ String.valueOf(gpa[1]);
					Card GPACard = new NoGradesCard("GPA", description,
							"#787878", "#787878", false, false);
					cardView.addCard(GPACard);
				}

				for (int k = 0; k < courses.length; k++) {

					Course course = courses[k];

					Semester firstSemester = course.semesters[0];
					Semester secondSemester = course.semesters[1];
					Cycle[] firstSemesterCycles = firstSemester.cycles;
					Cycle[] secondSemesterCycles = secondSemester.cycles;

					String[] cycleData = new String[(course.semesters.length * 2)
							+ firstSemesterCycles.length
							+ secondSemesterCycles.length];

					for (int i = 0; i < firstSemesterCycles.length; i++) {
						if (firstSemesterCycles[i].average != null
								&& firstSemesterCycles[i].average != -1) {
							cycleData[i] = String
									.valueOf(firstSemesterCycles[i].average);
						} else {
							cycleData[i] = "-";
						}
					}
					if (firstSemester.examGrade != null
							&& firstSemester.examGrade != -1) {
						cycleData[firstSemesterCycles.length] = String
								.valueOf(firstSemester.examGrade);
					} else {
						cycleData[firstSemesterCycles.length] = "-";
					}

					if (firstSemester.average != null
							&& firstSemester.average != -1) {
						cycleData[firstSemesterCycles.length + 1] = String
								.valueOf(firstSemester.average);
					} else {
						cycleData[firstSemesterCycles.length + 1] = "-";
					}

					for (int i = 0; i < secondSemesterCycles.length; i++) {
						if (secondSemesterCycles[i].average != null
								&& secondSemesterCycles[i].average != -1) {
							cycleData[i + firstSemesterCycles.length + 2] = String
									.valueOf(secondSemesterCycles[i].average);
						} else {
							cycleData[i + firstSemesterCycles.length + 2] = "-";
						}
					}

					if (secondSemester.examGrade != null
							&& secondSemester.examGrade != -1) {
						cycleData[firstSemesterCycles.length
								+ secondSemesterCycles.length + 2] = String
								.valueOf(secondSemester.examGrade);
					} else {
						cycleData[firstSemesterCycles.length
								+ secondSemesterCycles.length + 2] = "-";
					}
					if (secondSemester.average != null
							&& secondSemester.average != -1) {
						cycleData[firstSemesterCycles.length
								+ secondSemesterCycles.length + 3] = String
								.valueOf(secondSemester.average);
					} else {
						cycleData[firstSemesterCycles.length
								+ secondSemesterCycles.length + 3] = "-";
					}
					String color = colorGenerator.getCardColor(k);
					final Card courseCard = new CourseCard(course.title, "",
							color, "#787878", false, true);
					courseCard.setData(cycleData);
					// Set onClick
					courseCard.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// Find appropriate position
							int pos = 0;
							for (int e = 0; e < courses.length; e++) {
								if (courses[e].title
										.equals(((CourseCard) courseCard)
												.getCardTitle())) {
									pos = e;
								}
							}
							selectItem(pos + 1);
						}

					});
					cardView.addCard(courseCard);
				}
				cardView.setPersistentDrawingCache(3);

			}
		};
		thread.run();

	}

	/*
	 * Returns array of weighted and unweighted GPA for displaying in GPA card.
	 */
	public double[] getGPA(boolean online) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (online) {
			List<String> weightedClasses = new ArrayList<String>();
			Set<String> savedWeighted = sharedPref.getStringSet(
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
			Set<String> savedExcluded = sharedPref.getStringSet(
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
					if (excludedClasses.get(d).equals(courses[i].title)) {
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
			if (currentDistrict.equals("Austin")) {
				weightedGPA = GPACalc.weighted(courses, toWeighted,
						gradeSpeedDistrict.weightedGPABoost());
			} else if (currentDistrict.equals("RoundRock")) {
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

	public void setupActionBar() {
		// Create navigation drawer of courses

		// Make array of all of the headings for the drawer
		String[] titles = new String[courses.length + 1];
		titles[0] = "Overview";
		for (int i = 0; i < courses.length; i++) {
			titles[i + 1] = courses[i].title;
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
	public void makeDrawer() {
		drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(currentTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()

			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle("QuickHAC");
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};

		// Set the drawer toggle as the DrawerListener
		drawerLayout.setDrawerListener(drawerToggle);
		drawerList.setOnItemClickListener(new DrawerItemClickListener());
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	public void selectItem(int position) {
		lastPosition = position;
		drawerList.setItemChecked(position, true);
		if (position == 0) {
			Fragment fragment = new OverviewFragment();
			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getSupportFragmentManager();

			fragmentManager.beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.replace(R.id.content_frame, fragment).commit();

			// Highlight the selected item, update the title, and close the
			// drawer
			drawerList.setItemChecked(position, true);
			setTitle("Overview");
			drawerLayout.closeDrawer(drawer);
		} else {
			// Highlight the selected item, update the title, and close the
			// drawer
			drawerList.setItemChecked(position - 1, true);
			setTitle(courses[position - 1].title);
			// Check if we already have info, otherwise load the course info
			if (classGradesList.get(position - 1) == null) {
				drawerLayout.closeDrawer(drawer);
				loadCourseInfo(position - 1, currentDistrict);
			} else {
				createFragment(position - 1);
			}
		}
	}

	public void createFragment(int position) {
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

	@Override
	public void setTitle(CharSequence title) {
		currentTitle = (String) title;
		getActionBar().setTitle(title);
	}

	public void setRefreshActionButtonState(final boolean refreshing) {
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
		 * Scrapes ParentConnection remotely and returns a String of the webpage
		 * HTML.
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
			if (firstLogin.equals("yes")) {
				firstLog = true;
			} else {
				firstLog = false;
			}

			String html = "UNKNOWN_ERROR";
			if (school.equals("Austin")) {
				gradeSpeedDistrict = new Austin();
				html = scrape(username, password, id, gradeSpeedDistrict);
			} else if (school.equals("RoundRock")) {
				gradeSpeedDistrict = new RoundRock();
				html = scrape(username, password, id, gradeSpeedDistrict);
			}

			return html;
		}

		protected void onPostExecute(String response) {
			handleResponse(response);
		}

		public void handleResponse(String response) {
			if (response.equals("UNKNOWN_ERROR")) {
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
			} else if (response.equals("INVALID_LOGIN")) {
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
				if (!(studentList.length > 0)) {
					signInButton.setVisibility(View.VISIBLE);
				}
				startLogin();
			} else if (!firstLog) {
				PrettyTime p = new PrettyTime();
				lastUpdatedText.setText("Updated "
						+ p.format(new Date(System.currentTimeMillis() - 10)));
				lastUpdatedText.setTextColor(Color.BLACK);
				saver.saveCourses(courses, currentUsername, currentId);

				makeCourseCards(true);
				if (showDialog) {
					dialog.dismiss();
				}

				setupActionBar();
				setRefreshActionButtonState(false);
			} else {
				settingsManager.addStudent(username, password, id, school);
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
			status = "INVALID_LOGIN";

			final XHR.ResponseHandler getAveragesHandler = new XHR.ResponseHandler() {

				@Override
				public void onSuccess(String response) {
					if (status != "UNKNOWN_ERROR" && status != "INVALID_LOGIN") {
						setStatus("SUCCESS");
						courses = parser.parseAverages(response);
						cycleResponse = response;
						// Set up the classGradesList with unintialized
						// class grades
						for (int i = 0; i < courses.length; i++) {
							classGradesList.add(null);
						}
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

	}

	public class CycleScrapeTask extends AsyncTask<String, Void, String> {

		ProgressDialog dialog;
		Context context;
		int position;

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
			return "LOADED SUCCESSFULLY";
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
			Course course = courses[c];
			final ArrayList<ClassGrades> gradesList = new ArrayList<ClassGrades>();
			for (int i = 0; i < 6; i++) {
				int semester = 0;
				int cycle = 0;
				if (i < 3) {
					semester = 0;
					cycle = i;
				} else {
					semester = 1;
					cycle = i - 3;
				}
				final String hash = course.semesters[semester].cycles[cycle].urlHash;
				final int sem = semester;
				final int cy = cycle;
				if (hash != null) {
					retriever.getCycle(hash, Jsoup.parse(cycleResponse),
							new XHR.ResponseHandler() {

								@Override
								public void onSuccess(String response) {
									ClassGrades grades = parser
											.parseClassGrades(response, hash,
													sem, cy);
									gradesList.add(grades);
								}

								@Override
								public void onFailure(Exception e) {

								}

							});
				} else {
					gradesList.add(null);
				}
			}
			classGradesList.add(gradesList);
			classGradesList.set(c, gradesList);
			return "";
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
			if (viewPager.getCurrentItem() != 5) {
				viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
			}
		}

		public void showJumpCycleDialog() {

			AlertDialog.Builder builderSingle = new AlertDialog.Builder(
					getView().getContext());
			builderSingle.setTitle("Go to cycle");
			String[] items = new String[6];
			for (int i = 0; i < 6; i++) {
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

			if (isFirstCycle()) {
				ShowcaseView sv = ShowcaseView.insertShowcaseViewWithType(
						ShowcaseView.ITEM_ACTION_ITEM, R.id.action_nextCycle,
						getActivity(), R.string.showcase_cycle,
						R.string.showcase_cycle_description,
						new ShowcaseView.ConfigOptions());
				setFirstCycle(false);
			}
		}

		/*
		 * Helper method to say if it's the first time the cycle screen is
		 * loaded.
		 */
		public boolean isFirstCycle() {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getView().getContext());
			boolean firstLaunch = prefs.getBoolean("firstCycle", true);
			return firstLaunch;
		}

		/*
		 * Helper method to set the value of first cycle screen load.
		 */
		public void setFirstCycle(boolean first) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getView().getContext());
			Editor edit = prefs.edit();
			edit.putBoolean("firstCycle", first);
			edit.commit();
		}

		public class CollectionPagerAdapter extends FragmentStatePagerAdapter {
			public CollectionPagerAdapter(FragmentManager fm) {
				super(fm);
			}

			@Override
			public Fragment getItem(int i) {
				if (i == 0 || i == 1 || i == 2) {
					Fragment fragment = new CycleFragment();
					Bundle args = new Bundle();
					args.putInt(CycleFragment.INDEX_COURSE, index);
					args.putInt(CycleFragment.INDEX_CYCLE, i);
					fragment.setArguments(args);
					return fragment;
				} else if (i == 3 || i == 4 || i == 5) {
					Fragment fragment = new CycleFragment();
					Bundle args = new Bundle();
					args.putInt(CycleFragment.INDEX_COURSE, index);
					args.putInt(CycleFragment.INDEX_CYCLE, i);
					fragment.setArguments(args);
					return fragment;
				}
				return null;
			}

			@Override
			public int getCount() {
				return 6;
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return "OBJECT " + (position + 1);
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
				String titleText = "";
				int semester = 0;
				int cycle = cycleIndex;
				if (cycleIndex > 2) {
					semester = 1;
					cycle -= 3;
				}
				if (course.semesters[semester].cycles[cycle].average != null) {
					titleText = "Cycle " + (cycleIndex + 1) + " - "
							+ course.semesters[semester].cycles[cycle].average;
				} else {
					titleText = "Cycle " + (cycleIndex + 1);
				}
				title.setText(titleText);
				ArrayList<ClassGrades> gradesList = classGradesList
						.get(courseIndex);
				if (gradesList != null && gradesList.get(cycleIndex) != null) {
					ClassGrades grades = gradesList.get(cycleIndex);
					Category[] categories = grades.categories;
					makeCategoryCards(categories);
				} else {
					// create an arraylist of categories of size 0
					Category[] categories = new Category[0];
					makeCategoryCards(categories);

				}
			}

			public void makeCategoryCards(Category[] categories) {
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

	/*
	 * Helper method that restarts the activity.
	 */
	public void restartActivity() {
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	/*
	 * Helper method to check if internet is available
	 */
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	@Override
	public void onItemSelected(AdapterView<?> parentView,
			View selectedItemView, int position, long id) {
		if (initializationSpinnerCounter > 0) {
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
		initializationSpinnerCounter++;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	/*
	 * Helper method to say if it's the first time the overview screen is
	 * loaded.
	 */
	public boolean isFirstOverview() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean firstLaunch = prefs.getBoolean("firstOverview", true);
		return firstLaunch;
	}

	/*
	 * Helper method to set the value of first overview screen load.
	 */
	public void setFirstOverview(boolean first) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor edit = prefs.edit();
		edit.putBoolean("firstOverview", first);
		edit.commit();
	}

	/*
	 * Helper method to say if it's the first time the drawer is open.
	 */
	public boolean isFirstDrawer() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean firstLaunch = prefs.getBoolean("firstDrawer", true);
		return firstLaunch;
	}

	/*
	 * Helper method to set the value of first drawer open.
	 */
	public void setFirstDrawer(boolean first) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor edit = prefs.edit();
		edit.putBoolean("firstDrawer", first);
		edit.commit();
	}

}