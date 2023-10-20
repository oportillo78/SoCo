package edu.ucd.forcops.main.ui.ruleset;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckBox;

public class PrivControlRow {

	protected SimpleStringProperty id;
	//protected CheckBox selection;
	protected SimpleStringProperty name;
	
	
	
	public PrivControlRow(String id, /*CheckBox selection,*/ String name) {
		super();
		this.id = new SimpleStringProperty(id);
		//this.selection = selection;
		this.name = new SimpleStringProperty(name);
	}
	
	public String getId() {
		return id.get();
	}
	public void setId(String id) {
		this.id = new SimpleStringProperty(id);
	}
	/*public CheckBox getSelection() {
		return selection;
	}
	public void setSelection(CheckBox selection) {
		this.selection = selection;
	}*/
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name = new SimpleStringProperty(name);
	}
	
	
	

}
