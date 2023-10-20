package edu.ucd.forcops.main.ui.dictionary;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.ucd.forcops.main.MainUtils;
import edu.ucd.forcops.main.ui.MainClient;
import edu.ucd.forcops.main.ui.MainClientUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class EditDataDictionaryController implements Initializable{
	
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	@FXML
	private TableView<DataDictionaryRow> tableViewPO;
	@FXML
	private TableView<DataDictionaryRow> tableViewPDI;
	@FXML
	private TableColumn<DataDictionaryRow,String> colKey;		//cols in PO table
	@FXML
	private TableColumn<DataDictionaryRow,String> colValue;
	@FXML
	private TableColumn<DataDictionaryRow,String> colKey1;		//cols in PDI table
	@FXML
	private TableColumn<DataDictionaryRow,String> colValue1;
	@FXML
	private TextField txtFieldKey;
	@FXML
	private TextField txtFieldValue;
	@FXML
	private Label lblSaveStatus;
	
	
	// Name of the dictionary file that triggered this window
	//private String currentDictionary = null;
	private String currentPOfile = null, currentPDIfile = null;
	private static String DICTIONARY_SEP = ",";
	
	ObservableList<DataDictionaryRow> observableListPO = FXCollections.observableArrayList();
	ObservableList<DataDictionaryRow> observableListPDI = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		//Read dictionary file and show the info in the table 
		colKey.setCellValueFactory(new PropertyValueFactory<>("DictionaryKey"));
		colValue.setCellValueFactory(new PropertyValueFactory<>("DictionaryValue"));
		colKey1.setCellValueFactory(new PropertyValueFactory<>("DictionaryKey"));
		colValue1.setCellValueFactory(new PropertyValueFactory<>("DictionaryValue"));
		
		tableViewPO.setItems(observableListPO); //=>dummy assignment, will fill it up in showDataDictionary(String)
		tableViewPO.setEditable(true);
		tableViewPDI.setItems(observableListPDI); //=>dummy assignment, will fill it up in showDataDictionary(String)
		tableViewPDI.setEditable(true);
		
		colKey.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue.setCellFactory(TextFieldTableCell.forTableColumn());
		colKey1.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue1.setCellFactory(TextFieldTableCell.forTableColumn());
	}
	
	// Read dictionary file content and fill up observable list 
	
    public void showDataDictionary(String currentDir) throws Exception
    {
    	//Build filenames for data dictionary proc opers and data items
    	currentPOfile = currentDir + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXPO + ".txt";
    	currentPDIfile = currentDir + MainClient.DIR_OS_SEPARATOR + MainClient.SUFFIXPDI + ".txt";
    	
		//Read dictionary and show in table 
		String key = "";
		observableListPO.clear();
		observableListPDI.clear();
		lblSaveStatus.setText("");
		lblSaveStatus.setVisible(false);
		try 
		{
			// Read PO 
			List<String> inputLines = FileUtils.readLines(new File(currentPOfile));
			for (int x=0; x<inputLines.size(); x++)
			{
				if (inputLines.get(x).trim().startsWith("#"))
				{
					MainUtils.reportInfo("Skipping line ["+x+"] of file ["+currentPOfile+
							"] because is commented (i.e., starting with #).");
					continue;
				}
				
				String[] nextLine = inputLines.get(x).trim().split(DICTIONARY_SEP, 2); //READ first "," only
				key = nextLine[0];
				String values = nextLine[1];
				//logger.debug("Next item is: ["+key+"|"+values+"]");
				
				DataDictionaryRow row = new DataDictionaryRow(key, values);
				observableListPO.add(row);
			}
			// Read PDI
			inputLines.clear();
			inputLines = FileUtils.readLines(new File(currentPDIfile));
			for (int x=0; x<inputLines.size(); x++)
			{
				if (inputLines.get(x).trim().startsWith("#"))
				{
					MainUtils.reportInfo("Skipping line ["+x+"] of file ["+currentPDIfile+
							"] because is commented (i.e., starting with #).");
					continue;
				}

				String[] nextLine = inputLines.get(x).trim().split(DICTIONARY_SEP, 2); //READ first "," only
				key = nextLine[0];
				String values = nextLine[1];
				logger.debug("Next item is: ["+key+"|"+values+"]");
		
				DataDictionaryRow row = new DataDictionaryRow(key, values);
				observableListPDI.add(row);
			}
		}
		catch(Exception e)
		{
			MainUtils.reportFatalError("Error when trying to load the Dictionary item: ["+key+ 
					"]; description: ["+e.getMessage()+"] ...\n"+"Thus, stopping the program execution!!!\n");
		}		
		
    }
			
	
	public void buttonAddPO(ActionEvent actionEvent)
	{
		DataDictionaryRow row = new DataDictionaryRow(txtFieldKey.getText(),txtFieldValue.getText());
		tableViewPO.getItems().add(row);
		txtFieldKey.clear();
		txtFieldValue.clear();
	}
	public void buttonAddPDI(ActionEvent actionEvent)
	{
		DataDictionaryRow row = new DataDictionaryRow(txtFieldKey.getText(),txtFieldValue.getText());
		tableViewPDI.getItems().add(row);
		txtFieldKey.clear();
		txtFieldValue.clear();
	}
	
	public void buttonSave(ActionEvent actionEvent)
	{
				
		List <List<String>> arrList = new ArrayList<>(); //For printing
		
		DataDictionaryRow row = new DataDictionaryRow();
		String line = null;
		ArrayList<String> linesPO = new ArrayList<String>();
		ArrayList<String> linesPDI = new ArrayList<String>();
		
		// Iterate PO table to save
		for (int i=0; i<tableViewPO.getItems().size(); i++) {
			row = tableViewPO.getItems().get(i);
			line = row.dictionaryKey.get() + DICTIONARY_SEP +  row.dictionaryValue.get();
			linesPO.add(line);
			
			//For printing
			/*arrList.add(new ArrayList<>());
			arrList.get(i).add(row.dictionaryKey.get());
			arrList.get(i).add(row.dictionaryValue.get());*/
		}
		
		// Iterate PDI table to save
		for (int i=0; i<tableViewPDI.getItems().size(); i++) {
			row = tableViewPDI.getItems().get(i);
			line = row.dictionaryKey.get() + DICTIONARY_SEP +  row.dictionaryValue.get();
			linesPDI.add(line);
		}
		
		//Write POs to file (clear file and create a new one)
		try {
			File file = new File(currentPOfile);
			FileUtils.writeLines(file, linesPO, false);
			
			file = new File(currentPDIfile);
			FileUtils.writeLines(file, linesPDI, false);
			
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
			visiblePause.play();
			*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//For printing
		/*for (int i=0; i<arrList.size(); i++) {
			for (int j=0; j<arrList.get(i).size(); j++) {
				System.out.println(arrList.get(i).get(j));
			}
		}*/
	}
	
	public void buttonDeletePO(ActionEvent actionEvent)
	{
		ObservableList<DataDictionaryRow> allRows, singleRow;
		allRows = tableViewPO.getItems();
		singleRow = tableViewPO.getSelectionModel().getSelectedItems();
		singleRow.forEach(allRows::remove);
	}
	public void buttonDeletePDI(ActionEvent actionEvent)
	{
		ObservableList<DataDictionaryRow> allRows, singleRow;
		allRows = tableViewPDI.getItems();
		singleRow = tableViewPDI.getSelectionModel().getSelectedItems();
		singleRow.forEach(allRows::remove);
	}
	
	public void onEditKeyChangedPO(TableColumn.CellEditEvent<DataDictionaryRow,String> rowStringCellEditEvent)
	{
		DataDictionaryRow row = tableViewPO.getSelectionModel().getSelectedItem();
		row.setDictionaryKey(rowStringCellEditEvent.getNewValue());
	}
	
	public void onEditValueChangedPO(TableColumn.CellEditEvent<DataDictionaryRow,String> rowStringCellEditEvent)
	{
		DataDictionaryRow row = tableViewPO.getSelectionModel().getSelectedItem();
		row.setDictionaryValue(rowStringCellEditEvent.getNewValue());
	}
	public void onEditKeyChangedPDI(TableColumn.CellEditEvent<DataDictionaryRow,String> rowStringCellEditEvent)
	{
		DataDictionaryRow row = tableViewPDI.getSelectionModel().getSelectedItem();
		row.setDictionaryKey(rowStringCellEditEvent.getNewValue());
	}
	
	public void onEditValueChangedPDI(TableColumn.CellEditEvent<DataDictionaryRow,String> rowStringCellEditEvent)
	{
		DataDictionaryRow row = tableViewPDI.getSelectionModel().getSelectedItem();
		row.setDictionaryValue(rowStringCellEditEvent.getNewValue());
	}

}
