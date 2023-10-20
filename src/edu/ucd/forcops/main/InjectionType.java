package edu.ucd.forcops.main;

public enum InjectionType {
	AFTER_SUCCESS,
	//BEFORE_SUCCESS,
	AFTER_FAILURE,
	//BEFORE_FAILURE,
	NOW;

	public String getInjectionType()
	{
		if (this.equals(AFTER_SUCCESS)) return "AFTER_SUCCESS";
		//if (this.equals(BEFORE_SUCCESS)) return "BEFORE_SUCCESS";
		if (this.equals(AFTER_FAILURE)) return "AFTER_FAILURE";
		//if (this.equals(BEFORE_FAILURE)) return "BEFORE_FAILURE";
		if (this.equals(NOW)) return "NOW";
		else return "INVALID_INJECTION_TYPE";
	}
}
