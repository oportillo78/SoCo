package edu.ucd.forcops.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Dictionary 
{
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	private static Map<String,String[]> op_syn = new HashMap<String,String[]>();
	private static Map<String,String[]> act_syn = new HashMap<String,String[]>();
	
	//OPD.-Jun25,2020: Changing the PDI logic to support synonyms!
	private static Map<String,String[]> pdi_syn = new HashMap<String,String[]>();
	private static Set<String> pdiExtendedSet = new HashSet<String>(); //OPD.-Jun25,2020: (Temporarily) keeping the set to make it faster the access of all values (only calculated once!, but used in many places!) 
	
	public static void loadProcessingOperations(String filename) throws Exception
	{
		loadContextKeywords("ProcessingOperations", op_syn, filename);
	}
	
	public static void loadDomainTokens(String filename) throws Exception
	{
		loadContextKeywords("DomainTokens", act_syn, filename);
	}
	
	public static void loadPersonalDataItems(String filename) throws Exception
	{
		//pdItemSet.addAll(loadInfo(filename, "dictionary[PersonalDataItems]",","));
		loadContextKeywords("PersonalDataItems", pdi_syn, filename);
		
		//OPD.-Jun25,2020: (Temporarily) keeping this data as a set to make it faster the access of all values (only calculated once!, but used in many places!) 
		for (String[] nextPdi : pdi_syn.values())
		{
			pdiExtendedSet.addAll(Arrays.asList(nextPdi));
		}
	}
	
	private static void loadContextKeywords(String name, Map<String,String[]> dicObj, String filename) throws Exception
	{
		try 
		{
			List<String> inputLines = FileUtils.readLines(new File(filename));
			
			for (int x=0; x<inputLines.size(); x++)
			{
				String[] nextLine = inputLines.get(x).trim().split(",");

				//OPD.-Jul12,2020: From the time being, ASSUMING it is enough to ONLY convert the keys to lowercase (instead of also doing it to the values!).
				String key = nextLine[0].toLowerCase();
				String[] values = new String[nextLine.length-1];
				System.arraycopy(nextLine, 1 , values, 0, values.length);
				logger.debug("Next dictionary["+name+"] item is: ["+key+"|"+Arrays.toString(values)+"]");
				
				dicObj.put(key, values);
			}
			
		}
		catch(Exception e)
		{
			MainUtils.reportFatalError("Error when trying to load the Dictionary item: ["+name+ 
					"]; description: ["+e.getMessage()+"] ...\n"+"Thus, stopping the program execution!!!\n");
		}		
	}
		
	public static boolean isValidOperation(String opsname)
	{
		return op_syn.keySet().stream().anyMatch(opsname::equalsIgnoreCase);
	}
	
	public static String[] getOperationSynonyms(String opsname)
	{
		return op_syn.get(opsname.toLowerCase());
	}
	
	//OPD.- Dec 15, 2018: From the time being, blindly returning the name of the actor as its whole list of synonyms.
	//OPD.- Dec 18, 2018: Small patch to "consider" the special cases of Browser/Staff/Visitor.
	//OPD.- Feb2, 2019: Adding the support of the fenix service and domain aliases.
	public static Set<String> getActorSynonymsByValue(String actname)
	{
		Set<String> sactors = new HashSet<String>();

		//System.out.println("actname->"+actname);
		
		for (Map.Entry<String,String[]> entry : act_syn.entrySet()) 
		{
		    //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			sactors.addAll(Arrays.asList(entry.getValue()));
			if (sactors.stream().anyMatch(actname::equalsIgnoreCase))
			{
				break;
			}
			else
			{
				sactors.clear();
			}
		}

		sactors.add(actname);
		return sactors;
	}
	
	public static Set<String> getActorSynonymsByKey(String actname, boolean returnKey)
	{
		Set<String> sactors = new HashSet<String>();
		
		if (act_syn.keySet().stream().anyMatch(actname::equalsIgnoreCase))
		{
			sactors.addAll(Arrays.asList(act_syn.get(actname.toLowerCase())));
		}
		
		if (returnKey)
		{
			sactors.add(actname);
		}
		
		return sactors;
	}
	
	public static String[] getActorTokens()
	{
		Set<String> keys = new HashSet<String>();
		keys.addAll(act_syn.keySet());
		keys.removeIf(n -> (n.charAt(0) != '$'));
		return keys.toArray(new String[0]);
	}

	//OPD.-Feb4,2019: Temporarily only considering the scenario that the long name is a package,
	// 					so it is enough to remove EVERYTHING before the last dot!
	public static List<String> removeActorsPackages(List<String> actorsFullnames)
	{
		List<String> pactors = new ArrayList<String>();
		
		for (String actor: actorsFullnames)
		{
			int endOfPackage = actor.lastIndexOf(".");
			if (endOfPackage == -1)
			{
				pactors.add(actor);
			}
			else
			{
				pactors.add(actor.substring(endOfPackage+1));
			}
		}
		
		return pactors;
	}
	
	public static List<String> removeActorsPackages(Set<String> actorsFullnames)
	{
		List<String> pactors = new ArrayList<String>();
		pactors.addAll(actorsFullnames);
		return removeActorsPackages(pactors);
	}

	//=================================================
	
	public static Set<String> getExtendedPdItems()
	{
		return pdiExtendedSet;
	}
	
	public static Set<String> getKeyPdItems()
	{
		return pdi_syn.keySet();
	}
	
	public static String[] getPdiSynonyms(String pdiname)
	{
		return pdi_syn.get(pdiname.toLowerCase());
	}
	
	public static boolean isPdItem(String item)
	{
		return pdiExtendedSet.stream().anyMatch(item.trim()::equalsIgnoreCase);
	}
	
	public static String getFirstMatchInPdItems(String item)
	{
		for (String pdata: pdiExtendedSet)
		{
			if (item.toLowerCase().contains(pdata.toLowerCase()))
			{
				return pdata;
			}
		}
		
		return "";
	}
}
