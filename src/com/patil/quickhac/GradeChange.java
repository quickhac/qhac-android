package com.patil.quickhac;

public class GradeChange {
	String title;
	String oldGrade;
	String newGrade;
	boolean increased;
	boolean added;

	public GradeChange(String title, String oldGrade, String newGrade, boolean added) {
		this.title = title;
		this.added = added;
		this.oldGrade = oldGrade;
		this.newGrade = newGrade;
	}

	public String toString() {
		if (!added) {
			// Should return "Your grade in [course_name] [increased/decreased]
			// change points
			return "Your grade in " + title + " changed from a " + oldGrade + " to a " + newGrade;
		} else {
			return "You have new grades in " + title;
		}
	}
}
