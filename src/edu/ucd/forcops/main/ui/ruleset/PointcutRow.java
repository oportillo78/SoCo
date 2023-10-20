package edu.ucd.forcops.main.ui.ruleset;

import javafx.beans.property.SimpleStringProperty;

public class PointcutRow {

	protected SimpleStringProperty numMsgs;
	protected SimpleStringProperty procOperations;
	protected SimpleStringProperty actors;
	protected SimpleStringProperty dataTypes;
	

	
	public PointcutRow(String numMsgs, String procOperations, String actors, String dataTypes) {
		super();
		this.numMsgs = new SimpleStringProperty(numMsgs);
		this.procOperations = new SimpleStringProperty(procOperations);
		this.actors = new SimpleStringProperty(actors);
		this.dataTypes = new SimpleStringProperty(dataTypes);
	}
	
	
	public String getNumMsgs() {
		return numMsgs.get();
	}
	public void setNumMsgs(String numMsgs) {
		this.numMsgs = new SimpleStringProperty(numMsgs);
	}
	public String getProcOperations() {
		return procOperations.get();
	}
	public void setProcOperations(String procOperations) {
		this.procOperations = new SimpleStringProperty(procOperations);
	}
	public String getActors() {
		return actors.get();
	}
	public void setActors(String actors) {
		this.actors = new SimpleStringProperty(actors);
	}
	public String getDataTypes() {
		return dataTypes.get();
	}
	public void setDataTypes(String dataTypes) {
		this.dataTypes = new SimpleStringProperty(dataTypes);
	}
}
