package edu.ucd.forcops.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.sourceforge.plantuml.sequencediagram.Event;
import net.sourceforge.plantuml.sequencediagram.Message;

public class Pointcut 
{
	protected static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	public static String ANY_ACTOR="*";
	
	private String[] actors_original, ops_original, pdata_original;
	private List<String> actors_extended, actors_extended_sn;
	private List<String> ops_extended, pdata_extended;

	private int numReqOps;
	private InjectionType iType;
	
	
	public Pointcut(String[] _actors, String[] _ops, int _numReqOps, String[] _pdata, InjectionType _iType) throws Exception
	{
		this(_actors, _ops, _numReqOps, _pdata, _iType, true);
	}

	public Pointcut(String[] _actors, String[] _ops, int _numReqOps, String[] _pdata, InjectionType _iType, boolean checkPDInDictionary) throws Exception
	{
		actors_original = _actors;
		actors_extended = getInScopeActors(actors_original);
		actors_extended_sn = Dictionary.removeActorsPackages(actors_extended);
		ops_original = _ops;
		ops_extended = getInScopeOperations(ops_original);
		pdata_original = _pdata;
		pdata_extended = getInScopePdis(pdata_original);
		numReqOps = _numReqOps;
		
		iType = _iType;
		
		Set<String> pdInv = Dictionary.getKeyPdItems();
		for (String nextPd : pdata_original)
		{
			//logger.debug("NextPd in PC is:"+nextPd);
			if (checkPDInDictionary && !pdInv.stream().anyMatch(nextPd::equalsIgnoreCase) //!pdInv.contains(nextPd) 
					&& !nextPd.toLowerCase().equals("all"))
			{
				MainUtils.reportFatalError("\n The pointcut uses a personal data item not listed in the Data Dictionary ["+
						nextPd+"]. \n This needs to be fixed! ... Halting the program execution.\n");
			}
		}
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		
		//sb.append("A pointcut of type ["+iType.getInjectionType()+"] ");
		sb.append("A pointcut "); // to write to file in ui priv ctrl config 
		sb.append("composed of ["+(numReqOps)+"] message(s) ");
		sb.append("involving the "+Arrays.toString(ops_original)+" operations, ");
		sb.append("the "+Arrays.toString(actors_original)+" actors, ");
		sb.append("and the "+Arrays.toString(pdata_original)+" data types. ");
		
		return sb.toString();
	}
	
	public List<String> getInScopeActors(String[] actors)
	{
		List<String> pactors = new ArrayList<String>();
		
		for (String actor: actors)
		{
			pactors.addAll(Dictionary.getActorSynonymsByKey(actor, true));
		}
		
		return pactors;
	}
	
	public List<String> getInScopeOperations(String[] ops)
	{
		List<String> opsTmp = new ArrayList<String>();
		
		for (String operation: ops)
		{
			String[] syns = Dictionary.getOperationSynonyms(operation);
			if (syns!= null && syns.length !=0) {
				opsTmp.addAll(Arrays.asList(syns));
			}
		}
		
		return opsTmp;
	}
	
	public List<String> getInScopePdis(String[] pdi)
	{
		List<String> pdiTmp = new ArrayList<String>();
		
		for (String item: pdi)
		{
			//Patch to support "All"
			if (item.toLowerCase().equals("all"))
			{
				pdiTmp.addAll(Dictionary.getExtendedPdItems());
			}
			
			String[] syns = Dictionary.getPdiSynonyms(item);
			if (syns!= null && syns.length !=0) {
				pdiTmp.addAll(Arrays.asList(syns));
			}
		}
		
		return pdiTmp;
	}

	
	public boolean isRelevantOperationInMessage(String lineText)
	{
		//List<String> ops = getInScopeOperations();

		for (String next: ops_extended)
		{
			if (lineText.toLowerCase().contains(next.toLowerCase()))
			{
				return true;
			}
		}
		return false;		
	}
	
	public boolean isRelevantPersonalDataInMessage(String lineText)
	{
		Set<String> pdInv = Dictionary.getExtendedPdItems();

		for (String next: pdInv)
		{
			if (lineText.toLowerCase().contains(next.toLowerCase()))
			{
				//OPD.- Dec 19, 2018: The below line should help to ONLY consider in-scope personal data (as per the private pattern)
				//OPD.- Jan25, 2020: PENDING to fix this (potential) issue: As of now, pdata_extends does not contains All (as it contains the extendedset. Probably this is for the best, as the "All" comparison is case sensitive!
				if (pdata_extended.stream().anyMatch(next::equalsIgnoreCase) || 
						pdata_extended.stream().anyMatch("All"::equalsIgnoreCase)) 
				{
					return true;
				}

			}
		}
		return false;
	}

