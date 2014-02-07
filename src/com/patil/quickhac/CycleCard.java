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

public class CycleCard extends RecyclableCard {

	TableLayout gradeTable;
	TextView titleView;
	ImageView stripe;
	ColorGenerator generator;
	SettingsManager settingsManager;

	public CycleCard(String title, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable) {
		super(title, description, color, titleColor, hasOverflow, isClickable);
	}

	public String getCardTitle() {
		return titlePlay;
	}

	@Override
	protected void applyTo(View convertView) {
		GradeValue[] grades = (GradeValue[]) getData();
		Context context = convertView.getContext();
		generator = new ColorGenerator(context);
		settingsManager = new SettingsManager(context);
		// makeTitle(convertView);
		makeStripe(convertView);
		makeGradeTable(convertView, grades, context);
		makeClickable(convertView);
	}

	public void makeClickable(View convertView) {
		if (isClickable) {
			((LinearLayout) convertView.findViewById(R.id.contentLayout))
					.setBackgroundResource(R.drawable.selectable_background_cardbank);
		}
	}

	public void makeGradeTable(View convertView, GradeValue[] grades,
			Context context) {
		Typeface sansSerifLight = Typeface.create("sans-serif-light",
				Typeface.NORMAL);
		gradeTable = (TableLayout) convertView.findViewById(R.id.gradeTable);
		TableRow headerRow = new TableRow(context);
		headerRow.setPadding(0, 0, 0, 5);
		TableRow gradeRow = new TableRow(context);
		for (int i = 0; i < grades.length; i++) {
			TextView headerText = new TextView(context);
			if (i == 0) {
				headerText.setText("Average");
			} else if (i == 1) {
				headerText.setText("Semester Average");
			}
			headerText.setGravity(Gravity.CENTER_HORIZONTAL);
			headerText.setTypeface(sansSerifLight);
			headerText.setTextSize(18);
			headerRow.addView(headerText);
			TextView gradeText = new TextView(context);
			gradeText.setText(grades[i].toString());
			if (settingsManager.isGradeColorHighlightEnabled()) {
				gradeText.setBackgroundColor(getGradeColor(grades[i]));
			}
			gradeText.setPadding(0, 5, 0, 5);
			gradeText.setTypeface(sansSerifLight);
			gradeText.setTextSize(32);
			gradeText.setGravity(Gravity.CENTER);
			gradeRow.addView(gradeText);
		}
		gradeTable.addView(headerRow);
		gradeTable.addView(gradeRow);
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

	/*
	 * public void makeTitle(View convertView) { titleView = (TextView)
	 * convertView.findViewById(R.id.title); titleView.setText(titlePlay);
	 * titleView.setTextColor(Color.parseColor(titleColor)); }
	 */

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