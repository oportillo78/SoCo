package edu.ucd.forcops.main.ui.ruleset;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.ucd.forcops.main.Advice;
import edu.ucd.forcops.main.AdviceType;
import edu.ucd.forcops.main.Dictionary;
import edu.ucd.forcops.main.InjectionType;
import edu.ucd.forcops.main.KnowledgeBase;
import edu.ucd.forcops.main.MainUtils;
import edu.ucd.forcops.main.Pointcut;
import edu.ucd.forcops.main.PrivatePattern;
import edu.ucd.forcops.main.PrivateSubPattern;
import edu.ucd.forcops.main.ui.MainClient;
import edu.ucd.forcops.main.ui.MainClientUtils;
import edu.ucd.forcops.main.ui.TextAreaTableCell;
import edu.ucd.forcops.main.ui.dictionary.DataDictionaryRow;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;

public class PrivacyControlConfigController implements Initializable{
	
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	@FXML
	private Label lblSaveStatus;
	@FXML
	private TextField txtfieldCtrlName;
	@FXML
	private TextField txtfieldCtrlDescription;
	@FXML
	private TextField txtfieldCtrlNewActors;
	@FXML
	private TextField txtfieldCtrlSortingActors;
	
	//Pointcut
	@FXML
	private TableView<PointcutRow> tableViewPointcut;
	@FXML
	private TableColumn<PointcutRow,String> colPoincutNumMsgs;
	@FXML
	private TableColumn<PointcutRow,String> colPoincutProcOpers;
	@FXML
	private TableColumn<PointcutRow,String> colPoincutActors;
	@FXML
	private TableColumn<PointcutRow,String> colPoincutDataTypes;
	
	//Advice
	@FXML
	private TableView<AdviceRow> tableViewAdvice;
	@FXML
	private TableColumn<AdviceRow,String> colAdviceType;
	@FXML
	private TableColumn<AdviceRow,String> colAdviceBehavior;
	
	//Tokens
	@FXML
	private TableView<TokenRow> tableViewTokens;
	//@FXML
	//private TableColumn<PrivControlRow,String> colName;
	@FXML
	private TableColumn<TokenRow,String> colToken;
	@FXML
	private TableColumn<TokenRow,String> colValue;
	@FXML
	private TextField txtFieldToken;
	@FXML
	private TextField txtFieldValue;
	
	private String typeWnd = null;
	private String currentControlURL = null;
	private String currentTokensFile = null;
	private static String TOKENS_SEP = ",";
	private static Map<String,String> allTokensMap = new HashMap<String, String>();
	private static List<PrivatePattern> patterns;
	//ArrayList<String> privControls = new ArrayList<String>();
	
	//ObservableList<PrivControlRow> observableList = FXCollections.observableArrayList();
	ObservableList<PointcutRow> observableListPointcut = FXCollections.observableArrayList();
	
	//ObservableList<PrivControlRow> observableList = FXCollections.observableArrayList();
	ObservableList<AdviceRow> observableListAdvice = FXCollections.observableArrayList();
	
	ObservableList<TokenRow> observableListTokens = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		//Pointcut
		//---------------------------------------------------
		tableViewPointcut.setItems(observableListPointcut);
		tableViewPointcut.setEditable(true);
		
		colPoincutNumMsgs.setCellValueFactory(new PropertyValueFactory<PointcutRow,String>("NumMsgs"));
		colPoincutProcOpers.setCellValueFactory(new PropertyValueFactory<PointcutRow,String>("ProcOperations"));
		colPoincutActors.setCellValueFactory(new PropertyValueFactory<PointcutRow,String>("Actors"));
		colPoincutDataTypes.setCellValueFactory(new PropertyValueFactory<PointcutRow,String>("DataTypes"));
		
		colPoincutNumMsgs.setCellFactory(TextFieldTableCell.forTableColumn());
		colPoincutProcOpers.setCellFactory(TextFieldTableCell.forTableColumn());
		colPoincutActors.setCellFactory(TextFieldTableCell.forTableColumn());
		colPoincutDataTypes.setCellFactory(TextFieldTableCell.forTableColumn());
		
