package edu.ucd.forcops.main.ui.ruleset;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.ucd.forcops.main.AdviceType;
import edu.ucd.forcops.main.InjectionType;
import edu.ucd.forcops.main.KnowledgeBase;
import edu.ucd.forcops.main.MainUtils;
import edu.ucd.forcops.main.PrivatePattern;
import edu.ucd.forcops.main.PrivateSubPattern;
import edu.ucd.forcops.main.ui.MainClient;
import edu.ucd.forcops.main.ui.MainClientUtils;
import edu.ucd.forcops.main.ui.dictionary.DataDictionaryRow;
import edu.ucd.forcops.main.ui.scenarios.ScenarioConfigController;
import edu.ucd.forcops.main.ui.scenarios.ScenarioRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class CatalogDisplayController implements Initializable{
	
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	@FXML
	private TableView<PrivControlRow> tableViewControls;
	
	@FXML
	private TableColumn<PrivControlRow,String> colId;
	//@FXML
	//private TableColumn<PrivControlRow,CheckBox> colSelection;
	@FXML
	private TableColumn<PrivControlRow,String> colName;
	@FXML
	private Label lblSaveStatus;
	
	// Name of the dictionary file that triggered this window
	//private String currentDictionary = null;
	private String currentRulesetFile = null, currentTokensFile = null;
	
		
	// Name of the catalog file that triggered this window
	//private String currentCatalog = null;
	private static List<PrivatePattern> patterns;
	private static String TOKENS_SEP = ",";
	
		
	ObservableList<PrivControlRow> observableListPrivCtrls = FXCollections.observableArrayList();
	ObservableList<TokenRow> observableListTokens = FXCollections.observableArrayList();
	
	ArrayList<String> privControls = new ArrayList<String>();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) 
	{
		colId.setCellValueFactory(new PropertyValueFactory<PrivControlRow,String>("id"));
		//colSelection.setCellValueFactory(new PropertyValueFactory<PrivControlRow,CheckBox>("selection"));
		colName.setCellValueFactory(new PropertyValueFactory<PrivControlRow,String>("name"));
		
		tableViewControls.setItems(observableListPrivCtrls); 
		tableViewControls.setEditable(false);
	}
	
	
	/**
	 * Retrieve the priv control and display them in the Priv Controls Table
	 * @param workingDir - Path of the working directory (where the tokens and ruleset files are located)
	 * @throws Exception
	 */
    public void showRuleset(String workingDir) throws Exception  
    {
    	//Build filenames for data dictionary proc opers and data items
    	//currentRulesetFile = workingDir + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXRULESET;
    	currentTokensFile = workingDir + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXTOKENS + ".txt";
    	
    	//Read dictionary and show in table 
    	String token = "";
		observableListPrivCtrls.clear();
		observableListTokens.clear();
		lblSaveStatus.setText("");
		lblSaveStatus.setVisible(false);
		
		// READ PRIVACY CONTROLS -- new logic by using separate file for each control --
		// Retrieve all the control files found inside the "ruleset" default directory
		currentRulesetFile = workingDir + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXRULESET;
		//System.out.println("Uploading list of controls found in the directory " + currentRulesetFile);
		File repo = new File (currentRulesetFile);
        File[] fileList = repo.listFiles();
        for (int i = 0 ; i < fileList.length ; i++) {
        	PrivControlRow row = new PrivControlRow((i+1)+"", (fileList[i]).getName().trim());
			observableListPrivCtrls.add(row);
		}
        tableViewControls.setItems(observableListPrivCtrls);
    }
				
	
	public void buttonDelete(ActionEvent actionEvent)
	{
		ObservableList<PrivControlRow> allRows, singleRow;
		allRows = tableViewControls.getItems();
		singleRow = tableViewControls.getSelectionModel().getSelectedItems();
		
		if (MainClientUtils.validateTableViewSelection(singleRow, "Please select a control to delete")) {
			// ASK the user for confirmation
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete Confirmation Dialog");
			alert.setHeaderText("Confirmation to delete Privacy Control");
			alert.setContentText("Are you sure you want to delete the control " + (singleRow.get(0)).getName() + "?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK)
			{			
				String filenameToDelete = currentRulesetFile + MainClient.DIR_OS_SEPARATOR + (singleRow.get(0)).getName();
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
		}//end of [if (MainClientUtils.validateTableViewSelection(singleRow, "Please select a control to delete")) {]
				
	}
	
	// Open the Window where PRiv Ctrl are created (ruleset template)
	/***
	 * Open the configuration window for a privacy control 
	 * @param selectedControl - if existing, then the full path + filename; if new, then the full path only
	 * @param mode - new or existing
	 */
	private void loadPrivCtrlConfigView(String selectedControl, String mode)
	{
		try {
			
		    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PrivControlConfigView.fxml"));
		    Parent root1 = (Parent) fxmlLoader.load();
		    Stage stage = new Stage();
		    //stage.initModality(Modality.APPLICATION_MODAL);
		    stage.initModality(Modality.NONE);
		    //stage.initStyle(StageStyle.UNDECORATED);
		    stage.setTitle("Configure a Privacy Control");
		    stage.setScene(new Scene(root1)); 
		    //System.out.println("About to show CONTROL: " + selectedControl + " in mode " + mode);
		    PrivacyControlConfigController controller = fxmlLoader.getController();
		    controller.showPrivCtrlConfigWnd(selectedControl, mode);
		    stage.showAndWait();
		}
		catch (Exception e) {
			//e.printStackTrace();
			MainUtils.reportNonFatalError(e.getMessage());
		}
	}
	
	public void buttonAdd(ActionEvent actionEvent)
	{	
		loadPrivCtrlConfigView(currentRulesetFile, "new");
		//After user saves the new control, refresh list of controls in view
		try {
			//System.out.println("** Refreshing list of priv ctrls in view after adding NEW");
			showRuleset(new File(currentRulesetFile).getParent());
		} catch (Exception e) {
			MainUtils.reportNonFatalError("ERROR while refreshing list of scenarios after adding new");
			//e.printStackTrace();
		}
	}
	
	
	public void buttonConfigure(ActionEvent actionEvent)
	{
		ObservableList<PrivControlRow> singleRow;
		singleRow = tableViewControls.getSelectionModel().getSelectedItems();
		
		if (MainClientUtils.validateTableViewSelection(singleRow, "Please select a control to configure/view")) {
			String selectedControlName = singleRow.get(0).getName();
			String selectedControlURL = currentRulesetFile + MainClient.DIR_OS_SEPARATOR + selectedControlName;
			//System.out.println("Show config window for " + selectedControlName);
			loadPrivCtrlConfigView(selectedControlURL, "existing");			
		}

	}
	
}
