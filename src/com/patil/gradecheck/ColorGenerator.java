package com.patil.gradecheck;

public class ColorGenerator {
	/*
	 * Helper class to generate colors for cards and grades
	 */

	public ColorGenerator() {

	}

	/*
	 * Returns a color for each class.
	 */
	public String getCardColor(int i) {
		String color;
		if (i == 0) {
			color = "#009bce";
		} else if (i == 1) {
			color = "#9c34d0";
		} else if (i == 2) {
			color = "#5f8f00";
		} else if (i == 3) {
			color = "#fd8700";
		} else if (i == 4) {
			color = "#d20000";
		} else if (i == 5) {
			color = "#33b5e5";
		} else if (i == 6) {
			color = "#aa6fc7";
		} else if (i == 7) {
			color = "#9fd400";
		} else if (i == 8) {
			color = "#ffbd38";
		} else if (i == 9) {
			color = "#ff5252";
		} else {
			color = "#020202";
		}
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
