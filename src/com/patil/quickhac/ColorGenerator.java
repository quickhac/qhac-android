package com.patil.quickhac;

import android.content.Context;
import android.util.Log;
import com.patil.quickhac.R;

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
		String color;
		if (i == 0) {

			color = context.getResources().getString(R.string.petermann_river);
		} else if (i == 1) {
			color = context.getResources().getString(R.string.wisteria);
		} else if (i == 2) {
			color = context.getResources().getString(R.string.pomegranate);
		} else if (i == 3) {
			color = context.getResources().getString(R.string.carrot);
		} else if (i == 4) {
			color = context.getResources().getString(R.string.nephritis);
		} else if (i == 5) {
			color = context.getResources().getString(R.string.belize_hole);
		} else if (i == 6) {
			color = context.getResources().getString(R.string.amethyst);
		} else if (i == 7) {
			color = context.getResources().getString(R.string.alizarin);
		} else if (i == 8) {
			color = context.getResources().getString(R.string.orange);
		} else if (i == 9) {
			color = context.getResources().getString(R.string.turquoise);
		} else {
			color = context.getResources().getString(R.string.midnight_blue);
		}
		return color;
	}

	// DOESN'T WORK COMPLETELY
	public int getHueFromHex(String hex) {
		if (hex.equals("#FFFFFF")) {
			return 180;
		} else if (hex.equals("#1ABC9C")) {
			return 231;
		} else if (hex.equals("#16A085")) {
			return 124;
		} else if (hex.equals("#2ECC71")) {
			return 145;
		} else if (hex.equals("#27AE60")) {
			return 145;
		} else if (hex.equals("#3498DB")) {
			return 204;
		} else if (hex.equals("#2980B9")) {
			return 203;
		} else if (hex.equals("#9B59B6")) {
			return 282;
		} else if (hex.equals("#8E44AD")) {
			return 282;
		} else if (hex.equals("#34495E")) {
			return 210;
		} else if (hex.equals("#2C3E50")) {
			return 210;
		} else if (hex.equals("#F1C40F")) {
			return 48;
		} else if (hex.equals("#F39C12")) {
			return 36;
		} else if (hex.equals("#E67E22")) {
			return 28;
		} else if (hex.equals("#D35400")) {
			return 23;
		} else if (hex.equals("#E74C3C")) {
			return 5;
		} else if (hex.equals("#C0392B")) {
			return 5;
		} else if (hex.equals("#ECF0F1")) {
			return 192;
		} else if (hex.equals("#BDC3C7")) {
			return 204;
		} else if (hex.equals("#95A5A6")) {
			return 183;
		} else if (hex.equals("#7F8C8D")) {
			return 184;
		} else {
			return 0;
		}

	}

	/*
	 * Takes in a hex color, returns the color in HSV
	 * DOESN'T WORK COMPLETELY
	 */
	public float[] hexToHSV(String hexColor) {
		int[] rgb = hexToRGB(hexColor);
		// Divide by 255 to get on 0 - 1 scale
		float r = (float) rgb[0] / 255;
		float g = (float) rgb[1] / 255;
		float b = (float) rgb[2] / 255;
		float[] hsv = rgbToHSV(r, g, b);
		return hsv;
	}

	// DOESN'T WORK COMPLETELY
	public float[] rgbToHSV(float r, float g, float b) {
		float min, max, delta;

		float s, h, v;
		min = b;
		if (min > g)
			min = g;
		if (min > b)
			min = r;

		max = b;
		if (max < g)
			max = g;
		if (max < b)
			max = r;

		v = max; // v

		delta = max - min;

		if (max != 0) {
			s = delta / max; // s
		} else {
			// r = g = b = 0 // s = 0, v undefined
			s = 0;
			h = -1;
			return new float[] { h, s, v };
		}

		if (r == max) {
			h = (g - b) / delta;
		} else if (g == max) {
			h = 2 + (b - r) / delta;
		} else {
			h = 4 + (r - g) / delta;
		}

		h *= 60;
		if (h < 0)
			h += 360;

		return new float[] { h, s, v };

	}

	// DOESN'T WORK COMPLETELY
	public int[] hexToRGB(String hexColor) {
		int[] ret = new int[3];
		ret[0] = Integer.valueOf(hexColor.substring(1, 3), 16);
		ret[1] = Integer.valueOf(hexColor.substring(3, 5), 16);
		ret[2] = Integer.valueOf(hexColor.substring(5, 7), 16);

		return ret;
	}

	// DOESN'T WORK COMPLETELY
	public int hexToInt(char a, char b) {
		int x = a < 65 ? a - 48 : a - 55;
		int y = b < 65 ? b - 48 : b - 55;
		return x * 16 + y;
	}

	/*
	 * Returns a color for a grade. Colors according to severity. Returned is an
	 * array of ints, with rgb values hexColor is supplied if you want the grade
	 * shade to have a different color - NOT YET FUNCTIONAL
	 */
	public int[] getGradeColor(double grade, String hexColor) {
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
