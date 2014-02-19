package com.patil.quickhac;

import android.content.Context;
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
import com.quickhac.common.data.Assignment;
import com.quickhac.common.data.Category;
import com.quickhac.common.data.GradeValue;
import com.quickhac.common.util.Numeric;

public class CategoryCard extends RecyclableCard {

	TableLayout gradeTable;
	TextView titleView;
	ImageView stripe;
	ColorGenerator generator;
	View convertView;

	public CategoryCard(String title, String description, String color,
			String titleColor, Boolean hasOverflow, Boolean isClickable) {
		super(title, description, color, titleColor, hasOverflow, isClickable);
	}

	@Override
	protected void applyTo(View convertView) {
		Category category = (Category) getData();
		Context context = convertView.getContext();
		generator = new ColorGenerator(context);
		this.convertView = convertView;
		makeTitle();
		makeStripe();
		makeGradeTable(category, context);
		makeClickable();
		makeOverflow(hasOverflow);
	}
	
	public void makeOverflow(boolean hasOverflow) {
		if(hasOverflow) {
			((ImageView) convertView.findViewById(R.id.overflow))
			.setVisibility(View.VISIBLE);
		} else {
			((ImageView) convertView.findViewById(R.id.overflow))
			.setVisibility(View.GONE);
		}
	}

	public void makeGradeTable(Category category,
			Context context) {
		gradeTable = (TableLayout) convertView.findViewById(R.id.gradeTable);

		((TextView) convertView.findViewById(R.id.average)).setText("Average: "
				+ description);
		((TextView) convertView.findViewById(R.id.weight)).setText(Numeric
				.doubleToPrettyString(category.weight) + "%");
		((TextView) convertView.findViewById(R.id.weight)).setTextColor(Color
				.parseColor(titleColor));

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

			name.setText(assignment.title);
			String gradeString = makeGradeString(assignment);
			grade.setText(gradeString);

			name.setTextSize(16);
			name.setPadding(10, 0, 0, 0);

			grade.setGravity(Gravity.RIGHT);
			grade.setTextSize(16);
			grade.setPadding(0, 0, 10, 0);

			row.addView(name);
			row.addView(grade);
			gradeTable.addView(row);
		}
	}

	public String makeGradeString(Assignment assignment) {
		GradeValue ptsEarned = assignment.ptsEarned;
		Log.d("watawa", ptsEarned.toString());
		if (ptsEarned != null) {
			if (ptsEarned.type == GradeValue.TYPE_LETTER) {
				return ptsEarned.toString();
			} else if (ptsEarned.type == GradeValue.TYPE_DOUBLE
					|| ptsEarned.type == GradeValue.TYPE_INTEGER) {
				String pts = Numeric.doubleToPrettyString(ptsEarned.value_d);
				if (assignment.ptsPossible != 100) {
					pts += "/"
							+ Numeric
									.doubleToPrettyString(assignment.ptsPossible);
				}
				if (assignment.weight != 1.0) {
					pts += ("\u00D7" + Numeric
							.doubleToPrettyString(assignment.weight));
				}
				return pts;
			}
		}
		return "-";
	}

	public void makeClickable() {
		if (isClickable)
			((LinearLayout) convertView.findViewById(R.id.contentLayout))
					.setBackgroundResource(R.drawable.selectable_background_cardbank);
	}

	public void makeStripe() {
		stripe = (ImageView) convertView.findViewById(R.id.stripe);
		stripe.setBackgroundColor(Color.parseColor(color));
	}

	public void makeTitle() {
		titleView = (TextView) convertView.findViewById(R.id.title);
		titleView.setText(titlePlay);
		titleView.setTextColor(Color.parseColor(titleColor));
	}

	@Override
	protected int getCardLayoutId() {
		return R.layout.card_category;
	}

}
