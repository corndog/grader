package grader;

import java.util.HashMap;

class Main {
	public static void main(String[] args) {
		var x = "cat";
		System.out.println("hi " + x);
		var gradeMap = new HashMap<StudentCourse, Marks>();
		var  spreadsheet = new SpreadSheet("CustomReport_1_01_copy.xls");
		spreadsheet.marksFromCustomReport(gradeMap);

		var keys = gradeMap.keySet();

		for (var k : keys) {
			System.out.print(k + " : ");
			var vals = gradeMap.get(k).getMarks();
			for (var v : vals) {
				System.out.print(v + ", ");
			}
			System.out.println("\n");
		}

	
	}
}