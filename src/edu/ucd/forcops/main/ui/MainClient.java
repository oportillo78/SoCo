package edu.ucd.forcops.main.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;

import edu.ucd.forcops.main.MainUtils;
import edu.ucd.forcops.main.ui.dictionary.EditDataDictionaryController;
import edu.ucd.forcops.main.ui.ruleset.CatalogDisplayController;
import edu.ucd.forcops.main.ui.scenarios.ScenarioConfigController;
import edu.ucd.forcops.main.ui.scenarios.ScenariosDisplayController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class MainClient extends Application 
{
    public static Stage primaryStage;

 	public static final String DIR_OS_SEPARATOR = FileSystems.getDefault().getSeparator();
    
	//OPD Jan13, 2023: Changing the tmp image dir to use the same tmp as the tet data.
	//This is done to avoid having an "out-of-the-blue" directory around after closing the tool (which might confuse the reviewers).
    public static final String TMP_DIR = System.getProperty("user.dir")+MainClient.DIR_OS_SEPARATOR+"tmp";
    
    /*OPD.- Mar14, 2022: Changing all logic to only use IMG_PATH_ABSOLUTE to (1) make sure to maintain the code, 
     * (2) avoid odd behaviours previously  experienced, where PlantUML required the full relative path inside the Eclipse project (i.e., starting with bin),
     * while JavaFX assumed bin and skipt it (hence the need of IMG_PATH and IMG_PATH2!)
     */
 	//public static final String IMG_PATH = "edu/ucd/forcops/main/ui/images/";
 	//public static final String IMG_PATH2 = "bin/"+IMG_PATH;
 	//public static final String IMG_PATH_ABSOLUTE = new File(IMG_PATH2).getAbsolutePath();
 	public static final String IMG_PATH_ABSOLUTE = new File(TMP_DIR).getAbsolutePath()+MainClient.DIR_OS_SEPARATOR;
 	
 	// Default filenames for all configuration files
 	public static final String SUFFIXPO = "procOps";
 	public static final String SUFFIXPDI = "pdItems";
 	public static final String SUFFIXRULESET = "ruleset";
 	public static final String SUFFIXSCENARIOS = "diagrams";
 	public static final String SUFFIXTOKENS = "tokens";
 	public static final String SUFFIXDF= "dfilters";
 	
 	public static final String DF_EXT= ".df";
 	
 	public static final String SAMPLE_APP_UNIXYZ="unixyz";
 	public static final String SAMPLE_APP_FENIXEDU="fenixedu";
 	    
    public static void main(String[] args) throws ConfigurationException
    {
    	//OPD Mar14, 2022: Adding validation to make sure the directory has been successfully created
    	//OPD Jan13, 2023: Refactoring the logic to avoid having an extra dir that might confuse reviewers.
    	
    	try 
    	{
    		MainClientUtils.deleteDir(TMP_DIR);
    		Files.createDirectories(Paths.get(TMP_DIR));
			//Files.createDirectories(Paths.get(IMG_PATH2));
		} 
    	catch (IOException e) 
    	{
    		MainUtils.reportFatalError("Error trying to create the directory that is needed for creating the temporal PlantUML diagrams.");
		}
    	
        launch(args);
    }
	
    public static void shownComparison2(VisualResultsTO result) 
    {
    	
    	// First, generate the images.
    	String originalFile = result.getInputFilepath().getValue();
    	String modifiedFile = result.getOutputFilepath().getValue();
    	
    	MainUtils.generateUML(new File(originalFile), IMG_PATH_ABSOLUTE+"originalFile.png");
    	MainUtils.generateUML(new File(modifiedFile), IMG_PATH_ABSOLUTE+"modifiedFile.png");
    	
    	// Then, update the image placeholders in the view.
    	
    	ImageView imageViewOriginal = new ImageView();    	
    	imageViewOriginal.setImage(new Image (IMG_PATH_ABSOLUTE+"originalFile.png"));	   	 
    	imageViewOriginal.setFitWidth(400);
    	imageViewOriginal.setPreserveRatio(true);
    	imageViewOriginal.setSmooth(true);
    	imageViewOriginal.setCache(true);
    	
    	ImageView imageViewModified = new ImageView();
    	imageViewModified.setImage(new Image (IMG_PATH_ABSOLUTE+"modifiedFile.png")); 
    	imageViewModified.setFitWidth(400);
    	imageViewModified.setPreserveRatio(true);
    	imageViewModified.setSmooth(true);
    	imageViewModified.setCache(true);
    	
    	
         Group root = new Group();
         Scene scene = new Scene(root);
         scene.setFill(Color.BLACK);
         HBox box = new HBox();
         box.getChildren().add(imageViewOriginal);
         box.getChildren().add(imageViewModified);
         root.getChildren().add(box);
          
         Stage stage = new Stage();
         stage.setTitle("View Diagrams");
         stage.initModality(Modality.WINDOW_MODAL);
         stage.initOwner(primaryStage);
         stage.setWidth(800);
         stage.setHeight(500);
         stage.setScene(scene); 
         stage.sizeToScene();
     	
         stage.showAndWait();
    }
    
    // The showComparison (based on fxml) is still not working well, so the temporal (less flexible/programmatically) logic of showComparison2 was created.
    public static void showComparison(VisualResultsTO result) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainClient.class.getResource("ViewDiagrams.fxml"));
            AnchorPane anchor = loader.load();
            Stage stage = new Stage();
            stage.setTitle("View Diagrams ["+result.getInputFile().getValue()+","+result.getOutputFile().getValue()+"]");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            Scene scene = new Scene(anchor);
            stage.setScene(scene);
            ViewDiagramsController controller = loader.getController();
            controller.showDiagrams(result);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Stage dictWnd = null;
    
    //DATA INVENTORY: Show Data Inventory window and pass control to its controller
    public static void showEditDictionaryWnd(String fileName) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EditDataDictionaryController.class.getResource("EditDataDictionary.fxml"));
            AnchorPane anchor = loader.load(); //calls initialize method in the controller
            Stage stage = new Stage();
            stage.setTitle("DATA DICTIONARY");
            //stage.initModality(Modality.WINDOW_MODAL);
            stage.initModality(Modality.NONE);
            stage.initOwner(primaryStage);
            Scene scene = new Scene(anchor);
            stage.setScene(scene);
            EditDataDictionaryController controller = loader.getController();
            // ------ TEMPORAL while we make the changes to choose the dir only
            //File tmpFile = new File(fileName);
            //fileName = tmpFile.getParent();
            //-----------
            //System.out.println("Showing data dictionary");
            controller.showDataDictionary(fileName);
            dictWnd = stage;
            stage.showAndWait();
            dictWnd = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        } 
    }
    
    public static Stage rulesetWnd = null;
    
    //RULESET
    public static void showEditRulesetWnd(String fileName) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(CatalogDisplayController.class.getResource("CatalogView.fxml"));
            AnchorPane anchor = loader.load(); //calls initialize method in the controller
            Stage stage = new Stage();
            stage.setTitle("PRIVACY CONTROLS RULESET");
            //stage.initModality(Modality.WINDOW_MODAL);
            stage.initModality(Modality.NONE);
            stage.initOwner(primaryStage);
            Scene scene = new Scene(anchor);
            stage.setScene(scene);
            CatalogDisplayController controller = loader.getController();
            // ------ TEMPORAL while we make the changes to choose the dir only
            //File tmpFile = new File(fileName);
            //fileName = tmpFile.getParent();
            //-----------
            //System.out.println("Showing edit ruleset");
            controller.showRuleset(fileName);
            rulesetWnd = stage;
            stage.showAndWait();
            rulesetWnd = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        } 
    }
    
  //NON-COMPLIANT SCENARIOS
    public static void showEditScenariosWnd(String workingDir) {
    	showEditScenariosWnd(workingDir, "noncompliant");
    }
    
    public static Stage scenariosWnd = null;
    
  // EVOLVED DIAGRAMS
    public static void showEditScenariosWnd(String workingDir, String callerType) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ScenarioConfigController.class.getResource("ScenariosView2.fxml"));
            AnchorPane anchor = loader.load(); //calls initialize method in the controller
            ScenariosDisplayController controller = loader.getController();
            Stage stage = new Stage();
            if (callerType.equals("noncompliant")) {
            	stage.setTitle("NON-COMPLIANT DIAGRAMS");
            	controller.setCallerType("noncompliant");
            }if (callerType.equals("evolved")) {
            	stage.setTitle("EVOLVED DIAGRAMS");
            	controller.setCallerType("evolved");
            }
            //stage.initModality(Modality.WINDOW_MODAL);
            stage.initModality(Modality.NONE);
            stage.initOwner(primaryStage);
            Scene scene = new Scene(anchor);
            stage.setScene(scene);
            
            // ------ TEMPORAL while we make the changes to choose the dir only
            //File tmpFile = new File(fileName);
            //fileName = tmpFile.getParent();
            //-----------
            //System.out.println("Showing datadic elements for data filters.");//OPD, Nov28,2022: Data filter logic
            controller.loadDataDictionaryInfo(workingDir);
            
            //System.out.println("Load existing data filters.");//OPD, Nov28,2022: Data filter logic
            controller.loadDataFiltersInfo(workingDir);
            
            //System.out.println("Showing edit diagrams");
            controller.showScenarios(workingDir);
            scenariosWnd = stage;
            stage.showAndWait();
            scenariosWnd = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        } 
    }
    

    @Override
    public void start(Stage primaryStage) 
    {
    	MainClient.primaryStage = primaryStage;
    	MainClient.primaryStage.setTitle("SoCo"); //AdaptIT
    	MainClient.primaryStage.setMinHeight(800);
    	MainClient.primaryStage.setMinWidth(1500);
        showMainWindow();
    }

    public void showMainWindow() 
    {
    	
		InputStream myPlaceholderImg = this.getClass().getResourceAsStream("images/placeholder.png");
		File myPlaceholderFile = new File(TMP_DIR+MainClient.DIR_OS_SEPARATOR+"placeholder.png");
		try {
			FileUtils.copyInputStreamToFile(myPlaceholderImg, myPlaceholderFile);
		} catch (IOException e) {
			MainUtils.reportNonFatalError("Error extracting the dataset zip file from jarfile.");
		}
    	
        try 
        {
            FXMLLoader loader = new FXMLLoader();
            //loader.setLocation(MainClient.class.getResource("MainScreen.fxml"));
            loader.setLocation(MainClient.class.getResource("MainScreenv2.fxml"));
            AnchorPane anchor = (AnchorPane) loader.load();
            Scene scene = new Scene(anchor);
            MainClient.primaryStage.setScene(scene);
            MainClient.primaryStage.show();
            MainClient.primaryStage.setOnCloseRequest( event -> {
            												//Whenever the tool ends, any sample data (which should always be in a single directory!) must be deleted!
            												//MainScreenControllerv2.deleteSampleDataDir();     
            												MainClientUtils.deleteDir(TMP_DIR); // It now occurs ALWAYS.
            												System.exit(0);
            											  } );
            //MainScreenController controller = loader.getController();
            MainScreenControllerv2 controller = loader.getController();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    

}
