package grader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import static java.util.stream.Collectors.toList;
import java.io.*; // nio ??

// put our various functionality here
// create a new one for each spreadsheet we wish to process
class SpreadSheet {
	private final String fname;

	public SpreadSheet(String name) {
		fname = name; //  "C:\\Users\\hugh\\java\\grader\\data\\" + name;
	}


	// expect to find something but might be empty rows??? tbd
	private Optional<String> getStringValue(Cell cell) throws Exception {
		String value = null;
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING: 
				value = cell.getStringCellValue().trim();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				Double x = cell.getNumericCellValue();
				value = String.valueOf((int) Math.round(x));
				break;
			default:
				System.out.println("Bad cell when expecting string in cell " + cell.getCellType() + ", " + value);
		}
		if (value == null || value.equals("")) {
			System.out.println("bad string value processed: " + value);
		}
		return Optional.ofNullable(value);
	}

	private Optional<Integer> getIntegerValue(Cell cell) throws Exception {
		if (cell == null) {
			return Optional.empty();
		}
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
					System.out.println("Bad call when expecting numeric cell value: " + cell.getCellType() + ", " + value);
			}
		}
		catch (NumberFormatException ex) {
			System.out.println("Failed to make a number from " + cell.getStringCellValue().trim());
		}

		return Optional.ofNullable(value);
	}

	// need to find something or we're screwed
	private int getColumnIndex(String colName, Row row) throws IllegalArgumentException {
		for (int i = 0; i < row.getLastCellNum(); i++) {
			var cell = row.getCell(i);
			var value = cell.getStringCellValue().trim();
			if (value.equals(colName)) {
				return i;
			}
		}
		throw new IllegalArgumentException("failed to find column " + colName);
	}


	public void writeFinalGrade(HashMap<StudentCourse, Marks> marks) {
		// pass in the ___5 file
		try {
			var inputFile = new FileInputStream(new File(fname));
			var workbook = WorkbookFactory.create(inputFile);
			var sheet = workbook.getSheet("Grade_Data");
			
			var firstRow = sheet.getRow(0);
			var markIndex = getColumnIndex("Mark", firstRow);
			var studentIdIndex = getColumnIndex("StudentID", firstRow);
			var courseIndex = getColumnIndex("Course", firstRow);

			for  (int i = 1; i <= sheet.getLastRowNum(); i++) {
				var row = sheet.getRow(i);
				var studentId = getStringValue(row.getCell(studentIdIndex));
				var course = getStringValue(row.getCell(courseIndex));
				if (studentId.isPresent() && course.isPresent()) {
					var key = new StudentCourse(studentId.get(), course.get());
					var grades = marks.get(key);
					if (grades != null && grades.isComplete()) {
						System.out.println("write grade for " + key);
						//var cell = Optional.ofNullable(row.getCell(markIndex)).orElse(row.createCell(markIndex));
						var cell = row.getCell(markIndex);
						if (cell == null) {
							System.out.println("creating cell");
							cell = row.createCell(markIndex);
						}
						cell.setCellValue(grades.getFinalMark());
					}
					else {
						System.out.println("grades missing or incomplete for student/course " + key + " : " + grades);
					}
				}
				else {
					System.out.println("missing studentID or course");
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

	private void marksFromColumnNames(Sheet sheet, List<String> markColumnNames, HashMap<StudentCourse, Marks> marks) {
		try {
			var firstRow = sheet.getRow(0);
			var studentIdIndex = getColumnIndex("StudentID", firstRow);
			var courseIndex = getColumnIndex("Course", firstRow);
			var markIndexes = markColumnNames.stream().map(colName -> getColumnIndex(colName, firstRow)).collect(toList());
			var max = sheet.getLastRowNum();
			for  (int i = 1; i <= max; i++) {
				var row = sheet.getRow(i);
				var studentId = getStringValue(row.getCell(studentIdIndex));
				var course = getStringValue(row.getCell(courseIndex));
				if (studentId.isPresent() && course.isPresent()) {
					var studentCourse = new StudentCourse(studentId.get(), course.get());
					if (! marks.containsKey(studentCourse)) {
						marks.put(studentCourse, new Marks());
					}
					else {
						//System.out.println("Apparent duplicate row for student/course " + studentId.get() + "/" + course.get());
					}
					var grades = marks.get(studentCourse);

					for (var markIndex : markIndexes) {
						getIntegerValue(row.getCell(markIndex)).ifPresent(m -> grades.add(m));
					}
				}
				else {
					System.out.println("row missing studentId or course");
				}
			}
		} 
		catch (IllegalArgumentException ex) {
			System.out.println("file headers not named as expected, " + ex);
		}
		catch (Exception ex) {
			System.out.println("oh no " + ex);
		}
	}

	// pass in the grade map
	public void marksFromPeriodFile(HashMap<StudentCourse, Marks> marks) {
		try {
			var workbook = WorkbookFactory.create(new File(fname));
			var sheet = workbook.getSheet("Grade_Data");
			var markColumnNames = Arrays.asList("Mark");
			marksFromColumnNames(sheet, markColumnNames, marks);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	// pass in the grade map
	public void marksFromCustomReport(HashMap<StudentCourse, Marks> marks) {
		try {
			var workbook = WorkbookFactory.create(new File(fname));
			var sheet = workbook.getSheetAt(0);
			var markColumnNames = Arrays.asList("Mark1", "Mark2");//, "Mark3", "Mark4");
			marksFromColumnNames(sheet, markColumnNames, marks);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}