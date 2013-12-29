package com.patil.gradecheck;

import android.graphics.Color;
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
		TableLayout layout = (TableLayout) convertView
				.findViewById(R.id.gradeTable);
		((TextView) convertView.findViewById(R.id.title)).setText(titlePlay);
		((TextView) convertView.findViewById(R.id.title)).setTextColor(Color
				.parseColor(titleColor));
		String[] descs = description.split("DELIMROW");

		((TextView) convertView.findViewById(R.id.semester1)).setText(descs[0]
				.split("DELIMCOLUM")[0]);
		((TextView) convertView.findViewById(R.id.semester2)).setText(descs[0]
				.split("DELIMCOLUMN")[1]);

		for (int i = 1; i < descs.length; i++) {
			String[] columns = descs[i].split("DELIMCOLUMN");

			TableRow row = new TableRow(convertView.getContext());
			TableRow.LayoutParams lp = new TableRow.LayoutParams(
					TableRow.LayoutParams.WRAP_CONTENT);
			row.setPadding(0, 5, 0, 5);
			row.setLayoutParams(lp);

			row.setGravity(Gravity.CENTER);

			TextView semester1 = new TextView(convertView.getContext());
			semester1.setPadding(10, 0, 0, 0);
			semester1.setText(columns[0]);
			semester1.setTextSize(16);

			TextView semester2 = new TextView(convertView.getContext());
			semester2.setPadding(0, 0, 10, 0);

			semester2.setText(columns[1]);
			semester2.setGravity(Gravity.RIGHT);
			semester2.setTextSize(16);

			row.addView(semester1);
			row.addView(semester2);
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
		return R.layout.card_course;
	}

}
