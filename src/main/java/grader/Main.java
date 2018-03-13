package grader;

import java.util.HashMap;

class Main {
	public static void main(String[] args) {

		var gradeMap = new HashMap<StudentCourse, Marks>();

		var  markSheet = new SpreadSheet("CustomReport_1_01_copy.xls");
		// populate grade map
		markSheet.marksFromCustomReport(gradeMap);

		// put final grades in
		var resultSheet = new SpreadSheet("2017_1_5_R.xls");
		resultSheet.writeFinalGrade(gradeMap);
	}
}