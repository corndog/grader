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

	private Integer getIntegerValue(Cell cell) throws Exception {
		if (cell == null) {
			return null;
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

		return value;
	}

	// need to find something or we're screwed
	private int getColumnIndex(String colName, Row row) throws IllegalArgumentException {
		for (int i = 0; i < row.getLastCellNum(); i++) {
			Cell cell = row.getCell(i);
			String value = cell.getStringCellValue().trim();
			if (value.equals(colName)) {
				return i;
			}
		}
		throw new IllegalArgumentException("failed to find column " + colName);
	}


	public void writeFinalGrade(HashMap<StudentCourse, Marks> marks) {
		// pass in the ___5 file
		try {
			FileInputStream inputFile = new FileInputStream(new File(fname));
			Workbook workbook = WorkbookFactory.create(inputFile);
			Sheet sheet = workbook.getSheet("Grade_Data");
			
			Row firstRow = sheet.getRow(0);
			Integer markIndex = getColumnIndex("Mark", firstRow);
			Integer studentIdIndex = getColumnIndex("StudentID", firstRow);
			Integer courseIndex = getColumnIndex("Course", firstRow);

			for  (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				Optional<String> studentId = getStringValue(row.getCell(studentIdIndex));
				Optional<String> course = getStringValue(row.getCell(courseIndex));
				if (studentId.isPresent() && course.isPresent()) {
					StudentCourse key = new StudentCourse(studentId.get(), course.get());
					Marks grades = marks.get(key);
					if (grades != null && grades.isComplete()) {
						System.out.println("write grade for " + key);
						//var cell = Optional.ofNullable(row.getCell(markIndex)).orElse(row.createCell(markIndex));
						Cell cell = row.getCell(markIndex);
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
			FileOutputStream outputFile = new FileOutputStream(new File(fname));
			workbook.write(outputFile);
			//workbook.close(); wut???
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ok we have two scenarios
	private void marksFromColumnNames(Sheet sheet, List<String> markColumnNames, HashMap<StudentCourse, Marks> marks) {
		try {
			Row firstRow = sheet.getRow(0);
			Integer studentIdIndex = getColumnIndex("StudentID", firstRow);
			Integer courseIndex = getColumnIndex("Course", firstRow);
			List<Integer> markIndexes = markColumnNames.stream().map(colName -> getColumnIndex(colName, firstRow)).collect(toList());
			Integer max = sheet.getLastRowNum();
			for  (int i = 1; i <= max; i++) {
				Row row = sheet.getRow(i);
				Optional<String> studentId = getStringValue(row.getCell(studentIdIndex));
				Optional<String> course = getStringValue(row.getCell(courseIndex));
				if (studentId.isPresent() && course.isPresent()) {
					StudentCourse studentCourse = new StudentCourse(studentId.get(), course.get());
					if (! marks.containsKey(studentCourse)) {
						marks.put(studentCourse, new Marks());
					}
					else {
						//System.out.println("Apparent duplicate row for student/course " + studentId.get() + "/" + course.get());
					}
					Marks grades = marks.get(studentCourse);

					// TODO ok tweak this since we are going to assume Mark1,Mark2,Mark3,Mark4 ....
					int j = 0; // should be
					for (Integer markIndex : markIndexes) {
						Integer grade = getIntegerValue(row.getCell(markIndex));
						if (grade != null) {
							grades.add(j, grade);
						}
						j += 1;
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

	//
	private void marksFromMarkColumn(Sheet sheet, Integer term, HashMap<StudentCourse, Marks> marks) {
		try {
			Row firstRow = sheet.getRow(0);
			Integer studentIdIndex = getColumnIndex("StudentID", firstRow);
			Integer courseIndex = getColumnIndex("Course", firstRow);
			Integer markIndex = getColumnIndex("Mark", firstRow);
			Integer max = sheet.getLastRowNum();
			for  (int i = 1; i <= max; i++) {
				Row row = sheet.getRow(i);
				Optional<String> studentId = getStringValue(row.getCell(studentIdIndex));
				Optional<String> course = getStringValue(row.getCell(courseIndex));
				if (studentId.isPresent() && course.isPresent()) {
					StudentCourse studentCourse = new StudentCourse(studentId.get(), course.get());
					if (! marks.containsKey(studentCourse)) {
						marks.put(studentCourse, new Marks());
					}
					else {
						//System.out.println("Apparent duplicate row for student/course " + studentId.get() + "/" + course.get());
					}
					Marks grades = marks.get(studentCourse);
					Integer grade = getIntegerValue(row.getCell(markIndex));
					if (grade != null) {
						grades.add(term, grade);
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
	public void marksFromPeriodFile(HashMap<StudentCourse, Marks> marks, Integer term) {
		try {
			Workbook workbook = WorkbookFactory.create(new File(fname));
			Sheet sheet = workbook.getSheet("Grade_Data");
			List<String> markColumnNames = Arrays.asList("Mark");
			marksFromColumnNames(sheet, markColumnNames, marks);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	// pass in the grade map
	public void marksFromCustomReport(HashMap<StudentCourse, Marks> marks) {
		try {
			Workbook workbook = WorkbookFactory.create(new File(fname));
			Sheet sheet = workbook.getSheetAt(0);
			List<String> markColumnNames = Arrays.asList("Mark1", "Mark2");//, "Mark3", "Mark4");
			marksFromColumnNames(sheet, markColumnNames, marks);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}