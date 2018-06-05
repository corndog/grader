package grader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import static java.util.stream.Collectors.toList;
import java.io.*;
import java.nio.file.*;

// put our various functionality here
// create a new one for each spreadsheet we wish to process
class SpreadSheet {
	Sheet sheet; // only one will be of interest
	private final String fname;
	private FileInputStream inputFile;
	private Workbook workbook;
	private Boolean isCustomReport; // ugh
	// anything not customreport
	private Integer term = null;

	// for custom report
	public SpreadSheet(Path path) {
		fname = path.toAbsolutePath().toString(); //  "C:\\Users\\hugh\\java\\grader\\data\\" + name;
		isCustomReport = true;
		try {
			inputFile = new FileInputStream(new File(fname));
			workbook = WorkbookFactory.create(inputFile);
		}
		catch (Exception ex) {
			System.out.println("Error opening spreadsheet: " + ex);
			System.exit(1);
		}
	}

	// single term or final result file
	public SpreadSheet(Path name, Integer trm) {
		this(name);
		isCustomReport = false;
		term = trm;
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
		try {
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
						//System.out.println("write grade for " + key);
						Cell cell = row.getCell(markIndex);
						if (cell == null) {
							System.out.println("creating cell for " + key);
							cell = row.createCell(markIndex);
						}
						cell.setCellValue(grades.getFinalMark());
					}
					else {
						//System.out.println("grades missing or incomplete for student/course " + key + " : " + grades);
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

	
	private void readMarks(Sheet sheet,  HashMap<StudentCourse, Marks> marks) {
		List<Integer> markIndexes = null;
		Integer markIndex = null;
		try {
			Row firstRow = sheet.getRow(0);
			if (isCustomReport) {
				// NOTE probably need to adjust this each time!!!
				List<String> markColumnNames = Arrays.asList("Mark1", "Mark2", "Mark3");//, "Mark4");
				markIndexes = markColumnNames.stream().map(colName -> getColumnIndex(colName, firstRow)).collect(toList());
			}
			else {
				markIndex = getColumnIndex("Mark", firstRow);
			}
			Integer studentIdIndex = getColumnIndex("StudentID", firstRow);
			Integer courseIndex = getColumnIndex("Course", firstRow);
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

					// Either we're reading a custom report, or a single term file
					if (isCustomReport) {
						for (Integer ix : markIndexes) {
							Integer grade = getIntegerValue(row.getCell(ix));
							if (grade != null) {
								try {
									grades.add(ix, grade);
								}
								catch (Exception e) {
									System.out.println(e);
									System.out.println("\nError adding grade for student/course/term " + studentCourse + ", " + term);
									System.exit(1); // really stop
								}
							}
						}
					}
					else { // single term file
						Integer grade = getIntegerValue(row.getCell(markIndex));
						if (grade != null) {
							try {
								grades.add(term-1, grade);
							}
							catch (Exception e) {
								System.out.println(e);
								System.out.println("\nError adding grade for student/course/term " + studentCourse + ", " + term);
								System.exit(1); // really stop
							}
						}
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

	public void readMarksFromFile(HashMap<StudentCourse, Marks> marks) {
		Sheet sheet;
		try {
			if (isCustomReport) {
				sheet = workbook.getSheetAt(0);
			}
			else {
				sheet = workbook.getSheet("Grade_Data");
			}
			readMarks(sheet, marks);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}