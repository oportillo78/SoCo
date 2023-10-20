package edu.ucd.forcops.main.ui.ruleset;

import javafx.beans.property.SimpleStringProperty;

public class TokenRow {

	protected SimpleStringProperty token;
	protected SimpleStringProperty value;

	public TokenRow() {
		
	}
	
	public TokenRow(String token, String value) {
		super();
		this.token = new SimpleStringProperty(token);
		this.value = new SimpleStringProperty(value);
	}


	public String getToken() {
		return token.get();
	}


	public void setToken(String token) {
		this.token = new SimpleStringProperty(token);
	}


	public String getValue() {
		return value.get();
	}


	public void setValue(String value) {
		this.value = new SimpleStringProperty(value);
	}
	
	
}
