package grader

import org.apache.poi.ss.usermodel.*
import org.apache.poi.hssf.usermodel.*
// import java.util.HashMap;
// import java.util.List;
// import java.util.Optional;
// import java.util.Arrays;
// import static java.util.stream.Collectors.toList;
import java.io.* // nio ??

class SpreadSheet(val fname: String) {
	
	private fun getStringValue(cell: Cell): String? =
		when (cell.getCellType()) {
			Cell.CELL_TYPE_STRING -> cell.getStringCellValue().trim()
			Cell.CELL_TYPE_NUMERIC -> cell.getNumericCellValue().toInt().toString()
			else -> {
				print("Bad cell, of type ${cell.getCellType()} when expecting string value, in $cell")
				null
			}
		}

	private fun getIntegerValue(cell: Cell): Int? =
		try {
			when (cell.getCellType()) {
				Cell.CELL_TYPE_STRING -> cell.getStringCellValue().trim().toInt()
				Cell.CELL_TYPE_NUMERIC -> kotlin.math.round(cell.getNumericCellValue()).toInt()
				else -> {
					print("Bad cell, of type ${cell.getCellType()} when expecting numeric value, in $cell")
					null
				}
			}
		} catch (ex: NumberFormatException) {
			print("Failed to make a number from ${cell.getStringCellValue()}")
			null
		}

	private fun getColumnIndex(colName: String, row: Row): Int {
		for (i in 0..row.getLastCellNum()) {
			if (colName == row.getCell(i).getStringCellValue().trim())
				return i
		}
		throw IllegalArgumentException("failed to find column $colName")
	}

	public fun writeFinalGrade(marks: Map<StudentCourse, Marks>): Unit {
		val inputFile = FileInputStream(File(fname))
		val workbook = WorkbookFactory.create(inputFile)
		val sheet = workbook.getSheet("Grade_Data")
		val firstRow = sheet.getRow(0);
		val markIndex = getColumnIndex("Mark", firstRow);
		val studentIdIndex = getColumnIndex("StudentID", firstRow);
		val courseIndex = getColumnIndex("Course", firstRow);

		for (i in 1..sheet.getLastRowNum()) {
			val row = sheet.getRow(i)
			val studentId: String? = getStringValue(row.getCell(studentIdIndex))
			val course: String? = getStringValue(row.getCell(courseIndex))
			if (studentId != null && course != null) {
				val key = StudentCourse(studentId, course)
				val grades = marks.get(key)
				if (grades != null && grades.isComplete()) {
					var cell = row.getCell(markIndex)
					if (cell == null) {
						cell = row.createCell(markIndex)
					}
					cell.setCellValue(grades.getFinalMark().toDouble())
				}
				else {
					print("grades missing or incomplete for student/course $key")
				}
			}
			else {
				System.out.println("missing studentID or course")
			}
			inputFile.close()
			val outputFile = FileOutputStream(File(fname))
			workbook.write(outputFile)
		}
	}

	private fun marksFromColumnNames(sheet: Sheet, markColumnNames: List<String>, marks: MutableMap<StudentCourse, Marks>): Unit {
		val firstRow = sheet.getRow(0)
		val studentIdIndex = getColumnIndex("StudentID", firstRow)
		val courseIndex = getColumnIndex("Course", firstRow)
		val markIndexes: List<Int> = markColumnNames.map { getColumnIndex(it, firstRow) } 
		for (i in 1..sheet.getLastRowNum()) {
			val row = sheet.getRow(i)
			val studentId: String? = getStringValue(row.getCell(studentIdIndex))
			val course: String? = getStringValue(row.getCell(courseIndex))
			if (studentId != null && course != null) {
				val studentCourse = StudentCourse(studentId, course)
				if (! marks.containsKey(studentCourse)) {
					marks[studentCourse] = Marks()
				}
				val grades = marks.get(studentCourse)
				if (grades != null) {
					markIndexes.forEach {
						var cell: Cell? = row.getCell(it)
						if (cell != null) {
							val grade = getIntegerValue(cell)
							if (grade != null) {
								grades.add(grade) 
							}
						}
					}
				}
			}
			else {
				System.out.println("row missing studentId or course")
			}
		}
		//return Unit
	} 

	public fun marksFromPeriodFile(marks: MutableMap<StudentCourse, Marks>): Unit {
		try {
			val workbook = WorkbookFactory.create(File(fname))
			val sheet = workbook.getSheet("Grade_Data")
			val markColumnNames = listOf("Mark")
			marksFromColumnNames(sheet, markColumnNames, marks)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}	

	// pass in the grade map
	public fun marksFromCustomReport(marks: MutableMap<StudentCourse, Marks>): Unit {
		try {
			val workbook = WorkbookFactory.create(File(fname))
			val sheet = workbook.getSheetAt(0)
			val markColumnNames = listOf("Mark1", "Mark2")//, "Mark3", "Mark4")
			marksFromColumnNames(sheet, markColumnNames, marks)
		} catch (e: Exception) {
			e.printStackTrace()
		}	
	}
}