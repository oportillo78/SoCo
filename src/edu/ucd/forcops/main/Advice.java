package edu.ucd.forcops.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.sourceforge.plantuml.sequencediagram.Message;

public class Advice 
{
	protected static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	private AdviceType aType;

	private String[] templateAdvice;
	private String[] templateTokens;
	
	public static final ArrayList<String> reservedTokens = new ArrayList<>(Arrays.asList("$FIRSTACTOR1", "$FIRSTACTOR2", 
		"$LASTACTOR1", "$LASTACTOR2", "$LASTACTION", "$LASTPDATA", "$FIRSTMSG", "$LASTMSG", "$ALLMSG"));

	public Advice(AdviceType _aType, String[] _templateAdvice, String[] _templateTokens)
	{	
		aType = _aType;
		templateAdvice = _templateAdvice;
		templateTokens = _templateTokens;
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("An advice of type ["+aType.getAdviceType()+"] with the instructions:\n");
		for (String next: templateAdvice)
		{
			sb.append("\t"+next+"\n");			
		}
		//sb.append("with "+templateTokens.length+" required template tokens (i.e., "+Arrays.toString(templateTokens)+").");
		
		return sb.toString();
	}
	
	public String[] getTemplateTokens()
	{
		return templateTokens;
	}
	
	public String[] getTemplateAdvice()
	{
		return templateAdvice;
	}
	
	public String[] contextualizeAdvice(String[] advice, ApplicabilityProgress appProg, PlantUMLWrapper diagramWrap) throws Exception
	{
		//String[] contextualizedAdvice = new String[advice.length];
		
		//First, set/get the values of all the supported tokens/placeholders (i.e., pre-defined + domain specifics).
		Map<String,String> tokenValues = getContextTokens(appProg, diagramWrap);

		//Then, iterate the advice text to replace all (potentially applicable) tokens!
		for (int x=0; x<advice.length; x++)
		{
			for(Map.Entry<String, String> nToken : tokenValues.entrySet()) 
			{
		    	//logger.debug("Next advice line is: "+advice[x]+", and token is ["+nToken.getKey()+"|"+nToken.getValue()+"]");
		    	advice[x] = advice[x].replace(nToken.getKey(), nToken.getValue());
			}
			
			if (advice[x].contains("$"))
			{
				MainUtils.reportFatalError("After contextualizing the advice, there are some undefined tokens/placeholders in line ["+
											advice[x]+ "]. Halting execution, as it will crash PlantUML API!!!\n");
			}
		}	
		
		//Finally, return the updated advice!
		return advice;
	}
	
