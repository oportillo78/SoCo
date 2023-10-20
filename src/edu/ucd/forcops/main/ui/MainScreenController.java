package edu.ucd.forcops.main.ui;


import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea; 
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.apache.commons.io.FileUtils;

import edu.ucd.forcops.main.MainUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public class MainScreenController 
{

    @FXML
    private TableView<VisualResultsTO> results;
    
    @FXML
    private TableColumn<VisualResultsTO, String> idCol;
    @FXML
    private TableColumn<VisualResultsTO, String> inputFileCol;
    @FXML
    private TableColumn<VisualResultsTO, String> executionModeCol;
    @FXML
    private TableColumn<VisualResultsTO, String> appliedControlsCol;
    @FXML
    private TableColumn<VisualResultsTO, String> outputFileCol;
    
    @FXML
    private ChoiceBox<String> debugModes;

    @FXML
    private TextField pdiFilename;
    
    @FXML
    private TextField poFilename;
    
    @FXML
    private TextField tokensFilename;
    
    @FXML
    private TextField rulesetFilename;
    
    @FXML
    private TextField inputDiagramsFilename;
    
    @FXML
    public TextArea execLog;
    
    public static TextArea execLog4all;

    //@FXML
    //private Button analyseComparisonButton;

    Preferences preferences = Preferences.userRoot().node("SoCo");	//AdaptIT
    
    /**
     * Called automatically when this controller class is created. All JavaFX components are set up and populated.
     */
    @FXML
    public void initialize() 
    {
    	/*debugModes.getItems().add("Debug on/off");
    	debugModes.getItems().add("Debug on");
    	debugModes.getItems().add("Debug off");*/
    	debugModes.setItems(FXCollections.observableArrayList("Debug on/off", "Debug on", "Debug off"));
    	debugModes.getSelectionModel().select(0);
    	
    	idCol.setCellValueFactory(cellData -> cellData.getValue().getId());
    	inputFileCol.setCellValueFactory(cellData -> cellData.getValue().getInputFile());
    	executionModeCol.setCellValueFactory(cellData -> cellData.getValue().getExecutionMode());
    	appliedControlsCol.setCellValueFactory(cellData -> cellData.getValue().getAppliedControls());
    	outputFileCol.setCellValueFactory(cellData -> cellData.getValue().getOutputFile());
    	
    	execLog4all = execLog;
    	
    	results.setOnMousePressed(new EventHandler<MouseEvent>() {
    	    @Override 
    	    public void handle(MouseEvent event) {
    	        if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
    	            System.out.println(results.getSelectionModel().getSelectedItem());
    	            MainClient.showComparison(results.getSelectionModel().getSelectedItem());
    	            //MainClient.shownComparison2(results.getSelectionModel().getSelectedItem());
    	        }
    	    }
    	});
    }
    
    @FXML
    private void handleButtonPdi()
    {
    	fileClicked(pdiFilename);
    }
    
    @FXML
    private void handleButtonPo()
    {
    	fileClicked(poFilename);
    }
    
    @FXML
    private void handleButtonToken()
    {
    	fileClicked(tokensFilename);
    }
    
    @FXML
    private void handleButtonRuleset()
    {
    	fileClicked(rulesetFilename);
    }
    
    @FXML
    private void handleButtonInputFiles()
    {
    	fileClicked(inputDiagramsFilename);
    }
        
    private void fileClicked(TextField filename)
    {        
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = 
                new FileChooser.ExtensionFilter("TEXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
    	fileChooser.setTitle("Open File");

    	String lastSelectedDir = preferences.get("lastAccessedDir","");
    	File lastSelectedDirFile = new File (lastSelectedDir);
    	if (lastSelectedDir.length()>0)
    	{
    		if (lastSelectedDirFile.exists())
    		{
    			fileChooser.setInitialDirectory(new File(lastSelectedDir));	
    		}
    		else
    		{
    			preferences.remove("lastAccessedDir");
    		}
    	}
    	
        // Show open file dialog
        File file = fileChooser.showOpenDialog(MainClient.primaryStage);
        if (file != null) 
        {
        	filename.setText(file.getPath());
        	preferences.put("lastAccessedDir", file.getParent());
        }
    }

    @FXML
    private void executeClicked() 
    {
        if (validSettings()) 
        {
        	execLog.clear();
            results.setItems(null);
                        
            execLog.appendText("Evolving diagrams ...\n");

            List <String> lDebModes = new ArrayList<String>();
            //if (debugModes.getValue().contains("on")) lDebModes.add("True");
            //if (debugModes.getValue().contains("off")) lDebModes.add("False");
            if (debugModes.getValue().contains("Yes")) lDebModes.add("True");
            if (debugModes.getValue().contains("No")) lDebModes.add("False");
            String[] saDebModes = lDebModes.toArray(new String[lDebModes.size()]);
            
                new Thread() 
                {
                    public void run() 
                    {
                        List<VisualResultsTO> outputs;
				
                        try 
                        {
                        	outputs = MainUtils.processInjectionBatch( saDebModes,
										poFilename.getText(), pdiFilename.getText(), inputDiagramsFilename.getText(), 
										rulesetFilename.getText(), tokensFilename.getText());
                        	
                            ObservableList<VisualResultsTO> olResults = FXCollections.observableArrayList(outputs);
                            Platform.runLater(new Runnable() 
                            {
                                @Override
                                public void run() 
                                {
                                    results.setItems(olResults);
                                }
                            });

						}
                        catch (Exception e) 
                        {
                        	 execLog.appendText("Error while trying to evolve diagrams:"+"\n" + e.getMessage()+"\n");
						}
                    }
                }.start();
        } 
        else 
        {
        	execLog.appendText("Missing input fields. Please correct this.");
        }
    }

    private boolean validSettings() 
    {
    	
        if(
        	debugModes.getValue() == null || 
        	pdiFilename.getText() == null || pdiFilename.getText().length()==0 ||
        	poFilename.getText() == null || poFilename.getText().length()==0 ||
   			tokensFilename.getText() == null || tokensFilename.getText().length()==0 ||
   			rulesetFilename.getText() == null || rulesetFilename.getText().length()==0 ||
   			inputDiagramsFilename.getText() == null || inputDiagramsFilename.getText().length()==0
			)
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(MainClient.primaryStage);
            alert.setTitle("Invalid settings!");
            alert.setHeaderText("Empty input parameter");
            alert.setContentText("At least one of the input parameters is empty. Please select non-empty values.");
            alert.showAndWait();
        	return false;
        }
        else
        {
        	return true;
        }
    }
    
    /*
    @FXML
    private void analyseClicked() {
        VisualResultsTO comparison = results.getSelectionModel().getSelectedItem();
        if (comparison != null) {
            MainClient.showComparison(comparison);
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(MainClient.primaryStage);
            alert.setTitle("No comparison selected!");
            alert.setContentText(
                    "Please select a comparison from the results table to view.");
            alert.showAndWait();
        }
    }*/
    }
