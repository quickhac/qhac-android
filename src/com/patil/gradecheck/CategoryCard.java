package com.patil.gradecheck;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fima.cardsui.objects.RecyclableCard;

public class CategoryCard extends RecyclableCard {

	public CategoryCard(String title, String description, String color, String titleColor, Boolean hasOverflow, Boolean isClickable) {
		super(title, description, color, titleColor, hasOverflow, isClickable);
	}
	
	@Override
	protected void applyTo(View convertView) {
		((TextView) convertView.findViewById(R.id.title)).setText(titlePlay);
		((TextView) convertView.findViewById(R.id.title)).setTextColor(Color
				.parseColor(titleColor));
		Log.d("CardUIGenerator", "creating card");
		String assignmentNames = "";
		String pointsPossibles = "";
		String pointsEarned = "";
		String[] rows = description.split("DELIMROW");
		for(int i = 0; i < rows.length; i++) {
			String[] columns = rows[i].split("DELIMCOLUMN");
			assignmentNames += columns[0] + "\n";
			pointsEarned += columns[1] + "\n";
			pointsPossibles += columns[2] + "\n";
		}
		((TextView) convertView.findViewById(R.id.description_assignmentName))
				.setText(assignmentNames);
		((TextView) convertView.findViewById(R.id.description_pointsEarned))
		.setText(pointsEarned);
		((TextView) convertView.findViewById(R.id.description_pointsPossible))
		.setText(pointsPossibles);
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