	public Map<String,String> getContextTokens(ApplicabilityProgress appProg, PlantUMLWrapper diagramWrap)
	{
		Map<String,String> tokenValues = new HashMap<String,String>();

		//String listString = String.join("\n ", appProg.currentOpsText);
		//String lastline = appProg.currentOpsText.get(appProg.currentOpsText.size()-1);
		
		//Firstly, set the values of all the pre-defined tokens!
		//================================================================================================================
		Message firstMsg = (Message) appProg.context.le.get(0);	//***
		tokenValues.put("$FIRSTACTOR1", firstMsg.getParticipant1().getCode()); 	//By using "getCode()", we only support short names!!!
		tokenValues.put("$FIRSTACTOR2", firstMsg.getParticipant2().getCode());
		
		Message lastMsg = (Message) appProg.context.le.get(appProg.context.le.size()-1);
		tokenValues.put("$LASTACTOR1", lastMsg.getParticipant1().getCode());	//By using "getCode()", we only support short names!!!
		tokenValues.put("$LASTACTOR2", lastMsg.getParticipant2().getCode());
		
		//OPD.- Feb9, 2019: Adding two new tokens: $LASTACTION and $LASTPDATA
		String[] lastMsgTokens = lastMsg.getLabel().toString().split("\\("); 
		String lastMethodFirm= lastMsgTokens[0].replace("[", "").replace("]", "").replaceAll(",", "");
		int indexOfUpperLetter = Advice.getIndexOfFirstUpperCase(lastMethodFirm);
		if (indexOfUpperLetter!=-1)
		{
			tokenValues.put("$LASTACTION", lastMethodFirm.substring(0, indexOfUpperLetter));
			
			//OPD.- Feb9, 2019: Adding backup logic to check the params of the method IF the method names does not contain a PD item.
			String pdata = lastMethodFirm.substring(indexOfUpperLetter);
			if (!Dictionary.isPdItem(pdata))
			{
				String lastMethodParams = (lastMsgTokens.length>1)?lastMsgTokens[1].replace("]", ""):"";
				pdata = Dictionary.getFirstMatchInPdItems(lastMethodParams);
			}
			tokenValues.put("$LASTPDATA", pdata);
		}
		else
		{
			tokenValues.put("$LASTACTION", lastMethodFirm);
			tokenValues.put("$LASTPDATA", "");
		}

		tokenValues.put("$FIRSTMSG",
				Decorator.decoreateTemplateLine(appProg.currentOpsText.get(0), Decorator.getAddColor()));
		tokenValues.put("$LASTMSG",
				Decorator.decoreateTemplateLine(appProg.currentOpsText.get(appProg.currentOpsText.size()-1), 
						Decorator.getAddColor()));
		tokenValues.put("$ALLMSG", String.join("\n ", 
				Decorator.decorateTemplateAdvice(appProg.currentOpsText.toArray(new String[0]))));
		
		//Secondly, set the values of all the domain specific tokens!
		String[] domTokens = Dictionary.getActorTokens();
		List<String> domPossibleTokenValues = null;

		for (int x=0;x<domTokens.length;x++)
		{
			//By using "removeActorsPackages()", we only support short names!!!
			domPossibleTokenValues = Dictionary.removeActorsPackages(Dictionary.getActorSynonymsByKey(domTokens[x], false));
			
			//ABCDE - To do: Support regular expressions ...
			//The "first level" of context is the message itself.
			for (int ptv=0; ptv<domPossibleTokenValues.size(); ptv++)
			{
				if (domPossibleTokenValues.get(ptv).equalsIgnoreCase(lastMsg.getParticipant1().getCode()) || 
						domPossibleTokenValues.get(ptv).equalsIgnoreCase(lastMsg.getParticipant2().getCode()))
				{
					tokenValues.put(domTokens[x], domPossibleTokenValues.get(ptv));
					break;
				}
			}

			//OPD.-Feb7, 2019: Adding the logic to support the second level of context.
			//The "second level" of context is the set of available actors in the diagram! 
			//(ONLY used if the key was not in the 1st level of context) + As of now, returns the FIRST coincidence!!!
			//Not sure how useful in its current form, but it seems to be required by the [Consent Provided] pattern.
			//if (!tokenValues.containsKey(domTokens[x]))
			if (!tokenValues.keySet().stream().anyMatch(domTokens[x]::equalsIgnoreCase))
			{
				List<String> extendedContext = Dictionary.removeActorsPackages(diagramWrap.getOriginalActorSet());
				for (int ec=0; ec<extendedContext.size(); ec++)
				{
					if (domPossibleTokenValues.stream().anyMatch(extendedContext.get(ec)::equalsIgnoreCase))
					{
						tokenValues.put(domTokens[x], extendedContext.get(ec));
						break;
					}
				}
			}
		}

		return tokenValues;
	}
	
