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
import com.quickhac.common.data.Assignment;
import com.quickhac.common.data.Category;

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
		((TextView) convertView.findViewById(R.id.average)).setText("Average: "
				+ description);

		Category category = (Category) getData();
		for (int i = 0; i < category.assignments.length; i++) {
			Assignment assignment = category.assignments[i];
			TableRow row = new TableRow(convertView.getContext());
			TableRow.LayoutParams lp = new TableRow.LayoutParams(
					TableRow.LayoutParams.WRAP_CONTENT);
			row.setPadding(0, 5, 0, 5);
			row.setLayoutParams(lp);
			row.setGravity(Gravity.CENTER);

			TextView name = new TextView(convertView.getContext());
			TextView grade = new TextView(convertView.getContext());
			TextView possible = new TextView(convertView.getContext());

			name.setText(assignment.title);

			if (assignment.ptsEarned != null) {
				double ptsEarned = assignment.ptsEarned;
				if ((int) ptsEarned == ptsEarned) {
					grade.setText(String.valueOf((int) ptsEarned));
				} else {
					grade.setText(String.valueOf(ptsEarned));
				}
			} else {
				grade.setText("-");
			}

			if ((int) assignment.ptsPossible == assignment.ptsPossible) {
				possible.setText(String.valueOf((int) assignment.ptsPossible));
			} else {
				possible.setText(String.valueOf(assignment.ptsPossible));
			}

			name.setTextSize(16);
			name.setPadding(10, 0, 0, 0);

			grade.setGravity(Gravity.CENTER_HORIZONTAL);
			grade.setTextSize(16);

			possible.setGravity(Gravity.RIGHT);
			possible.setTextSize(16);
			possible.setPadding(0, 0, 10, 0);

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
