package edu.ucd.forcops.main.ui;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.commons.io.FileUtils;

import edu.ucd.forcops.main.MainUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.sourceforge.plantuml.Log;

public class MainScreenControllerv2 
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
    private TextField wdFilename;
    
    @FXML
    public TextArea execLog;
    
    public static TextArea execLog4all;
    public static Stage testDataStage;
    public static List<Button> alSteps;
    
    public static String sampleDatasetPath;


    Preferences preferences = Preferences.userRoot().node("AdaptIT");
    
    /**
     * Called automatically when this controller class is created. All JavaFX components are set up and populated.
     */
    @FXML
    public void initialize() 
    {
    	debugModes.setItems(FXCollections.observableArrayList("","Yes", "No"));
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
    	            //System.out.println(results.getSelectionModel().getSelectedItem());
    	            MainClient.showComparison(results.getSelectionModel().getSelectedItem());
    	            //MainClient.shownComparison2(results.getSelectionModel().getSelectedItem());
    	        }
    	    }
    	});
    }
    
    
    @FXML
    private void handleButtonChooseWD()
    {
    	directoryChooser(wdFilename);
    	
    	// A wd has been chosen
    	if (wdFilename.getText() != null && wdFilename.getText().length() != 0) 
    	{
	    	//Check if default artifact file exits, create them if not
	    	 String poFilename = wdFilename.getText() + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXPO + ".txt";
	         String pdiFilename = wdFilename.getText() + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXPDI + ".txt";
	         String tokensFilename = wdFilename.getText() + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXTOKENS + ".txt";
	         String rulesetDir = wdFilename.getText() + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXRULESET; //directory ruleset
	         String scenariosDir = wdFilename.getText() + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXSCENARIOS; //directory scenarios
	         boolean poexists = new File(poFilename).exists();
	         boolean pdiexists = new File(pdiFilename).exists();
	         boolean tokenexists = new File(tokensFilename).exists();
	         boolean rulesetexists = new File(rulesetDir).exists();
	         boolean scenexists = new File(scenariosDir).exists();
	         
	         if (!poexists) {
	        	 try {
					new File(poFilename).createNewFile();
					Log.debug("Creating default artifact file " + poFilename);
				} catch (IOException e) {
					e.printStackTrace();
				}
	         }
	         if (!pdiexists) {
	        	 try {
					new File(pdiFilename).createNewFile();
					Log.debug("Creating default artifact file " + pdiFilename);
				} catch (IOException e) {
					e.printStackTrace();
				}
	         }
	         if (!tokenexists) {
	        	 try {
					new File(tokensFilename).createNewFile();
					Log.debug("Creating default artifact file " + tokensFilename);
				} catch (IOException e) {
					e.printStackTrace();
				}
	         }
	         if (!rulesetexists) {
	        	 new File(rulesetDir).mkdir();
	        	 Log.debug("Creating default artifact folder " + rulesetDir);
	         }
	         if (!scenexists) {
	        	 new File(scenariosDir).mkdir();
	        	 Log.debug("Creating default artifact folder " + scenariosDir);
	         }
    	}
    }
    
    private boolean validateWorkingDirectory() 
    {
    	String wdFileName = wdFilename.getText();
    	if (wdFileName == null || wdFileName.length()==0) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(MainClient.primaryStage);
            alert.setTitle("Invalid Input Parameter");
            alert.setHeaderText("Working Directory is empty");
            alert.setContentText("Please select one directory to upload artifact files");
            alert.showAndWait();
            return false;
    	} else {
    		return true;
    	}
    }
       
    // DATA INVENTORY: EDIT button for Data Inventory was clicked. Call main client to show new window.
    @FXML
    private void handleButtonDataDicEdit()
    {
    	if (validateWorkingDirectory()) {
    		MainClient.showEditDictionaryWnd(wdFilename.getText());
    	}     	
    }
    
    @FXML
    private void handleButtonRulesetEdit()
    {
    	if (validateWorkingDirectory()) {
    		MainClient.showEditRulesetWnd(wdFilename.getText());
    	}
    }
    
    @FXML
    private void handleButtonScenariosEdit()
    {
    	if (validateWorkingDirectory()) {
    		MainClient.showEditScenariosWnd(wdFilename.getText());
    	}
    }
    @FXML
    private void handleButtonEScenariosEdit()
    {
    	if (validateWorkingDirectory()) {
    		MainClient.showEditScenariosWnd(wdFilename.getText(), "evolved");
    	}
    }
    
    
    private void directoryChooser(TextField wdFilename)
    {
    	DirectoryChooser directoryChooser = new DirectoryChooser();
    	String lastSelectedDir = preferences.get("lastAccessedDir","");
    	File lastSelectedDirFile = new File (lastSelectedDir);
    	if (lastSelectedDir.length()>0)
    	{
    		if (lastSelectedDirFile.exists())
    		{
    			directoryChooser.setInitialDirectory(new File(lastSelectedDir));	
    		}
    		else
    		{
    			preferences.remove("lastAccessedDir");
    		}
    	}
    	File selectedDirectory = directoryChooser.showDialog(MainClient.primaryStage);
        Log.info(">>> WORKING DIRECTORY set as " + selectedDirectory.getAbsolutePath() + "<<<");
        if (selectedDirectory != null) 
        {
        	wdFilename.setText(selectedDirectory.getPath());
        	preferences.put("lastAccessedDir", selectedDirectory.getParent());
        }
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
    
    private boolean createScenariosFile(String scenariosDir) 
    {
    	//Iterate directory of scenarios
    	File repo = new File (scenariosDir);
        File[] fileList = repo.listFiles();
        ArrayList<String> filePaths = new ArrayList<String>();
        //iterate list of files in directory to add their name in a list and then write it in a file
        for (int i = 0 ; i < fileList.length ; i++) {
        	//filter out the output files
        	if (!fileList[i].getName().contains("_o[")) {
        		filePaths.add((fileList[i]).getAbsolutePath().trim());
        	}
        }
      //Write POs to file (clear file and create a new one)
        File file = new File(scenariosDir+ ".txt");
      	try {
			FileUtils.writeLines(file, filePaths, false);
			Log.debug("Diagrams file " + scenariosDir+ ".txt" + " created");
			return true;
		} catch (IOException e) {
			MainUtils.reportNonFatalError("Error while generating the diagrams file " + file.getAbsolutePath());
			//e.printStackTrace();
			return false;
		}		
    }
    
    
    private boolean consolidateRulesetFile(String rulesetDir) 
    {
    	//Iterate directory of priv ctrl rules
    	File repo = new File (rulesetDir);
        File[] fileList = repo.listFiles();
        ArrayList<String> allInstructions = new ArrayList<String>();
        
        // Delete old file if existing
        File consolidatedFile = new File(rulesetDir+ ".txt");
        boolean resultDelete = consolidatedFile.delete();
        if (resultDelete) {
        	Log.debug("Old version of consolidated ruleset file " + rulesetDir+ ".txt" + " deleted");
        }
        
        //iterate list of rule files in directory
        for (int i = 0 ; i < fileList.length ; i++)
        {
        	//retrieve lines in current file and write to consolidated file
        	try {
				List<String> ruleLines = FileUtils.readLines(fileList[i]);
				 //Add blank line to separate one control from another
				ruleLines.add("\n");
				FileUtils.writeLines(consolidatedFile, ruleLines, true);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
        }
        return true;
    }

    @FXML
    private void executeClicked() 
    {  	
    	Log.debug("EXECUTE btn clicked, creating diagrams file, consolidating rulesets...");
    	String wdFileName = wdFilename.getText();
    	//Working directory has been chosen
    	if (validateWorkingDirectory()) 
    	{
    		String poFilename, pdiFilename, diagFilename, rulesetFilename, tokensFilename;
            poFilename = wdFileName + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXPO + ".txt";
            pdiFilename = wdFileName + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXPDI + ".txt";
            diagFilename = wdFileName + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXSCENARIOS + ".txt";
            rulesetFilename = wdFileName + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXRULESET + ".txt";
            tokensFilename = wdFileName + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXTOKENS + ".txt";
	        
            //Create the files that need to be sent to support backend compatibility
            //scenarios, get the scenarios directory only
            boolean scenariosFileCreated = createScenariosFile(diagFilename.replace(".txt", ""));
            //consolidate the content of priv ctrl rules in a file
            boolean rulesetFileCreated = consolidateRulesetFile(rulesetFilename.replace(".txt", ""));
            //pdi (is now key, value)
            
            
            if (validateArtifactFiles(poFilename, pdiFilename, diagFilename, rulesetFilename, tokensFilename)) 
	        {
	        	//Prepare the files needed for backend compatibility
	        	//execLog.clear();
	            results.setItems(null);
	            
	            List <String> lDebModes = new ArrayList<String>();
	            if (debugModes.getValue().contains("Yes")) 
	            {
	            	lDebModes.add("True");
	            }
	            else if (debugModes.getValue().contains("No")) 
	            	{
	            	lDebModes.add("False");
	            	}
	            else 
	            {
	                Alert alert = new Alert(AlertType.ERROR);
	                alert.initOwner(MainClient.primaryStage);
	                alert.setTitle("Missing debug mode");
	                alert.setHeaderText("Show evolution in diagrams combobox is unselected!");
	                alert.setContentText("Please select one of the options (i.e., Yes or No)");
	                alert.showAndWait();
	                return;
	            }
	            
	            execLog.appendText("Evolving diagrams ...\n");
	            
	            String[] saDebModes = lDebModes.toArray(new String[lDebModes.size()]);
	            
	                new Thread() 
	                {
	                    public void run() 
	                    {
	                        List<VisualResultsTO> outputs;
					
	                        try 
	                        {
	                        	outputs = MainUtils.processInjectionBatch(saDebModes,
	                        			poFilename, pdiFilename, diagFilename, rulesetFilename, tokensFilename);
	                        	
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
        
    }

    private boolean validateArtifactFiles(String poFilename, String pdiFilename, String diagFilename, String rulesetFilename, String tokensFilename) 
    {
       //Check that files are not empty, they must exist as we create them when choosing wd if they dont
        boolean isPoEmpty = new File(poFilename).length()==0?true:false;
        boolean isPdiEmpty = new File(pdiFilename).length()==0?true:false;
        boolean isDiagEmpty = new File(diagFilename).length()==0?true:false;
        boolean isRuleEmpty = new File(rulesetFilename).length()==0?true:false;
        boolean isTokEmpty = new File(tokensFilename).length()==0?true:false;
        
        if(debugModes.getValue() == null || isPoEmpty || isPdiEmpty || isDiagEmpty || isRuleEmpty || isTokEmpty )
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(MainClient.primaryStage);
            alert.setTitle("Invalid settings");
            alert.setHeaderText("Empty input parameter");
            alert.setContentText("At least one of the artifact files is empty. Please define all parameters");
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
    
    
    @FXML
    private void handleButtonLoadSampleDataUni()
    {
    	handleButtonLoadSampleData(MainClient.SAMPLE_APP_UNIXYZ);
    }
    
    @FXML
    private void handleButtonLoadSampleDataFenix()
    {
   		handleButtonLoadSampleData(MainClient.SAMPLE_APP_FENIXEDU);
    }
    
    private void handleButtonLoadSampleData(String dataset)
    {
    	//System.out.println("Entering handleButtonLoadSampleData!");

    	if (testDataStage != null)
    	{
    		Alert alert = new Alert(AlertType.INFORMATION);
    		alert.setTitle("Load Sample Windows already opened!");
    		alert.setHeaderText("Overwrite Alert");
    		alert.setContentText("The contents of the selected dataset will overwrite the existing content of the Load Sample window.");
    		alert.showAndWait();
    		testDataStage.close();
    	}
    	
        TableView<StepInfo> table = new TableView<>();
        ObservableList<StepInfo> tvObservableList = FXCollections.observableArrayList();
        //ObservableList<StepInfo> tvObservableList = FXCollections.observableArrayList(si -> new Observable[] {si.name});
        
        alSteps = new ArrayList<>();
        
        String popupTitle = "Steps to replicate ["+dataset+"] application";
        String destDir = MainClient.TMP_DIR;//System.getProperty("user.dir")+MainClient.DIR_OS_SEPARATOR+"tmp";
        
        tvObservableList.addAll(
        		new StepInfo(1, "Import\nInputs",
        				"TASK 1. All visual components are cleared at the main window.\n"+
        				"TASK 2. A working folder is created at the same location of the tool's JAR file.\n"+
        				"TASK 3. The data dictionary, the non-compliant diagrams, and the controls catalogue are imported. "+
        				"They can be visually inspected by clicking their corresponding buttons below the 'Admin of Artifacts' header in the main window.", 
        				true,
        				() -> {
        						MainUtils.reportInfo("About to execute step 1 (import inputs) for the ["+dataset+"] application!");
        						
        						// Just to be safe, closing all edit windows first!
        						closeEditWndsIfOpen();
        						
        						//First, we clear all the GUI components.
        						clearGUI();
        						
        						//Next, we load the export/unzip/place the sample data for the chosen application in a new subdir.
        						loadSampleData(dataset, destDir);
        						
        						//Next, we configure the new subdir as the working directory (so that all buttons -in the 'Admin of Artifacts' section- used it to parse/show the data).
        						setWorkingDirToSampleDataDir(dataset, destDir);
        				}),
                new StepInfo(2, "Create\nData Filters", 
                		"TASK 1. All relevant data filters are created. "+
                		((dataset.equals(MainClient.SAMPLE_APP_UNIXYZ))?"(For UniXYZ, only a few examples are created to illustrate the functionality)":"")+
                		"\n"+
                		"They are visible in the data filter combo box located at the left side of the non-compliant diagrams screen,"+
                		"which is accessible via its corresponding button below the 'Admin of Artifacts' header in the main window.",
                		() -> {
                				MainUtils.reportInfo("About to execute step 2 (create data filters) for the ["+dataset+"] application!");

        						// Just to be safe, closing all edit windows first!
                				closeEditWndsIfOpen();
                				
                				loadSampleDataFilters();
                		}), 
                new StepInfo(3, "Configure\nControls", 
                		"TASK 1. Based on the observations taken in the previous step, the catalogue of controls is configured.\n"+
                		"They can be visually inspected in the Catalog of PrivControls screen "+
                		"(which is accessible via its corresponding button below the 'Admin of Artifacts' header in the main window). "+
                		"The pointcuts, advices, and tokens associated to a particular control can be inspected by selecting the control of interest and pressing the button 'configure'.",
                		() -> {
                				MainUtils.reportInfo("About to execute step 3 (configure controls) for the ["+dataset+"] application!");
            				
        						// Just to be safe, closing all edit windows first!
                				closeEditWndsIfOpen();
                				
                				configControls();
            				
                		}), 
                new StepInfo(4, "Evolve\nControls", 
                		"TASK 1. Based on the configured controls, the non-compliant diagrams are evolved (with the 'show evolution in diagrams' flag enabled for easier visualization of the changes.\n"+
                		"The evolved diagrams are listed in the big table located at the center/right of the main window."+
                		"A non-compliant diagram can be visually compared against its corresponding evolved one by clicking on its corresponding table row "+
                		"(which also lists what controls were applied per diagram)", 
                		() -> {
                			MainUtils.reportInfo("About to execute step 4 (evolve controls) for the ["+dataset+"] application!");

    						// Just to be safe, closing all edit windows first!
            				closeEditWndsIfOpen();
            				
            				triggerEvolution();
                		})
                );

        /*    
		S02. Create all data filters used + highlights of what architect noted? (maybe high-level summary of what becomes the rules?) + also sorting?
		S04. Evolve controls.        
         */

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefWidth(1000);
        table.setPrefHeight(630);
        table.setItems(tvObservableList);

        TableColumn<StepInfo, Integer> colId = new TableColumn<>("Step Id");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<StepInfo, String> colName = new TableColumn<>("Step Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<StepInfo, String> colSummary= new TableColumn<>("Step Summary");
        colSummary.setCellValueFactory(new PropertyValueFactory<>("longText"));
        
        //Logic to implement wrapping for a particular column!
        colSummary.setCellFactory(new Callback<TableColumn<StepInfo,String>, TableCell<StepInfo,String>>() {
			@Override
			public TableCell<StepInfo, String> call( TableColumn<StepInfo, String> param) {
				final TableCell<StepInfo, String> cell = new TableCell<StepInfo, String>() {
					private Text text;
					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (!isEmpty()) {
							text = new Text(item.toString());
							text.setWrappingWidth(700); // Setting the wrapping width to the Text
							setGraphic(text);
						}
					}
				};
				return cell;
			}
		});
                
        TableColumn<StepInfo, Void> colBtn = new TableColumn<>("Execute Step");
        colBtn.setCellFactory(
        		new Callback<TableColumn<StepInfo, Void>, TableCell<StepInfo, Void>>() {
		            @Override
		            public TableCell<StepInfo, Void> call(final TableColumn<StepInfo, Void> param) {
		                final TableCell<StepInfo, Void> cell = new TableCell<StepInfo, Void>() {
		
		                	// OPD, Nov29,2022: I temporarily added the size to the button name to see what was happening.
		                	// I found out that, for an unknown reason (probably a JavaFX bug!), any method that refreshes the UI inverts the sorting of the table!
		                	// (so, after the first refresh the first buttons -i.e., the ones that should work- become invisible as they are now at the bottom!).
		                    //public final Button btn = new Button("Pending to Execute"+alSteps.size());
		                    public final Button btn = new Button("Pending to Execute");
		                    
		                    {	
		                        btn.setOnAction((ActionEvent event) -> {
		                        	
		                        	//First, get the data related to this element.
		                            StepInfo data = getTableView().getItems().get(getIndex());
		                            //System.out.println("selectedData: " + data);
		                            
		                            //Then, execute whatever logic is needed (mainly in the main screen!).
		                            data.getLogicToExec().execute();
		                         
		                            //Then, update the text that is presented in the Step Summary column.
		                            data.setName(data.getName()+":\n"+data.getLongText());
		                            //System.out.println(getTableView().getColumns().size());
		                            
		                            /*
		                            getTableView().getColumns().get(0).setVisible(false);// Trick to refresh the values in the Table. For some unknown reason, the button column gets inversely sorted.
		                            getTableView().getColumns().get(0).setVisible(true);
		                            table.getColumns().remove(2);
		                            table.getColumns().remove(1);
		                            table.getColumns().add(1,colName);
		                            table.getColumns().add(1,colBtn);
		                            */
		                            //table.refresh();
		                            //table.sort();
		                            
		                            //Finally, disable this button + enable the next button!
		                            btn.setDisable(true);
		                            btn.setText("Executed");
		                            //alSteps.get(data.getId()).setDisable(true);	 //1
		                            //alSteps.get(data.getId()).setText("Executed"); //1
		                            alSteps.get(data.getId()).setDisable(false);     //2
		                            
		                        });		                        
		                        
		                        //In order to control/have access to the other buttons, storing them inside a List!
		                        if (alSteps.size()>0)//Not sure why 2 :-P ...
		                        {
		                        	//System.out.println("alSteps size:"+alSteps.size());
		                        	btn.setDisable(true);
		                        }
			                    alSteps.add(btn);
		                    }
		
		                    // It seems this method is required to actually make the button visible!
		                    @Override
		                    public void updateItem(Void item, boolean empty) {
		                        super.updateItem(item, empty);
		                        if (empty) {
		                            setGraphic(null);
		                        } else {
		                            setGraphic(btn);
		                        }
		                    }
		                };
		                return cell;
		            }
		        });
        
        table.getColumns().addAll(colId, colName, colSummary, colBtn);
        
        //*
        table.sortPolicyProperty().set(t -> {
            Comparator<StepInfo> comparator = (r1, r2) -> {return r1.getId()-r2.getId();};
            FXCollections.sort(table.getItems(), comparator);
            return true;
        });//*/
        
        //*
        colId.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        colName.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        colSummary.prefWidthProperty().bind(table.widthProperty().multiply(0.7));
        colBtn.prefWidthProperty().bind(table.widthProperty().multiply(0.13));
        
        colId.setResizable(false);
        colName.setResizable(false);
        colSummary.setResizable(false);
        colBtn.setResizable(false);
        //*/

        /*
        colId.setPrefWidth(50);
        colName.setPrefWidth(50);
        colSummary.setPrefWidth(850);
        colBtn.setPrefWidth(50);
        */
                
        table.setFixedCellSize(150.0);
        //final ScrollBar  scrollBar = (ScrollBar) table.lookup(".scroll-bar");
        //scrollBar.setVisible(false);
        
        VBox vbox = new VBox(table);
        
        Scene scene = new Scene(vbox);
        //Scene scene = new Scene(new Group(table));
                
        testDataStage = new Stage();
        testDataStage.setTitle(popupTitle);
        testDataStage.setWidth(1000);
        testDataStage.setHeight(670);
        testDataStage.setScene(scene);
        testDataStage.setResizable(false);
        testDataStage.show();

        testDataStage.setOnCloseRequest( event -> {testDataStage=null;} );
    }
    
    public void closeEditWndsIfOpen()
    {
    	if (MainClient.dictWnd != null)
    	{
    		MainUtils.reportInfo("The dictionary edit window is opened, so closing it (to avoid possible data corruption).");
    		MainClient.dictWnd.close();
    	}
    	
    	if (MainClient.scenariosWnd != null)
    	{
    		MainUtils.reportInfo("The diagrams edit window is opened, so closing it (to avoid possible data corruption).");
    		MainClient.scenariosWnd.close();
    	}
    	
    	if (MainClient.rulesetWnd != null)
    	{
    		MainUtils.reportInfo("The ruleset edit window is opened, so closing it (to avoid possible data corruption).");
    		MainClient.rulesetWnd.close();
    	} 		
    }
    
    public void triggerEvolution()
    {
    	//First, set the checkbox to show the changes.
		Platform.runLater(new Runnable() 
	    {
	         @Override
	         public void run()
	         {
	        	 //debugModes.getSelectionModel().select(1); 
	        	 debugModes.setValue("Yes");
	        	 
	     		//Then, start the software evolution by calling the SAME method that the onClick of its button!
	        	//Initially outside the runLater, but IF the debug mode is not set, the logic (obviously!) fails. 
	     		executeClicked();
	         }
	    });

    }
    
    public void configControls()
    {
        File new_ruleset = new File(sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXRULESET+"_new");
        File config_ruleset = new File(sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXRULESET+"_configured");
        File ruleset = new File (config_ruleset.getAbsolutePath().replace("_configured",""));
    	
    	// First, we rename the ruleset subdir back to ruleset_new.
        //System.out.println("About to rename ["+ruleset.getPath()+"] as ["+new_ruleset.getPath()+"].");
        boolean successRename = ruleset.renameTo(new_ruleset);
    	
    	// Then, we rename the ruleset_configured to ruleset.
        //System.out.println("About to rename ["+config_ruleset.getPath()+"] as ["+ruleset.getPath()+"].");
        successRename = config_ruleset.renameTo(ruleset);
        
        // Next, we will rename tokens.txt as tokens_dfilters.txt and tokens_configured.txt as tokens.txt (so that the configured values are accessible for the configured control rules!).
        File dfilters_tokens = new File(sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXTOKENS+"_dfilters.txt");
        File config_tokens = new File(sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXTOKENS+"_configured.txt");
        File tokens = new File (config_tokens.getAbsolutePath().replace("_configured",""));
        successRename = tokens.renameTo(dfilters_tokens);
        successRename = config_tokens.renameTo(tokens);
    }
    
    public static void deleteSampleDataDir()
    {	
     	if (sampleDatasetPath != null)
    	{
            File dirToDelete = new File(sampleDatasetPath).getParentFile();
            if (dirToDelete.exists()) {
            	//System.out.println("If ["+sampleDatasetPath+"] directory exists, it should be deleted!");
            	MainUtils.deleteDir(dirToDelete);
            }
    	}
    }

    
    public void setWorkingDirToSampleDataDir(String dataset, String destDir)
    {
    
		Platform.runLater(new Runnable() 
	    {
	         @Override
	         public void run()
	         {
					wdFilename.setText(destDir+MainClient.DIR_OS_SEPARATOR+dataset);
	         }
	    });
    }
    
    public void loadSampleData(String dataset, String destDir)
    {
    	// First, create a tmp subdir in the SAME location as the jar file (already tested!).
    	// (in case the test data has already been tried, the old one will also be deleted).
        //String destDir = System.getProperty("user.dir")+"/tmp";
        //System.out.println("destDir: " + destDir);
    	
    	//OPD Jan13 2023: Logic has changed: Now, it is EXPECTED that the tmp directory exist, so this validation is only to make sure old dataset-info does not exist there!
    	// Probably easier to review BOTH supported datasets (if more datasets are supported later on, it might be worth revisiting this approach!).
        File destDirFile = new File(destDir);
    	/*
        if (destDirFile.exists()) {
        	//System.out.println("If directory exists, it should be deleted!");
        	MainUtils.deleteDir(destDirFile);
        }
        destDirFile.mkdir();
        */
    	MainClientUtils.deleteDir(destDir+MainClient.DIR_OS_SEPARATOR+MainClient.SAMPLE_APP_UNIXYZ);
    	MainClientUtils.deleteDir(destDir+MainClient.DIR_OS_SEPARATOR+MainClient.SAMPLE_APP_FENIXEDU);
    	
        // Next, decompress the zip file (of the appropriate dataset!) from the jarfile into the tmp subdir.
		InputStream myZip = this.getClass().getResourceAsStream("demos/"+dataset+".zip");
		File myZipFile = new File(destDirFile.getAbsolutePath()+MainClient.DIR_OS_SEPARATOR+dataset+".zip");
		try {
			FileUtils.copyInputStreamToFile(myZip, myZipFile);
		} catch (IOException e) {
			MainUtils.reportNonFatalError("Error extracting the dataset zip file from jarfile.");
		}

    	// Next, unzip the zip file in the new subdir.
        String zipFilePath = myZipFile.getAbsolutePath();
		//System.out.println("zipFilePath:"+zipFilePath);
        
        try {
			MainUtils.unzip(Paths.get(zipFilePath), Charset.forName("UTF-8"));
		} catch (IOException e) {
			MainUtils.reportNonFatalError("Error trying to unzip the dataset file.");
		}
        
        // As "workaround" to have an initial "template" for the controls (pending to be cleared, by the way).
        // (storing the path in the variable "sampleDatasetPath" as it will be used for the other steps!).
        sampleDatasetPath = zipFilePath.replace(".zip", ""); //The dataset name is ALREADY part of the zip filename!!!
        File new_ruleset = new File(sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXRULESET+"_new");
        File ruleset = new File (new_ruleset.getAbsolutePath().replace("_new",""));
        //System.out.println("About to rename ["+new_ruleset.getPath()+"] as ["+ruleset.getPath()+"].");
        boolean successRename = new_ruleset.renameTo(ruleset);
        
        // Next, we will rename tokens_new.txt as tokens.txt (so that the default/initial values are accessible for the template control rules.
        File new_tokens = new File(sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXTOKENS+"_new.txt");
        File tokens = new File (new_tokens.getAbsolutePath().replace("_new",""));
        successRename = new_tokens.renameTo(tokens);
        
        // Finally, delete the zip file as it is no longer needed (as its files have been unzipped).
        myZipFile.delete();
    }
    
    public void loadSampleDataFilters()
    {        
        File new_dfs = new File(sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXDF+"_new");
        File config_dfs = new File(sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXDF+"_configured");
        File dfs = new File (sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXDF);
    	
    	// First, we rename the existing DF subdir to ruleset_new.
        boolean successRename = dfs.renameTo(new_dfs);
    	
    	// Then, we rename the ruleset_configured to ruleset.
        successRename = config_dfs.renameTo(dfs);
        
        // Next, we replace the original Tokens template with the version that has "dummy" values used by the DF (used in the analysis).
        File new_tokens = new File(sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXTOKENS+"_new.txt");
        File dfilters_tokens = new File(sampleDatasetPath+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXTOKENS+"_dfilters.txt");
        File tokens = new File (dfilters_tokens.getAbsolutePath().replace("_dfilters",""));
        successRename = tokens.renameTo(new_tokens);
        successRename = dfilters_tokens.renameTo(tokens);
    }
    
    public void clearGUI()
    {
    	 Platform.runLater(new Runnable() 
         {
             @Override
             public void run() 
             {
            	 //First, we clear the 4 visual components in the main windows 
            	 //(i.e., the working directory text field, execution text area, results table, and the dropdown to select if the changes will be shown).
            	 //execLog.clear();
            	 debugModes.getSelectionModel().select(0); //debugModes.setValue("");
            	 wdFilename.clear();
            	 
            	 results.setItems(null);
                 results.refresh();
                 
                 //Then, we need to "clear" the other Windows that are accessible via the buttons (data dic, nc diagrams, sec controls, and evolved diagrams).
                 //However, this step is not actually needed thanks to the current behaviour of those buttons! 
                 //(if an user tries to access them when the working dir is empty, they cannot access them! Thus, nothing to do here :-).
             }
         });
    }
    
    //**********************************************************************************
    //**********************************************************************************
    //**********************************************************************************
    
    public interface StepExec{
    	
    	void execute();
    }
	    
    public class StepInfo {

    	private final StringProperty name = new SimpleStringProperty("");
    	
        private int id;
        //private String name;
        private String longText;
        private boolean btnEnabled = false;
        private StepExec logicToExec;

        private StepInfo(int id, String name, String longText) {
            this.id = id;
            this.setName(name);
            this.longText = longText;
        }

        private StepInfo(int id, String name, String longText, StepExec se) {
            this.id = id;
            this.setName(name);
            this.longText = longText;
            this.logicToExec = se;
           
        }
        
        private StepInfo(int id, String name, String longText, boolean btnEnabled) {
            this.id = id;
            this.setName(name);
            this.longText = longText;
            this.btnEnabled = btnEnabled;
        }
        
        private StepInfo(int id, String name, String longText, boolean btnEnabled, StepExec se) {
            this.id = id;
            this.setName(name);
            this.longText = longText;
            this.btnEnabled = btnEnabled;
            this.logicToExec = se;
        }
        
        public int getId() {
            return id;
        }

        public void setId(int ID) {
            this.id = ID;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String nme) {
            this.name.set(nme);
        }
        
        public void setBtnEnabled(boolean enabled) {
            btnEnabled = enabled;
        }
        
        public boolean getBtnEnabled() {
            return btnEnabled;
        }
        
        public StepExec getLogicToExec() {
			return logicToExec;
		}

		public void setLogicToExec(StepExec logicToExec) {
			this.logicToExec = logicToExec;
		}
		
		public String getLongText() {
			return longText;
		}

		public void setLongText(String longText) {
			this.longText = longText;
		}

		@Override
        public String toString() {
            return "id: " + id + " - " + "name: " + name + "btnEnabled: " + btnEnabled;
        }

    }//end of StepInfo
}
