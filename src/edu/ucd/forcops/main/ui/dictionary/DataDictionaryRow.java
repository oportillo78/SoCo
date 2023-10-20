package edu.ucd.forcops.main.ui.dictionary;

import javafx.beans.property.SimpleStringProperty;

public class DataDictionaryRow {

	protected SimpleStringProperty dictionaryKey;
	protected SimpleStringProperty dictionaryValue;

	public DataDictionaryRow() {
		
	}
	
	public DataDictionaryRow(String dictionaryKey, String dictionaryValue) {
		super();
		this.dictionaryKey = new SimpleStringProperty(dictionaryKey);
		this.dictionaryValue = new SimpleStringProperty(dictionaryValue);
	}


	public String getDictionaryKey() {
		return dictionaryKey.get();
	}


	public void setDictionaryKey(String dictionaryKey) {
		this.dictionaryKey = new SimpleStringProperty(dictionaryKey);
	}


	public String getDictionaryValue() {
		return dictionaryValue.get();
	}


	public void setDictionaryValue(String dictionaryValue) {
		this.dictionaryValue = new SimpleStringProperty(dictionaryValue);
	}
	
	
}
