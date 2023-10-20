package edu.ucd.forcops.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.ucd.forcops.main.ui.InjectionCounter;

public class KnowledgeBase 
{
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	private static List<PrivatePattern> patterns;
	private static boolean debugMode = false;
	
	public static void initialize(String rulesetFilename) throws Exception
	{		
		loadPrivacyPatterns(rulesetFilename);

		//OPD.- Jul9, 2020: Validation no longer needed.
		/*
		if (!isInventoryFullyCovered())
		{
			MainUtils.reportFatalError("Inventory not fully covered by the privacy patterns! ... stopping the program!!!\n");
		}//*/
	}

	public static void setDebugMode(boolean _debugMode)
	{
		debugMode = _debugMode;
	}
	
	public static void loadPrivacyPatterns(String rulesetFilename) throws Exception
	{
		patterns = new ArrayList<PrivatePattern>();

		logger.debug("**********************************************");
		logger.debug("About to start loading the privacy patterns!!!");
		logger.debug("**********************************************");
		
		try 
		{
			List<String> inputLines = FileUtils.readLines(new File(rulesetFilename));
			
			// IMPORTANT: Currently not supporting tokens!
			// Also, assuming that a blank line indicates the end of a pattern!!!
			// (internally, it does not matter the order, EXCEPT that the pointcut 
			// MUST come BEFORE its advice!)
			String privPatName = null, description = null, newActors[] = null, sortedActors[] = null;
			List<PrivateSubPattern> subpats = new ArrayList<PrivateSubPattern>();
			String[] spActors = null, spOps = null, spPdata = null, spTempTokens= {};
			List<String> spTempAdvice = null;
			int spOpsPerActor = -1;
			InjectionType spInjType = null;
			AdviceType spAdvType = null;
			
			for (int x=0; x<inputLines.size(); x++)
			{
				String nextLine = inputLines.get(x).trim();
				
				if (nextLine.startsWith("#")) continue;
				
				String[] nLineTokens= nextLine.split("\\[");
				String key = nLineTokens[0].trim();
				String value = (nextLine.length()>0)?nextLine.substring(nLineTokens[0].length()+1):"";
							
				logger.debug("Next key/value is: ["+key+"|"+value+"]");
				
				switch(key)
				{
					case "The control":
						privPatName = value.replace("]", "");
						subpats.clear();
						break;
						
					case "With description":
						description = value.replace("]", "");
						break;
						
					case "With new actors":
						newActors = value.replace("]", "").split(",");
						if (newActors[0].equals("")) newActors = new String[0];
						else trimStringArray(newActors);
						break;
						
					case "With sorting":
						sortedActors = value.replace("]", "").split(",");
						if (sortedActors[0].equals("")) sortedActors = new String[0];
						else trimStringArray(sortedActors);
						break;
						
					case "A pointcut of type":
						String[] pcutTokens = value.split("\\]");
						spInjType = InjectionType.valueOf(pcutTokens[0].toUpperCase());
						spOpsPerActor = Integer.valueOf(pcutTokens[1].split("\\[")[1].trim());
						
						spOps = pcutTokens[2].split("\\[")[1].split(",");
						trimStringArray(spOps);
						spActors = pcutTokens[3].split("\\[")[1].split(",");
						trimStringArray(spActors);
						spPdata = pcutTokens[4].split("\\[")[1].split(",");
						trimStringArray(spPdata);
						break;

					//OPD.- Feb19,2019: Adding logic to support the case of NOT explicitly setting the injection type.
					//					If that happens, a NOW injection type is assummed!
					case "A pointcut composed of":
						spInjType = InjectionType.NOW;
						pcutTokens = value.split("\\]");
						spOpsPerActor = Integer.valueOf(pcutTokens[0]);
						
						//OPD.- Feb19,2019: For simplicity, copy/pasting the spOps/spActors/spPdata logic of the above case.
						//					(with the obvious -1 index). On hold trying to refactor to have a single logic instead of two!
						spOps = pcutTokens[1].split("\\[")[1].split(",");
						trimStringArray(spOps);
						spActors = pcutTokens[2].split("\\[")[1].split(",");
						trimStringArray(spActors);
						spPdata = pcutTokens[3].split("\\[")[1].split(",");
						trimStringArray(spPdata);
						break;
						
					case "An advice of type":
						String[] advTokens = value.split("\\]");
						spAdvType = AdviceType.valueOf(advTokens[0].toUpperCase());
						
						 spTempAdvice = new ArrayList<String>();
						 do
						 {
							 x++;
							 //OPD.- Feb9, 2019: Adding logic to skip those lines starting with #
							 if (!inputLines.get(x).startsWith("#"))
							 {
								 spTempAdvice.add(inputLines.get(x).replaceFirst("\t", ""));
							 }
						 } while (x+1<inputLines.size() && 
								 (!inputLines.get(x+1).trim().equals("") && !inputLines.get(x+1).trim().contains("A pointcut "))); //OPD.-Jul10,2020: To be consistent with the logic of this method, this contains remains (for the time being!) case sensitive!

						subpats.add(new PrivateSubPattern(spActors, spOps, spOpsPerActor, 
								spPdata, spInjType, spAdvType, spTempAdvice.toArray(new String[spTempAdvice.size()]), 
								spTempTokens));
						break;
						
					case "":
						if (privPatName != null)//To support blank spaces at the beginning
						{
							patterns.add(new PrivatePattern(privPatName, description, newActors,
								subpats.toArray(new PrivateSubPattern[subpats.size()]), sortedActors));
							logger.debug("New pattern added: "+patterns.get(patterns.size()-1).getName());
							privPatName = null;
						}
						while (x+1<inputLines.size() && inputLines.get(x+1).trim().equals(""))
						{
							x++;
						}
						break;
							
					default:
						logger.error("Unsupported Operation! ["+key+"]");
						break;
				}// end of "switch(key)"
			}// for (int x=0; x<inputLines.size(); x++)
			
			//Quick patch to avoid not adding the last pattern of the file in the case where there is no empty line at the end!
			//IMPORTANT: Assumming that it is enough to check the name!
			if (privPatName != null)
			{
				patterns.add(new PrivatePattern(privPatName, description, newActors,
						subpats.toArray(new PrivateSubPattern[subpats.size()]), sortedActors));
				logger.debug("New pattern added: "+patterns.get(patterns.size()-1).getName());
			}
			
		}//end of try
		catch(Exception e)
		{
			MainUtils.reportFatalError("Error when trying to load the Sec/Priv Ruleset: \n ["+e.getMessage()+"] ...\n"+
			"Thus, stopping the program execution!!!\n");
		}
		
		logger.debug("**********************************************");
		logger.debug("The loading of ["+patterns.size()+"] privacy pattern"+(patterns.size()>1?"s":"")+" finished!!!");
		logger.debug("**********************************************");
	}
	
