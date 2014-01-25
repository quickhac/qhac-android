package com.patil.gradecheck;

import android.content.Context;

public class ColorGenerator {
	/*
	 * Helper class to generate colors for cards and grades
	 */

	Context context;

	public ColorGenerator(Context context) {
		this.context = context;
	}

	/*
	 * Returns a color for each class.
	 */
	public String getCardColor(int i) {
		int colorInt;
		if (i == 0) {
			colorInt = context.getResources().getColor(R.color.petermann_river);
		} else if (i == 1) {
			colorInt = context.getResources().getColor(R.color.wisteria);
		} else if (i == 2) {
			colorInt = context.getResources().getColor(R.color.pomegranate);
		} else if (i == 3) {
			colorInt = context.getResources().getColor(R.color.carrot);
		} else if (i == 4) {
			colorInt = context.getResources().getColor(R.color.nephritis);
		} else if (i == 5) {
			colorInt = context.getResources().getColor(R.color.belize_hole);
		} else if (i == 6) {
			colorInt = context.getResources().getColor(R.color.amethyst);
		} else if (i == 7) {
			colorInt = context.getResources().getColor(R.color.alizarin);
		} else if (i == 8) {
			colorInt = context.getResources().getColor(R.color.orange);
		} else if (i == 9) {
			colorInt = context.getResources().getColor(R.color.turquoise);
		} else {
			colorInt = context.getResources().getColor(R.color.midnight_blue);
		}

		String color = "#" + Integer.toHexString(colorInt);
		return color;
	}

	/*
	 * Returns a color for a grade. Colors according to severity. Returned is an
	 * array of ints, with rgb values
	 */
	public int[] getGradeColor(double grade) {
		double hue = 0;
		int asianness = 4;
		// Make sure asianness isn't negative
		int asiannessLimited = Math.max(0, asianness);
		double h = 0, s = 0, v = 0, r = 0, g = 0, b = 0;
		if (grade < 0) {
			return new int[] { 225, 228, 225 };
		} else {

			h = Math.min(0.25 * Math.pow(grade / 100, asiannessLimited)
			// The following line limits the amount hue is allowed to
			// change in the gradient depending on how far the hue is
			// from a multiple of 90.
					+ Math.abs(45 - (hue + 45) % 90) / 256,
			// The following line puts a hard cap on the hue change.
					0.13056);
			s = 1 - Math.pow(grade / 100, asiannessLimited * 2);
			v = 0.86944 + h;
		}

		// apply hue transformation
		h += hue / 360;
		h %= 1;
		if (h < 0)
			h += 1;

		// extra credit gets a special color
		if (grade > 100) {
			h = 0.5;
			s = Math.min((grade - 100) / 15, 1);
			v = 1;
		}

		// convert to rgb: http://goo.gl/J9ra3
		double i = Math.floor(h * 6);
		double f = h * 6 - i;
		double p = v * (1 - s);
		double q = v * (1 - f * s);
		double t = v * (1 - (1 - f) * s);
		switch (((int) i) % 6) {
		case 0:
			r = v;
			g = t;
			b = p;
			break;
		case 1:
			r = q;
			g = v;
			b = p;
			break;
		case 2:
			r = p;
			g = v;
			b = t;
			break;
		case 3:
			r = p;
			g = q;
			b = v;
			break;
		case 4:
			r = t;
			g = p;
			b = v;
			break;
		case 5:
			r = v;
			g = p;
			b = q;
			break;
		}

		return new int[] { (int) (r * 255), (int) (g * 255), (int) (b * 255) };
	}
}
