package edu.ucd.forcops.main.ui.scenarios;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckBox;

public class ScenarioRow {

	protected SimpleStringProperty id;
	protected SimpleStringProperty name;
	protected SimpleStringProperty msgNum;
	
	
	public ScenarioRow(String id, String name, int msgNum) {
		super();
		this.id = new SimpleStringProperty(id);
		this.name = new SimpleStringProperty(name);
		
		//String format = "%0"+(5-String.valueOf(msgNum).length())+"d";
		String result = String.format("%05d", msgNum);
		this.msgNum = new SimpleStringProperty(result);
	}
	
	public String getId() {
		return id.get();
	}
	public void setId(String id) {
		this.id = new SimpleStringProperty(id);
	}
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name = new SimpleStringProperty(name);
	}
	
	public String getMsgNum() {
		return msgNum.get();
	}
	public void setMsgNum(String msgNum) {
		this.msgNum = new SimpleStringProperty(msgNum);
	}

}