	public static void trimStringArray(String[] values)
	{
		for (int x=0; x<values.length; x++)
		{
			values[x] = values[x].trim();
		}
	}
	
	public static boolean isDebugMode()
	{
		return debugMode;
	}
	
	public static void showSupportedPatterns()
	{
		logger.debug("=================================================================================");
		logger.debug("List of supported patterns:");
		
		for (int x=0 ; x<patterns.size(); x++)
		{
			logger.debug("Pattern #"+(x+1)+":\n"+patterns.get(x).toString());
		}
		
		logger.debug("=================================================================================");
	}
	
	public static String[] checkForApplicablePatterns(PlantUMLWrapper pWrap) throws Exception
	{
		List<String> appliedPats = new ArrayList<String>();
		
		for (PrivatePattern p: patterns)
		{
			//OPD Jan16, 2023: Tmp logic to calculate the applied control (probably the applied pointcuts)
			InjectionCounter.currentControl = p.getName();
			
			// assess eligiblity and apply the pattern
			if (p.assessApplicability(pWrap))
			{
				appliedPats.add(p.getName());
				logger.info("PrivacyPattern ["+p.getName()+"] applied to diagram");
			}
			else
			{
				logger.info("PrivacyPattern ["+p.getName()+"] WAS NOT applied to diagram");
			}
		}
		return appliedPats.toArray(new String[0]);
	}

	public static boolean isInventoryFullyCovered()
	{
		Set<String> pdataPrivPatt = new HashSet<String>();

		for (PrivatePattern p: patterns)
		{
			pdataPrivPatt.addAll(p.getKeyPdItems());
		}
		
		if (pdataPrivPatt.stream().anyMatch("All"::equalsIgnoreCase)) 
		{
			logger.info("All inventory elements are used by the KB privacy patterns!");
			return true;
		}
		else
		{
			for (String nextPdItem: Dictionary.getKeyPdItems())
			{
				if (!pdataPrivPatt.stream().anyMatch(nextPdItem::equalsIgnoreCase))
				{
					logger.error("The inventory keyword ["+nextPdItem+"] is not used by the privacy control ruleset!");
					return false;
				}
			}
		}
		
		logger.info("All inventory elements are (individually) used by the KB privacy patterns!");
		return true;
	}

	
	//Better than the method in InjectionCounter, but not sure what happens with those partially implemented!
	public static void printCountInjections()
	{
		System.out.println("SUMMARY OF TOTAL APPLIED CONTROLS");
		for (PrivatePattern p: patterns)
		{
			Integer pointcutsApplied = InjectionCounter.injectionsPerType.get(p.getName());
	        System.out.println(p.getName() + " = "+ pointcutsApplied/p.getSubpatterns().length);
		}		
	}
}
