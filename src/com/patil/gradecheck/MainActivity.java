package com.patil.gradecheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.views.CardUI;

public class MainActivity extends FragmentActivity {

	public static ArrayList<Course> courses;
	ListView drawerList;
	DrawerLayout drawerLayout;
	ActionBarDrawerToggle drawerToggle;

	String currentTitle;

	Button signInButton;

	CardUI cardView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setTitle("Overview");

		currentTitle = "Overview";
		signInButton = (Button) findViewById(R.id.button_signin);

		String[] credentials = getCredentials();

		if (credentials[0].length() > 0 && credentials[1].length() > 0
				&& credentials[2].length() > 0 && credentials[3].length() > 0) {
			new ScrapeTask(this).execute(new String[] { credentials[0],
					credentials[1], credentials[2], credentials[3] });
			signInButton.setVisibility(View.GONE);
		} else {
			/*
			 * Prompt for login and set the login button as visible.
			 */
			signInButton.setVisibility(View.VISIBLE);
			startLogin();
		}
		makeDrawer();
	}

	/*
	 * Helper class that returns the credentials of the student.
	 */
	public String[] getCredentials() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String[] credentials = new String[4];
		String user = prefs.getString("user", "");
		String pass = prefs.getString("pass", "");
		String district = prefs.getString("district", "");
		String id = prefs.getString("id", "");
		credentials[0] = user;
		credentials[1] = pass;
		credentials[2] = id;
		credentials[3] = district;
		return credentials;
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
				.setView(textEntryView)
				.setPositiveButton("Login",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (userName.getText().length() > 0
										&& password.getText().length() > 0
										&& studentId.length() > 0
										&& district.getSelectedItem() != null) {
									saveCredentials(userName.getText()
											.toString(), password.getText()
											.toString(), studentId.toString(),
											district.getSelectedItem()
													.toString());
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
	 * Uses the newly loaded assignments data to create fragments for each part
	 * of the slideout navigation drawer.
	 */
	public void createSlideout() {

	}

	/*
	 * Saves credentials of student to SharedPreferences.
	 * 
	 * @param The username
	 * 
	 * @param The password
	 * 
	 * @param The student id
	 * 
	 * @param The district (AISD or RRISD)
	 */
	public void saveCredentials(String user, String pass, String id,
			String district) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor edit = prefs.edit();
		edit.putString("user", user);
		edit.putString("pass", pass);
		edit.putString("id", id);
		edit.putString("district", district);
		edit.commit();
		restartActivity();
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
			Fragment fragment = new ClassFragment();

			Bundle args = new Bundle();
			args.putInt(ClassFragment.INDEX, position - 1);
			fragment.setArguments(args);
			// Insert the fragment by replacing any existing fragment
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();

			// Highlight the selected item, update the title, and close the
			// drawer
			drawerList.setItemChecked(position, true);
			setTitle(courses.get(position - 1).title);
			drawerLayout.closeDrawer(drawerList);

		}
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
		for (int i = 0; i < courses.size(); i++) {
			Course course = courses.get(i);
			String gradeSummaryFirstSemester = "";
			int[] exams = course.examGrades;
			int[] semesters = course.semesterAverages;
			for (int d = 0; d < 3; d++) {
				if (course.sixWeeksAverages[d] != -1) {

					gradeSummaryFirstSemester += "Cycle " + (d + 1) + ": "
							+ String.valueOf(course.sixWeeksAverages[d]) + "\n";
				} else {
					gradeSummaryFirstSemester += "Cycle " + (d + 1)
							+ ": N/A \n";
				}
			}

			String gradeSummarySecondSemester = "";
			for (int d = 3; d < course.sixWeeksAverages.length; d++) {
				if (course.sixWeeksAverages[d] != -1) {

					gradeSummarySecondSemester += "Cycle " + (d + 1) + ": "
							+ String.valueOf(course.sixWeeksAverages[d]) + "\n";
				} else {
					gradeSummarySecondSemester += "Cycle " + (d + 1)
							+ ": N/A \n";
				}
			}

			if (exams[0] == -1) {
				gradeSummaryFirstSemester += "Exam 1: N/A\n";
			} else {
				gradeSummaryFirstSemester += "Exam 1: "
						+ String.valueOf(exams[0]) + "\n";
			}

			if (semesters[0] == -1) {
				gradeSummaryFirstSemester += "Semester 1: N/A";
			} else {
				gradeSummaryFirstSemester += "Semester 1: "
						+ String.valueOf(semesters[0]);
			}

			if (exams[1] == -1) {
				gradeSummarySecondSemester += "Exam 2: N/A\n";
			} else {
				gradeSummarySecondSemester += "Exam 2: "
						+ String.valueOf(exams[1]) + "\n";
			}

			if (semesters[1] == -1) {
				gradeSummarySecondSemester += "Semester 2: N/A";
			} else {
				gradeSummarySecondSemester += "Semester 2: "
						+ String.valueOf(semesters[1]);
			}

			// delimeter of DELIM
			String desc = gradeSummaryFirstSemester + "DELIM"
					+ gradeSummarySecondSemester;
			String color = getCardColor(i);
			cardView.addCard(new CourseCard(course.title, desc, color,
					"#787878", false, true));
		}
		cardView.refresh();
	}

	/*
	 * Returns a color for each class.
	 */
	public String getCardColor(int i) {
		String color;
		if (i == 0) {
			color = "#009bce";
		} else if (i == 1) {
			color = "#9c34d0";
		} else if (i == 2) {
			color = "#5f8f00";
		} else if (i == 3) {
			color = "#fd8700";
		} else if (i == 4) {
			color = "#d20000";
		} else if (i == 5) {
			color = "#33b5e5";
		} else if (i == 6) {
			color = "#aa6fc7";
		} else if (i == 7) {
			color = "#9fd400";
		} else if (i == 8) {
			color = "#ffbd38";
		} else if (i == 9) {
			color = "#ff5252";
		} else {
			color = "#020202";
		}
		return color;
	}

	public void loadCycleInfo() {
		String[] credentials = getCredentials();
		new CycleScrapeTask(this).execute(new String[] { credentials[0],
				credentials[1], credentials[2], credentials[3] });
	}

	public void setupActionBar() {
		// Create navigation drawer of courses

		// Make array of all of the headings for the drawer
		String[] titles = new String[courses.size() + 1];
		titles[0] = "Overview";
		for (int i = 0; i < courses.size(); i++) {
			titles[i + 1] = courses.get(i).title;
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
		if (item.getItemId() == R.id.action_refresh) {
			restartActivity();
		}
		if (item.getItemId() == R.id.action_signout) {
			eraseCredentials();
			restartActivity();
		}

		return super.onOptionsItemSelected(item);
	}

	/*
	 * Helper method that erases credentials.
	 */
	public void eraseCredentials() {
		saveCredentials("", "", "", "");
	}

	public class ScrapeTask extends AsyncTask<String, Void, String> {

		ProgressDialog dialog;
		Context context;

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
		 * @param[3] The id of the school logging in with. "AISD" or "RRISD".
		 * 
		 * @return A String of the webpage HTML.
		 */
		protected String doInBackground(String... information) {
			String username = information[0];
			String password = information[1];
			String id = information[2];
			String school = information[3];

			VerifiedHttpClientFactory httpClientFactory = new VerifiedHttpClientFactory();
			HttpClient client = httpClientFactory.getNewHttpClient();

			String html = "UNKNOWN_ERROR";

			if (school.equals("AISD")) {
				html = scrapeAISD(username, password, id, client);
			} else if (school.equals("RRISD")) {
				html = scrapeRRISD(username, password, id, client);
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
				// HTML scraping worked
				parseHTML(response);
			}
		}

		/*
		 * Parses general grade info using JSoup. The names and six weeks
		 * averages of each grade are parsed and loaded into an ArrayList of
		 * courses. Then, individual six weeks details (assignments) are
		 * scraped. Then sets up UI.
		 * 
		 * @param The HTML of the response.
		 */
		public void parseHTML(String html) {
			CourseParser parser = new CourseParser(html);
			courses = parser.parseCourses();
			loadCycleInfo();
			setupActionBar();
			makeCourseCards();
			dialog.dismiss();
		}

		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage("Loading");
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
		public String scrapeAISD(String username, String password, String id,
				HttpClient client) {
			URI loginURL;
			HttpPost loginPost;
			HttpResponse loginResponse;
			HttpEntity loginEntity;
			List<NameValuePair> loginPairs = new ArrayList<NameValuePair>();
			loginPairs.add(new BasicNameValuePair("txtUserName", username));
			loginPairs.add(new BasicNameValuePair("txtPassword", password));

			String gradeHTML = "UNKNOWN_ERROR";

			try {
				loginURL = new URI("https://gradespeed.austinisd.org/pc/");
				loginPost = new HttpPost(loginURL);
				loginPost.setEntity(new UrlEncodedFormEntity(loginPairs));
				loginResponse = client.execute(loginPost);
				loginEntity = loginResponse.getEntity();

				InputStream loginStream = loginEntity.getContent();
				BufferedReader loginReader = new BufferedReader(
						new InputStreamReader(loginStream));
				StringBuilder loginBuilder = new StringBuilder();
				String loginLine = null;
				while ((loginLine = loginReader.readLine()) != null) {
					loginBuilder.append(loginLine);
				}
				String loginHTML = loginBuilder.toString();

				HttpPost gradeRequest;
				HttpResponse gradeResponse;

				try {
					if (!loginHTML.contains("Invalid")) {
						gradeRequest = new HttpPost(
								"https://gradespeed.austinisd.org/pc/ParentStudentGrades.aspx");
						gradeResponse = client.execute(gradeRequest);

						InputStream gradeStream = gradeResponse.getEntity()
								.getContent();
						BufferedReader gradeReader = new BufferedReader(
								new InputStreamReader(gradeStream));

						StringBuilder gradeBuilder = new StringBuilder();
						String gradeLine = null;
						while ((gradeLine = gradeReader.readLine()) != null) {
							gradeBuilder.append(gradeLine);
						}
						gradeStream.close();
						gradeHTML = gradeBuilder.toString();

					} else {
						gradeHTML = "INVALID_LOGIN";
					}
				} catch (UnsupportedEncodingException e) {
					gradeHTML = "UNSUPPORTED_ENCODING_EXCEPTION";
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					gradeHTML = "CLIENT_PROTOCOL_EXCEPTION";
					e.printStackTrace();
				} catch (IOException e) {
					gradeHTML = "IO_EXCEPTION";
					e.printStackTrace();
				}
			} catch (UnsupportedEncodingException e) {
				gradeHTML = "UNSUPPORTED_ENCODING_EXCEPTION";
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				gradeHTML = "CLIENT_PROTOCOL_EXCEPTION";
				e.printStackTrace();
			} catch (IOException e) {
				gradeHTML = "IO_EXCEPTION";
				e.printStackTrace();
			} catch (URISyntaxException e) {
				gradeHTML = "URI_SYNTAX_EXCEPTION";
				e.printStackTrace();
			}

			return gradeHTML;
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

		public String scrapeRRISD(String username, String password, String id,
				HttpClient client) {
			return "INVALID_LOGIN";
		}

	}

	public class CycleScrapeTask extends AsyncTask<String, Void, String> {

		ProgressDialog dialog;
		Context context;

		public CycleScrapeTask(Context context) {
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
		 * @param[3] The id of the school logging in with. "AISD" or "RRISD".
		 * 
		 * @return A String of the webpage HTML.
		 */
		protected String doInBackground(String... information) {
			String username = information[0];
			String password = information[1];
			String id = information[2];
			String school = information[3];

			VerifiedHttpClientFactory httpClientFactory = new VerifiedHttpClientFactory();
			HttpClient client = httpClientFactory.getNewHttpClient();

			if (school.equals("AISD")) {
				Log.d("CourseParser", "Starting scrape");
				scrapeAISD(username, password, id, client);
			} else if (school.equals("RRISD")) {

			}

			return "";
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
			// Make each part of the slide out navigation
			createSlideout();
		}

		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage("Loading");
			dialog.show();
		}

		/*
		 * Scrapes AISD for a cycle info.
		 * 
		 * @param The username.
		 * 
		 * @param The password.
		 * 
		 * @param The student id.
		 * 
		 * @param The HttpClient.
		 * 
		 * @return The HTML of AISD scrape with specific cycle info.
		 */
		public String scrapeAISD(String username, String password, String id,
				HttpClient client) {

			URI loginURL;
			HttpPost loginPost;
			HttpResponse loginResponse;
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("txtUserName", username));
			nameValuePairs.add(new BasicNameValuePair("txtPassword", password));

			HttpGet request;
			HttpResponse resp = null;
			try {
				loginURL = new URI("https://gradespeed.austinisd.org/pc/");
				loginPost = new HttpPost(loginURL);
				loginPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				loginResponse = client.execute(loginPost);

				// Load in each cycle details
				for (int i = 0; i < courses.size(); i++) {
					Course course = courses.get(i);
					String[] dataLinks = course.gradeLinks;
					CycleGrades[] cycles = new CycleGrades[6];
					for (int d = 0; d < dataLinks.length; d++) {
						Log.d("CourseParser",
								String.valueOf(i) + " " + String.valueOf(d));
						String link = dataLinks[d];
						if (!link.equals("NO_GRADE")) {
							// Get HTML
							String gradesHTML = "";
							String gradeURL = "https://gradespeed.austinisd.org/pc/ParentStudentGrades.aspx"
									+ link;
							Log.d("CourseParser", "Parsing with link: " + link);
							request = new HttpGet(gradeURL);
							resp = client.execute(request);

							InputStream in = resp.getEntity().getContent();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(in));
							StringBuilder str = new StringBuilder();
							String line = null;
							while ((line = reader.readLine()) != null) {
								str.append(line);
							}
							in.close();
							gradesHTML = str.toString();
							// Parse through each HTML and create grades object
							CycleParser parser = new CycleParser(gradesHTML);
							CycleGrades grades = parser.parseCycle();
							grades.average = course.sixWeeksAverages[d];
							grades.title = course.title;
							cycles[d] = grades;
						} else {
							Log.d("CourseParser", "Not parsing, NO_GRADE");
							cycles[d] = null;
						}
					}

					course.setSixWeekGrades(cycles);
					courses.set(i, course);
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
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

		public String scrapeRRISD(String username, String password, String id,
				HttpClient client, String link) {
			return "INVALID_LOGIN";
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
		}

		// Since this is an object collection, use a FragmentStatePagerAdapter,
		// and NOT a FragmentPagerAdapter.
		public class CollectionPagerAdapter extends FragmentStatePagerAdapter {
			public CollectionPagerAdapter(FragmentManager fm) {
				super(fm);
			}

			@Override
			public Fragment getItem(int i) {
				Fragment fragment = new CycleFragment();
				Bundle args = new Bundle();
				// Our object is just an integer :-P
				args.putInt(CycleFragment.INDEX_COURSE, index);
				Log.d("CardUIGenerator", String.valueOf(i));
				args.putInt(CycleFragment.INDEX_CYCLE, i);
				fragment.setArguments(args);
				return fragment;
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
				// TODO Auto-generated method stub
				super.onViewCreated(view, savedInstanceState);
				Bundle args = getArguments();
				courseIndex = args.getInt(INDEX_COURSE);
				cycleIndex = args.getInt(INDEX_CYCLE);
				title = (TextView) getView().findViewById(R.id.title_text);
				Course course = MainActivity.courses.get(courseIndex);
				String titleText = "Cycle " + (cycleIndex + 1) + " - "
						+ course.sixWeeksAverages[cycleIndex];
				title.setText(titleText);
				Log.d("CardUIGenerator", "What cyclefragment sees: " + String.valueOf(courseIndex));
				if (course.sixWeekGrades != null && course.sixWeekGrades[cycleIndex] != null) {
					ArrayList<Category> categories = course.sixWeekGrades[cycleIndex].categories;
					makeCategoryCards(categories);
				} else if(course.sixWeekGrades == null) {
					Log.d("CardUIGenerator", "sixWeekGrades are null");
				} else{
					Log.d("CardUIGenerator", "sixWeekGrades at cycleindex are null");
				}
			}

			public void makeCategoryCards(ArrayList<Category> categories) {
				cardUI = (CardUI) getView().findViewById(R.id.cardsview);
				cardUI.setSwipeable(false);
				
				Log.d("CardUIGenerator", "category cards are being made");

				for (int i = 0; i < categories.size(); i++) {
					Category category = categories.get(i);
					String title = category.title;

					// DELIMROW separates rows, DELIMCOLUMN separates columns
					String desc = "";
					desc += "ASSIGNMENTDELIMCOLUMNPOINTS EARNEDDELIMCOLUMNPOINTS POSSIBLEDELIMROW";
					for (int d = 0; d < category.assignments.size(); d++) {
						Assignment a = category.assignments.get(d);
						desc += a.title + "DELIMCOLUMN";
						desc += a.ptsEarned + "DELIMCOLUMN";
						desc += a.ptsPossible + "DELIMROW";
					}

					String color = getCardColor(i);

					CategoryCard card = new CategoryCard(title, desc, color,
							"#787878", false, false);
					cardUI.addCard(card);
				}

				cardUI.refresh();
			}

			/*
			 * Returns a color for each class.
			 */
			public String getCardColor(int i) {
				String color;
				if (i == 0) {
					color = "#009bce";
				} else if (i == 1) {
					color = "#9c34d0";
				} else if (i == 2) {
					color = "#5f8f00";
				} else if (i == 3) {
					color = "#fd8700";
				} else if (i == 4) {
					color = "#d20000";
				} else if (i == 5) {
					color = "#33b5e5";
				} else if (i == 6) {
					color = "#aa6fc7";
				} else if (i == 7) {
					color = "#9fd400";
				} else if (i == 8) {
					color = "#ffbd38";
				} else if (i == 9) {
					color = "#ff5252";
				} else {
					color = "#020202";
				}
				return color;
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