		colPoincutNumMsgs.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setNumMsgs(e.getNewValue()));
		colPoincutProcOpers.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setProcOperations(e.getNewValue()));
		colPoincutActors.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setActors(e.getNewValue()));
		colPoincutDataTypes.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setDataTypes(e.getNewValue()));
		
		//Advice
		//---------------------------------------------------
		tableViewAdvice.setItems(observableListAdvice);
		tableViewAdvice.setEditable(true);
		
		colAdviceType.setCellValueFactory(new PropertyValueFactory<AdviceRow,String>("Type"));
		colAdviceBehavior.setCellValueFactory(new PropertyValueFactory<AdviceRow,String>("Behavior"));
		
		colAdviceType.setCellFactory(TextFieldTableCell.forTableColumn());
		colAdviceBehavior.setCellFactory(TextAreaTableCell.forTableColumn()); // CUSTOMIZED TEXT AREA add StringConverter if neccessary
		
		colAdviceType.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setType(e.getNewValue()));
		colAdviceBehavior.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setBehavior(e.getNewValue()));
		
		//Tokens
		//---------------------------------------------------
		tableViewTokens.setItems(observableListTokens);
		tableViewTokens.setEditable(true);
		
		colToken.setCellValueFactory(new PropertyValueFactory<TokenRow,String>("token"));
		colValue.setCellValueFactory(new PropertyValueFactory<TokenRow,String>("value"));

		colToken.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue.setCellFactory(TextFieldTableCell.forTableColumn());
		
		colToken.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setToken(e.getNewValue()));
		colValue.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setValue(e.getNewValue()));
	}
	
	// FIRST method to call when we want to open the Config Window for an Scenario (either New or Existing)
	public void showPrivCtrlConfigWnd(String selectedControlURL, String type) throws Exception
    {
		//Read the plantuml scenario description and upload to plant uml?
		lblSaveStatus.setText("");
		lblSaveStatus.setVisible(false);
		
		//Save in what case was this window opened (new or existing)
		typeWnd = type;
		 
		//save filename or path for current scenario
		currentControlURL = selectedControlURL; 
		// current control or directory will be inside ruleset, so get parent dir for tokens
		
		
		// Open Config Window for NEW scenario
		if(type.equals("new")) {
			//nothing to do when new (C:\Users\vayal\scenarioUniXYZ\ruleset)
			currentTokensFile = new File(selectedControlURL).getParent() + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXTOKENS + ".txt";
		}
		// Open Wnd for EXISTING scenario to modify it
		else {
			 // Fill out the data in the view with the control data
			//System.out.println("About to fill out the view with the selected priv control data");
			//we need the working directory C:\Users\vayal\scenarioUniXYZ\ruleset\dbaudit.txt
			currentTokensFile = new File(new File(selectedControlURL).getParent()).getParent()  + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXTOKENS + ".txt";
			readTokensFromFile();
			readControlFromFile(selectedControlURL);
			fillInViewWithPattern(); //including tokens
		}
    }
	
	// Read the tokens artifact file, upload them in a hashmap
	private void readTokensFromFile() throws Exception
	{
		String key = "";
		allTokensMap = new HashMap<String, String>();
		try 
		{
			List<String> inputLines = FileUtils.readLines(new File(currentTokensFile));
			for (int x=0; x<inputLines.size(); x++)
			{
				String[] nextLine = inputLines.get(x).trim().split(TOKENS_SEP, 2); //READ first "," only
				key = nextLine[0];
				String values = nextLine[1];
				allTokensMap.put(key, values);
			}
		}
		catch(Exception e)
		{
			MainUtils.reportFatalError("Error when trying to load Token file, error description: ["+e.getMessage()+"] ...\n"
					+"Thus, stopping the program execution!!!\n");
		}		
	}
	
	private void readControlFromFile(String currentRulesetFile) throws Exception
	 {	
		// READ PRIVACY CONTROLS
		// Code taken from edu.ucd.forcops.main.KnowledgeBase loadPrivacyPatterns(String rulesetFilename)
		patterns = new ArrayList<PrivatePattern>();

		//logger.info("**********************************************");
		logger.info("About to start loading the privacy patterns in view!!!");
		//logger.info("**********************************************");
		
		try 
		{
			List<String> inputLines = FileUtils.readLines(new File(currentRulesetFile));
			
			// IMPORTANT: Currently not supporting tokens!
			// Also, assuming that a blank line indicates the end of a pattern!!!
			// (internally, it does not matter the order, EXCEPT that the pointcut 
			// MUST come BEFORE its advice!)
			String privPatName = null, description = null, newActors[] = null, sortedActors[] = null;
			List<PrivateSubPattern> subpats = new ArrayList<PrivateSubPattern>();
			String[] spActors = null, spOps = null, spPdata = null, spTempTokens= {};
			List<String> spTempAdvice = null;
			int spOpsPerActor = -1;
			InjectionType spInjType = null;
			AdviceType spAdvType = null;
			
			for (int x=0; x<inputLines.size(); x++)
			{
				String nextLine = inputLines.get(x).trim();
				
				if (nextLine.startsWith("#")) continue;
				
				// SPLIT by "[" to get the value of the 1st att (all atts are enclosed in [] except advice instructions)
				//[A pointcut composed of , 1] message(s) involving the , modify] operations, the , *, $db] actors, and the , studentRecord] data types.]
				String[] nLineTokens= nextLine.split("\\[");
				//"A pointcut composed of"
				String key = nLineTokens[0].trim();
				//"1] message(s) involving the [modify] operations, the [*, $db] actors, and the [studentRecord] data types."
				String value = (nextLine.length()>0)?nextLine.substring(nLineTokens[0].length()+1):"";
							
				//logger.debug("Next key|value is: ["+key+"|"+value+"]");
				
				switch(key)
				{
					case "The control":
						privPatName = value.replace("]", "");
						subpats.clear();
						break;
						
					case "With description":
						description = value.replace("]", "");
						break;
						
					case "With new actors":
						newActors = value.replace("]", "").split(",");
						if (newActors[0].equals("")) //optional
							newActors = new String[0];
						else 
							KnowledgeBase.trimStringArray(newActors);
						break;
						
					case "With sorting":
						sortedActors = value.replace("]", "").split(",");
						if (sortedActors[0].equals("")) 
							sortedActors = new String[0];
						else 
							KnowledgeBase.trimStringArray(sortedActors);
						break;
						
					case "A pointcut of type":
						String[] pcutTokens = value.split("\\]");
						spInjType = InjectionType.valueOf(pcutTokens[0].toUpperCase());
						spOpsPerActor = Integer.valueOf(pcutTokens[1].split("\\[")[1].trim());
						
						spOps = pcutTokens[2].split("\\[")[1].split(",");
						KnowledgeBase.trimStringArray(spOps);
						spActors = pcutTokens[3].split("\\[")[1].split(",");
						KnowledgeBase.trimStringArray(spActors);
						spPdata = pcutTokens[4].split("\\[")[1].split(",");
						KnowledgeBase.trimStringArray(spPdata);
						break;

					//OPD.- Feb19,2019: Adding logic to support the case of NOT explicitly setting the injection type.
					//					If that happens, a NOW injection type is assummed!
						// "A pointcut composed of|1] message(s) involving the [modify] operations, the [*, $db] actors, and the [studentRecord] data types.]"
					case "A pointcut composed of":
						spInjType = InjectionType.NOW;
						//"1,  message(s) involving the [modify,  operations, the [*, $db,  actors, and the [studentRecord,  data types."
						pcutTokens = value.split("\\]");
						//num of msgs per actor //1
						spOpsPerActor = Integer.valueOf(pcutTokens[0]);
						
						//OPD.- Feb19,2019: For simplicity, copy/pasting the spOps/spActors/spPdata logic of the above case.
						//					(with the obvious -1 index). On hold trying to refactor to have a single logic instead of two!
						//"modify"
						spOps = pcutTokens[1].split("\\[")[1].split(",");
						KnowledgeBase.trimStringArray(spOps);
						//"*, $db"
						spActors = pcutTokens[2].split("\\[")[1].split(",");
						KnowledgeBase.trimStringArray(spActors);
						//"studentRecord"
						spPdata = pcutTokens[3].split("\\[")[1].split(",");
						KnowledgeBase.trimStringArray(spPdata);
						break;
						
					case "An advice of type":
						//"ADD_AFTER,  with the instructions:
						String[] advTokens = value.split("\\]");
						//"ADD_AFTER"
						spAdvType = AdviceType.valueOf(advTokens[0].toUpperCase());
						
						 spTempAdvice = new ArrayList<String>();
						 do
						 {
							 //Line where the advice instructions starts (one after "an advice.." term is found
							 x++;
							 //OPD.- Feb9, 2019: Adding logic to skip those lines starting with #
							 if (!inputLines.get(x).startsWith("#"))
							 {
								 spTempAdvice.add(inputLines.get(x).replaceFirst("\t", ""));
							 }
							 //Save all advice instructions in spTempAdvice starting from "An advice of type" term is found 
							 //till end of instructions which is when the next pointcut/advice pair starts
						 } while (x+1<inputLines.size() && 
								 (!inputLines.get(x+1).trim().equals("") && !inputLines.get(x+1).trim().contains("A pointcut ")));

						subpats.add(new PrivateSubPattern(spActors, spOps, spOpsPerActor, 
								spPdata, spInjType, spAdvType, spTempAdvice.toArray(new String[spTempAdvice.size()]), 
								spTempTokens, false));
						break;
						
					case "":
						if (privPatName != null)//To support blank spaces at the beginning
						{
							patterns.add(new PrivatePattern(privPatName, description, newActors,
								subpats.toArray(new PrivateSubPattern[subpats.size()]), sortedActors));
							logger.info("New pattern added: "+patterns.get(patterns.size()-1).getName());
							//privControls.add(privPatName); //ADD TO PRIV CTRL LIST FOR TABLE VIEW
							privPatName = null;
						}
						while (x+1<inputLines.size() && inputLines.get(x+1).trim().equals(""))
						{
							x++;
						}
						break;
							
					default:
						logger.error("Unsupported Operation! ["+key+"]");
						break;
			
				}// end of "switch(key)"
			}// for (int x=0; x<inputLines.size(); x++)
			
			//Quick patch to avoid not adding the last pattern of the file in the case where there is no empty line at the end!
			//IMPORTANT: Assumming that it is enough to check the name!
			if (privPatName != null)
			{
				//System.out.println("About to add new pattern");
				patterns.add(new PrivatePattern(privPatName, description, newActors,
						subpats.toArray(new PrivateSubPattern[subpats.size()]), sortedActors));
				//logger.info("New pattern added: "+patterns.get(patterns.size()-1).getName());
				//VAR FILL the priv ctrl names
				//privControls.add(privPatName); //ADD TO PRIV CTRL LIST FOR TABLE VIEW
			}
			
			
		}//end of try
		catch(Exception e)
		{
			e.printStackTrace();
			MainUtils.reportFatalError("Error when trying to load the Sec/Priv Ruleset: \n ["+e.getMessage()+"] ...\n"+
			"Thus, stopping the program execution!!!\n");
		}
		
		logger.info("**********************************************");
		logger.info("The loading of ["+patterns.size()+"] privacy pattern"+(patterns.size()>1?"s":"")+" finished!!!");
		logger.info("**********************************************");
		
	 }
	
	// patterns list at this point is filled with the priv control
	private void fillInViewWithPattern() 
	{
		if(patterns != null && patterns.size() !=0) {
			//Extract the 1st pattern (expected that only one will be inside)
			PrivatePattern privCtrl = patterns.get(0);
			//System.out.println("==== EXTRACTED PRIV CTRL, printing... ======");
			//System.out.println(privCtrl.toString());
			
			// upload general fields
			txtfieldCtrlName.setText(privCtrl.getName());
			txtfieldCtrlDescription.setText(privCtrl.getDescription());
			txtfieldCtrlNewActors.setText(ArraysToStringNoBrackets(privCtrl.getTemplateActors()));
			txtfieldCtrlSortingActors.setText(ArraysToStringNoBrackets(privCtrl.getSortedActors()));
			
			//iterate pointcut and advice
			PrivateSubPattern[] subp = privCtrl.getSubpatterns();
			Pointcut pcut = null;
			Advice adv = null;
			// To find tokens 
			Set<String> tokensToRetrieve = new HashSet<String>(); //declare outside pcut/adv pairs to avoid duplicate tokens in pairs
			String tokenRegex = "\\$[a-zA-Z0-9]+"; //alphanumeric word starting with $
			Pattern pattern = Pattern.compile(tokenRegex);
			
			for (int i=0; i<subp.length; i++) 
			{
				pcut = subp[i].getPcut();
				
				PointcutRow rowP = new PointcutRow(pcut.getNumReqOps() + "", ArraysToStringNoBrackets(pcut.getOps_original()), 
						ArraysToStringNoBrackets(pcut.getActors_original()), ArraysToStringNoBrackets(pcut.getPdi_original()));
				observableListPointcut.add(rowP);
				tableViewPointcut.setItems(observableListPointcut);

				adv = subp[i].getAdvice();
				AdviceRow rowA = new AdviceRow(adv.getaType().getAdviceType(), String.join("\n", adv.getTemplateAdvice()));
				observableListAdvice.add(rowA);
				tableViewAdvice.setItems(observableListAdvice);
				
				//Upload the TOKENS corresponding to the priv ctrl
				//Tokens can be found in actors of pointcut and instructions of advice
				//Obtain the tokens available in the priv pattern
				String actors[] = pcut.getActors_original();
				for (int j=0; j<actors.length; j++) {
					// if it is a token, retrieve its value from file/map
					if (actors[j].startsWith("$")) {
						tokensToRetrieve.add(actors[j]);
					}
				}
				// Obtain the tokens found in instructions
				String instructions[] = adv.getTemplateAdvice();
				for (int j=0; j<instructions.length; j++) {
					// if it is a token, retrieve its value from file/map
					if (instructions[j].contains("$")) {
						Matcher matcher = pattern.matcher(instructions[j]);
						while (matcher.find()) { 
							String foundToken = matcher.group();
							// ignore reserved tokens
							if (!Advice.reservedTokens.contains(foundToken))
								tokensToRetrieve.add(matcher.group()); 
				        } 
					}
				}
			}// end iterating pcut/advice pairs
			// Upload the values for tokens that are in the priv ctrl only
			 Iterator<String> it = tokensToRetrieve.iterator();
		     while(it.hasNext()){
		    	String keyToRetrieve = it.next();
		    	String values = allTokensMap.get(keyToRetrieve);
		    	TokenRow row = new TokenRow(keyToRetrieve, values);
		    	observableListTokens.add(row);
				tableViewTokens.setItems(observableListTokens);
		     }
		}
		
	}
	
	private String ArraysToStringNoBrackets(String[] toConvert){
		String temp = Arrays.toString(toConvert);
		return temp.substring(1, temp.toString().length()-1);
	}
	 	
	public void buttonAdd(ActionEvent actionEvent)
	{
		PointcutRow row = new PointcutRow("","","", "");
		tableViewPointcut.getItems().add(row);
		AdviceRow advicerow = new AdviceRow("","");
		tableViewAdvice.getItems().add(advicerow);
	}
	
	public void buttonDelete(ActionEvent actionEvent)
	{
		ObservableList<PointcutRow> allPointcutRows, selectedPointcutRows;
		ObservableList<AdviceRow> allAdviceRows, selectedAdviceRows;
		ObservableList<Integer> selectedPointcutRowsIndices;
	    
		//delete rows from Pointcut table
		allPointcutRows = tableViewPointcut.getItems();
		selectedPointcutRows = tableViewPointcut.getSelectionModel().getSelectedItems();
		selectedPointcutRowsIndices = tableViewPointcut.getSelectionModel().getSelectedIndices(); //save indexes to delete the same in advice
		
		// Delete the same row indices from Advice table 
		
		// Get the advice rows associated with the selected pointcut to be deleted
		selectedAdviceRows = FXCollections.observableArrayList();
		for (int i=0; i<selectedPointcutRowsIndices.size(); i++) {
			AdviceRow adviceRow = tableViewAdvice.getItems().get(selectedPointcutRowsIndices.get(i));
			selectedAdviceRows.add(adviceRow);
		}
		allAdviceRows = tableViewAdvice.getItems();
		
		// LAST STEP is REMOVE (otherwise the model of Pointcut table changes and advice cannot delete index)
		selectedAdviceRows.forEach(allAdviceRows::remove);
		selectedPointcutRows.forEach(allPointcutRows::remove);
	}
	
	// BUTTON ACTION save changes in config window (use the same file and if it is new, Save As)
	 public void buttonSaveChanges(ActionEvent actionEvent)
	{
		 //check that all mandatory fields are defined (at least one pair of pointcut and advice)
		 if (validateMandatoryParams()) 
		 {
			 // ------ SAVE THE KEYS and KEY/VALUE OF ALL THE TOKENS IN TOKENTABLE (TO VALIDATE LATER)
			 Set<String> tokenKeysInTableSet = new HashSet<String>();
			 //allTokensMap.clear();
			 Map<String,String> tokensInCtrl = new HashMap<String, String>();
			 for (TokenRow row : tableViewTokens.getItems()) {
				 tokenKeysInTableSet.add(row.getToken());
				 tokensInCtrl.put(row.getToken(), row.getValue());
			 }
			
			 // --------------- GET PRIV CTRL DATA FROM UI ---------------
			 Set<String> undefinedEmptyTokens = new HashSet<String>();
			 String privCtrlName = txtfieldCtrlName.getText();
			 String description = txtfieldCtrlDescription.getText();
			 String sortedActors[] = txtfieldCtrlSortingActors.getText().split(",");
			 String newActors[] = txtfieldCtrlNewActors.getText().split(",");
			 
			//retrieve all pointcut and advice pais from tables
			 ObservableList<PointcutRow> allPointcutRows = tableViewPointcut.getItems();
			 ObservableList<AdviceRow> allAdviceRows = tableViewAdvice.getItems();
			 //iterate pointcut table (advice should have same length, as they are pairs)
			 PrivateSubPattern[] pointcutAdvPairs = new PrivateSubPattern[allPointcutRows.size()];
			 for (int i=0; i<allPointcutRows.size(); i++) 
			 {
				 PointcutRow pointcutRow = allPointcutRows.get(i);
				 AdviceRow adviceRow = allAdviceRows.get(i);
				 
				 String actors[] = pointcutRow.getActors().split(",");
				 String ops[] = pointcutRow.getProcOperations().split(",");
				 int numMsgs = Integer.parseInt(pointcutRow.getNumMsgs());
				 String dataTypes[] = pointcutRow.getDataTypes().split(",");
				 Pointcut pcut = null;
				 try {
					pcut = new Pointcut(actors, ops, numMsgs, dataTypes, null, false);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				 AdviceType atype = AdviceType.valueOf(adviceRow.getType().toUpperCase());	  
				 String[] advInstructions = adviceRow.getBehavior().split("\n");
				 Advice adv = new Advice(atype, advInstructions, null);
				
				 //Save pointcut/advice pair information in privateSubPattern
				 PrivateSubPattern pointcutAdvPair = null;
				try {
					pointcutAdvPair = new PrivateSubPattern(
							 actors, ops, numMsgs, dataTypes, null, 				// Pointcut elements.
							 atype, advInstructions, null,							// Advice elements.
							 false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//save all pointcut/advice pairs in array
				 pointcutAdvPairs[i] = pointcutAdvPair;
				 
				 // ---- VALIDATE TOKENS
				// check if the tokens declared in poincut actors are also defined in tokens table
				 for (int j=0; j<actors.length; j++) {
					 // if it is a token
					 //System.out.println("checking TOKEN:" + actors[j] + ".");
					 String token = MainUtils.trimBothSides(actors[j]);
					if (token.startsWith("$")) {
						// check if the token in actors is declared/present in table 
						if (!tokenKeysInTableSet.contains(token)) {
							undefinedEmptyTokens.add(token);
						}
						// if token declared is present in table but undefined/empty
						else if (tokensInCtrl.get(token) == null ||  
								tokensInCtrl.get(token).equals("") || 
								tokensInCtrl.get(token).equals("null")) {
							undefinedEmptyTokens.add(token);
						 }
					}
				}
			 } //end iterating all pointcut in table
			 PrivatePattern privCtrl = new PrivatePattern(privCtrlName, description, 
					 newActors, pointcutAdvPairs, sortedActors);
			 
			 //System.out.println("PRIV CTRL TO SAVE: \n\n" + privCtrl.toString());
			 
			//-------------  VALIDATE TOKENS VALUES -------------------
			 //check if there are undefined or empty tokens
			 if (undefinedEmptyTokens.size()>0) {
				 Alert alert = new Alert(AlertType.ERROR);
				 alert.initOwner(MainClient.primaryStage);
				 alert.setTitle("Invalid settings");
				 alert.setHeaderText("Empty mandatory parameter");
				 alert.setContentText("Please define a value for all tokens declared in the control");
				 alert.showAndWait();
				 // do not continue
				 return;
			 }
			 
			 //------------ SHOW SAVING DIALOG -------------------
				//Check if it is NEW
				 String fileToSave = null;
				 if (typeWnd.equals("new")) {
					 // Open SAVE filechooser
		            FileChooser fileChooser = new FileChooser();
		            fileChooser.setTitle("Save Privacy Control Configuration");
		            fileChooser.setInitialDirectory(new File(currentControlURL));	
		            File file = fileChooser.showSaveDialog(MainClient.primaryStage);
		            if (file != null) {
		            	fileToSave = file.getAbsolutePath();
		            }    
				 }
				 else {
					 fileToSave = currentControlURL;
				 }
				 //System.out.println("FILE TO SAVE: " + fileToSave);
			 
			// ------------------ SAVE PRIV CTRL TO FILE --------------------
			 if (fileToSave != null)
			 {
				//Write POs to file (clear file and create a new one)
				try {
					File file = new File(fileToSave);
					FileUtils.write(file, privCtrl.toString(), false);
					//System.out.println("Priv Ctrl file saved " + fileToSave);
					//System.out.println();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			 
			 // ------------------ SAVE TOKENS DATA TO FILE --------------------
			 // Need to update the values allTokensMap in case
			 // some of the tokens were updated (can't write allTokensMap directly because there might
			 // be the case that a priv ctrl does not have all tokens in the file)
			 ArrayList<String> tokensInCtrlKeys = new ArrayList<String>(tokensInCtrl.keySet());
			 ArrayList<String> tokensInCtrlValues = new ArrayList<String>(tokensInCtrl.values());
			 // iterate tokens in token table view
			 for(int i=0; i<tokensInCtrlKeys.size(); i++)  {
				 //override with new value (even f there were no changes to the value, we override, it is easier)
				 allTokensMap.put(tokensInCtrlKeys.get(i), tokensInCtrlValues.get(i));
			 }

			 //AT this point allTokensMap is updated with the possible changes made in this priv ctrl cfg window
			 try {
				 	ArrayList<String> keyList = new ArrayList<String>(allTokensMap.keySet());
				 	ArrayList<String> valueList = new ArrayList<String>(allTokensMap.values());
				 	ArrayList<String> lines = new ArrayList<String>();
				 	for(int i=0; i<keyList.size(); i++) {
				 		lines.add(keyList.get(i) + TOKENS_SEP + valueList.get(i));
				 	}
				 	
					File file = new File(currentTokensFile);
					FileUtils.writeLines(file, lines, false);
					
					//System.out.println("Tokens file saved " + fileToSave);
					//System.out.println();
					
					// Show saved until both sets in data dictionary have been saved
					MainClientUtils.showLabelTemporarily(lblSaveStatus, "File saved", 2);
					/*
					lblSaveStatus.setText("File saved");
					lblSaveStatus.setTextFill(Color.RED);
					lblSaveStatus.setVisible(true);
					PauseTransition visiblePause = new PauseTransition(
					        Duration.seconds(2)
					);
					visiblePause.setOnFinished(
					        event -> lblSaveStatus.setVisible(false)
					);
					visiblePause.play();*/
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }
	}
	 
	 //Validate mandatory parameters
	 private boolean validateMandatoryParams() 
	 {
		 
		 String privCtrlName = txtfieldCtrlName.getText();
		 ObservableList<PointcutRow> allPointcutRows = tableViewPointcut.getItems();
		 ObservableList<AdviceRow> allAdviceRows = tableViewAdvice.getItems();
		 
		 boolean emptyMandatoryAttInTable = false;
		 //Check for empty attributes in tables
		 for (int i=0; i<allPointcutRows.size(); i++) 
		 {
			 PointcutRow pcut = allPointcutRows.get(i);
			 AdviceRow adv = allAdviceRows.get(i);
			 if (pcut.getNumMsgs() == null || pcut.getNumMsgs().equals("") || pcut.getNumMsgs().equals("null") ||
				 pcut.getProcOperations() == null || pcut.getProcOperations().equals("") || pcut.getProcOperations().equals("null") ||
				 pcut.getActors() == null || pcut.getActors().equals("") || pcut.getActors().equals("null") ||
				 pcut.getDataTypes() == null || pcut.getDataTypes().equals("") || pcut.getDataTypes().equals("null") ||
				 adv.getType() == null || adv.getType().equals("") || adv.getType().equals("null") ||
				 adv.getBehavior() == null || adv.getBehavior().equals("") || adv.getBehavior().equals("null") 
				 )
			 {
				 emptyMandatoryAttInTable = true;
			 }
		 }
		 
		 
		 if(privCtrlName.length() ==0 || allPointcutRows.size() == 0 || allAdviceRows.size() == 0 || emptyMandatoryAttInTable) 
		 {
			 Alert alert = new Alert(AlertType.ERROR);
			 alert.initOwner(MainClient.primaryStage);
			 alert.setTitle("Invalid settings");
			 alert.setHeaderText("Empty mandatory parameter");
			 alert.setContentText("Please define mandatory parameters (*)");
			 alert.showAndWait();
			 return false;
		 }
		 return true;
	 }
	
	 /*
	public void onEditMsgsChangedPcut(TableColumn.CellEditEvent<PointcutRow,String> rowStringCellEditEvent)
	{
		PointcutRow row = tableViewPointcut.getSelectionModel().getSelectedItem();
		row.setNumMsgs(rowStringCellEditEvent.getNewValue());
	}
	
	public void onEditPOChangedPcut(TableColumn.CellEditEvent<PointcutRow,String> rowStringCellEditEvent)
	{
		PointcutRow row = tableViewPointcut.getSelectionModel().getSelectedItem();
		row.setProcOperations(rowStringCellEditEvent.getNewValue());
	}
	public void onEditActorsChangedPcut(TableColumn.CellEditEvent<PointcutRow,String> rowStringCellEditEvent)
	{
		PointcutRow row = tableViewPointcut.getSelectionModel().getSelectedItem();
		row.setActors(rowStringCellEditEvent.getNewValue());
	}
	
	public void onEditDTypesChangedPcut(TableColumn.CellEditEvent<PointcutRow,String> rowStringCellEditEvent)
	{
		PointcutRow row = tableViewPointcut.getSelectionModel().getSelectedItem();
		row.setDataTypes(rowStringCellEditEvent.getNewValue());
	}
	
	public void onEditTypeChangedAdvice(TableColumn.CellEditEvent<AdviceRow,String> rowStringCellEditEvent)
	{
		AdviceRow row = tableViewAdvice.getSelectionModel().getSelectedItem();
		row.setType(rowStringCellEditEvent.getNewValue());
	}
	
	public void onEditBehaviorChangedAdvice(TableColumn.CellEditEvent<AdviceRow,String> rowStringCellEditEvent)
	{
		AdviceRow row = tableViewAdvice.getSelectionModel().getSelectedItem();
		row.setBehavior(rowStringCellEditEvent.getNewValue());
	}*/
	
	//Tokens
	public void buttonAddToken(ActionEvent actionEvent)
	{
		TokenRow row = new TokenRow(txtFieldToken.getText(),txtFieldValue.getText());
		tableViewTokens.getItems().add(row);
		txtFieldToken.clear();
		txtFieldValue.clear();
	}
	
	public void buttonDeleteToken(ActionEvent actionEvent)
	{
		ObservableList<TokenRow> allRows, singleRow;
		allRows = tableViewTokens.getItems();
		singleRow = tableViewTokens.getSelectionModel().getSelectedItems();
		singleRow.forEach(allRows::remove);
	}
	
	/*
	public void onEditTokenChanged(TableColumn.CellEditEvent<DataDictionaryRow,String> rowStringCellEditEvent)
	{
		TokenRow row = tableViewTokens.getSelectionModel().getSelectedItem();
		row.setToken(rowStringCellEditEvent.getNewValue());
	}
	
	public void onEditValueChanged(TableColumn.CellEditEvent<DataDictionaryRow,String> rowStringCellEditEvent)
	{
		TokenRow row = tableViewTokens.getSelectionModel().getSelectedItem();
		row.setValue(rowStringCellEditEvent.getNewValue());
	}*/

	
}
