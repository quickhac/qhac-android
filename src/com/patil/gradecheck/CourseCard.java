package com.patil.gradecheck;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.fima.cardsui.objects.RecyclableCard;

public class CourseCard extends RecyclableCard {

	String title;

	public CourseCard(String title, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable) {
		super(title, description, color, titleColor, hasOverflow, isClickable);
		this.title = title;
	}

	public String getCardTitle() {
		return title;
	}

	@Override
	protected void applyTo(View convertView) {
		TableLayout semester1Table = (TableLayout) convertView
				.findViewById(R.id.semester1Table);
		TableLayout semester2Table = (TableLayout) convertView
				.findViewById(R.id.semester2Table);
		((TextView) convertView.findViewById(R.id.title)).setText(titlePlay);
		((TextView) convertView.findViewById(R.id.title)).setTextColor(Color
				.parseColor(titleColor));

		String[] firstSemesterGrades = description.split("DELIMROW")[0]
				.split("DELIMCOLUMN");
		String[] secondSemesterGrades = description.split("DELIMROW")[1]
				.split("DELIMCOLUMN");

		TableRow row1 = new TableRow(convertView.getContext());
		TableRow.LayoutParams lp1 = new TableRow.LayoutParams(
				TableRow.LayoutParams.WRAP_CONTENT);
		row1.setPadding(0, 5, 0, 5);
		row1.setLayoutParams(lp1);
		row1.setGravity(Gravity.CENTER);
		for (int i = 0; i < firstSemesterGrades.length; i++) {
			TextView text = new TextView(convertView.getContext());
			text.setText(firstSemesterGrades[i]);
			if (!firstSemesterGrades[i].equals("-")) {

				String rgb = new ColorGenerator().getGradeColor(Integer
						.parseInt(firstSemesterGrades[i]));
				String[] values = rgb.split(",");
				Log.d("colorizer", rgb);
				int r = Integer.parseInt(values[0]);
				int g = Integer.parseInt(values[1]);
				int b = Integer.parseInt(values[2]);
				text.setBackgroundColor(Color.rgb(r, g, b));
			}
			text.setTypeface(Typeface.create("sans-serif-light",
					Typeface.NORMAL));
			text.setTextSize(24);
			text.setGravity(Gravity.CENTER);
			row1.addView(text);
		}
		semester1Table.addView(row1);

		TableRow row2 = new TableRow(convertView.getContext());
		TableRow.LayoutParams lp2 = new TableRow.LayoutParams(
				TableRow.LayoutParams.WRAP_CONTENT);
		row2.setPadding(0, 5, 0, 5);
		row2.setLayoutParams(lp2);
		row2.setGravity(Gravity.CENTER);
		for (int i = 0; i < secondSemesterGrades.length; i++) {

			TextView text = new TextView(convertView.getContext());
			text.setText(secondSemesterGrades[i]);
			if (!secondSemesterGrades[i].equals("-")) {
				
				String rgb = new ColorGenerator().getGradeColor(Integer
						.parseInt(secondSemesterGrades[i]));
				Log.d("colorizer", rgb);
				String[] values = rgb.split(",");
				int r = Integer.parseInt(values[0]);
				int g = Integer.parseInt(values[1]);
				int b = Integer.parseInt(values[2]);
				text.setBackgroundColor(Color.rgb(r, g, b));
			}
			text.setTextSize(24);
			text.setTypeface(Typeface.create("sans-serif-light",
					Typeface.NORMAL));
			text.setGravity(Gravity.CENTER);
			text.setClickable(true);
			row2.addView(text);

		}
		semester2Table.addView(row2);

		((ImageView) convertView.findViewById(R.id.stripe))
				.setBackgroundColor(Color.parseColor(color));

		if (isClickable == true)
			((LinearLayout) convertView.findViewById(R.id.contentLayout))
					.setBackgroundResource(R.drawable.selectable_background_cardbank);

		if (hasOverflow == true)
			((ImageView) convertView.findViewById(R.id.overflow))
					.setVisibility(View.VISIBLE);
		else
			((ImageView) convertView.findViewById(R.id.overflow))
					.setVisibility(View.GONE);
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.card_course;
	}

}
