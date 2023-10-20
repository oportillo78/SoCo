package edu.ucd.forcops.main.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class that is used in the table view. Contains two submission names and all their associated tool results.
 */
public class VisualResultsTO implements Comparable<VisualResultsTO>
{
    private StringProperty id;
    private StringProperty inputFile;
    private StringProperty inputFilepath;
    private StringProperty executionMode;
    private StringProperty appliedControls;
    private StringProperty outputFile;
    private StringProperty outputFilepath;
    
    public VisualResultsTO(String _id, String _inputFile, String _inputFilepath, 
    		boolean _executionMode, String _appliedControls, String _outputFile, String _outputFilepath)
    {
    	id = new SimpleStringProperty(_id);
    	inputFile = new SimpleStringProperty(_inputFile);
    	inputFilepath = new SimpleStringProperty(_inputFilepath);
    	//executionMode = new SimpleStringProperty(_executionMode?"Debug on":"Debug off");
    	executionMode = new SimpleStringProperty(_executionMode?"Yes":"No");
    	appliedControls = new SimpleStringProperty(_appliedControls);
    	outputFile = new SimpleStringProperty(_outputFile);
    	outputFilepath = new SimpleStringProperty(_outputFilepath);
    }
  
	public StringProperty getId() {
		return id;
	}
	public void setId(StringProperty id) {
		this.id = id;
	}
	public StringProperty getAppliedControls() {
		return appliedControls;
	}
	public void setAppliedControls(StringProperty appliedControls) {
		this.appliedControls = appliedControls;
	}
	public StringProperty getInputFile() {
		return inputFile;
	}
	public void setInputFile(StringProperty inputFile) {
		this.inputFile = inputFile;
	}
	public StringProperty getInputFilepath() {
		return inputFilepath;
	}
	public void setInputFilepath(StringProperty inputFilepath) {
		this.inputFilepath = inputFilepath;
	}
	public StringProperty getExecutionMode() {
		return executionMode;
	}
	public void setExecutionMode(StringProperty executionMode) {
		this.executionMode = executionMode;
	}
	public StringProperty getOutputFile() {
		return outputFile;
	}
	public void setOutputFile(StringProperty outputFile) {
		this.outputFile = outputFile;
	}
	public StringProperty getOutputFilepath() {
		return outputFilepath;
	}
	public void setOutputFilepath(StringProperty outputFilepath) {
		this.outputFilepath = outputFilepath;
	}

	@Override
	public int compareTo(VisualResultsTO o) 
	{
		String myKey = inputFile.getValue()+"_"+executionMode.getValue();
		String otherKey = o.getInputFile().getValue()+"_"+o.getExecutionMode().getValue();
		
		return myKey.compareTo(otherKey);
	}

	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append(inputFile.getValue()+", ");
		sb.append(inputFilepath.getValue()+", ");
		sb.append(executionMode.getValue()+", ");
		sb.append(appliedControls.getValue().replaceAll(",", " |")+", ");
		sb.append(outputFile.getValue()+", ");
		sb.append(outputFilepath.getValue());
		
		return sb.toString();
	}
	
	
	
}