	//IMPORTANT NOTE: Not considering the tokens yet! (e.g., hardcoding the SIS in the first example)
	public int apply(PlantUMLWrapper diagramWrap, ApplicabilityProgress appProg) throws Exception
	{	
		//OPD.- Feb5, 2019: An undesired consequence of fixing the Event list (that did not self-update correctly), 
		//					Now, some patterns tend to fall within a never-ending loop. The return of this method should fix it,
		//					by letting the logic know the number of lines to be skip (i.e., line additions minus line removals) 
		int numberOfLinesToSkip = 0;
		
		// Adding logic to adjust the insertion point to conside ANY "participant" lines existing in the RawText.
		int numberOfNonMsgLines = diagramWrap.getNumberOfNonMsgLines();
		
		// +1 is needed to adjust the event list with the rawdata, +1 to start AFTER the current line (unless "abovePointcutStart").
		int injectPointTop = appProg.currentOps.get(0)+1+1+numberOfNonMsgLines;
	
		// +1 is needed to consider the "@startuml" in the plain text, +1 to start AFTER the current line.
		int injectPointBottom = appProg.currentOps.get(appProg.currentOps.size()-1)+1+1+numberOfNonMsgLines;
		
		String[] decTemplateAdvice = Decorator.decorateTemplateAdvice(templateAdvice);
		
		numberOfLinesToSkip+=decTemplateAdvice.length;
		
		switch (aType)
		{

			case ADD_BEFORE:

				injectPointTop--;
				//pWrap.addRawUmlData(decTemplateAdvice, injectPointTop);
				addNewText(diagramWrap, decTemplateAdvice, appProg, injectPointTop);
			
			break;
		
			case ADD_AFTER:
				
				//pWrap.addRawUmlData(decTemplateAdvice, injectPointBottom);
				addNewText(diagramWrap, decTemplateAdvice, appProg, injectPointBottom);
				
				break;
				
			case WRAP_AROUND:
				
				boolean abovePointcutStart = decTemplateAdvice[0].toLowerCase().contains("alt");	//To know if the first line belongs to the if (or not!) 
				int endOfTopPart = identifyEndOfTopPartInTemplate();				//To know which parts of the template belong to the if and the else!
				
				//Once the "endOfTopPart" has been identified, the template advice is cut in two in preparation for their insertion.
				String[] decTemplateAdviceTop = new String[endOfTopPart+1];
				String[] decTemplateAdviceBottom = new String[decTemplateAdvice.length-(endOfTopPart+1)];
				//				(src   , src-offset  , dest , offset, count)
				System.arraycopy(decTemplateAdvice, 0 , decTemplateAdviceTop, 0, decTemplateAdviceTop.length);
				System.arraycopy(decTemplateAdvice, decTemplateAdviceTop.length, decTemplateAdviceBottom, 0, decTemplateAdviceBottom.length);

				//pWrap.addRawUmlData(decTemplateAdviceBottom, injectPointBottom);
				addNewText(diagramWrap, decTemplateAdviceBottom, appProg, injectPointBottom);
				
				if (abovePointcutStart) injectPointTop--;
				//pWrap.addRawUmlData(decTemplateAdviceTop, injectPointTop);
				addNewText(diagramWrap, decTemplateAdviceTop, appProg, injectPointTop);
				
				break;

			case REPLACE_ALL:
				
				//OPD.- Dec 21, 2018: Highlight the old text in red (i.e., the WHOLE pointcut).
				highlightOldText(diagramWrap, appProg.currentOps);
			
				// Then, add/decorate the new text (as we are removing EVERYTHING, it is ok to add at position x).
				//pWrap.addRawUmlData(decTemplateAdvice, injectPointBottom);
				addNewText(diagramWrap, decTemplateAdvice, appProg, injectPointBottom);

				//OPD.- Dec 21, 2018: Logic to remove the old text (if debug mode off).
				if (!KnowledgeBase.isDebugMode())	{	numberOfLinesToSkip-=deleteOldText(diagramWrap);	}
				
				break;
				
			case REPLACE_BETWEEN:

				//First, we need to identify the "middle" of the pointcut.
				//IMPORTANT NOTE: For the time being, not considering any special logic for cases where the 
				//				  template size is impair (in that case, the regular round down would apply).
				//IMPORTANT NOTE 2: As the "middlepoint" vars are used to read events, they do not consider the "+1" yet.
				int middlepoint1 = appProg.currentOps.get(appProg.currentOps.size()/2-1);
				int middlepoint2 = appProg.currentOps.get(appProg.currentOps.size()/2+1-1);
		
				//Then, generate a set involving all the lines that will be replaced.
				//IMPORTANT NOTE: The below logic will (ALMOST!) certainly fail whenever there are "participant" elements explicitly stated!!! (or blank lines hanging around)
				List<Integer> toBeDeletedOps = new ArrayList<Integer>();
				for (int x=middlepoint1+1;x<middlepoint2; x++)
				{
					toBeDeletedOps.add(x);
				}
				
				//OPD.- Dec 30, 2018: Highlight the old text in red (i.e., the WHOLE pointcut).
				highlightOldText(diagramWrap, toBeDeletedOps);
			
				// Then, add/decorate the new text (as we are removing EVERYTHING, it is ok to add at position x).
				//pWrap.addRawUmlData(decTemplateAdvice, middlepoint2+1);	//Here, we add "+1" to convert the index to the rawtext!
				addNewText(diagramWrap, decTemplateAdvice, appProg, middlepoint2+1+diagramWrap.getNumberOfNonMsgLines());
				
				//OPD.- Dec 21, 2018: Logic to remove the old text (if debug mode off).
				if (!KnowledgeBase.isDebugMode())	{	numberOfLinesToSkip-=deleteOldText(diagramWrap);	}
				
				break;
				
			case ADD_BETWEEN:

				//First, we need to identify the "middle" of the pointcut.
				//IMPORTANT NOTE: For the time being, not considering any special logic for cases where the 
				//				  template size is impair (in that case, the regular round down would apply).
				//IMPORTANT NOTE 2: As the "middlepoint" vars are used to read events, they do not consider the "+1" yet.
				int middlepoint = appProg.currentOps.get(appProg.currentOps.size()/2);
					
				// Then, add/decorate the new text (as we are removing EVERYTHING, it is ok to add at position x).
				//pWrap.addRawUmlData(decTemplateAdvice, middlepoint2+1);	//Here, we add "+1" to convert the index to the rawtext!
				addNewText(diagramWrap, decTemplateAdvice, appProg, middlepoint+1+diagramWrap.getNumberOfNonMsgLines());
				
				break;
				
		default:
				logger.error("Unsupported AdviceType!!!");
			break;

		}
	
		// Re-process the diagram (to update all variables)
		diagramWrap.updateUmlItemBasedData(diagramWrap.getRawUmlData());
		
		// Saving the variables to indicate that the PC has passed + the PP was successful.
		appProg.setPatternApplied();
		appProg.clearPointCutAchieved();
		
		return numberOfLinesToSkip;
	}
	
