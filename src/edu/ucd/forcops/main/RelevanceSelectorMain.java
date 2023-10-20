package edu.ucd.forcops.main;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class RelevanceSelectorMain {

	private static final Logger logger = LogManager.getLogger("PrivSecCtrl-RelevanceSelector");

	public static void main(String[] args) throws Exception 
	{
		String configFilename;
		
		//================================================================
		configFilename = "./configurations/fenix_v2.properties";
		//================================================================
		if (args.length>=1)		configFilename = args[0];
		
		PropertiesConfiguration config = MainUtils.readConfigFile(configFilename);

		String bpdiFilename = config.getString("bpdiFilename");
		String bpoFilename = config.getString("bpoFilename");		
		String allDiagramsFilename = config.getString("allDiagramsFilename");

		String word2vecCorpus = config.getString("word2vecCorpus");
		int numberOfSynonyms = config.getInt("numberOfSynonyms");
		
		String generateImages = config.getString("generateImages");
		
		MainUtils.processRelevanceSelectionBatch(allDiagramsFilename, bpdiFilename, bpoFilename, 
				word2vecCorpus, numberOfSynonyms, generateImages);
	}

}
