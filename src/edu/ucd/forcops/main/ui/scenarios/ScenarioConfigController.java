package edu.ucd.forcops.main.ui.scenarios;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.ucd.forcops.main.MainUtils;
import edu.ucd.forcops.main.ui.MainClient;
import edu.ucd.forcops.main.ui.MainClientUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import net.sourceforge.plantuml.SourceStringReader;

public class ScenarioConfigController implements Initializable{
	
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	@FXML
	private Label lblSaveStatus;
	@FXML
	private ImageView imageViewScenario;
	@FXML
	private TextArea txtAreaPlantUMLText;
	@FXML
	private Label lblTitle;
	
	private String currentScenarioURL = null;
	
	//public static final String IMG_PATH = "edu/ucd/forcops/main/ui/images/";
	//public static final String IMG_PATH2 = "bin/"+IMG_PATH;
	
	private String typeWnd = null;
	
	private ScenariosDisplayController sdControllerRef;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
    	AnchorPane ap = (AnchorPane) imageViewScenario.getParent();
    	//System.out.println(ap.getClass());
    	//System.out.println(ap.widthProperty().getClass());

    	/*
    	imageViewScenario.fitWidthProperty().bind(ap.widthProperty().add(ap.widthProperty().doubleValue()*0.7*-1));
    	imageViewScenario.fitHeightProperty().bind(ap.heightProperty().add(ap.heightProperty().doubleValue()*0.7*-1));	
    	*/
	}
	
	// BUTTON ACTION to update the diagram image after the user types new PlantUML instructions
	public void buttonUpdateDiagram(ActionEvent actionEvent) 
	{
		//MainUtils.generateUML(originalFile, IMG_PATH2+"originalFile.png");
		// Then, update the image placeholders in the view.
    	//imageViewOriginal.setImage(new Image (IMG_PATH+"originalFile.png"));
		String umlText = txtAreaPlantUMLText.getText();
		//umlSource = String.join("\n", inputLines);
		//VERIFY IF THERE ARE ENTERS
		generateUMLImg(umlText, MainClient.IMG_PATH_ABSOLUTE+"tmp.png");
		updateImageView(new Image (MainClient.IMG_PATH_ABSOLUTE+"tmp.png"));
	}
	// FIRST method to call when we want to open the Config Window for an Scenario (either New or Existing)
	public void showConfigScenarioPlantUMLWnd(String selectedScenarioURL, String type,
			ScenariosDisplayController _sdControllerRef) throws Exception
    {
		sdControllerRef = _sdControllerRef;
	
		File scenarioFile = new File(selectedScenarioURL);
		//System.out.println(scenarioFile.getName());
		lblTitle.setText("Diagram - "+scenarioFile.getName().split("\\.")[0]);
		
		//Read the plantuml scenario description and upload to plant uml?
		lblSaveStatus.setText("");
		lblSaveStatus.setVisible(false);
		
		//Save in what case was this window opened (new or existing)
		typeWnd = type;
		 
		String plantUMLText = null;
		//save filename or path for current scenario
		currentScenarioURL = selectedScenarioURL; 
		
		// Open Config Window for NEW scenario
		if(type.equals("new")) {
			String dummyText = "@startuml \n"
							+ "Bob -> Alice : hello \n"
							+ "@enduml";
			plantUMLText = dummyText;
		}
		// Open Wnd for EXISTING scenario to modify it
		else {
			 plantUMLText = readScenarioFromFile(selectedScenarioURL);
			 plantUMLText = applyDataFilterFormat(plantUMLText, sdControllerRef.dfSetList);
		}
		txtAreaPlantUMLText.setText(plantUMLText);
		generateUMLImg(plantUMLText, MainClient.IMG_PATH_ABSOLUTE+"tmp.png");
		updateImageView(new Image (MainClient.IMG_PATH_ABSOLUTE+"tmp.png"));
    }
	 
	// Utility method to read plantUML instructions from a file
	 public static String readScenarioFromFile(String inputFilename)
	 {
		 List<String> inputLines;
		 String umlSource ="";
			try 
			{
				inputLines = FileUtils.readLines(new File(inputFilename));
				//String umlSource = inputLines.toString();
				umlSource = String.join("\n", inputLines);
			} 
			catch (IOException e) 
			{
				MainUtils.reportNonFatalError("Failed to read input file ["+inputFilename+"]  " + e.getMessage());
			}
			return umlSource;
	 }
	
	 public static String applyDataFilterFormat(String umlSource, List<Set<String>> dfSetList)
	 {
		 //String newUmlSource = umlSource;

		 if (dfSetList == null)
		 {
			 return umlSource;
		 }
		 
		 /*
		 //OPD.-Dec15, 2022: To avoid breaking PlantUML code, we need to first remove the lines "@startuml" and "@enduml"
		 // BEFORE applying the DFs.
		 newUmlSource = newUmlSource.replace("@startuml","[SSSSS]");
		 newUmlSource = newUmlSource.replace("@enduml","[EEEEE]");
		 
		 for (Set<String> nextDFSet:dfSetList)
		 {
			 for (String nextDFelement:nextDFSet)
	 		 {
				 newUmlSource = newUmlSource.replaceAll("(?i)"+nextDFelement, "<color green>"+nextDFelement+"<color black>"); 			
	 		 }
		 }
		 
		 //OPD.-Dec15, 2022: Then we recover those lines AFTER applying the DFs.
		 newUmlSource = newUmlSource.replace("[SSSSS]","@startuml");
		 newUmlSource = newUmlSource.replace("[EEEEE]","@enduml");
		 */
		//OPD.-Dec15, 2022: New logic to support multiple exception cases.
		 
		 String[] newUmlSource = umlSource.split("\n");
		 
		 for (int x=0; x<newUmlSource.length; x++)
		 {
			 if (newUmlSource[x].contains("@startuml") || newUmlSource[x].contains("@enduml"))
			 {
				 logger.debug("Skipping start/enduml lines: "+newUmlSource[x]);
				 continue;
			 }
			 
			 String[] newUmlSourceTokens = newUmlSource[x].split(":");
			 
			 if (newUmlSourceTokens.length>1)
			 {
				 for (Set<String> nextDFSet:dfSetList)
				 {
					 for (String nextDFelement:nextDFSet)
			 		 {
						 newUmlSourceTokens[1] = newUmlSourceTokens[1].replaceAll("(?i)"+nextDFelement, "<color green>"+nextDFelement+"<color black>");
			 		 }
				 }
				 newUmlSource[x] = newUmlSourceTokens[0]+":"+newUmlSourceTokens[1];
			 }
			 else
			 {
				 //OPD Dec21, 2022: Changing the logic to make sure we ONLY consider "participant" as a valid DF element inside the line without considering the PlantUML syntax.
				 if (newUmlSource[x].trim().toLowerCase().startsWith("participant"))
//				 if (StringUtils.containsIgnoreCase(newUmlSource[x], "participant"))
				 {
					 newUmlSourceTokens = newUmlSource[x].trim().split(" ", 2);

					 outerloop:
					 for (Set<String> nextDFSet:dfSetList)
					 {
						 for (String nextDFelement:nextDFSet)
				 		 {
							 //OPD Dec20,2022: Adding logic to also make green the participants. From the time being, only lower case + explicitly stated!
							 if (StringUtils.containsIgnoreCase(newUmlSourceTokens[1], nextDFelement) || 
									 StringUtils.containsIgnoreCase(newUmlSourceTokens[1], nextDFelement.replace(".","\\n")))	//OPD Dec21,2022: Patch added to support \n in the participants.
							 {
								 newUmlSource[x] = newUmlSource[x]+" #green";
								 break outerloop;
							 }
				 		 }
					 } 
				 }
				 else
				 {
					 logger.debug("Skipping non-message line: "+newUmlSource[x]);
				 }
			 }

		 }
		 
		 String newText = String.join("\n", newUmlSource);
		 
		 return newText;
	 }
	 
	 public static String removeDataFilterFormat(String umlSource, List<Set<String>> dfSetList)
	 {
		 String newUmlSource = umlSource;

		 if (dfSetList == null)
		 {
			 return umlSource;
		 }
		 
		 for (Set<String> nextDFSet:dfSetList)
		 {
			 for (String nextDFelement:nextDFSet)
	 		 {
				 newUmlSource = newUmlSource.replaceAll("(?i)"+"<color green>"+nextDFelement+"<color black>",nextDFelement);
	 		 }
 		 }
		 
		 newUmlSource = newUmlSource.replaceAll("(?i)"+" #green","");

		 return newUmlSource;
	 }
	 
	 //Utility method to generate an image file with the seq diagram based on the instructions in plantUML
	 public static void generateUMLImg(String umlSource, String outputFilename)
    {
		try 
		{
			OutputStream png = new FileOutputStream(outputFilename);
			SourceStringReader reader = new SourceStringReader(umlSource);
			//System.out.println("About to generate img for " + outputFilename);
			reader.generateImage(png);
			//logger.debug("Output UML Sequence diagram with name '" + outputFilename + ".png' is generated in base directory");
		} 
		catch (IOException exception) 
		{
			MainUtils.reportNonFatalError("Failed to write output file ["+outputFilename+"]  " + exception.getMessage());
		}
	}
	
	 // Utility method to update the image in the window (after the user has modified the plantUML code in the text area)
	 private void updateImageView(Image img)
	 {
		 imageViewScenario.setImage(img);
		 //System.out.println("img.getHeight()->"+img.getHeight());
		 //System.out.println("img.getWidth()->"+img.getWidth());
		 
		 //OPD, Dec19.- The "tricky" to properly show the scroll bar (other than adding/configuring it in SceneBuilder)
		 //      		was to set the height/width of BOTH pane and imageview equals to the image's ones.
		 AnchorPane ap = (AnchorPane) imageViewScenario.getParent();
		 ap.setPrefHeight(img.getHeight());
		 ap.setPrefWidth(img.getWidth());
		 imageViewScenario.setFitHeight(img.getHeight());
		 imageViewScenario.setFitWidth(img.getWidth());
		 
		 //imageViewScenario.setFitWidth(400);
		 imageViewScenario.setPreserveRatio(true);
		 imageViewScenario.setSmooth(true);
		 imageViewScenario.setCache(true);
	 }
	 
	 // BUTTON ACTION save changes in config window (use the same file and if it is new, Save As)
	 public void buttonSaveChanges(ActionEvent actionEvent)
	{
		 
		 //Retrieve lines from textarea
		 //String scenarioTxt = txtAreaPlantUMLText.getText();
		 String scenarioTxt = removeDataFilterFormat(txtAreaPlantUMLText.getText(), sdControllerRef.dfSetList);
		 String[] lines =  scenarioTxt.split("\\r?\\n");
		 List<String> linesTxtArea = Arrays.asList(lines);
		 
		 //Check if it is NEW
		 String fileToSave = null;
		 if (typeWnd.equals("new")) {
			 //System.out.println("NEW FILE TO BE SAVED IN DIR: " + currentScenarioURL);
			 // Open SAVE filechooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Diagram description");
            fileChooser.setInitialDirectory(new File(currentScenarioURL));	
            File file = fileChooser.showSaveDialog(MainClient.primaryStage);
            if (file != null) {
            	fileToSave = file.getAbsolutePath();
            }    
		 }
		 else {
			 //System.out.println("EXISTING FILE TO BE SAVED: " + currentScenarioURL);
			 fileToSave = currentScenarioURL;
		 }
		 
		 
		 if (fileToSave != null)
		 {
			//Write POs to file (clear file and create a new one)
			try {
				File file = new File(fileToSave);
				FileUtils.writeLines(file, linesTxtArea, false);
				
				//System.out.println("About to save file: " + fileToSave);
				//System.out.println();
				
				
				// Show saved until both sets in data dictionary have been saved
				MainClientUtils.showLabelTemporarily(lblSaveStatus, "File saved", 2);
				
				sdControllerRef.updateScenariosAfterCreate();
				
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
	 
	 
	
}
