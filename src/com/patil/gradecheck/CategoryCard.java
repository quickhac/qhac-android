package com.patil.gradecheck;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.fima.cardsui.objects.RecyclableCard;

public class CategoryCard extends RecyclableCard {

	public CategoryCard(String title, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable) {
		super(title, description, color, titleColor, hasOverflow, isClickable);
	}

	@Override
	protected void applyTo(View convertView) {
		TableLayout layout = (TableLayout) convertView
				.findViewById(R.id.gradeTable);
		((TextView) convertView.findViewById(R.id.title)).setText(titlePlay);
		((TextView) convertView.findViewById(R.id.title)).setTextColor(Color
				.parseColor(titleColor));
		((TextView) convertView.findViewById(R.id.average)).setText("Average: " + description.split("DELIMAVERAGE")[1]);
		String[] grows = description.split("DELIMAVERAGE");
		String[] rows = grows[0].split("DELIMROW");
		for (int i = 1; i < rows.length; i++) {
			String[] columns = rows[i].split("DELIMCOLUMN");

			TableRow row = new TableRow(convertView.getContext());
			TableRow.LayoutParams lp = new TableRow.LayoutParams(
					TableRow.LayoutParams.WRAP_CONTENT);
			row.setPadding(0, 5, 0, 5);
			row.setLayoutParams(lp);
			/*
			 * // alternate colors if (i % 2 == 0) {
			 * row.setBackgroundColor(Color.LTGRAY); } else { }
			 */
			row.setGravity(Gravity.CENTER);
			TextView name = new TextView(convertView.getContext());
			name.setPadding(10, 0, 0, 0);
			TextView grade = new TextView(convertView.getContext());
			TextView possible = new TextView(convertView.getContext());
			possible.setPadding(0, 0, 10, 0);
			name.setText(columns[0]);
			name.setTextSize(16);

			if (columns[1].equals("-1")) {
				grade.setText("N/A");
			} else if(columns[1].equals("-2")) {
				grade.setText("N/A");
			} else {
				grade.setText(columns[1]);
			}
			grade.setGravity(Gravity.CENTER_HORIZONTAL);
			grade.setTextSize(16);

			if (columns[2].equals("-1")) {
				possible.setText("100");
			} else if (columns[2].equals("-2")) {
				possible.setText("N/A");
			} else {
				possible.setText(columns[2]);
			}
			possible.setGravity(Gravity.RIGHT);
			possible.setTextSize(16);

			row.addView(name);
			row.addView(grade);
			row.addView(possible);
			layout.addView(row);
		}
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
		return R.layout.card_category;
	}

}