	/*
	 * Links with possible ideas to change the below logic to support some sort of regular expressions!!!
	 * 
	 * 1. https://stackoverflow.com/questions/42988414/java-how-to-check-multiple-regex-patterns-against-an-input
	 * 2. https://www.tutorialspoint.com/java/java_string_matches.htm
	 * 
	 * */
	public boolean areRelevantActorsInMessage(Message m)
	{
		//List<String> pactors = removeActorsPackages(getInScopeActors());
		
		//OPD.- Dec 28, 2018: Adding a condition to support the usage of * to indicate any actor.
		if (actors_extended_sn.stream().anyMatch(ANY_ACTOR::equalsIgnoreCase))
		{
			return actors_extended_sn.stream().anyMatch(m.getParticipant1().getCode()::equalsIgnoreCase) || 
					actors_extended_sn.stream().anyMatch(m.getParticipant2().getCode()::equalsIgnoreCase);
		}
		else
		{
			return actors_extended_sn.stream().anyMatch(m.getParticipant1().getCode()::equalsIgnoreCase) && 
					actors_extended_sn.stream().anyMatch(m.getParticipant2().getCode()::equalsIgnoreCase);
		}
	}
	
	public boolean assessActorsApplicability(PlantUMLWrapper diagramWrap, String patternName)
	{
		// a) Fail-fast condition: If the # of actors in the diagram is smaller than the involved ones,
		// exit fast!
		if (diagramWrap.getCp().size()<actors_original.length)
		{
			logger.debug("The # of actors in the diagram ["+diagramWrap.getCp().size()+
					"] is smaller than the # of actors involved in the pattern ["+actors_original.length+"], "
					+ "so the pattern ["+patternName+"] is not applicable");
			return false;
		}

		// b) Alternative condition:
		// Firstly, get all the synonyms of the diagram actors. 
		// From the time being they are put TOGETHER. Hopefully, this is not an issue! :-)
		Set<String> diagramActors = diagramWrap.getExtendedActorSet();

		// Secondly, compare the pattern actors with those in the diagram.
		// If ANY of the pattern actors is not present in the diagram, fail fast!
		for (String nextActor : actors_extended)
		{
			if (!diagramActors.stream().anyMatch(getTopActorByKey(nextActor)::equalsIgnoreCase) && 
					!nextActor.equals(ANY_ACTOR))	//Adding logic to consider the * placeholder.
			{
				logger.debug("The actor ["+nextActor+"] is not present in the diagram, "
						+ "so the pattern ["+patternName+"] is not applicable");
				return false;
			}
		}

		return true;
	}
	
	// Method to get one syn for the placeholders defined in the rulsets.
	// (if it exists). Otherwise, it is assume NOT to be a placeholder and the original value is returned!
	public String getTopActorByKey(String actorName)
	{
		Set<String> pactors = Dictionary.getActorSynonymsByKey(actorName, false);
		
		if (pactors.size()==0)
		{
			return actorName;
		}
		else
		{
			return pactors.iterator().next();
		}
		
	}
	
