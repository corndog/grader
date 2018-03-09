package grader;

import java.util.HashMap;

class Main {
	public static void main(String[] args) {
		var x = "cat";
		System.out.println("hi " + x);
		var gradeMap = new HashMap<StudentCourse, Marks>();

		var  markSheet = new SpreadSheet("CustomReport_1_01_copy.xls");
		// populate grade map
		markSheet.marksFromCustomReport(gradeMap);

		// put final grades in
		var resultSheet = new SpreadSheet("2017_1_5_R.xls");
		resultSheet.writeFinalGrade(gradeMap);

		// var keys = gradeMap.keySet();

		// for (var k : keys) {
		// 	System.out.print(k + " : ");
		// 	var vals = gradeMap.get(k).getMarks();
		// 	for (var v : vals) {
		// 		System.out.print(v + ", ");
		// 	}
		// 	System.out.println("\n");
		// }

	
	}
}