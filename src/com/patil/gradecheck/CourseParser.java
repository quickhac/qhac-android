package com.patil.gradecheck;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CourseParser {
	/*
	 * Parses general Course data using JSoup.
	 */

	String html;

	public CourseParser(String html) {
		this.html = html;
	}

	/*
	 * Returns an ArrayList of courses parsed using JSoup.
	 */
	public ArrayList<Course> parseCourses() {
		ArrayList<Course> courses = new ArrayList<Course>();
		if (html != null) {
			if (html.length() > 0) {
				Document doc = Jsoup.parse(html);

				Element content = doc.getElementById("_ctl0_tdMainContent");

				// Parse all necessary details
				ArrayList<String> teacherEmails = parseTeacherEmails(content);
				ArrayList<String> teacherNames = parseTeacherNames(content);
				ArrayList<int[]> cycleGrades = parseCycleGrades(content);
				ArrayList<int[]> examGrades = parseExamGrades(content);
				ArrayList<int[]> semesterGrades = parseSemesterGrades(content);
				ArrayList<String[]> gradeLinks = parseGradeLinks(content);
				ArrayList<String> titles = parseCourseTitles(content);

				// Add to courses
				for (int i = 0; i < titles.size(); i++) {
					Course course = new Course(titles.get(i),
							teacherNames.get(i), teacherEmails.get(i),
							cycleGrades.get(i), examGrades.get(i),
							semesterGrades.get(i), gradeLinks.get(i));
					courses.add(course);
				}

				
			}
		}
		return courses;
	}

	/*
	 * Parses grade links for all courses. Each grade link links to another page
	 * with cycle details.
	 */
	public ArrayList<String> parseCourseTitles(Element content) {
		ArrayList<String> courseTitles = new ArrayList<String>();

		Elements dataRow = content.getElementsByClass("DataRow");
		Elements dataRowAlt = content.getElementsByClass("DataRowAlt");
		ArrayList<Element> rows = new ArrayList<Element>();
		for (int i = 0; i < (dataRow.size() + dataRowAlt.size()); i++) {
			if (i % 2 == 0) {
				rows.add(dataRow.get(i / 2));
			} else {
				rows.add(dataRowAlt.get(i / 2));
			}
		}
		for (Element row : rows) {
			Elements cells = row.getElementsByTag("td");
			courseTitles.add(cells.get(1).text());
		}
		return courseTitles;
	}

	/*
	 * Parses grade links for all courses. Each grade link links to another page
	 * with cycle details.
	 */
	public ArrayList<String[]> parseGradeLinks(Element content) {
		ArrayList<String[]> gradeLinks = new ArrayList<String[]>();

		Elements dataRow = content.getElementsByClass("DataRow");
		Elements dataRowAlt = content.getElementsByClass("DataRowAlt");
		ArrayList<Element> rows = new ArrayList<Element>();
		for (int i = 0; i < (dataRow.size() + dataRowAlt.size()); i++) {
			if (i % 2 == 0) {
				rows.add(dataRow.get(i / 2));
			} else {
				rows.add(dataRowAlt.get(i / 2));
			}
		}
		for (Element row : rows) {
			Elements cells = row.getElementsByTag("td");
			ArrayList<Element> grades = new ArrayList<Element>();
			// offset of 4
			for (int i = 3; i < 13; i++) {
				// ignoring exam and semester averages
				if (i != 6 && i != 7 && i != 11 && i != 12) {
					grades.add(cells.get(i));
				}
			}

			String[] finalLinks = new String[6];
			for (int i = 0; i < grades.size(); i++) {
				// if grade exists/has been entered
				if (isInteger(grades.get(i).text())) {
					finalLinks[i] = grades.get(i).getElementsByTag("a").get(0)
							.attr("href");
				} else {
					// otherwise show grade doesn't exist
					finalLinks[i] = "NO_GRADE";
				}
			}
			gradeLinks.add(finalLinks);

		}
		return gradeLinks;
	}

	/*
	 * Parses semester grades for all courses. Each course has 2 semester
	 * grades, not including cycles or exam grades.
	 */
	public ArrayList<int[]> parseSemesterGrades(Element content) {
		ArrayList<int[]> semesterGrades = new ArrayList<int[]>();

		Elements dataRow = content.getElementsByClass("DataRow");
		Elements dataRowAlt = content.getElementsByClass("DataRowAlt");
		ArrayList<Element> rows = new ArrayList<Element>();
		for (int i = 0; i < (dataRow.size() + dataRowAlt.size()); i++) {
			if (i % 2 == 0) {
				rows.add(dataRow.get(i / 2));
			} else {
				rows.add(dataRowAlt.get(i / 2));
			}
		}
		for (Element row : rows) {
			Elements cells = row.getElementsByTag("td");
			ArrayList<Element> grades = new ArrayList<Element>();
			// offset of 4
			for (int i = 3; i < 13; i++) {
				// only add if semester grade
				if (i == 7 || i == 12) {
					grades.add(cells.get(i));
				}
			}

			int[] finalGrades = new int[2];
			for (int i = 0; i < grades.size(); i++) {
				// if grade exists/has been entered
				if (isInteger(grades.get(i).text())) {
					finalGrades[i] = Integer.valueOf(grades.get(i).text());
				} else {
					// otherwise show grade doesn't exist
					finalGrades[i] = -1;
				}
			}
			semesterGrades.add(finalGrades);

		}
		return semesterGrades;
	}

	/*
	 * Parses exam grades for all courses. Each course has 2 exam grades, not
	 * including cycles or semester averages.
	 */
	public ArrayList<int[]> parseExamGrades(Element content) {
		ArrayList<int[]> examGrades = new ArrayList<int[]>();

		Elements dataRow = content.getElementsByClass("DataRow");
		Elements dataRowAlt = content.getElementsByClass("DataRowAlt");
		ArrayList<Element> rows = new ArrayList<Element>();
		for (int i = 0; i < (dataRow.size() + dataRowAlt.size()); i++) {
			if (i % 2 == 0) {
				rows.add(dataRow.get(i / 2));
			} else {
				rows.add(dataRowAlt.get(i / 2));
			}
		}
		for (Element row : rows) {
			Elements cells = row.getElementsByTag("td");
			ArrayList<Element> grades = new ArrayList<Element>();
			// offset of 4
			for (int i = 3; i < 13; i++) {
				// only add if exam grade
				if (i == 6 || i == 11) {
					grades.add(cells.get(i));
				}
			}

			int[] finalGrades = new int[2];
			for (int i = 0; i < grades.size(); i++) {
				// if grade exists/has been entered
				if (isInteger(grades.get(i).text())) {
					finalGrades[i] = Integer.valueOf(grades.get(i).text());
				} else {
					// otherwise show grade doesn't exist
					finalGrades[i] = -1;
				}
			}
			examGrades.add(finalGrades);

		}
		return examGrades;
	}

	/*
	 * Parses cycle grades for all courses. Each course has 6 cycle grades, not
	 * including exams or semester averages.
	 */
	public ArrayList<int[]> parseCycleGrades(Element content) {
		ArrayList<int[]> cycleGrades = new ArrayList<int[]>();

		Elements dataRow = content.getElementsByClass("DataRow");
		Elements dataRowAlt = content.getElementsByClass("DataRowAlt");
		ArrayList<Element> rows = new ArrayList<Element>();
		for (int i = 0; i < (dataRow.size() + dataRowAlt.size()); i++) {
			if (i % 2 == 0) {
				rows.add(dataRow.get(i / 2));
			} else {
				rows.add(dataRowAlt.get(i / 2));
			}
		}
		for (Element row : rows) {
			Elements cells = row.getElementsByTag("td");
			ArrayList<Element> grades = new ArrayList<Element>();
			// offset of 4
			for (int i = 3; i < 13; i++) {
				// ignoring exam and semester averages
				if (i != 6 && i != 7 && i != 11 && i != 12) {
					grades.add(cells.get(i));
				}
			}

			int[] finalGrades = new int[6];
			for (int i = 0; i < grades.size(); i++) {
				// if grade exists/has been entered
				if (isInteger(grades.get(i).text())) {
					finalGrades[i] = Integer.valueOf(grades.get(i).text());
				} else {
					// otherwise show grade doesn't exist
					finalGrades[i] = -1;
				}
			}
			cycleGrades.add(finalGrades);

		}
		return cycleGrades;
	}

	/*
	 * Parses teacher names for each course.
	 */
	public ArrayList<String> parseTeacherNames(Element content) {
		ArrayList<String> teacherNames = new ArrayList<String>();
		Elements names = content.getElementsByClass("EmailLink");
		for (Element name : names) {
			teacherNames.add(name.text());
		}
		return teacherNames;
	}

	/*
	 * Parses teacher emails for each course.
	 */
	public ArrayList<String> parseTeacherEmails(Element content) {
		ArrayList<String> emailLinks = new ArrayList<String>();
		Elements emails = content.getElementsByClass("EmailLink");
		for (Element email : emails) {
			emailLinks.add(email.attr("href"));
		}
		return emailLinks;
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
}
