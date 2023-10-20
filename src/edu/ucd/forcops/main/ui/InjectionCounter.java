package edu.ucd.forcops.main.ui;

import java.util.HashMap;
import java.util.Map;

import edu.ucd.forcops.main.PrivatePattern;

public class InjectionCounter {

	public static Map<String,Integer> injectionsPerType = new HashMap<>();
	
	public static String currentControl = null;
	
	public static void countInjection()
	{
		//Check if the map element exists.
		if (injectionsPerType.containsKey(currentControl))
		{
			//If it does exist, add one.
			Integer currentValue = injectionsPerType.get(currentControl);
			injectionsPerType.put(currentControl, currentValue+1);
		}
		else
		{
			//If it does not exist, create it and initialize it to one.
			injectionsPerType.put(currentControl, 1);
		}
	}
	
	//This methods returns the TOTAL pointcuts, so it needs to be divided by the number of pointcuts configured 
	//Thus, a similar/better/more accurate method has been created in the KB class!
	public static void printCountInjections()
	{		
		for (Map.Entry<String, Integer> set:InjectionCounter.injectionsPerType.entrySet()) {
           // Printing all elements of a Map
           System.out.println(set.getKey() + " = "+ set.getValue());
       }
	}
}
