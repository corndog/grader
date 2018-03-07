package grader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import java.util.HashMap;

// put our various functionality here
// create a new one for each spreadsheet we wish to process
class SpreadSheet {
	private final String fname;

	public SpreadSheet(String name) {
		fname = "C:\\Users\\hugh\\java\\grader\\data\\" + name;
	}

	private String getStringValue(Cell cell) throws Exception {
		String value = "";
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING: 
				value = cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				Double x = cell.getNumericCellValue();
				value = String.valueOf((int) Math.round(x));
				break;
			default:
				throw new Exception("Bad value when expecting string in cell");
		}
		return value.trim();
	}

	private Integer getIntegerValue(Cell cell) throws Exception {
		Integer value;
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING: 
				value = Integer.parseInt(cell.getStringCellValue().trim());
				break;
			case Cell.CELL_TYPE_NUMERIC:
				Double x = cell.getNumericCellValue();
				value = (int) Math.round(x);
				break;
			default:
				throw new Exception("Bad value when expecting numeric cell value");
		}
		return value;
	}

	// pass in the grade map
	public void marksFromCustomReport(HashMap<StudentCourse, Marks> marks) {
		// ok so we can look for the indexes of the rows we are interested int
		int mark1Index = -1;
		int mark2Index = -1;
		int studentIdIndex = -1;
		int courseIndex = -1;
		int finalMarkIndex = -1;

		try {
			var workbook = WorkbookFactory.create(new java.io.File(fname));
			var sheet = workbook.getSheetAt(0);
			for  (int i = 0; i < sheet.getLastRowNum(); i++) {
				var row = sheet.getRow(i);
				if (i == 0) { // figure cell indexes of columns of interest
					for (int j = 0; j < row.getLastCellNum(); j++) {
						var cell = row.getCell(j);
						var value = cell.getStringCellValue().trim();
						if (value.equals("Mark1")) 
							mark1Index = j;
						if (value.equals("Mark2"))
							mark2Index = j;
						if (value.equals("FinalMark"))
							finalMarkIndex = j;
						if (value.equals("StudentID"))
							studentIdIndex = j;
						if (value.equals("Course"))
							courseIndex = j;
					}
				}
				else {
					if (mark1Index < 0 || mark2Index < 0 || studentIdIndex < 0 || courseIndex < 0 || finalMarkIndex < 0)
						throw new Exception("Didn't find columns for some of Mark1, Mark2, FinalMark, StudentID or Course");

					// figure course info and marks
					var studentIdCell = row.getCell(studentIdIndex);
					var courseCell = row.getCell(courseIndex);
					var mark1Cell = row.getCell(mark1Index);
					var mark2Cell = row.getCell(mark2Index);

					// expect only one row per student/course
					if (studentIdCell != null && courseCell != null) {
						var studentCourse = new StudentCourse(getStringValue(studentIdCell), getStringValue(courseCell));
						if (! marks.containsKey(studentCourse)) {
							marks.put(studentCourse, new Marks(2));
						}
						else {
							System.out.println("Apparent duplicate row for student/course " + studentIdCell.getStringCellValue() + "/" + courseCell.getStringCellValue());
						}
						var grades = marks.get(studentCourse);
						// TODO check for fuckery of these values
						// might be empty strings or bs like "CR" instead of numbers.
						if (mark1Cell != null) // && !mark1Cell.getStringCellValue().trim().equals(""))
							grades.add(getIntegerValue(mark1Cell));
						if (mark2Cell != null)// && !mark2Cell.getStringCellValue().trim().equals(""))
							grades.add(getIntegerValue(mark2Cell));
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void read() {
		try {
			var workbook = WorkbookFactory.create(new java.io.File(fname));
			var sheet = workbook.getSheet("Grade_Data");

			for  (var row : sheet) {

				for (var cell: row) {

					// seems to all be strings
					switch (cell.getCellType()) {
						case Cell.CELL_TYPE_STRING: 
							System.out.println(cell.getStringCellValue());
							break;
						case Cell.CELL_TYPE_NUMERIC:
							System.out.println(cell.getNumericCellValue());
							break;
						default:
							System.out.println("OOPS");
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}