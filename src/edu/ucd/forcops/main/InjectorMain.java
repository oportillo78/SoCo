package edu.ucd.forcops.main;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class InjectorMain 
{
	private static final Logger logger = LogManager.getLogger("PrivSecCtrl-Injector");
		
	public static void main(String args[]) throws Exception
	{
		String configFilename;		
		//================================================================
		//configFilename = "./configurations/uniXYZ.properties";
		//configFilename = "./configurations/fenix.properties";
		//configFilename = "./configurations/fenix_ext.properties";
		configFilename = "./configurations/fenix_v2.properties";
		//================================================================
		if (args.length>=1)		configFilename = args[0];
		
		
		PropertiesConfiguration config = MainUtils.readConfigFile(configFilename);
		
		String[] debugModes = config.getStringArray("debugModes");
		
		String tpdiFilename = config.getString("tpdiFilename");
		String tpoFilename = config.getString("tpoFilename");		
		String ncDiagramsFilename = config.getString("ncDiagramsFilename");
		
		String tokensFilename = config.getString("tokensFilename");	
		String rulesetFilename = config.getString("rulesetFilename");
		
		//================================================================

		MainUtils.processInjectionBatch(debugModes, tpoFilename, tpdiFilename, 
				ncDiagramsFilename, rulesetFilename, tokensFilename);
	}

}
