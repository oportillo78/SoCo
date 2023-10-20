package edu.ucd.forcops.main.ui;

import java.io.File;

import edu.ucd.forcops.main.MainUtils;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class MainClientUtils {

	public static void showLabelTemporarily(Label l, String text, int duration)
	{		
		l.setText(text);
		l.setTextFill(Color.RED);
		l.setVisible(true);
		PauseTransition visiblePause = new PauseTransition(
		        Duration.seconds(duration)
		);
		visiblePause.setOnFinished(event -> l.setVisible(false));
		visiblePause.play();
	}
	
	//OPD.-Dec19,2022: This refactorized method has two responsibilities: 
	// (1) Validates that the user has selected something, AND (2) shows the alert dialog to report the error.
	// This was decided in order to reuse it in other parts where the validation is missing.
	public static boolean validateTableViewSelection(ObservableList<?> singleRow, String alertMessage)
	{		
		if (singleRow != null && singleRow.size()>0)
		{
			return true;
		}
		else
		{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information Dialog");
			alert.setHeaderText("Information Dialog");
			alert.setContentText(alertMessage);

			alert.showAndWait();
			
			return false;
		}
	}
	
	public static void deleteDir(String dirPath)
	{
		File dirToDelete = new File(dirPath);
		 if (dirToDelete.exists()) {
         	//System.out.println("If ["+dirPath+"] directory exists, it should be deleted!");
         	MainUtils.deleteDir(dirToDelete);
         }
	}
}

