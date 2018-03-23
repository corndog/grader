package grader;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.util.HashMap;

public class Main extends Application {
		static String inputPath = "";
		static String outputPath = "";

	public static void main(String[] args) {
		launch(args);

		//final var x = 5;
		//x = 9; yay
	}

	@Override public void start(Stage primaryStage) {
		primaryStage.setTitle("Grader");
		var root = new StackPane();

		var fileChooser = new FileChooser();
		fileChooser.setTitle("Input file");

		var closeButton = new Button();
		closeButton.setText("Done! Close Me.");
		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//System.out.println("Yarp");
				System.exit(0);
			}

		});	

		var button2 = new Button();
		button2.setText("Choose output file");
		button2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//System.out.println("Yarp");
				var file = fileChooser.showOpenDialog(primaryStage);
				outputPath = file.getAbsolutePath();
				System.out.println("output path " + outputPath + "\n input path " + inputPath);
				populateData(inputPath, outputPath);
				root.getChildren().remove(button2);
				root.getChildren().add(closeButton);
			}

		});	

		var button1 = new Button();
		button1.setText("Choose input file");
		button1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//System.out.println("Yarp");
				var file = fileChooser.showOpenDialog(primaryStage);
				inputPath = file.getAbsolutePath();
				System.out.println("input path " + inputPath);
				root.getChildren().remove(button1);
				root.getChildren().add(button2);
			}

		});	

		
		root.getChildren().add(button1);
		
		primaryStage.setScene(new Scene(root, 400, 320));
		primaryStage.show();
	}


	// fetch string names 
	public static void populateData(String inputFileName, String outputFileName) {

		//var inputFileName = "CustomReport_1_01_copy.xls"; // fetch these from file dialog
		//var outputFileName = "2017_1_5_R.xls";

		var gradeMap = new HashMap<StudentCourse, Marks>();

		readMarks(inputFileName, gradeMap);
		// populate grade map
		//markSheet.marksFromCustomReport(gradeMap);

		// put final grades in
		writeResults(outputFileName, gradeMap);
		//resultSheet.writeFinalGrade(gradeMap);
	}

	public static void readMarks(String inputFileName, HashMap<StudentCourse, Marks> gradeMap) {
		var  markSheet = new SpreadSheet(inputFileName);
		markSheet.marksFromCustomReport(gradeMap);
	}

	public static void writeResults(String outputFileName, HashMap<StudentCourse, Marks> gradeMap) {
		var resultSheet = new SpreadSheet(outputFileName);
		resultSheet.writeFinalGrade(gradeMap);
	}
}