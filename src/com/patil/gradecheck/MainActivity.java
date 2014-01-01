package com.patil.gradecheck;

import java.util.ArrayList;

import org.jsoup.Jsoup;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;
import com.fima.cardsui.views.CardUI;
import com.quickhac.common.GPACalc;
import com.quickhac.common.GradeCalc;
import com.quickhac.common.GradeParser;
import com.quickhac.common.GradeRetriever;
import com.quickhac.common.data.Assignment;
import com.quickhac.common.data.Category;
import com.quickhac.common.data.ClassGrades;
import com.quickhac.common.data.Course;
import com.quickhac.common.districts.GradeSpeedDistrict;
import com.quickhac.common.districts.impl.Austin;
import com.quickhac.common.districts.impl.RoundRock;
import com.quickhac.common.http.XHR;

public class MainActivity extends FragmentActivity {

	public static Course[] courses;
	ListView drawerList;
	DrawerLayout drawerLayout;
	ActionBarDrawerToggle drawerToggle;

	String currentTitle;

	Button signInButton;

	String cycleResponse;

	CardUI cardView;
	GradeParser parser;

	TextView GPAText;
	TextView lastUpdatedText;

	GradeRetriever retriever;

	// Handler to make sure drawer closes smoothly
	Handler drawerHandler = new Handler();

	SettingsManager settingsManager;
	CardColorGenerator colorGenerator;
	CourseSaver saver;

	static ArrayList<ArrayList<ClassGrades>> classGradesList = new ArrayList<ArrayList<ClassGrades>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setTitle("Overview");
		settingsManager = new SettingsManager(this);
		colorGenerator = new CardColorGenerator();
		currentTitle = "Overview";
		signInButton = (Button) findViewById(R.id.button_signin);
		GPAText = (TextView) findViewById(R.id.gpa_text);
		lastUpdatedText = (TextView) findViewById(R.id.lastUpdate_text);

