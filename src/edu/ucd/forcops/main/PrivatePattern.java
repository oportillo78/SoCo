package edu.ucd.forcops.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PrivatePattern 
{
	protected static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	private String name;
	private String description;
	
	private String[] templateActors;
	private PrivateSubPattern[] subpatterns;
	private String[] sortedActors;
	
	//used to persist tokens throughout the UI, not part of the object functionality
	private String[] tokens;
	

	public PrivatePattern(String _name, String _description, 
						String[] _templateActors, PrivateSubPattern[] _subpatterns, String[] _sortedActors)
	{		
		name = _name;
		description = _description;
		templateActors = _templateActors;
		subpatterns = _subpatterns;
		sortedActors = _sortedActors;
		
		//OPD.- Dec 31, 2018: Setting a reference of the privacy pattern to all the sub-patterns.
		for (int x=0;x<subpatterns.length;x++)
		{
			subpatterns[x].setPriPat(this);
			subpatterns[x].setPriPatId(x+1);
		}
	}
	
	public boolean assessApplicability(PlantUMLWrapper pWrap) throws Exception
	{
		logger.debug("*********************************************************************************");
		logger.debug("About to assess applicability of "+this.getName());
		logger.debug("*********************************************************************************");
		
		//First, get a recovery point of the rawtext.
		List<String> rawUmlDataBk = new ArrayList<String>();
		rawUmlDataBk.addAll(pWrap.getRawUmlData());
				
		//Then, iterate trough all the subpatterns.
		for (PrivateSubPattern subp : subpatterns)
		{
			//If anyone is NOT successfully applied, everything has to be rolled back.
			if (!subp.assessApplicability(pWrap))
			{
				logger.debug("As not all the subpatterns applied, all applied subpatterns were rolled back");
				pWrap.updateUmlItemBasedData(rawUmlDataBk);
				return false;
			}
		}
		
		//OPD.- Dec 25, 2018: Logic to make sure the participants are in order.
		sortInvolvedActors(pWrap);
		
		//Then, re-process the diagram (to update all variables) after the participant sorting!
		pWrap.updateUmlItemBasedData(pWrap.getRawUmlData());
		
		return true;
	}
	
	//OPD.- Dec 25, 2018: New logic to make sure the participants are in order.
	protected void sortInvolvedActors(PlantUMLWrapper pWrap)
	{
		List<String> rawUmlData = pWrap.getRawUmlData();
		int rawUmlSize = rawUmlData.size();
		
		List<String> oldExplicitParticipants = new ArrayList<String>();
		
		//First, delete any existing participants (among the sorted ones)
		//OPD.- Feb7, 2019: We have to start from the end, to avoid an arrayoutofbound exception! (as we are deleting the elements!)
		for (int y=rawUmlSize-1;y>=0;y--)
		{
			if (rawUmlData.get(y).trim().toLowerCase().startsWith("participant"))
			{
				oldExplicitParticipants.add(rawUmlData.get(y));
				pWrap.delRawUmlData(y);
			}
		}
		
		//Then, insert them in reverse order (so that they are in order at the end, as we are ALWAYS inserting them at position 1!). 
		//Whenever adding one, check if that is a new participant. If so, format it as new (i.e., blue).
		String[] nextSortedActor = null;
		int nextOrderValue = -1;
		for (int y=sortedActors.length-1;y>=0;y--)
		{
			nextSortedActor = sortedActors[y].split(":");
			nextOrderValue = (nextSortedActor.length==1)?(y*50):Integer.valueOf(nextSortedActor[1]);
			if ( isActorNew(nextSortedActor[0]) )
			{
				pWrap.addRawUmlData(Decorator.decoreateTemplateLine("participant "+nextSortedActor[0], Decorator.getAddColor())+" order "+(nextOrderValue), 1);
			}
			else
			{
				// From the time  being, spacing the participants by 50!
				pWrap.addRawUmlData("participant "+nextSortedActor[0]+" order "+(nextOrderValue), 1);
			}
		}

		//OPD.- Feb4,2019: Adding logic to avoid losing valuable participant information (e.g., short names, previous orders, etc.!!!)
		//IMPORTANT NOTES: There are current problems of duplicated sortings if the actors are BOTH part of the diagram sorting (before the pattern),
		//					AND the pointcut sorting. To partially fix it, the diagram sorting is inserted AFTER the pointcut one.
		//					That said, there is still a duplication issue IF/WHEN a synonym (w.r.t. the pointcut) is used in the diagram.
		Collections.reverse(oldExplicitParticipants);//OPD.-Feb 7, reversing the list to make sure the participants are reinserted in their original order! (as we got the list in reverse order!)
		pWrap.addRawUmlData(oldExplicitParticipants.toArray(new String[0]), 1);
	}
	
	//OPD.- Dec 25, 2016: To check if an actor is new, we review the list of actors in the (pointcut) pattern.
	public boolean isActorNew(String _actor)
	{
		for (int x=0; x<templateActors.length; x++)
		{
			if (templateActors[x].equalsIgnoreCase(_actor))
			{
				return true;
			}
		}
		return false;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public List<String> getKeyPdItems()
	{
		List<String> pdata = new ArrayList<String>();
		for (PrivateSubPattern subpat: subpatterns)
		{
			pdata.addAll(subpat.getKeyPdItems());
		}
		
		return pdata;
	}
	
	public String[] getTemplateActors() {
		return templateActors;
	}

	public String[] getSortedActors() {
		return sortedActors;
	}
	
	public PrivateSubPattern[] getSubpatterns() {
		return subpatterns;
	}

	//used to persist tokens throughout the UI, not part of the object functionality
	public String[] getTokens() {
		return tokens;
	}

	//used to persist tokens throughout the UI, not part of the object functionality
	public void setTokens(String[] tokens) {
		this.tokens = tokens;
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();

		//sb.append("The pattern [" + name + "]\n");
		sb.append("The control [" + name + "]\n");
		sb.append("With description [" + description + "]\n");
		sb.append("With new actors " + Arrays.toString(templateActors) + "\n");
		sb.append("With sorting " + Arrays.toString(sortedActors) + "\n");
		//sb.append("* NUMBER OF SUBPATTERNS: "+subpatterns.length+" \n");
		//sb.append("* LIST OF SUBPATTERNS: \n");
		for (PrivateSubPattern subpat: subpatterns)
		{
			sb.append(subpat.toString());
		}
		
		return sb.toString();
	}
}