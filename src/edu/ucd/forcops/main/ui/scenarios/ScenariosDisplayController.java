package edu.ucd.forcops.main.ui.scenarios;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.ucd.forcops.main.MainUtils;
import edu.ucd.forcops.main.ui.MainClient;
import edu.ucd.forcops.main.ui.MainClientUtils;
import edu.ucd.forcops.main.ui.TreeItemSerialisationWrapper;
import edu.ucd.forcops.main.ui.TreeViewWalker;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ScenariosDisplayController implements Initializable{
	
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	private static final String DF_APPLIED_MSG = ": [XXX] data filter applied";
	
	@FXML
	private Label lblSaveStatus;
	@FXML
	private TableView<ScenarioRow> tableViewScenario;
	@FXML
	private TableColumn<ScenarioRow,String> colId;
	@FXML
	private TableColumn<ScenarioRow,String> colName;
	@FXML
	private TableColumn<ScenarioRow,String> colMsgNum;
	@FXML
	private Label lblTitle;
	@FXML
	private Button btnAdd;
	@FXML
	private Button btnCfg;
	
	@FXML
	private Label lblDataFilters;
	@FXML
	private TreeView<String> tvDataFilters;
	@FXML
	private ComboBox<String> cbDataFilters;
	@FXML
	private TextField tfDataFilterName;
	@FXML
	private Label lblDataFilterElements;
	
	// To know if it was non-compliant or evolved the caller to this window
	private String callerType = "noncompliant";

	ObservableList<ScenarioRow> observableList = FXCollections.observableArrayList();
	
	ArrayList<String> scenarios = new ArrayList<String>();
	
	List<Set<String>> dfSetList = null; //OPD.- Dec2, 2022: This variable must ONLY be not-null when a DF has been applied.
	
	
	// Name of the scenarios directory that triggered this window
	private String currentScenariosFile = null;
	
	// To store the data filter dir
	private String dataFilterDir = null;
	
	//OPD, Dec20,2022: Temporal logic to quickly store the message number. If more meta-data is needed per diagram,
	//				it might be worth investing more time keeping it in a better data structure.
	private static int tmpLatestMsgNumber = -1;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		tableViewScenario.setItems(observableList);
		
		colId.setCellValueFactory(new PropertyValueFactory<ScenarioRow,String>("id"));
		colName.setCellValueFactory(new PropertyValueFactory<ScenarioRow,String>("name"));
		colMsgNum.setCellValueFactory(new PropertyValueFactory<ScenarioRow,String>("msgNum"));
		
		tvDataFilters.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		MultipleSelectionModel<TreeItem<String>> tvSelModel = tvDataFilters.getSelectionModel();
		
	    // Use a change listener to respond to a selection within a tree view (this SEEMS to work! ... so far)
		tvSelModel.getSelectedIndices().addListener((InvalidationListener) observable -> {
 
				//System.out.println("Selected POs/PDIs:");
				/*System.out.println(tvDataFilters.getSelectionModel().getSelectedItems()
  			        					.stream()
  			        					.map(TreeItem::getValue)
  			        					.collect(Collectors.toList()));*/

			 Platform.runLater(() -> {
				 	List<String> dfElements = tvDataFilters.getSelectionModel().getSelectedItems()
									.stream()
									.map(TreeItem::getValue)
									.collect(Collectors.toList());
				 	lblDataFilterElements.setText(dfElements.toString());
		 		});
	        });
		
		  //OPD: I tried this approach first, but the listener does not always receive the de-selections ...
		  //     https://stackoverflow.com/questions/21911773/javafx-weird-behavior-of-treeview-on-removal-of-the-selected-item
	      tvSelModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
	    	  @Override
	    	  public void changed(ObservableValue<? extends TreeItem<String>> changed, TreeItem<String> oldVal,
	               TreeItem<String> newVal) {
	    		  
	    		  /*
	    		  System.out.println("Selected POs/PDIs:");
	    		  System.out.println(tvDataFilters.getSelectionModel().getSelectedItems()
	    			        					.stream()
	    			        					.map(TreeItem::getValue)
	    			        					.collect(Collectors.toList()));
	    		  */
	    		/*
	            // Display the selection and its complete path from the root.
	            if (newVal != null) {
	               // create the entire path to the selected item.
	               String path = newVal.getValue();
	               TreeItem<String> tmp = newVal.getParent();
	               while (tmp != null) {
	                  path = tmp.getValue() + " -> " + path;
	                  tmp = tmp.getParent();
	               }

	               // Display the selection and the entire path.
	               System.out.println("Selection is " + newVal.getValue() + "\nComplete path is " + path);
	            }
	            else {
	               // create the entire path to the de-selected item.
	               String path = oldVal.getValue();
	               TreeItem<String> tmp = oldVal.getParent();
	               while (tmp != null) {
	                  path = tmp.getValue() + " -> " + path;
	                  tmp = tmp.getParent();
	               }

	               // Display the selection and the entire path.
	               System.out.println("De-selection is " + oldVal.getValue() + "\nComplete path is " + path);
	            }*/
	         }

	      });
		
	}

	public void buttonDeleteDataFilter()
	{
		 if (tfDataFilterName.getText().trim().length()==0) {
			 Alert alert = new Alert(AlertType.ERROR);
			 alert.initOwner(MainClient.scenariosWnd);
			 alert.setTitle("Invalid data filter deletion");
			 alert.setHeaderText("Empty mandatory parameter");
			 alert.setContentText("Please define the name of the data filter that you are trying to delete");
			 alert.showAndWait();
			 return;
		 }
		
		 String dfilterDir = currentScenariosFile.replace(MainClient.SUFFIXSCENARIOS,MainClient.SUFFIXDF);
		 File dfFile = new File(dfilterDir+MainClient.DIR_OS_SEPARATOR+tfDataFilterName.getText().trim()+MainClient.DF_EXT);
		
		 if (!dfFile.exists())
		 {
			 Alert alert = new Alert(AlertType.ERROR);
			 alert.initOwner(MainClient.scenariosWnd);
			 alert.setTitle("Non-existing data filter");
			 alert.setHeaderText("Non-existing data filter");
			 alert.setContentText("The data filter that you are trying to delete does not exist");
			 alert.showAndWait();
			 return;
		 }
		 else
		 {
			 //System.out.println("About to delete data filter!:"+dfFile.getAbsolutePath());
			 dfFile.delete();
			 
			 /*
			 Alert alert = new Alert(AlertType.INFORMATION);
			 alert.setTitle("Successful deletion");
			 alert.setHeaderText("Data Filter");
			 alert.setContentText("The data filter has been successfully deleted.");
			 alert.showAndWait();
			 */
			 MainClientUtils.showLabelTemporarily(lblSaveStatus, "DF deleted", 2);
			 
			 //PENDING TO REFLECT IT IN THE COMBO BOX!
			 String newItem = dfFile.getName().replace(MainClient.DF_EXT,"");
			 //System.out.println("About to remove deleted data filter to combobox:"+newItem);
			 Platform.runLater(() -> {
				 	cbDataFilters.getItems().remove(newItem);
				 	tvDataFilters.getSelectionModel().clearSelection();
					tfDataFilterName.clear();
					lblDataFilterElements.setText("");
			 		});
		 }
	}
	
	public void buttonResetDataFilter()
	{
		Platform.runLater(() -> {
			tvDataFilters.getSelectionModel().clearSelection();
			tfDataFilterName.clear();
			cbDataFilters.getSelectionModel().clearSelection();
			//OPD Dec21, 2022: Commenting the below line to make it (hopefully!) less confusing, as the DF is not actually removed from the TableView AFTER applying an empty one!
			//lblTitle.setText(lblTitle.getText().split(":")[0]);	//To remove the DF text, in case it exist! 
			});
		dfSetList = null;	//OPD.- Dec2, 2022: As the DF is not an instance variable, making it null as part of the reset button!
	}
	
	
	public void buttonSaveDataFilter()
	{
		 if (tfDataFilterName.getText().trim().length()==0) {
			 Alert alert = new Alert(AlertType.ERROR);
			 alert.initOwner(MainClient.scenariosWnd);
			 alert.setTitle("Invalid data filter configuration");
			 alert.setHeaderText("Empty mandatory parameter");
			 alert.setContentText("Please define a name for the data filter that you are trying to save");
			 alert.showAndWait();
			 return;
		 }
		 
		 List<TreeItem<String>> dfInfo = tvDataFilters.getSelectionModel().getSelectedItems()
				 															.stream()
				 															.collect(Collectors.toList());
		 if (dfInfo.isEmpty())
		 {
			 Alert alert = new Alert(AlertType.ERROR);
			 alert.initOwner(MainClient.scenariosWnd);
			 alert.setTitle("Invalid data filter configuration");
			 alert.setHeaderText("Empty mandatory parameter");
			 alert.setContentText("Please select at least one data filter element for the data filter that you are trying to save");
			 alert.showAndWait();
			 return;
		 }
		 
		 //System.out.println("Ready to save the data filter!: "+currentScenariosFile);
		 
		 String dfilterDir = currentScenariosFile.replace(MainClient.SUFFIXSCENARIOS,MainClient.SUFFIXDF);
		 File newdfFile = new File(dfilterDir+MainClient.DIR_OS_SEPARATOR+tfDataFilterName.getText().trim()+MainClient.DF_EXT);
		 
		 ObjectOutputStream os;
		 try {
			 os = new ObjectOutputStream(new FileOutputStream(newdfFile));
			 os.writeObject(serializeTreeItems(dfInfo));
			 os.close();

			 /*
			 Alert alert = new Alert(AlertType.INFORMATION);
			 alert.setTitle("Successful creation");
			 alert.setHeaderText("Data Filter");
			 alert.setContentText("The data filter has been successfully created.");
			 alert.showAndWait();
			 */
			 MainClientUtils.showLabelTemporarily(lblSaveStatus, "DF created", 2);

			 //PENDING TO REFLECT IT IN THE COMBO BOX!
			 String newItem = newdfFile.getName().replace(MainClient.DF_EXT,"");
			 //System.out.println("About to add newly created data filter to combobox:"+newItem);
			 Platform.runLater(() -> {
				 if (!cbDataFilters.getItems().contains(newItem))
				 {
					 cbDataFilters.getItems().add(newItem);
				 }
			 });
			 
		 } catch (Exception e) {
			 MainUtils.reportNonFatalError("There was an error while trying to save your data filter. Its details are:"+e.getMessage());
		}

	}
	
	public List<TreeItemSerialisationWrapper<String>> serializeTreeItems(List<TreeItem<String>> originalTreeItems)
	{	
		List<TreeItemSerialisationWrapper<String>> serializedTreeItems = new ArrayList<>();
		
		for (TreeItem<String> nextTI: originalTreeItems)
		{
			serializedTreeItems.add(new TreeItemSerialisationWrapper<String>(nextTI));
		}

		return serializedTreeItems;
	}
		
	public void loadDataFiltersInfo(String workingDir)
	{
        //dataFilterDir = workingDir.replace(MainClient.SUFFIXSCENARIOS,MainClient.SUFFIXDF);
        dataFilterDir = workingDir+MainClient.DIR_OS_SEPARATOR+MainClient.SUFFIXDF;
        
        List<String> files;
		try {
			files = MainUtils.findFiles(Paths.get(dataFilterDir), MainClient.DF_EXT);
			
	        files.forEach(x -> {
	        					//System.out.println("Data filter filename:"+x);
	        					
	        					String newItem = new File(x).getName().replace(MainClient.DF_EXT,"");
	        					//System.out.println("Data filter:"+newItem);
	        					
	        					Platform.runLater(() -> cbDataFilters.getItems().add(newItem));
	        			 		});

		} catch (IOException e) {
			MainUtils.reportNonFatalError("Error reading the data filter directory:"+dataFilterDir);
		}

		//Setting a listener to change the selected data filter elements depending on what data filter the user has chosen.
		cbDataFilters.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			   //System.out.println("The newly selected combo box value is:"+newValue);
			   //System.out.println("=> Which involves the following file:"+dataFilterDir+MainClient.DIR_OS_SEPARATOR+newValue+MainClient.DF_EXT);
			   
			   FileInputStream newdfFile;
			   List<TreeItem<String>> retrievedTreeItems;
			   
			   try {
				   newdfFile = new FileInputStream(dataFilterDir+MainClient.DIR_OS_SEPARATOR+newValue+MainClient.DF_EXT);

			        try (ObjectInputStream ois = new ObjectInputStream(newdfFile)) {
			        	retrievedTreeItems = (List<TreeItem<String>>) ois.readObject();
			        }
		        
			        //List<TreeItem<String>> dfElements = deserializeTreeItems(retrievedTreeItems);
				   
			        //retrievedTreeItems.forEach(x -> System.out.println("Retrieved TreeItem: "+x.getValue()+"] ["+x.getChildren().size()+"]"));
			        
			        MultipleSelectionModel<TreeItem<String>> msm = tvDataFilters.getSelectionModel();
			        
			        //OPD Dec21, 2022: Clearing the selection BEFORE start selecting the elements of the selected DF.
			        msm.clearSelection();
			        
			        TreeViewWalker.visit(tvDataFilters, (x) -> {
	    						//System.out.println("OPD: value of treeitem is:"+x.getValue());
	    						
	    						// As my TreeItemWrappers ONLY stores the value and the TI's children, 
	    						// ... that is what I will use to compare the elements!!!
	    						for (TreeItem<String> next: retrievedTreeItems)
			        			if (next.getValue().equals(x.getValue()) && 
			        					next.getChildren().size() == x.getChildren().size())
			        			{
			        				//System.out.println("==>>OPD, treeItem about to get selected!");
			        				msm.select(x);
			        			}
		        			});

					Platform.runLater(() -> tfDataFilterName.setText(newValue));
 
			} catch (FileNotFoundException e) {
				MainUtils.reportNonFatalError("File not found. Message:"+e.getMessage());
			}
			catch (IOException e) {
				MainUtils.reportNonFatalError("IO Exception. Message:"+e.getMessage());
			}
			catch (ClassNotFoundException e) {
				MainUtils.reportNonFatalError("Class Not Found Exception. Message:"+e.getMessage());
			}	 

			   
			});

	}
	
	public void loadDataDictionaryInfo(String workingDir)
	{
		//OPD Nov28, 2022: Adding logic to initialize the TreeView with the Data Dic elements: 
		//					POs in one branch, PDs in another branch.
		// This is inspired by "EditDataDictionaryController.showDataDictionary" which has similar logic!		
		TreeItem<String> rootItem = new TreeItem<String> ("Data Filter Elements");
        rootItem.setExpanded(true);

        //Build filenames for data dictionary POs and PDs
        String dataDicDir = workingDir.replace(MainClient.SUFFIXSCENARIOS,MainClient.SUFFIXRULESET);
    	String currentPOfile = dataDicDir + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXPO + ".txt";
    	String currentPDIfile = dataDicDir + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXPDI + ".txt";
    	
    	String currentTokenfile = dataDicDir.replace(MainClient.SUFFIXRULESET,"") + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXTOKENS + ".txt";
		
		// Read PO
    	parseDataDictionaryFile(rootItem, currentPOfile, "Processing Operations");
    
		// Read PDI
    	parseDataDictionaryFile(rootItem, currentPDIfile, "Processing Data Items");
    	
		// Read Tokens
    	parseDataDictionaryFile(rootItem, currentTokenfile, "Tokens");
    	
    	/*
        for (int i = 1; i < 6; i++) {
            TreeItem<String> item = new TreeItem<String> ("Message" + i);            
            rootItem.getChildren().add(item);
        }*/
        
		tvDataFilters.setRoot(rootItem);
	}
	
	//OPD: This method ONLY WORKS for two levels (keys and values) like our current config files! If they were deeper than two levels, the method will fail!
	public void parseDataDictionaryFile(TreeItem<String> rootItem, String filename, String typeName)
	{
		String DICTIONARY_SEP = ",";
		List<String> inputLines;

		try 
		{
			TreeItem<String> topItem = new TreeItem<String> (typeName); 
			rootItem.getChildren().add(topItem);
			
			inputLines = FileUtils.readLines(new File(filename));
			
			for (int x=0; x<inputLines.size(); x++)
			{
				if (inputLines.get(x).trim().startsWith("#"))
				{
					MainUtils.reportInfo("Skipping line ["+x+"] of file ["+filename+
							"] because is commented (i.e., starting with #).");
					continue;
				}
				
				String[] nextLine = inputLines.get(x).trim().split(DICTIONARY_SEP, 2); //READ first "," only

				String key = nextLine[0];
				TreeItem<String> keyItem = new TreeItem<String> (key);
				topItem.getChildren().add(keyItem);
				
				List<String> values = Arrays.asList(nextLine[1].split(DICTIONARY_SEP)); // Then add everything as values! (2nd level)
				for (String nextValue:values)
				{
					keyItem.getChildren().add(new TreeItem<String> (nextValue));
				}
				
				//OPD.- Dec19,2022: Sorting the DF elements for better clarity.
				keyItem.getChildren().sort(Comparator.comparing(t->t.getValue()));
			}
			
			//OPD.- Dec19,2022: Sorting the DF elements for better clarity.
			topItem.getChildren().sort(Comparator.comparing(t->t.getValue()));
		}
		catch (IOException e) 
		{
			MainUtils.reportNonFatalError("Error loading the data dictionary ("+typeName+") for the data filters.");
		}	
	}
	
	public void buttonApplyDataFilter()
	{
		dfSetList = new ArrayList<>();

		for (TreeItem<String> nextTI: tvDataFilters.getSelectionModel().getSelectedItems())
		{
			Set<String> nextDF = new HashSet<>();
			appendDataFilters(nextDF, nextTI);
			dfSetList.add(nextDF);
			logger.debug("Elements of the data filter set that will be applied:"+nextDF);
		}
		
		Platform.runLater(() -> {
			lblTitle.setText(lblTitle.getText().split(":")[0]+DF_APPLIED_MSG.replace("XXX", tfDataFilterName.getText()));
		});
		
		//System.out.println("currentScenariosFile->"+currentScenariosFile);
		showScenarios(currentScenariosFile, dfSetList);
		
		//xxx
		//hay que revisar ya que me trono . posiblemente xq necesito revisar que file list is not zero! (+ I need to implement the DS logic!)
	}
	
	public void appendDataFilters (Set<String> dfSet, TreeItem<String> nextTI)
	{
		
		if (nextTI.getValue().trim().length()>0)
		{
			dfSet.add(nextTI.getValue());
		}
		else
		{
			logger.debug("DF has an empty child value, so skipping it from the actual DF (as it causes errors while trying to apply the DF + it is meaningless!).");
		}
		
		for(TreeItem<String> child: nextTI.getChildren())
		{
			appendDataFilters(dfSet, child);
		}
	}
	
	// The original method showScenarios is the one called from the MainScreen (as NO data filters are ever applied then).
	public void showScenarios(String workingDir) //throws Exception
	{
		//System.out.println("====> SETTING THE VARIABLE currentScenariosFile");
		currentScenariosFile = workingDir + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXSCENARIOS;
		showScenarios(currentScenariosFile, null);
	}
	
	/**
	 * Retrieve the list of scenarios and display them in the Scenarios Table
	 * @param workingDir - Path of the working directory, expected to already have the "scenarios" nested dir!
	 * @param dataFilters - Set of data filters to apply (FOR THE TIME BEING, a Set seems to do the work!)
	 * @throws Exception
	 */
	public void showScenarios(String workingDir, List<Set<String>> datafilters) //throws Exception
    {
		//String key = "";
		observableList.clear();
		//lblSaveStatus.setText("");
		//lblSaveStatus.setVisible(false);
		if (callerType.equals("noncompliant")) {
			lblTitle.setText("Non-Compliant Diagrams");
			btnAdd.setDisable(false);
			btnCfg.setText("Configure");
		}else if (callerType.equals("evolved")) {
			lblTitle.setText("Evolved Diagrams");
			btnAdd.setDisable(true);
			btnCfg.setText("View");
		}
	
		try 
		{
			// NEW LOGIC: Retrieve all the scenario files found inside the "scenarios" default directory
			// OPD, Dec2, 2022: Adding a condition to only set the variable one! (as this method can now be called multiple times).
			//currentScenariosFile = workingDir + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXSCENARIOS;
			
			//System.out.println("ABOUT TO upload list of scenarios from DIR " + workingDir);
			int scenariosSeq = 0;
			File repo = new File (workingDir);
	        File[] fileList = repo.listFiles();
	        for (int i = 0 ; i < fileList.length ; i++) {
	        	
	        	if (shouldScenarioBeShown(callerType, fileList[i], datafilters))
				{
        			scenariosSeq++;
        			ScenarioRow row = new ScenarioRow(scenariosSeq+"", (fileList[i]).getName().trim(),tmpLatestMsgNumber);
        			observableList.add(row);
				}
	        	
	        	/*
	        	if (callerType.equals("noncompliant")) {
	        		//filter out the output files
	        		if (!fileList[i].getName().contains("_o[")) {
	        			scenariosSeq++;
	        			ScenarioRow row = new ScenarioRow(scenariosSeq+"", (fileList[i]).getName().trim());
	        			observableList.add(row);	
	        		}
	        	} else if (callerType.equals("evolved")) {
	        		//filter out the output files
	        		if (fileList[i].getName().contains("_o[")) {
	        			scenariosSeq++;
	        			ScenarioRow row = new ScenarioRow(scenariosSeq+"", (fileList[i]).getName().trim());
	        			observableList.add(row);
	        		}
	        	}*/
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//MainUtils.reportFatalError("Error when trying to load the Scenarios:\n"+"Thus, stopping the program execution!!!\n");
		}		
		
    }
	
	public static boolean shouldScenarioBeShown(String callerType, File scenarioFile, List<Set<String>> dataFilters)
	{
		boolean result = false;
		int dsMatches = 0;

    	if (callerType.equals("noncompliant") && !scenarioFile.getName().contains("_o[")) 
    	{
    		result = true;
    	}
    	else if (callerType.equals("evolved") && scenarioFile.getName().contains("_o[")) 
    	{
    		result = true;
    	}

		String filetext = ScenarioConfigController.readScenarioFromFile(scenarioFile.getAbsolutePath());
		//OPD Dec20,2022: For the time being, easy logic (# of lines) AND ignoring any edge-cases such as @start/enduml, comments, etc.
		//tmpLatestMsgNumber = (filetext.length()-filetext.replace("\n", "").length());
		tmpLatestMsgNumber = filetext.split("\n").length;
		
    	//Only IF the scenario is candidate to be shown AND there is a datafilter to be applied AND the DF has elements!, then the DF logic is evaluated!
    	if (result == true && dataFilters!=null && dataFilters.size()>0) 
    	{
    		//OPD Dec20,2022: Moving this line BEFORE the if, so that it is always executed (as it is used to populate the msgNum column!)
    		//String filetext = ScenarioConfigController.readScenarioFromFile(scenarioFile.getAbsolutePath());
    		
    		for (Set<String> nextDF:dataFilters)
    		{
    			for (String nextDFelement: nextDF)
    			{
    				//OPD Dec21, 2022: Another (ugly) patch to try supporting participant as PDI (as FenixEdu does) by removing "participant \"".
        			//OPD, Dec3,2022: I think the comparison should be lower case!
        			//if (filetext.toLowerCase().contains(nextDFelement.toLowerCase()))
        			if (StringUtils.containsIgnoreCase(filetext.replaceAll("(?i)participant \"", ""), nextDFelement) || 
							 StringUtils.containsIgnoreCase(filetext.replaceAll("(?i)participant \"", ""), nextDFelement.replace(".","\\n")))	//OPD Dec21,2022: Patch added to support \n in the participants.
        			{
        				//System.out.println("==>> OPD, the file ["+scenarioFile.getAbsolutePath()+"] contains the DF ["+nextDF+"]");
        				dsMatches++; // If at least one DF exists in the file, it should be counted (+ that is enough!).
        				break;
        			}
    			}
    			
    		}
    		
			//System.out.println("==>> OPD, If this line is shown, then the file ["+scenarioFile.getAbsolutePath()+"] has no DF and must not be shown!");
			result = (dsMatches==dataFilters.size())?true:false;
    	}
    	
		return result;
	}
	
	// Delete from View and system
	public void buttonDelete(ActionEvent actionEvent)
	{
		ObservableList<ScenarioRow> allRows, singleRow;
		allRows = tableViewScenario.getItems();
		singleRow = tableViewScenario.getSelectionModel().getSelectedItems();
		
		if (MainClientUtils.validateTableViewSelection(singleRow, "Please select a diagram to delete")) {	

			// ASK the user for confirmation
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete Confirmation Dialog");
			alert.setHeaderText("Confirmation to delete diagram");
			alert.setContentText("Are you sure you want to delete the diagram " + (singleRow.get(0)).getName() + "?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK)
			{			
				String filenameToDelete = currentScenariosFile + MainClient.DIR_OS_SEPARATOR + (singleRow.get(0)).getName();
				//System.out.println("About to delete file " + filenameToDelete);
				
				File fileToDelete = new File(filenameToDelete);
				// delete the file specified
				boolean resultDelete = fileToDelete.delete();
		
				// test if successfully deleted the file
				if (resultDelete) {
					MainUtils.reportInfo("Successfully deleted: " + filenameToDelete);
					singleRow.forEach(allRows::remove);
				} else {
					MainUtils.reportNonFatalError("Failed deleting " + filenameToDelete);
				}
			} else {
			    // ... user chose CANCEL or closed the dialog
				MainUtils.reportInfo("User chose to cancel delete");
			}
			
		}//end of [if (MainClientUtils.validateTableViewSelection(singleRow, "Please select a scenario to delete")) {]
	}
	
	
	/**
	 * Open the Window where Scenarios are created using PlantUML
	 * @param selectedScenarioURL
	 * @param mode - "new" or "existing" priv control
	 */
	private void loadScenarioConfigPlantUMLView(String selectedScenarioURL, String mode)
	{
		try {
			
		    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ScenarioConfigView.fxml"));
		    Parent root1 = (Parent) fxmlLoader.load();
		    Stage stage = new Stage();
		    //stage.initModality(Modality.APPLICATION_MODAL);
		    stage.initModality(Modality.NONE);
		    //stage.initStyle(StageStyle.UNDECORATED);
			
		    //stage.setTitle("Configure a Non-Compliant Scenario");
		    if (callerType.equals("noncompliant")){
		    	stage.setTitle("Visualize a Non-Compliant Diagram");
		    }
		    if (callerType.equals("evolved")) {
			    stage.setTitle("Visualize an Evolved Diagram");
			}

		    stage.setScene(new Scene(root1)); 
		    //System.out.println("About to show SCENARIO: " + selectedScenarioURL);
		    ScenarioConfigController controller = fxmlLoader.getController();
		    controller.showConfigScenarioPlantUMLWnd(selectedScenarioURL, mode, this);
		    stage.showAndWait();
		}
		catch (Exception e) {
			//e.printStackTrace();
			MainUtils.reportNonFatalError(e.getMessage());
		}
	}
	
	
	public void buttonAdd(ActionEvent actionEvent)
	{
		//openScenarioConfigWnd(new File(currentScenariosFile).getPath(), "new");
		loadScenarioConfigPlantUMLView(currentScenariosFile, "new");
		//After user saves the new scenario, refresh list of scenarios in view
		/*try {*/
			//OPD.- Dec2, 2022: Replacing showScenario 1-arg with the 2-arg version for two reasons: 
			// (1) The original 1-arg version now SETS the currentScenariosFile variable (which we only want to do when openning the original window!).
			// (2) The add logic should (I think!) respect any applied DF.
		
		//OPD.- Dec2, 2022: The list of files ONLY refreshed AFTER closing the add scenario windows!
		//The call to this logic (refactored inside new updateScenariosAfterCreate method) has been moved to ScenarioConfigController.buttonSaveChanges
		//System.out.println("===> About to update the list of files!!!!");
		//showScenarios(currentScenariosFile, dfSet);
						
		/*} catch (Exception e) {
			// TODO Auto-generated catch block
			MainUtils.reportNonFatalError("ERROR while refreshing list of scenarios after adding new");
			e.printStackTrace();
		}*/
	}
	
	public void updateScenariosAfterCreate()
	{
		//System.out.println("===> About to update the list of files!!!!");
		showScenarios(currentScenariosFile, dfSetList);
	}
	
	public void buttonConfigure(ActionEvent actionEvent)
	{
		ObservableList<ScenarioRow> singleRow;
		singleRow = tableViewScenario.getSelectionModel().getSelectedItems();
		//if (singleRow != null && singleRow.get(0)!=null) {
		if (MainClientUtils.validateTableViewSelection(singleRow, "Please select a diagram to configure/view")) {	
			String selectedScenarioName = singleRow.get(0).getName();
			String selectedScenarioURL = currentScenariosFile + MainClient.DIR_OS_SEPARATOR + selectedScenarioName;
			//System.out.println("Show config window for " + currentScenariosFile);
			loadScenarioConfigPlantUMLView(selectedScenarioURL, "existing");
		}
	}
	


	public String getCallerType() {
		return callerType;
	}

	public void setCallerType(String callerType) {
		this.callerType = callerType;
	}
	
	
}
