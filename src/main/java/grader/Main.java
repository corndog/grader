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
import java.util.Optional;
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
			try {
				runGrader(dirPath);
			}
			catch (Exception ex) {
				System.out.println("Failure running grader: " + ex);
				System.exit(1);
			}
			root.getChildren().remove(button1);
			root.getChildren().add(closeButton);
		});	
		
		root.getChildren().add(button1);
		
		primaryStage.setScene(new Scene(root, 500, 400));
		primaryStage.show();
	}

	// latest, we could have either a CustomReport, with 3 (or maybe 4) columns
	// and if 3, then a term_4 sheet as well for everyone
	// and the usual output file_5 files
	// so we read all possible input files first, then put the results in the output files
	public static void runGrader(String directory) throws FileNotFoundException, IOException {
		HashMap<StudentCourse, Marks> gradeMap = new HashMap<>();
		List<Path> paths = getFileNames(directory);
		List<Path> inputFiles = paths.stream().filter(p -> !isOutputFile(p)).collect(toList());
		List<Path> outputFiles = paths.stream().filter(p -> isOutputFile(p)).collect(toList());

		for (Path path : inputFiles) {
			System.out.println("Reading grades from " + path.getFileName().toString());
			SpreadSheet  markSheet = null;
			if (isCustomReportFile(path)){
				markSheet = new SpreadSheet(path);
			}
			else {
				markSheet = new SpreadSheet(path, numPart(path));
			}
			markSheet.readMarksFromFile(gradeMap);
		}

		// write whatever grades we loaded to our output file(s)
		for (Path path : outputFiles) {
			System.out.println("writing to " + path.getFileName().toString());
			//writeResults(fname, gradeMap);
			SpreadSheet resultSheet = new SpreadSheet(path, numPart(path));
			resultSheet.writeFinalGrade(gradeMap);
			System.out.println("PROCESSED " + path.getFileName().toString());
		}
		System.out.println("Done.");
		System.out.println("Processed " + outputFiles.size() + " groups of data");
	}

	public static Boolean isCustomReportFile(Path path) {
		return path.getFileName().toString().contains("CustomReport");
	}

	// passing around full file path in names, probably should fix that/this part
	// so we can have an underscore in temp directory!!!!
	public static Integer numPart(Path path) {
		//System.out.println("parsing: " + fname);
		String num = path.getFileName().toString().split("_")[2];
		return Integer.parseInt(num);
	}

	public static boolean isOutputFile(Path path) {
		return 5 == numPart(path);
	}

	public static List<Path> getFileNames(String directory) {
		List<Path> files = new ArrayList<Path>();
		Path dir = Paths.get(directory);
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
			for (Path path : ds) {
				files.add(path);
			}
		} catch (IOException ex) {
			System.out.println("oops\n " + ex);
		}
		return files;
	}
}