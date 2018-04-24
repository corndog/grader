package grader;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
//import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import static java.util.stream.Collectors.toList;

import java.io.*;
import java.nio.file.*;

// tasks
// todo: skip writing to already marked cells...

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override 
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Grader");
		StackPane root = new StackPane();

		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Choose directory");

		Button closeButton = new Button();
		closeButton.setText("Done! Close Me.");
		closeButton.setOnAction((event) -> {
			System.exit(0);
		});	

		Button button1 = new Button();
		button1.setText("Choose data directory");
		button1.setOnAction((event) -> {
			File dir = dirChooser.showDialog(primaryStage);
			String dirPath = dir.getAbsolutePath();
			runGrader(dirPath);
			root.getChildren().remove(button1);
			root.getChildren().add(closeButton);
		});	
		
		root.getChildren().add(button1);
		
		primaryStage.setScene(new Scene(root, 500, 400));
		primaryStage.show();
	}

	// ok we need to figure out another state
	//
	public static void runGrader(String directory) {
		// figure if we have a bunch of files, 5 per 
		List<String> files = getFileNames(directory);
		Integer numOutputFiles = files.stream().filter(s -> "5".equals(s.split("_")[2])).collect(toList()).size();

		if (numOutputFiles == 1) {
			runGraderOneOutputFile(directory);
		}
		else if (numOutputFiles > 1) {
			rundGraderFivePer(directory);
		}
		else {
			System.out.println("Couldn't find an output file!");
		}
	}

	// ok we have five files per
	// probably easiest to run them in groups of five....
	// just  find the output file and its input files and call it like below
	public static void rundGraderFivePer(String directory) {
		List<String> files = getFileNames(directory);
		List<String> outputFiles = files.stream().filter(s -> "5".equals(s.split("_")[2])).collect(toList());

		// file name eg 2017_1_5_Smith...
		for (String outputFile : outputFiles) {
			String[] nameParts = outputFile.split("_");
			String matchPart = nameParts[3];
			List<String> inputFiles = files.stream().filter(s -> !"5".equals(s.split("_")[2]) && matchPart.equals(s.split("_")[3])).collect(toList());
			if (inputFiles.size() != 2) {
				System.out.println("Found wrong number of input files: ${input.size} for ${matchPart}");
			}
			else {
				HashMap<StudentCourse, Marks> gradeMap = new HashMap<>();
				for (String fname : inputFiles) {
					readMarksFromPeriodFile(fname, gradeMap);
				}
				writeResults(outputFile, gradeMap);
			}
			System.out.println("PROCESSED " + outputFile);
		}
	}

	public static void runGraderOneOutputFile(String directory) {

		List<String> files = getFileNames(directory);
	    HashMap<StudentCourse, Marks> gradeMap = new HashMap<>();
		String outputFileName = files.stream().filter(s -> "5".equals(s.split("_")[2])).findFirst().get();
		List<String> inputFiles = files.stream().filter(s -> s != outputFileName).collect(toList());
		for (String fname : inputFiles) {
			if (fname.contains("CustomReport"))
				readMarksFromCustomReport(fname, gradeMap);
			else
				readMarksFromPeriodFile(fname, gradeMap);
		}
		writeResults(outputFileName, gradeMap);
	}

	public static void readMarksFromPeriodFile(String inputFileName, HashMap<StudentCourse, Marks> gradeMap) {
		SpreadSheet markSheet = new SpreadSheet(inputFileName);
		markSheet.marksFromPeriodFile(gradeMap);
	}

	public static void readMarksFromCustomReport(String inputFileName, HashMap<StudentCourse, Marks> gradeMap) {
		SpreadSheet  markSheet = new SpreadSheet(inputFileName);
		markSheet.marksFromCustomReport(gradeMap);
	}

	public static void writeResults(String outputFileName, HashMap<StudentCourse, Marks> gradeMap) {
		SpreadSheet resultSheet = new SpreadSheet(outputFileName);
		resultSheet.writeFinalGrade(gradeMap);
	}

	public static List<String> getFileNames(String directory) {
		List<String> files = new ArrayList<String>();
		Path dir = Paths.get(directory);
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
			for (Path path : ds) {
				files.add(path.toAbsolutePath().toString());
			}
		} catch (IOException ex) {
			System.out.println("oops\n " + ex);
		}
		return files;
	}
}