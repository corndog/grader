package grader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import java.util.HashMap;
import java.util.Optional;
import java.io.*;

// put our various functionality here
// create a new one for each spreadsheet we wish to process
class SpreadSheet {
	private final String fname;

	public SpreadSheet(String name) {
		fname = "C:\\Users\\hugh\\java\\grader\\data\\" + name;
	}


	// TODO investigate Option in java
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

	private Optional<Integer> getIntegerValue(Cell cell) throws Exception {
		Integer value = null;
		try {
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
		}
		catch (NumberFormatException ex) {
			System.out.println("Failed to make a number from " + cell.getStringCellValue().trim());
		}

		return Optional.ofNullable(value);
	}

	// Option??
	private int getColumnIndex(String colName, Row row) {
		int ix = -1;
		for (int i = 0; i < row.getLastCellNum(); i++) {
			var cell = row.getCell(i);
			var value = cell.getStringCellValue().trim();
			if (value.equals(colName)) {
				ix = i;
				break;
			}
		}
		return ix;
	}


	public void writeFinalGrade(HashMap<StudentCourse, Marks> marks) {
		// pass in the ___5 file
		try {
			var inputFile = new FileInputStream(new File(fname));
			var workbook = WorkbookFactory.create(inputFile);
			var sheet = workbook.getSheet("Grade_Data");

			// find index of "Mark" column
			var firstRow = sheet.getRow(0);
			var markIndex = getColumnIndex("Mark", firstRow);
			var studentIdIndex = getColumnIndex("StudentID", firstRow);
			var courseIndex = getColumnIndex("Course", firstRow);

			if (markIndex < 0 || studentIdIndex < 0 || courseIndex <0)
				throw new Exception("Couldn't find Mark/StudentID/Course column");

			for  (int i = 1; i < sheet.getLastRowNum(); i++) {
				var row = sheet.getRow(i);
				var studentId = getStringValue(row.getCell(studentIdIndex));
				var course = getStringValue(row.getCell(courseIndex));
				var key = new StudentCourse(studentId, course);
				var grades = marks.get(key);
				if (grades != null && grades.isComplete()) {
					var markCell = row.getCell(markIndex);
					System.out.println("attempting to write for " + key + " : " + grades.getFinalMark());
					markCell.setCellValue(grades.getFinalMark());
				}
				else {
					System.out.println("found nothing for " + key);
				}
			}
			inputFile.close();
			var outputFile = new FileOutputStream(new File(fname));
			workbook.write(outputFile);
			//workbook.close(); wut???
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// pass in the grade map
	public void marksFromCustomReport(HashMap<StudentCourse, Marks> marks) {
		// ok so we can look for the indexes of the rows we are interested int
		int mark1Index = -1;
		int mark2Index = -1;
		int studentIdIndex = -1;
		int courseIndex = -1;
		int finalMarkIndex = -1; // maybe don't care?? add it anyway or double check???

		try {
			var inputFile = new java.io.File(fname);
			var workbook = WorkbookFactory.create(new File(fname));
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
						// Get some flatMap action going here
						if (mark1Cell != null) { // && !mark1Cell.getStringCellValue().trim().equals(""))
							var mark1 = getIntegerValue(mark1Cell);
							mark1.ifPresent(m -> grades.add(m));
						}
						if (mark2Cell != null) {// && !mark2Cell.getStringCellValue().trim().equals(""))
							var mark2 = getIntegerValue(mark2Cell);
							mark2.ifPresent(m -> grades.add(m));
						}
					}
				}
			}
			//workbook.close(); //???
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