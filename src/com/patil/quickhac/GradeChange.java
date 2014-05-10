package com.patil.quickhac;

import com.quickhac.common.data.GradeValue;

public class GradeChange {
	String title;
	String oldGrade;
	String newGrade;
	GradeValue oldRawGrade;
	GradeValue newRawGrade;
	boolean increased;
	boolean added;

	public GradeChange(String title, String oldGrade, String newGrade, boolean added, GradeValue oldRawGrade, GradeValue newRawGrade) {
		this.title = title;
		this.added = added;
		this.oldGrade = oldGrade;
		this.newGrade = newGrade;
		this.oldRawGrade = oldRawGrade;
		this.newRawGrade = newRawGrade;
	}

	public String toString() {
		if (!added) {
			// Should return "Your grade in [course_name] [increased/decreased]
			// change points
			return "Your grade in " + title + " changed from a " + oldGrade + " to a " + newGrade;
		} else {
			return "You have a new " + newGrade + " grade in " + title;
		}
	}
}
