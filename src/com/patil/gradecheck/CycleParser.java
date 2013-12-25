package com.patil.gradecheck;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CycleParser {
	/*
	 * Parses general Course data using JSoup.
	 */

	String html;

	public CycleParser(String html) {
		this.html = html;
	}

	/*
	 * Uses JSoup to parse the HTML and load it into a CycleGrades object.
	 */
	public CycleGrades parseCycle() {
		Document doc = Jsoup.parse(html);
		Element content = doc.getElementById("_ctl0_tdMainContent");
		Elements tables = content.getElementsByClass("DataTable");
		// Get our tables of assignments
		ArrayList<Element> assignmentTables = new ArrayList<Element>();
		// Get our category details
		Elements categoryNames = content.getElementsByClass("CategoryName");
		for (int i = 1; i < tables.size(); i++) {
			assignmentTables.add(tables.get(i));
		}
		ArrayList<Category> categories = new ArrayList<Category>();
		for (int i = 0; i < assignmentTables.size(); i++) {

			// Load in Assignments
			Elements dataRow = assignmentTables.get(i).getElementsByClass(
					"DataRow");
			Elements dataRowAlt = assignmentTables.get(i).getElementsByClass(
					"DataRowAlt");
			ArrayList<Element> rows = new ArrayList<Element>();
			for (int d = 0; d < (dataRow.size() + dataRowAlt.size()); d++) {
				if (d % 2 == 0) {
					rows.add(dataRow.get(d / 2));
				} else {
					rows.add(dataRowAlt.get(d / 2));
				}
			}
			ArrayList<Assignment> assignments = new ArrayList<Assignment>();
			for (int d = 0; d < rows.size(); d++) {
				Element row = rows.get(d);
				String name = "";
				String dateAssigned = "";
				String dateDue = "";
				int assignmentGrade = -1;
				int assignmentPointsPossible = -1;
				String assignmentNotes = "";
				Elements names = row.getElementsByClass("AssignmentName");
				Elements datesAssigned = row.getElementsByClass("DateAssigned");
				Elements datesDue = row.getElementsByClass("DateDue");
				Elements assignmentGrades = row
						.getElementsByClass("AssignmentGrade");
				Elements assignmentsPointsPossible = row
						.getElementsByClass("AssignmentPointsPossible");
				Elements assignmentsNotes = row
						.getElementsByClass("AssignmentNote");
				if (names.size() > 0) {
					name = names.get(0).text();
				}
				if (datesAssigned.size() > 0) {
					dateAssigned = datesAssigned.get(0).text();
				}
				if (datesDue.size() > 0) {
					dateDue = datesDue.get(0).text();
				}
				if (assignmentGrades.size() > 0) {
					if (isDouble(assignmentGrades.get(0).text())) {
						assignmentGrade = Double.valueOf(
								assignmentGrades.get(0).text()).intValue();
					} else if(assignmentGrades.get(0).text().length() > 0) {
						assignmentGrade = -2;
					}
				}
				if (assignmentsPointsPossible.size() > 0) {
					if (isDouble(assignmentsPointsPossible.get(0).text())) {
						assignmentPointsPossible = Double.valueOf(
								assignmentsPointsPossible.get(0).text())
								.intValue();
					} else if(assignmentsPointsPossible.get(0).text().length() > 0) {
						assignmentPointsPossible = -2;
					}
				}
				if (assignmentsNotes.size() > 0) {
					assignmentNotes = assignmentsNotes.get(0).text();
				}
				assignments.add(new Assignment(name, dateAssigned, dateDue,
						assignmentGrade, assignmentPointsPossible, 0.0,
						assignmentNotes, false));
			}

			// Create category
			// Get name
			String categoryName = categoryNames.get(i).text();
			// Get weight
			String[] split = categoryName.split(" - ");
			String weightText = split[1].substring(0, split[1].length() - 1);
			double weight = -1;
			if (isInteger(weightText)) {
				weight = Integer.valueOf(weightText) / 100;
			}
			// Get average
			Elements datas = assignmentTables.get(i).getElementsByTag("tr");
			Element lastData = datas.get(datas.size() - 1);
			String averageText = lastData.getElementsByTag("td").get(3).text();
			double average = -1;
			if (isDouble(averageText)) {
				average = Double.valueOf(averageText);
			}
			Category category = new Category(categoryName, weight, average,
					assignments);
			categories.add(category);
		}
		return new CycleGrades("", -1, categories);
	}

	/*
	 * Helper method to make sure a grade can be converted to int
	 */
	public static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c <= '/' || c >= ':') {
				return false;
			}
		}
		return true;
	}

	/*
	 * Helper method that determines if a string is a double
	 */
	public boolean isDouble(String test) {
		final String Digits = "(\\p{Digit}+)";
		final String HexDigits = "(\\p{XDigit}+)";
		// an exponent is 'e' or 'E' followed by an optionally
		// signed decimal integer.
		final String Exp = "[eE][+-]?" + Digits;
		final String fpRegex = ("[\\x00-\\x20]*" + // Optional leading
													// "whitespace"
				"[+-]?(" + // Optional sign character
				"NaN|" + // "NaN" string
				"Infinity|" + // "Infinity" string

				// A decimal floating-point string representing a finite
				// positive
				// number without a leading sign has at most five basic pieces:
				// Digits . Digits ExponentPart FloatTypeSuffix
				//
				// Since this method allows integer-only strings as input
				// in addition to strings of floating-point literals, the
				// two sub-patterns below are simplifications of the grammar
				// productions from the Java Language Specification, 2nd
				// edition, section 3.10.2.

				// Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
				"(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

		// . Digits ExponentPart_opt FloatTypeSuffix_opt
				"(\\.(" + Digits + ")(" + Exp + ")?)|" +

				// Hexadecimal strings
				"((" +
				// 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
				"(0[xX]" + HexDigits + "(\\.)?)|" +

				// 0[xX] HexDigits_opt . HexDigits BinaryExponent
				// FloatTypeSuffix_opt
				"(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

				")[pP][+-]?" + Digits + "))" + "[fFdD]?))" + "[\\x00-\\x20]*");// Optional
																				// trailing
																				// "whitespace"

		if (Pattern.matches(fpRegex, test))
			return true;
		else {
			return false;
		}
	}
}