	public boolean assessApplicability(PlantUMLWrapper diagramWrap, ApplicabilityProgress appProg, PrivateSubPattern subPat) throws Exception
	{		
		// Review that all involved actors are present (considering the synonyms).
		// Fail-fast if the relevant actors are not involved!
		//----------------------------------------------------------------------------
		if (!assessActorsApplicability(diagramWrap, subPat.getPriPat().getName()))
		{
			return false;
		}
		
		// If previous step passed successfully ...
		// Iteratively review (line-by-line) if the involved operations/messages (considering the synonyms) 
		// and personal data exists.
		
		List<Event> le = diagramWrap.getLe();
						
		for (int x=0; x<le.size(); x++)
		{
			//OPD.- Feb5,2019: Logic to print ONLY the message text! (and not the hash info of the class that its toString method returns!).
			String nextMsgTokens[] = le.get(x).toString().split(" ");
			String nextMsg = le.get(x).toString().replace(nextMsgTokens[0], "");
			logger.debug("Next Message ["+x+"] is: "+nextMsg);
			
			if (Decorator.isDeletedLine(nextMsg))
			{
				logger.debug("Next Message ["+x+"] is an old deleted line, so skipping it!");
				continue;
			}
			
			String msgType = le.get(x).getClass().getSimpleName();
			
			switch (msgType)
			{
				
				case "GroupingStart": // IMPORTANT NOTE: The current logic DOES NOT support recursive if/else conditions!.

					logger.debug("Within an IF!!!");
					appProg.setIfPos();
					
					break;
					
				case "GroupingLeaf":
					
					if (appProg.isWithinIf())
					{
						logger.debug("Within an ELSE!!!");
						appProg.setElsePos();
						
						if (appProg.isPointcutAchieved() && iType.equals(InjectionType.AFTER_SUCCESS))
						{
							logger.debug("Applying pattern before closing the if!!!");
							x+=subPat.getAdvice().apply(diagramWrap, appProg);
						}
					}
					else if (appProg.isWithinElse())
					{
						logger.debug("Closing an IF!!!");
						appProg.setNonePos();
						
						if (appProg.isPointcutAchieved() && iType.equals(InjectionType.AFTER_FAILURE))
						{
							logger.debug("Applying pattern before closing the else!!!");
							x+=subPat.getAdvice().apply(diagramWrap, appProg);
						}
					}
					
					break;
					
				case "Message":
					
					//If the pointcut has already been achieved, we have to move until the end of the IF/ELSE to add the pattern text.
					//(this is ONLY applicable for the AFTER SUCCESS/FAILURE injection types!, as the NOW is applied immediately!)
					if (!appProg.isPointcutAchieved())
					{
						Message m = (Message) le.get(x);
						
						//Check first that the message contains personal data.
						if (isRelevantPersonalDataInMessage(m.getLabel().toString()))
						{
							logger.debug("(1/3) The message #["+x+"] contains in-scope personal data ["+m.getLabel().toString()+"].");
							
							//Then, check if the message involves the relevant actors. 
							//IMPORTANT NOTE: Not checking exactly which of the relevant actors are involved!!!
							if (areRelevantActorsInMessage(m))
							{
								logger.debug("(2/3) The message #["+x+"] involves the relevant actors! ["+m.getParticipant1().getCode()+"] "
										+ "and ["+m.getParticipant2().getCode()+"]");
								
								if (isRelevantOperationInMessage(m.getLabel().toString()))
								{
									logger.debug("(3/3) The message #["+x+"] involves a relevant operation! ["+m.getLabel().toString()+"] ");
									
									appProg.considerOperation(x, getMessageRawLine(diagramWrap, x, nextMsg));
									
									if (appProg.hasRequiredOpsAchieved(numReqOps))
									{
										//Apply change!; IMPORTANT NOTE: Currently not considering recursive IF/ELSE logic.
										logger.debug("APPLYING CHANGE!!! (as # of required operations achieved!!! ["+appProg.getCurrentOpsNumber()+" out of "+numReqOps+"]) ");
										appProg.setPointcutAchieved();
										
										//OPD.- Dec 21, 2018: Adding the support to the "InjectionType.NOW"
										if (iType.equals(InjectionType.NOW))
										{
											logger.debug("Applying pattern NOW (i.e., not considering IF/ELSE criteria)!!!");
											x+=subPat.getAdvice().apply(diagramWrap, appProg);
										}//if (iType.equals(InjectionType.NOW))
										
									}//appProg.hasRequiredOpsAchieved(numberOfRequiredOperations)
								
								}//isRelevantOperationInMessage
							
							}//areRelevantActorsInMessage
						
						}//isRelevantPersonalDataInMessage
						
					}//if (!appProg.pointcutAchieved)
					
					break;
					
				default:
					
					logger.error("Event not currently considered:"+le.get(x).toString());
					
					break;
			}// switch (msgType)
			
		}// for (int x=0; x<le.size(); x++)

		return appProg.isPatternApplied();
	}

	//OPD.- Feb5, 2019: Ugly patch to try to fix the gap in the number of the message ... unknown cause!!!!
	// 					As of now, issue happened on files: EditWrittenEvaluation
	public String getMessageRawLine(PlantUMLWrapper diagramWrap, int msgIndex, String msgText)
	{
		String rawLine, a1;
		boolean correctLine = false;
		int c=0;
		do{
			//OPD.-Feb 4,2019: Adding "getNumberOfParticipantLines" to consider the participants at insertion time!
			rawLine = diagramWrap.getRawUmlData().get(msgIndex+1+diagramWrap.getNumberOfNonMsgLines()+c);
			a1 = msgText.replaceAll(">","").replaceAll("<","").split("-")[0];
			c++;
			correctLine = rawLine.trim().toLowerCase().contains(a1.trim().toLowerCase());
			if (!correctLine)
			{
				logger.debug("Unexpected line found: ["+c+"], correctLine["+correctLine+"], rawLine["+rawLine+"], a1["+a1+"]");				
			}
		}while (!correctLine);
		
		return rawLine;
	}
	
	public int getNumReqOps() {
		return numReqOps;
	}
	public String[] getActors_original() {
		return actors_original;
	}
	public String[] getOps_original() {
		return ops_original;
	}
	public String[] getPdi_original() {
		return pdata_original;
	}
}
