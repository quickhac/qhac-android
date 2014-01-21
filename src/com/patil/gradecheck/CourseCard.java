package com.patil.gradecheck;

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

public class CourseCard extends RecyclableCard {

	TableLayout gradeTable;
	TextView titleView;
	ImageView stripe;

	public CourseCard(String title, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable) {
		super(title, description, color, titleColor, hasOverflow, isClickable);
	}

	public String getCardTitle() {
		return titlePlay;
	}

	@Override
	protected void applyTo(View convertView) {
		gradeTable = (TableLayout) convertView
				.findViewById(R.id.gradeTable);
		titleView = (TextView) convertView.findViewById(R.id.title);
		stripe = (ImageView) convertView.findViewById(R.id.stripe);

		Context context = convertView.getContext();
		Typeface sansSerifLight = Typeface.create("sans-serif-light",
				Typeface.NORMAL);

		titleView.setText(titlePlay);
		titleView.setTextColor(Color.parseColor(titleColor));

		String[] firstSemesterGrades = description.split("DELIMROW")[0]
				.split("DELIMCOLUMN");
		String[] secondSemesterGrades = description.split("DELIMROW")[1]
				.split("DELIMCOLUMN");

		TableRow row1 = new TableRow(context);
		row1.setPadding(0, 5, 0, 5);
		row1.setGravity(Gravity.CENTER);
		ColorGenerator generator = new ColorGenerator();
		TableRow row2 = new TableRow(context);
		row2.setPadding(0, 5, 0, 5);
		row2.setGravity(Gravity.CENTER);
		for (int i = 0; i < firstSemesterGrades.length; i++) {
			TextView text = new TextView(context);
			text.setText(firstSemesterGrades[i]);
			if (!firstSemesterGrades[i].equals("-")) {

				int[] values = generator.getGradeColor(Integer
						.parseInt(firstSemesterGrades[i]));
				text.setBackgroundColor(Color.rgb(values[0], values[1],
						values[2]));
			}
			text.setTypeface(sansSerifLight);
			text.setTextSize(24);
			text.setGravity(Gravity.CENTER);
			row1.addView(text);

			// now do second semester
			TextView text2 = new TextView(context);
			text2.setText(secondSemesterGrades[i]);
			if (!secondSemesterGrades[i].equals("-")) {

				int[] values = generator.getGradeColor(Integer
						.parseInt(secondSemesterGrades[i]));
				text2.setBackgroundColor(Color.rgb(values[0], values[1],
						values[2]));
			}
			text2.setTextSize(24);
			text2.setTypeface(sansSerifLight);
			text2.setGravity(Gravity.CENTER);
			text2.setClickable(true);
			row2.addView(text2);

		}
		gradeTable.addView(row1, 1);
		gradeTable.addView(row2);

		stripe.setBackgroundColor(Color.parseColor(color));
		if (isClickable == true)
			((LinearLayout) convertView.findViewById(R.id.contentLayout))
					.setBackgroundResource(R.drawable.selectable_background_cardbank);
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.card_course;
	}

}
