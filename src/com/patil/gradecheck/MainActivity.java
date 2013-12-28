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
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

	HttpClient client;

	CardUI cardView;

	// Handler to make sure drawer closes smoothly
	Handler drawerHandler = new Handler();

	SettingsManager settingsManager;
	CardColorGenerator colorGenerator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setTitle("Overview");
		settingsManager = new SettingsManager(this);
		colorGenerator = new CardColorGenerator();
		currentTitle = "Overview";
		signInButton = (Button) findViewById(R.id.button_signin);
		
		// This is used to store persistent cookies
		Drawable drawable = getResources().getDrawable(getResources().getIdentifier("cookie_storage", "drawable", getPackageName()));

		startDisplayingGrades();

		makeDrawer();
	}

	public void startDisplayingGrades() {
		String[] credentials = settingsManager.getLoginInfo();
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
									String distr = "";
									if (district.getSelectedItem().toString()
											.equals("AISD")) {
										distr = "Austin";
									} else if (district.getSelectedItem()
											.toString().equals("RRISD")) {
										distr = "RoundRock";
									}
									settingsManager.saveLoginInfo(userName
											.getText().toString(), password
											.getText().toString(), studentId
											.toString(), distr);
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
			setTitle(courses.get(position - 1).title);
			// Check if we already have info, otherwise load the course info
			if (courses.get(position - 1).sixWeekGrades == null) {
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
		for (int i = 0; i < courses.size(); i++) {
			Course course = courses.get(i);
			String gradeDescription = "";
			int[] semesters = course.semesterAverages;
			int[] exams = course.examGrades;
			int[] sixWeeksAverages = course.sixWeeksAverages;
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
			cardView.addCard(new CourseCard(course.title, gradeDescription,
					color, "#787878", false, true));
		}
		cardView.refresh();
	}

	public void loadCourseInfo(int course, String school) {
		new CycleScrapeTask(this).execute(new String[] {
				String.valueOf(course), school });
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
		}

		return super.onOptionsItemSelected(item);
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

			VerifiedHttpClientFactory httpClientFactory = new VerifiedHttpClientFactory();
			client = httpClientFactory.getNewHttpClient();

			String html = "UNKNOWN_ERROR";

			if (school.equals("Austin")) {
				html = scrapeAustin(username, password, id, client);
			} else if (school.equals("RoundRock")) {
				html = scrapeRoundRock(username, password, id, client);
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
			Log.d("THEHTML", response);
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
			if (!html.equals("UNKNOWN_ERROR") && !html.equals("INVAILID_LOGIN")
					&& !html.equals("IO_EXCEPTION")
					&& !html.equals("UNSUPPORTED_ENCODING_EXCEPTION")
					&& !html.equals("CLIENT_PROTOCOL_EXCEPTION")
					&& !html.equals("URI_SYNTAX_EXCEPTION")) {
				CourseParser parser = new CourseParser(html);
				courses = parser.parseCourses();
				setupActionBar();
				makeCourseCards();
				dialog.dismiss();
			} else {
				dialog.dismiss();
				Toast.makeText(
						context,
						"Something went wrong. Make sure you're connected to the internet.",
						Toast.LENGTH_SHORT).show();
				startLogin();
			}
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
		public String scrapeAustin(String username, String password, String id,
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
			Log.d("RRISDSupport", gradeHTML);
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

		public String scrapeRoundRock(String username, String password,
				String id, HttpClient client) {
			URI loginURL;
			HttpPost loginPost;
			HttpResponse loginResponse;
			HttpEntity loginEntity;
			List<NameValuePair> loginPairs = new ArrayList<NameValuePair>();
			loginPairs.add(new BasicNameValuePair("ctl00$plnMain$txtLogin",
					username));
			loginPairs.add(new BasicNameValuePair("ctl00$plnMain$txtPassword",
					password));
			loginPairs.add(new BasicNameValuePair("student_id", String
					.valueOf(id)));
			loginPairs.add(new BasicNameValuePair("student_id", id));

			String gradeHTML = "UNKNOWN_ERROR";

			try {
				loginURL = new URI(
						"https://accesscenter.roundrockisd.org/homeaccess/default.aspx");
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
				Log.d("WHATTHEHECK", loginHTML);

				HttpPost gradeRequest;
				HttpResponse gradeResponse;

				try {
					if (!loginHTML.contains("Invalid")) {
						gradeRequest = new HttpPost(
								"https://accesscenter.roundrockisd.org/homeaccess/Student/DailySummary.aspx");
						List<NameValuePair> pairs = new ArrayList<NameValuePair>();
						pairs.add(new BasicNameValuePair("student_id", String
								.valueOf(id)));
						pairs.add(new BasicNameValuePair("student_id", id));
						gradeRequest.setEntity(new UrlEncodedFormEntity(pairs));
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
						String accessHTML = gradeBuilder.toString();
						Log.d("WHATTHEHECK", accessHTML);

						HttpPost realGradeRequest;
						HttpResponse realGradeResponse;
						try {
							realGradeRequest = new HttpPost(
									"https://accesscenter.roundrockisd.org/homeaccess/Student/Gradespeed.aspx?target=https://gradebook.roundrockisd.org/pc/displaygrades.aspx");
							realGradeRequest
									.setEntity(new UrlEncodedFormEntity(pairs));
							realGradeResponse = client
									.execute(realGradeRequest);
							InputStream realGradeStream = realGradeResponse
									.getEntity().getContent();
							BufferedReader realGradeReader = new BufferedReader(
									new InputStreamReader(realGradeStream));

							StringBuilder realGradeBuilder = new StringBuilder();
							String realGradeLine = null;
							while ((realGradeLine = realGradeReader.readLine()) != null) {
								realGradeBuilder.append(realGradeLine);
							}
							realGradeStream.close();
							String realGradeHTML = realGradeBuilder.toString();
							Log.d("WHATTHEHECK", realGradeHTML);

							HttpPost realRealGradeRequest;
							HttpResponse realRealGradeResponse;

							try {
								realRealGradeRequest = new HttpPost(
										"https://accesscenter.roundrockisd.org/homeaccess/Student/Gradespeed.aspx?target=https://gradebook.roundrockisd.org/pc/displaygrades.aspx");
								realRealGradeRequest
										.setEntity(new UrlEncodedFormEntity(
												pairs));
								realRealGradeResponse = client
										.execute(realRealGradeRequest);
								InputStream realRealGradeStream = realRealGradeResponse
										.getEntity().getContent();
								BufferedReader realRealGradeReader = new BufferedReader(
										new InputStreamReader(realRealGradeStream));

								StringBuilder realRealGradeBuilder = new StringBuilder();
								String realRealGradeLine = null;
								while ((realRealGradeLine = realRealGradeReader
										.readLine()) != null) {
									realRealGradeBuilder.append(realRealGradeLine);
								}
								realRealGradeStream.close();
								gradeHTML = realRealGradeBuilder
										.toString();
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
						}

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

			return "UNKNOWN_ERROR";
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
				Log.d("CourseParser", "Starting scrape");
				scrapeAustin(course, client);
			} else if (school.equals("RoundRock")) {

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
		public String scrapeAustin(int c, HttpClient client) {

			HttpGet request;
			HttpResponse resp = null;
			try {

				Course course = courses.get(c);
				String[] dataLinks = course.gradeLinks;
				CycleGrades[] cycles = new CycleGrades[6];
				for (int d = 0; d < dataLinks.length; d++) {
					Log.d("CourseParser", String.valueOf(d));
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
				courses.set(c, course);

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
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

		public String scrapeRoundRock(String username, String password,
				String id, HttpClient client, String link) {
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
				Course course = MainActivity.courses.get(courseIndex);

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
					if (course.examGrades[semesterIndex] != -1) {
						desc = String.valueOf(course.examGrades[semesterIndex]);
					} else {
						desc = "No Grade :(";
					}
				} else {
					if (course.semesterAverages[semesterIndex] != -1) {
						desc = String
								.valueOf(course.semesterAverages[semesterIndex]);
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
				Course course = MainActivity.courses.get(courseIndex);
				String titleText = "";
				if (course.sixWeeksAverages[cycleIndex] != -1) {
					titleText = "Cycle " + (cycleIndex + 1) + " - "
							+ course.sixWeeksAverages[cycleIndex];
				} else {
					titleText = "Cycle " + (cycleIndex + 1);
				}
				title.setText(titleText);
				Log.d("CardUIGenerator",
						"What cyclefragment sees: "
								+ String.valueOf(courseIndex));
				if (course.sixWeekGrades != null
						&& course.sixWeekGrades[cycleIndex] != null) {
					ArrayList<Category> categories = course.sixWeekGrades[cycleIndex].categories;
					makeCategoryCards(categories);
				} else {
					// create an arraylist of categories of size 0
					ArrayList<Category> categories = new ArrayList<Category>();
					makeCategoryCards(categories);

				}
			}

			public void makeCategoryCards(ArrayList<Category> categories) {
				cardUI = (CardUI) getView().findViewById(R.id.cardsview);
				cardUI.setSwipeable(false);

				Log.d("CardUIGenerator", "category cards are being made");
				if (categories.size() > 0) {
					for (int i = 0; i < categories.size(); i++) {
						Category category = categories.get(i);
						String title = category.title;
						if (category.title.length() > 0
								&& category.assignments.size() > 0) {
							// DELIMROW separates rows, DELIMCOLUMN separates
							// columns
							String desc = "";
							desc += "ASSIGNMENTDELIMCOLUMNPOINTS EARNEDDELIMCOLUMNPOINTS POSSIBLEDELIMROW";
							for (int d = 0; d < category.assignments.size(); d++) {
								Assignment a = category.assignments.get(d);
								desc += a.title + "DELIMCOLUMN";
								desc += a.ptsEarned + "DELIMCOLUMN";
								desc += a.ptsPossible + "DELIMROW";
							}
							CardColorGenerator gen = new CardColorGenerator();
							String color = gen.getCardColor(i);

							CategoryCard card = new CategoryCard(title, desc,
									color, "#787878", false, false);
							cardUI.addCard(card);
						} else if (category.assignments.size() == 0) {
							// There aren't any grades for the category, so
							// create a nogrades card
							NoGradesCard card = new NoGradesCard(title,
									"No Grades :(", "#787878", "#787878",
									false, false);
							cardUI.addCard(card);
						}
					}
				}
				Log.d("CardUIGens", String.valueOf(categories.size()));
				if (categories.size() == 0) {
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
