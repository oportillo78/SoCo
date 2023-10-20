package edu.ucd.forcops.main.ui.ruleset;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ComboBox;

public class AdviceRow {

	protected SimpleStringProperty type;
	protected SimpleStringProperty behavior;
	
	
	public AdviceRow(String type, String behavior) {
		super();
		this.type = new SimpleStringProperty(type);
		this.behavior = new SimpleStringProperty(behavior);
	}
	
	public String getType() {
		return type.get();
	}
	public void setType(String type) {
		this.type = new SimpleStringProperty(type);
	}
	public String getBehavior() {
		return behavior.get();
	}
	public void setBehavior(String behavior) {
		this.behavior = new SimpleStringProperty(behavior);
	}
	
	
	
}