		// This is used to store persistent cookies
		Drawable drawable = getResources().getDrawable(
				getResources().getIdentifier("cookie_storage", "drawable",
						getPackageName()));
		saver = new CourseSaver(this);
		makeDrawer();
		startDisplayingGrades();

	}

	public void startDisplayingGrades() {
		Course[] savedCourses;
		savedCourses = saver.getSavedCourses();
		String[] credentials = settingsManager.getLoginInfo();
		if (credentials[0].length() > 0 && credentials[1].length() > 0
				&& credentials[2].length() > 0 && credentials[3].length() > 0) {
			if (isNetworkAvailable()) {
				new ScrapeTask(this).execute(new String[] { credentials[0],
						credentials[1], credentials[2], credentials[3] });
			} else if (!isNetworkAvailable() && savedCourses != null) {
				courses = savedCourses;
				long lastUpdateMillis = System.currentTimeMillis()
						- saver.getLastUpdated();
				String toDisplay;
				// If less than one hour
				if (lastUpdateMillis < 3600000) {
					int minutes = (int) ((lastUpdateMillis / (1000 * 60)) % 60);
					if (minutes != 1) {
						toDisplay = "Updated " + String.valueOf(minutes)
								+ " minutes ago";
					} else {
						toDisplay = "Updated " + String.valueOf(minutes)
								+ " minute ago";
					}
				} else {
					int hours = (int) ((lastUpdateMillis / (1000 * 60 * 60)) % 24);
					if (hours != 1) {
						toDisplay = "Updated " + String.valueOf(hours)
								+ " hours ago";
					} else {

						toDisplay = "Updated " + String.valueOf(hours)
								+ " hour ago";
					}
				}
				lastUpdatedText.setText(toDisplay);
				Toast.makeText(
						this,
						"No internet connection detected. Displaying saved grades.",
						Toast.LENGTH_SHORT).show();
				setupActionBar();
				makeCourseCards();
				calculateGPA();
			} else {
				Toast.makeText(
						this,
						"You must be connected to the internet to load grades for the first time.",
						Toast.LENGTH_SHORT).show();
			}
			signInButton.setVisibility(View.GONE);
		} else {
			/*
			 * Prompt for login and set the login button as visible.
			 */
			signInButton.setVisibility(View.VISIBLE);
			startLogin();
		}
	}

	/*
	 * Detects if Sign In button has been pressed.
	 */
	public void onSignInClick(View v) {
		startLogin();
	}

	public void displayGPA(double GPA) {
		GPAText.setVisibility(View.VISIBLE);
		GPAText.setText("GPA: " + String.valueOf(GPA));
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
									Log.d("really?", "text passed in"
											+ studentId.getText().toString());
									settingsManager.saveLoginInfo(userName
											.getText().toString(), password
											.getText().toString(), studentId
											.getText().toString(), distr);
									restartActivity();
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
							}
						}).setMessage("Use your GradeSpeed credentials.");
		alert.show();
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
	private void selectItem(int position) {
		drawerList.setItemChecked(position, true);
		if (position == 0) {
			Fragment fragment = new OverviewFragment();
			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getSupportFragmentManager();

			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();

			// Highlight the selected item, update the title, and close the
			// drawer
			drawerList.setItemChecked(position, true);
			setTitle("Overview");
			drawerLayout.closeDrawer(drawerList);
		} else {
			// Highlight the selected item, update the title, and close the
			// drawer
			drawerList.setItemChecked(position - 1, true);
			setTitle(courses[position - 1].title);
			// Check if we already have info, otherwise load the course info
			if (classGradesList.get(position - 1) == null) {
				drawerLayout.closeDrawer(drawerList);
				loadCourseInfo(position - 1, settingsManager.getLoginInfo()[3]);
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
				drawerLayout.closeDrawer(drawerList);
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

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * Makes descriptions and loads in cards to display.
	 */
	public void makeCourseCards() {
		cardView = (CardUI) findViewById(R.id.cardsview);
		cardView.setSwipeable(false);
		for (int i = 0; i < courses.length; i++) {
			Course course = courses[i];
			String gradeDescription = "";
			int[] semesters = new int[2];
			for (int d = 0; d < 2; d++) {
				if (course.semesters[d].average != null) {
					semesters[d] = course.semesters[d].average;
				}
			}
			int[] exams = new int[2];
			for (int d = 0; d < 2; d++) {
				if (course.semesters[d].examGrade != null) {
					exams[d] = course.semesters[d].examGrade;
				}
			}
			int[] sixWeeksAverages = new int[6];
			for (int d = 0; d < 6; d++) {
				if (d < 3) {
					if (course.semesters[0].cycles[d].average != null) {
						sixWeeksAverages[d] = course.semesters[0].cycles[d].average;
					}
				} else {
					if (course.semesters[1].cycles[d - 3].average != null) {
						sixWeeksAverages[d] = course.semesters[1].cycles[d - 3].average;
					}
				}
			}
			if (semesters[0] != -1) {
				gradeDescription += "Semester 1: "
						+ String.valueOf(semesters[0]) + "DELIMCOLUMN";
			} else {
				gradeDescription += "Semester 1: N/A" + "DELIMCOLUMN";
			}
			if (semesters[1] != -1) {
				gradeDescription += "Semester 2: "
						+ String.valueOf(semesters[1]) + "DELIMROW";
			} else {
				gradeDescription += "Semester 2: N/A" + "DELIMROW";
			}
			for (int d = 0; d < 3; d++) {
				if (sixWeeksAverages[d] != -1) {
					gradeDescription += "Cycle " + (d + 1) + ": "
							+ String.valueOf(sixWeeksAverages[d])
							+ "DELIMCOLUMN";
				} else {
					gradeDescription += "Cycle " + (d + 1) + ": N/A"
							+ "DELIMCOLUMN";
				}
				if (sixWeeksAverages[d + 3] != -1) {
					gradeDescription += "Cycle " + (d + 4) + ": "
							+ String.valueOf(sixWeeksAverages[d + 3])
							+ "DELIMROW";
				} else {
					gradeDescription += "Cycle " + (d + 4) + ": N/A"
							+ "DELIMROW";
				}
			}

			if (exams[0] != -1) {
				gradeDescription += "Exam 1: " + String.valueOf(exams[0])
						+ "DELIMCOLUMN";
			} else {
				gradeDescription += "Exam 1: N/A" + "DELIMCOLUMN";
			}
			if (exams[1] != -1) {
				gradeDescription += "Exam 2: " + String.valueOf(exams[1]);
			} else {
				gradeDescription += "Exam 2: N/A";
			}
			String color = colorGenerator.getCardColor(i);
			final Card courseCard = new CourseCard(course.title,
					gradeDescription, color, "#787878", false, true);

			// Set onClick
			courseCard.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// Find appropriate position
					int pos = 0;
					for (int e = 0; e < courses.length; e++) {
						if (courses[e].title.equals(((CourseCard) courseCard)
								.getCardTitle())) {
							pos = e;
						}
					}
					selectItem(pos + 1);
				}
			});
			cardView.addCard(courseCard);
		}
		cardView.refresh();
	}

	public void loadCourseInfo(int course, String school) {
		if (isNetworkAvailable()) {
			new CycleScrapeTask(this).execute(new String[] {
					String.valueOf(course), school });
		} else {
			Toast.makeText(this, "You need internet to view assignments.",
					Toast.LENGTH_SHORT).show();
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

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
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
			settingsManager.eraseLoginInfo();
			restartActivity();
			break;
		case (R.id.action_about):
			AboutDialog about = new AboutDialog(this);
			about.setTitle("QuickHAC for Android");
			about.show();
			break;
		case (R.id.action_settings):
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, 1);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			restartActivity();
		}
	}

	public void calculateGPA() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean gpaPref = sharedPref.getBoolean("pref_showGPA", true);
		if (gpaPref) {
			double GPA = GPACalc.unweighted(courses);
			displayGPA(GPA);
		} else {
			GPAText.setVisibility(View.GONE);
		}
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

	public class ScrapeTask extends AsyncTask<String, Void, String> {

		ProgressDialog dialog;
		Context context;
		String district;

		public ScrapeTask(Context context) {
			this.context = context;
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
			String username = information[0];
			String password = information[1];
			String id = information[2];
			String school = information[3];
			district = school;

			String html = "UNKNOWN_ERROR";

			if (school.equals("Austin")) {
				html = scrapeAustin(username, password, id);
			} else if (school.equals("RoundRock")) {
				html = scrapeRoundRock(username, password, id);
			}

			return html;
		}

		protected void onPostExecute(String response) {
			handleResponse(response);
		}

		/*
		 * Looks at the response from the scraper and acts accordingly.
		 * 
		 * @param The response - either HTML or an error.
		 */
		public void handleResponse(String response) {
			if (response.equals("UNKNOWN_ERROR")) {
				dialog.dismiss();
				// Error unknown
				Toast.makeText(
						context,
						"Something went wrong. GradeSpeed servers are probably down. Try relogging or refreshing.",
						Toast.LENGTH_SHORT).show();
				signInButton.setVisibility(View.VISIBLE);
			} else if (response.equals("INVALID_LOGIN")) {
				dialog.dismiss();
				// Wrong credentials sent
				Toast.makeText(
						context,
						"Invalid username, password, student ID, or school district.",
						Toast.LENGTH_SHORT).show();
				signInButton.setVisibility(View.VISIBLE);
				startLogin();
			} else {
				setupActionBar();
				makeCourseCards();
				calculateGPA();
				lastUpdatedText.setText("Updated a few seconds ago");
				saveCourseInfo();
				dialog.dismiss();
			}

		}

		public void saveCourseInfo() {
			new CourseSaver(context).saveCourses(courses);
		}

		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage("Loading Grades...");
			dialog.show();
		}

		/*
		 * Scrapes AISD.
		 * 
		 * @param The username.
		 * 
		 * @param The password.
		 * 
		 * @param The student id.
		 * 
		 * @param The HttpClient.
		 * 
		 * @return The HTML of AISD scrape.
		 */
		public String scrapeAustin(String username, String password, String id) {
			GradeSpeedDistrict district = new Austin();
			retriever = new GradeRetriever(district);
			parser = new GradeParser(district);
			retriever.login(username, password, id, new XHR.ResponseHandler() {

				@Override
				public void onSuccess(String response) {
					retriever.getAverages(new XHR.ResponseHandler() {

						@Override
						public void onSuccess(String response) {
							courses = parser.parseAverages(response);
							cycleResponse = response;
							// Set up the classGradesList with unintialized
							// class grades
							for (int i = 0; i < courses.length; i++) {
								classGradesList.add(null);
							}
						}

						@Override
						public void onFailure(Exception e) {
							// TODO Auto-generated method stub

						}
					});

				}

				@Override
				public void onFailure(Exception e) {
					// TODO Auto-generated method stub

				}
			});
			return "";
		}

		/*
		 * Scrapes RRISD.
		 * 
		 * @param The username.
		 * 
		 * @param The password.
		 * 
		 * @param The student id.
		 * 
		 * @param The HttpClient.
		 * 
		 * @return The HTML of RRISD scrape. Not yet implemented.
		 */

		public String scrapeRoundRock(String username, String password,
				String id) {
			GradeSpeedDistrict district = new RoundRock();
			retriever = new GradeRetriever(district);
			parser = new GradeParser(district);
			retriever.login(username, password, id, new XHR.ResponseHandler() {

				@Override
				public void onSuccess(String response) {
					retriever.getAverages(new XHR.ResponseHandler() {

						@Override
						public void onSuccess(String response) {
							courses = parser.parseAverages(response);
							cycleResponse = response;
							// Set up the classGradesList with unintialized
							// class grades
							for (int i = 0; i < courses.length; i++) {
								classGradesList.add(null);
							}
						}

						@Override
						public void onFailure(Exception e) {
							// TODO Auto-generated method stub

						}
					});

				}

				@Override
				public void onFailure(Exception e) {
					// TODO Auto-generated method stub

				}
			});
			return "";
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
			String school = information[1];
			if (school.equals("Austin")) {
				scrapeAustin(course);
			} else if (school.equals("RoundRock")) {
				scrapeRoundRock(course);
			}

			return "LOADED SUCCESSFULLY";

		}

		protected void onPostExecute(String response) {
			handleResponse(response);
		}

		/*
		 * Looks at the response from the scraper and acts accordingly.
		 * 
		 * @param The response - either HTML or an error.
		 */
		public void handleResponse(String response) {
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

		/*
		 * Scrapes AISD for a cycle info.
		 * 
		 * @param The course to scrape for
		 * 
		 * @param The HttpClient.
		 * 
		 * @return The HTML of AISD scrape with specific cycle info.
		 */
		public String scrapeAustin(int c) {
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
									for( String line : response.split("\n") ) {
									    Log.d( "ScrapingResponse", line );
									}
									ClassGrades grades = parser
											.parseClassGrades(response, hash,
													sem, cy);
									gradesList.add(grades);
								}

								@Override
								public void onFailure(Exception e) {
									// TODO Auto-generated method stub

								}

							});
				} else {
					gradesList.add(null);
				}
			}
			classGradesList.set(c, gradesList);
			return "";
		}

		/*
		 * Scrapes RRISD.
		 * 
		 * @param The username.
		 * 
		 * @param The password.
		 * 
		 * @param The student id.
		 * 
		 * @param The HttpClient.
		 * 
		 * @return The HTML of RRISD scrape. Not yet implemented.
		 */

		public String scrapeRoundRock(int c) {
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
									// TODO Auto-generated method stub

								}

							});
				} else {
					gradesList.add(null);
				}
			}
			classGradesList.add(gradesList);
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

		public ViewPager getViewPager() {
			return viewPager;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onViewCreated(view, savedInstanceState);
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
				if (latest >= 3) {
					latest += 2;
				}
				viewPager.setCurrentItem(latest);
			} else {
				Toast.makeText(
						getView().getContext(),
						"Something went wrong with creating the fragment. Make sure you're still connected to the internet.",
						Toast.LENGTH_SHORT).show();
			}
		}

		// Since this is an object collection, use a
		// FragmentStatePagerAdapter,
		// and NOT a FragmentPagerAdapter.
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
				} else if (i == 3 || i == 4) {
					Fragment fragment = new SemesterExamFragment();
					Bundle args = new Bundle();
					args.putInt(SemesterExamFragment.INDEX_COURSE, index);
					args.putInt(SemesterExamFragment.TYPE, i - 3);
					args.putInt(SemesterExamFragment.INDEX_SEMESTER, 0);
					fragment.setArguments(args);
					return fragment;
				} else if (i == 5 || i == 6 || i == 7) {
					Fragment fragment = new CycleFragment();
					Bundle args = new Bundle();
					args.putInt(CycleFragment.INDEX_COURSE, index);
					args.putInt(CycleFragment.INDEX_CYCLE, i - 2);
					fragment.setArguments(args);
					return fragment;
				} else if (i == 8 || i == 9) {
					Fragment fragment = new SemesterExamFragment();
					Bundle args = new Bundle();
					args.putInt(SemesterExamFragment.INDEX_COURSE, index);
					args.putInt(SemesterExamFragment.TYPE, i - 8);
					args.putInt(SemesterExamFragment.INDEX_SEMESTER, 1);
					fragment.setArguments(args);
					return fragment;
				}
				return null;
			}

			@Override
			public int getCount() {
				return 10;
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return "OBJECT " + (position + 1);
			}
		}

		/*
		 * A fragment for a semester exam or semester average. Each class has 4
		 * of these.
		 */
		public static class SemesterExamFragment extends Fragment {
			CardUI cardUI;
			public static final String INDEX_COURSE = "indexCourse";
			public static final String TYPE = "type";
			public static final String INDEX_SEMESTER = "indexSemester";

			TextView title;

			// Type 0 = exam, type 1 = semester average
			int type;
			int semesterIndex;
			int courseIndex;

			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				// Inflate the layout for this fragment
				return inflater.inflate(R.layout.fragment_cycle, container,
						false);
			}

			@Override
			public void onViewCreated(View view, Bundle savedInstanceState) {
				// TODO Auto-generated method stub
				super.onViewCreated(view, savedInstanceState);
				Bundle args = getArguments();
				type = args.getInt(TYPE);
				semesterIndex = args.getInt(INDEX_SEMESTER);
				title = (TextView) getView().findViewById(R.id.title_text);
				courseIndex = args.getInt(INDEX_COURSE);
				Course course = courses[courseIndex];

				// Setting the right title for the card
				String titleText = "";
				if (type == 0) {
					if (semesterIndex == 0) {
						titleText = "Exam 1";
					} else {
						titleText = "Exam 2";
					}
				} else if (type == 1) {
					if (semesterIndex == 0) {
						titleText = "Semester 1";
					} else {
						titleText = "Semester 2";
					}
				}

				title.setText(titleText);

				makeCard(course);
			}

			public void makeCard(Course course) {
				cardUI = (CardUI) getView().findViewById(R.id.cardsview);
				cardUI.setSwipeable(false);
				String desc = "";
				if (type == 0) {
					if (course.semesters[semesterIndex].examGrade != null) {
						desc = String
								.valueOf(course.semesters[semesterIndex].examGrade);
					} else {
						desc = "No Grade :(";
					}
				} else {
					if (course.semesters[semesterIndex].average != null) {
						desc = String
								.valueOf(course.semesters[semesterIndex].average);
					} else {
						desc = "No Grade :(";
					}
				}
				NoGradesCard card = new NoGradesCard(course.title, desc,
						"#000000", "#787878", false, false);
				cardUI.addCard(card);

				cardUI.refresh();
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
				// TODO Auto-generated method stub
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
					titleText = "Cycle "
							+ (cycleIndex + 1)
							+ " - "
							+ course.semesters[semester].cycles[cycle].average;
				} else {
					titleText = "Cycle " + (cycleIndex + 1);
				}
				title.setText(titleText);
				Log.d("CardUIGenerator",
						"What cyclefragment sees: "
								+ String.valueOf(courseIndex));
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

				Log.d("CardUIGenerator", "category cards are being made");
				if (categories.length > 0) {
					for (int i = 0; i < categories.length; i++) {
						Category category = categories[i];
						String title = category.title;
						if (category.title.length() > 0
								&& category.assignments.length > 0) {
							// DELIMROW separates rows, DELIMCOLUMN
							// separates
							// columns
							String desc = "";
							desc += "ASSIGNMENTDELIMCOLUMNPOINTS EARNEDDELIMCOLUMNPOINTS POSSIBLEDELIMROW";
							for (int d = 0; d < category.assignments.length; d++) {
								Assignment a = category.assignments[d];
								desc += a.title + "DELIMCOLUMN";
								desc += a.ptsEarned + "DELIMCOLUMN";
								desc += a.ptsPossible + "DELIMROW";
							}
							desc += "DELIMAVERAGE"
									+ String.valueOf(GradeCalc
											.categoryAverage(category.assignments));
							CardColorGenerator gen = new CardColorGenerator();
							String color = gen.getCardColor(i);

							CategoryCard card = new CategoryCard(title, desc,
									color, "#787878", false, false);
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
				Log.d("CardUIGens", String.valueOf(categories.length));
				if (categories.length == 0) {
					Log.d("CardUIGens", "Creating nogrades");
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

}
