package com.patil.quickhac;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.fima.cardsui.objects.RecyclableCard;
import com.quickhac.common.data.Course;
import com.quickhac.common.data.Cycle;
import com.quickhac.common.data.GradeValue;
import com.quickhac.common.data.Semester;

public class CourseCard extends RecyclableCard {

	TableLayout gradeTable;
	TextView titleView;
	ImageView stripe;
	ColorGenerator generator;
	SettingsManager settingsManager;

	public CourseCard(String title, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable) {
		super(title, description, color, titleColor, hasOverflow, isClickable);
	}

	public String getCardTitle() {
		return titlePlay;
	}

	@Override
	protected void applyTo(View convertView) {
		Course course = (Course) getData();
		Context context = convertView.getContext();
		settingsManager = new SettingsManager(context);
		generator = new ColorGenerator(context);
		makeTitle(convertView);
		makeStripe(convertView);
		makeGradeTable(convertView, course, context);
		makeClickable(convertView);
	}

	public void makeClickable(View convertView) {
		if (isClickable) {
			((LinearLayout) convertView.findViewById(R.id.contentLayout))
					.setBackgroundResource(R.drawable.selectable_background_cardbank);
		}
	}

	public void makeGradeTable(View convertView, Course course, Context context) {
		gradeTable = (TableLayout) convertView.findViewById(R.id.gradeTable);
		Typeface sansSerifLight = Typeface.create("sans-serif-light",
				Typeface.NORMAL);
		// Go through each semester
		for (int semesterIndex = 0; semesterIndex < course.semesters.length; semesterIndex++) {
			Semester semester = course.semesters[semesterIndex];
			TableRow semesterHeader = new TableRow(context);
			semesterHeader.setPadding(0, 0, 0, 5);
			TableRow semesterRow = new TableRow(context);
			// Go through each cycle for each semester
			for (int cycleIndex = 0; cycleIndex < semester.cycles.length; cycleIndex++) {
				Cycle cycle = semester.cycles[cycleIndex];
				// Add to the semester header
				TextView headerText = new TextView(context);
				// Find the right cycle number (ex. Cycle 2)
				int cycleNumber = (semesterIndex * semester.cycles.length)
						+ cycleIndex + 1;
				String cycleHeader = "Cycle " + String.valueOf(cycleNumber);
				headerText.setText(cycleHeader);
				headerText.setGravity(Gravity.CENTER_HORIZONTAL);
				headerText.setTypeface(sansSerifLight);
				headerText.setTextSize(14);
				semesterHeader.addView(headerText);
				// Add to the semester row
				TextView cycleText = new TextView(context);
				String cycleGrade = "";
				if (cycle.average != null) {
					cycleGrade = cycle.average.toString();
					if (settingsManager.isGradeColorHighlightEnabled()) {
						cycleText
								.setBackgroundColor(getGradeColor(cycle.average));
					}
				}
				cycleText.setText(cycleGrade);
				cycleText.setPadding(0, 5, 0, 5);
				cycleText.setTypeface(sansSerifLight);
				cycleText.setTextSize(24);
				cycleText.setGravity(Gravity.CENTER);
				semesterRow.addView(cycleText);
			}
			// only make exam and semester averages if not elementary grades
			if (!isElementaryGrades(course)) {
				// Make exam related stuffs
				// Header
				TextView examHeader = new TextView(context);
				examHeader.setText("Exam " + String.valueOf(semesterIndex + 1));
				examHeader.setGravity(Gravity.CENTER_HORIZONTAL);
				examHeader.setTypeface(sansSerifLight);
				examHeader.setTextSize(14);
				semesterHeader.addView(examHeader);
				// Grade
				TextView examText = new TextView(context);
				String examGrade = "";
				if (semester.examGrade != null) {
					examGrade = semester.examGrade.toString();
					if (settingsManager.isGradeColorHighlightEnabled()) {
						examText.setBackgroundColor(getGradeColor(semester.examGrade));
					}
				}

				examText.setPadding(0, 5, 0, 5);
				examText.setText(examGrade);
				examText.setTypeface(sansSerifLight);
				examText.setTextSize(24);
				examText.setGravity(Gravity.CENTER);
				semesterRow.addView(examText);

				// Make semester related stuffs
				// Header
				TextView averageHeader = new TextView(context);
				averageHeader.setText("Average");
				averageHeader.setGravity(Gravity.CENTER_HORIZONTAL);
				averageHeader.setTypeface(sansSerifLight);
				averageHeader.setTextSize(14);
				semesterHeader.addView(averageHeader);
				// Grade
				TextView averageText = new TextView(context);
				String semesterGrade = "";
				if (semester.average != null) {
					semesterGrade = semester.average.toString();
					if (settingsManager.isGradeColorHighlightEnabled()) {
						averageText
								.setBackgroundColor(getGradeColor(semester.average));
					}
				}
				averageText.setPadding(0, 5, 0, 5);
				averageText.setText(semesterGrade);
				averageText.setTypeface(sansSerifLight);
				averageText.setTextSize(24);
				averageText.setGravity(Gravity.CENTER);
				semesterRow.addView(averageText);
			}

			gradeTable.addView(semesterHeader);
			gradeTable.addView(semesterRow);
		}
	}

	public int getGradeColor(GradeValue value) {
		// default of white
		int[] values = new int[] { 255, 255, 255 };
		if (value.type == GradeValue.TYPE_DOUBLE) {
			values = generator.getGradeColorNumber((int) value.value_d, color);
		} else if (value.type == GradeValue.TYPE_INTEGER) {
			values = generator.getGradeColorNumber(value.value, color);
		} else if (value.type == GradeValue.TYPE_LETTER) {
			values = generator.getGradeColorLetter(value.value, color);
		}
		return Color.rgb(values[0], values[1], values[2]);
	}

	public void makeTitle(View convertView) {
		titleView = (TextView) convertView.findViewById(R.id.title);
		titleView.setText(titlePlay);
		titleView.setTextColor(Color.parseColor(titleColor));
	}

	public void makeStripe(View convertView) {
		stripe = (ImageView) convertView.findViewById(R.id.stripe);
		stripe.setBackgroundColor(Color.parseColor(color));
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.card_course;
	}

	// checks to see if a grade is for elementary school by seeing if there are
	// any letter grades
	public boolean isElementaryGrades(Course course) {
		for (int semesterIndex = 0; semesterIndex < course.semesters.length; semesterIndex++) {
			Semester semester = course.semesters[semesterIndex];
			if (semester != null) {
				for (int cycleIndex = 0; cycleIndex < semester.cycles.length; cycleIndex++) {
					Cycle cycle = semester.cycles[cycleIndex];
					if (cycle != null && cycle.average != null) {
						if (cycle.average.type == GradeValue.TYPE_LETTER) {
							return true;
						}
					}
				}
				if (semester.average != null) {
					if (semester.average.type == GradeValue.TYPE_LETTER) {
						return true;
					}
				}
				if (semester.examGrade != null) {
					if (semester.examGrade.type == GradeValue.TYPE_LETTER) {
						return true;
					}
				}
			}
		}
		return false;
	}

}