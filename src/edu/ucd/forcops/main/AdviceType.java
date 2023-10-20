package edu.ucd.forcops.main;

public enum AdviceType {
	ADD_BEFORE,
	ADD_AFTER,
	WRAP_AROUND,
	ADD_BETWEEN,
	REPLACE_BETWEEN,
	REPLACE_ALL;

	public String getAdviceType()
	{
		if (this.equals(ADD_BEFORE)) return "ADD_BEFORE";
		if (this.equals(ADD_AFTER)) return "ADD_AFTER";
		if (this.equals(WRAP_AROUND)) return "WRAP_AROUND";
		if (this.equals(ADD_BETWEEN)) return "ADD_BETWEEN";
		if (this.equals(REPLACE_ALL)) return "REPLACE_ALL";
		if (this.equals(REPLACE_BETWEEN)) return "REPLACE_BETWEEN";
		//if (this.equals(DELETE)) return "DELETE";
		else return "INVALID_ADVICE_TYPE";
	}
}
