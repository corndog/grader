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
		var root = new StackPane();

		var dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Choose directory");

		var closeButton = new Button();
		closeButton.setText("Done! Close Me.");
		closeButton.setOnAction((event) -> {
			System.exit(0);
		});	

		var button1 = new Button();
		button1.setText("Choose data directory");
		button1.setOnAction((event) -> {
			var dir = dirChooser.showDialog(primaryStage);
			var dirPath = dir.getAbsolutePath();
			runGrader(dirPath);
			root.getChildren().remove(button1);
			root.getChildren().add(closeButton);
		});	
		
		root.getChildren().add(button1);
		
		primaryStage.setScene(new Scene(root, 500, 400));
		primaryStage.show();
	}

	public static void runGrader(String directory) {

		List<String> files = getFileNames(directory);
		var gradeMap = new HashMap<StudentCourse, Marks>();
		var outputFileName = files.stream().filter(s -> "5".equals(s.split("_")[2])).findFirst().get();
		var inputFiles = files.stream().filter(s -> s != outputFileName).collect(toList());
		for (var fname : inputFiles) {
			if (fname.contains("CustomReport"))
				readMarksFromCustomReport(fname, gradeMap);
			else
				readMarksFromPeriodFile(fname, gradeMap);
		}
		writeResults(outputFileName, gradeMap);
	}

	public static void readMarksFromPeriodFile(String inputFileName, HashMap<StudentCourse, Marks> gradeMap) {
		var markSheet = new SpreadSheet(inputFileName);
		markSheet.marksFromPeriodFile(gradeMap);
	}

	public static void readMarksFromCustomReport(String inputFileName, HashMap<StudentCourse, Marks> gradeMap) {
		var  markSheet = new SpreadSheet(inputFileName);
		markSheet.marksFromCustomReport(gradeMap);
	}

	public static void writeResults(String outputFileName, HashMap<StudentCourse, Marks> gradeMap) {
		var resultSheet = new SpreadSheet(outputFileName);
		resultSheet.writeFinalGrade(gradeMap);
	}

	public static List<String> getFileNames(String directory) {
		var files = new ArrayList<String>();
		var dir = Paths.get(directory);
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
			for (var path : ds) {
				files.add(path.toAbsolutePath().toString());
			}
		} catch (IOException ex) {
			System.out.println("oops\n " + ex);
		}
		return files;
	}
}