	//OPD.- Dec 29, 2018: This method MUST ONLY be called when using "WRAP_AROUND".
	public int identifyEndOfTopPartInTemplate()
	{
		int result = -1;
		
		// First, we look for the "else".
		for (int x=0; x<templateAdvice.length;x++)
		{
			if (templateAdvice[x].toLowerCase().contains("else")) 
			{
				result = x;
				break;
			}
		}
		
		//If the "else" was not found, we search for "end".
		if (result == -1)
		{
			for (int x=0; x<templateAdvice.length;x++)
			{
				if (templateAdvice[x].toLowerCase().contains("end"))
				{
					result = x;
					break;
				}
			}			
		}
		return result-1;
	}
	
	protected void highlightOldText(PlantUMLWrapper diagramWrap, List<Integer> operationNumbers)
	{
		String nextText = null;
		
		for (Integer nextLine: operationNumbers)
		{
			// Adding 1 to adjust the UML info to the raw data! (+ # of non-message lines!)
			nextText = diagramWrap.getRawUmlData().get(nextLine+1+diagramWrap.getNumberOfNonMsgLines());
			nextText = Decorator.decoreateTemplateLine(nextText, Decorator.getDelColor());
			diagramWrap.replaceRawUmlData(nextText, nextLine+1+diagramWrap.getNumberOfNonMsgLines());
		}
	}
	
	protected void addNewText(PlantUMLWrapper diagramWrap, String[] advice, ApplicabilityProgress ap, int index) throws Exception
	{	
		logger.debug("About to insert the advice ["+Arrays.toString(advice)+"] at index ["+index+"]");
		String[] cAdvice = contextualizeAdvice(advice, ap, diagramWrap);
		diagramWrap.addRawUmlData(cAdvice, index);
	}
	
	protected int deleteOldText(PlantUMLWrapper diagramWrap)
	{
		List<String> rawUmlData = diagramWrap.getRawUmlData();
		int rawUmlSize = rawUmlData.size();
		int numberOfDeletedLines = 0;
		
		for (int y=rawUmlSize-1;y>=0;y--)
		{
			String text = rawUmlData.get(y);
			if (text.toLowerCase().contains("<color "+Decorator.getDelColor()+">") || 
					text.toLowerCase().contains("<font color="+Decorator.getDelColor()+">"))
			{
				logger.debug("About to delete "+text);
				diagramWrap.delRawUmlData(y);
				numberOfDeletedLines++;
			}
		}
		return numberOfDeletedLines;
	}
	
	public static int getIndexOfFirstUpperCase(String msg) 
	{ 
	    for (int i = 0; i < msg.length(); i++) 
	        if (Character.isUpperCase(msg.charAt(i)))
	        {
	            return i;	        	
	        }
	    return -1; 
	} 
	
	public AdviceType getaType() {
		return aType;
	}
}
