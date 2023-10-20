package edu.ucd.forcops.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.ucd.forcops.main.ui.InjectionCounter;

public class ApplicabilityProgress
{
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
 	private IfPosition ifPos = IfPosition.NONE;
	public List<Integer> currentOps = new ArrayList<Integer>();
	public List<String> currentOpsText = new ArrayList<String>();
	public PlantUMLWrapper context = null;
	
	private boolean pointcutAchieved = false;
	private boolean patternApplied = false;	//If the (sub) pattern has been applied AT LEAST once!, this flag is set to true!

	public boolean isPointcutAchieved()
	{
		return pointcutAchieved;
	}
	
	public void setPointcutAchieved()
	{
		pointcutAchieved = true;
		
		//OPD.- Feb5, 2019: Adding a "context" (the PC converted into PlantUML component) to (easily) know the actors/messages involved in the PC!
		context = new PlantUMLWrapper(currentOpsText.toArray(new String[0]));
		
		//OPD Jan16, 2023: Tmp logic to calculate the applied control (probably the applied pointcuts)
		InjectionCounter.countInjection();
	}
	
	public void clearPointCutAchieved()
	{
		pointcutAchieved = false;
		
		//Also restart variables for future instances of the pointcut!
		resetOperations();
	}
	
	public void setPatternApplied()
	{
		patternApplied = true;
	}
	
	public boolean isPatternApplied()
	{
		return patternApplied;
	}
	
	public void considerOperation(int x, String opText)
	{
		logger.debug("Adding ["+opText+"] to the considered operations!");
		currentOps.add(x);
		currentOpsText.add(opText);
	}
	
	public void resetOperations()
	{
		currentOps.clear();
		currentOpsText.clear();
		context = null;
	}
	
	public int getCurrentOpsNumber()
	{
		return currentOps.size();
	}
	
	public boolean hasRequiredOpsAchieved(int requiredOpsNumber)
	{
		return 	getCurrentOpsNumber() == requiredOpsNumber;
	}
	
	public boolean isWithinIf()
	{
		return ifPos.equals(IfPosition.WITHIN_IF);
	}
	
	public boolean isWithinElse()
	{
		return ifPos.equals(IfPosition.WITHIN_ELSE);
	}
	
	public void setIfPos()
	{
		ifPos = IfPosition.WITHIN_IF;
	}
	
	public void setElsePos()
	{
		ifPos = IfPosition.WITHIN_ELSE;
	}
	
	public void setNonePos()
	{
		ifPos = IfPosition.NONE;
	}

//====================================================================================================================================
		
	private enum IfPosition 
	{
		WITHIN_IF,
		WITHIN_ELSE,
		NONE;
	
	}
}


