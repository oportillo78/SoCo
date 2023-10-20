package edu.ucd.forcops.main.ui;

import java.io.File;
import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.ucd.forcops.main.MainUtils;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ViewDiagramsController 
{
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	//public static final String IMG_PATH = "edu/ucd/forcops/main/ui/images/";
	//public static final String IMG_PATH2 = "bin/"+IMG_PATH;
	
	@FXML
	ImageView imageViewOriginal;

	@FXML
	ImageView imageViewModified;
	
	@FXML
	Label appliedControls;
	
    @FXML
    private void initialize()
	{    	    	
    	String shortName = "file:///"+MainClient.IMG_PATH_ABSOLUTE+"placeholder.png";
    	
    	imageViewOriginal.setImage(new Image (shortName));	
    	imageViewModified.setImage(new Image (shortName));
    	
    	//Tratar de hacer que las imagenes se expandan (posiblemene necesite un listener o "binding" su width a su papa    	
    	AnchorPane ap = (AnchorPane) imageViewOriginal.getParent();
    	//System.out.println(ap.getClass());
    	//System.out.println(ap.widthProperty().getClass());

    	/*
    	imageViewOriginal.fitWidthProperty().bind(ap.widthProperty().add(ap.widthProperty().doubleValue()*0.5*-1));
    	imageViewOriginal.fitHeightProperty().bind(ap.heightProperty().add(ap.heightProperty().doubleValue()*0.5*-1));

    	imageViewModified.fitWidthProperty().bind(ap.widthProperty().add(ap.widthProperty().doubleValue()*0.5*-1));
    	imageViewModified.fitHeightProperty().bind(ap.heightProperty().add(ap.heightProperty().doubleValue()*0.5*-1));
    	 */
	}


    
    @FXML
    public void showDiagrams(VisualResultsTO result)
    {
    	// First, generate the images.
    	String originalFile = result.getInputFilepath().getValue();
    	String modifiedFile = result.getOutputFilepath().getValue();
    	
    	MainUtils.generateUML(new File(originalFile), MainClient.IMG_PATH_ABSOLUTE+"originalFile.png");
    	MainUtils.generateUML(new File(modifiedFile), MainClient.IMG_PATH_ABSOLUTE+"modifiedFile.png");
    	
    	//- Applied Controls: ["+
    	appliedControls.setText("["+result.getAppliedControls().getValue()+"]");
    	
    	// Then, update the image placeholders in the view.
		 //OPD.- The "tricky" to properly show the scroll bar (other than adding/configuring it in SceneBuilder)
		 //      was to set the height/width of BOTH pane and imageview equals to the image's ones.    	
    	Image imgOriginal = new Image ("file:///"+MainClient.IMG_PATH_ABSOLUTE+"originalFile.png");
    	imageViewOriginal.setImage(imgOriginal);
    	
		AnchorPane apOriginal = (AnchorPane) imageViewOriginal.getParent();
		apOriginal.setPrefHeight(imgOriginal.getHeight());
		apOriginal.setPrefWidth(imgOriginal.getWidth());
		imageViewOriginal.setFitHeight(imgOriginal.getHeight());
		imageViewOriginal.setFitWidth(imgOriginal.getWidth());
		 
		Image imgModif = new Image ("file:///"+MainClient.IMG_PATH_ABSOLUTE+"modifiedFile.png");
		imageViewModified.setImage(imgModif);
		
		AnchorPane apModif = (AnchorPane) imageViewModified.getParent();
		apModif.setPrefHeight(imgModif.getHeight());
		apModif.setPrefWidth(imgModif.getWidth());
		imageViewModified.setFitHeight(imgModif.getHeight());
		imageViewModified.setFitWidth(imgModif.getWidth());

    }
    

}
