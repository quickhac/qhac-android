package com.patil.gradecheck;

public class CardColorGenerator {
	/*
	 * Helper class to generate colors for cards
	 */
	
	public CardColorGenerator() {
		
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

}
