package edu.ucd.forcops.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.ucd.forcops.main.ui.InjectionCounter;
import edu.ucd.forcops.main.ui.MainClient;
import edu.ucd.forcops.main.ui.MainScreenControllerv2;
import edu.ucd.forcops.main.ui.VisualResultsTO;
import javafx.application.Platform;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import net.sourceforge.plantuml.SourceStringReader;


public class MainUtils 
{
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	private static List<String> consolidatedResults;
	
	public static void initConsolidatedInjectionResults()
	{
		consolidatedResults = new ArrayList<String>();
		consolidatedResults.add("id, filename, debugMode, readTime, processTime, saveTime, execTime, appPats.length, appPats(|)");
	}
	
	public static void initConsolidatedRelevanceResults()
	{
		consolidatedResults = new ArrayList<String>();
		consolidatedResults.add("id, filename, word2vecTime, processTime, #bpos, #bpdi, relevant?, bpos(|), bpdi(|),");
	}
	
	public static void addConsolidatedResults(String nextResults)
	{
		consolidatedResults.add(nextResults);
	}
	
	public static double calcExecTimeInSec(double start, double stop)
	{
		return (stop-start)/1000;
	}
	
	public static String calcExecTime(long start, long stop)
	{
		long durationInMillis = stop - start;
		long millis = durationInMillis % 1000;
		long second = (durationInMillis / 1000) % 60;
		long minute = (durationInMillis / (1000 * 60)) % 60;
		long hour = (durationInMillis / (1000 * 60 * 60)) % 24;
		
		return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
	}
	
	//Method to format the collection as CSV because that is the file format expected by the dictionaries.
	public static String formatMapInfo(Map<String,Set<String>> info)
	{
		StringBuilder sb = new StringBuilder();
		
		for (Map.Entry<String,Set<String>> pair : info.entrySet()) 
		{
			if (sb.length()>0)
			{
				sb.append("\n");	
			}
			
			sb.append(pair.getKey()+",");
			
			//For each key, iterate its values. If ANY of them exists within the file, mark it (by adding the key in the set!) as existing!
			for (String nextValue: pair.getValue())
			{
				sb.append(nextValue+",");
			}			
		}
		return sb.toString();
	}

	//Method to format the lists of relevant diagrams as CSV (1 element per line).
	public static String formatSetInfo(List<String> info)
	{
		StringBuilder sb = new StringBuilder();
		
		for (String nextValue: info)
		{
			if (sb.length()>0)
			{
				sb.append("\n");	
			}
			
			sb.append(nextValue);			
		}
		
		return sb.toString();
	}

	
	public static String getSummaryResults()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("*********************************************************************************"+"\n");	
		sb.append("*********************************************************************************"+"\n");	
		sb.append("SUMMARY RESULTS"+"\n");
		sb.append("*********************************************************************************"+"\n");
		for (String nextLine: consolidatedResults)
		{
			sb.append(nextLine+"\n");
		}
		sb.append("*********************************************************************************"+"\n");	
		return sb.toString();
	}
	
	public static VisualResultsTO processNextFile(File inputXmlFile, int x) throws Exception
	{	
		logger.debug(x+".a) Read input file.");
		long startRead = System.currentTimeMillis();
		PlantUMLWrapper pWrap = PlantUMLHelper.read2(inputXmlFile);
		long stopRead = System.currentTimeMillis();
		double readTime = MainUtils.calcExecTimeInSec(startRead, stopRead);
		
		logger.debug(x+".b) Check applicable patterns.");
		long startProcess = System.currentTimeMillis();
		String[] appPats = KnowledgeBase.checkForApplicablePatterns(pWrap); //--> Main method (parsing, selection, etc.)
		long stopProcess = System.currentTimeMillis();
		double processTime = MainUtils.calcExecTimeInSec(startProcess, stopProcess);
		
		logger.debug(x+".c) Save output file.");
		long startSave = System.currentTimeMillis();
		PlantUMLHelper.save2(pWrap); //-> write the augmented diagram
		long stopSave = System.currentTimeMillis();
		double saveTime = MainUtils.calcExecTimeInSec(startSave, stopSave);

		MainUtils.addConsolidatedResults(
			x+","+inputXmlFile.getName()+","+KnowledgeBase.isDebugMode()+","+
			readTime+","+processTime+","+saveTime+","+(readTime+processTime+saveTime)+","+
			appPats.length+","+Arrays.toString(appPats).replaceAll(",", " |")
			);
		
		VisualResultsTO vrto = new VisualResultsTO(String.valueOf(x), inputXmlFile.getName(), inputXmlFile.getAbsolutePath(),
				KnowledgeBase.isDebugMode(), Arrays.toString(appPats), pWrap.getOption().getOutputFile().getName(), 
				pWrap.getOption().getOutputFile().getAbsolutePath());
		return vrto;
	}
	
	public static List<VisualResultsTO> processInjectionBatch(String[] debugModes, String poFilename, String pdiFilename,
			String inputFilesList, String rulesetFilename, String tokensFilename) 
					throws Exception
	{	
		List<VisualResultsTO> visualResults = new ArrayList<>();
		
		MainUtils.initConsolidatedInjectionResults();				//metrics gathered for this run
		
		Dictionary.loadProcessingOperations(poFilename);
		Dictionary.loadDomainTokens(tokensFilename);
		Dictionary.loadPersonalDataItems(pdiFilename);
		
		KnowledgeBase.initialize(rulesetFilename);
		KnowledgeBase.showSupportedPatterns();
		
		List<String> inputFiles = loadInfo(inputFilesList, "inputFiles", null);
		int fileId=0;
		
		for (String debugMode: debugModes)
		{
			KnowledgeBase.setDebugMode(Boolean.valueOf(debugMode));
			
			for (String nextFile: inputFiles)
			{
				reportInfo("===========================================");
				reportInfo("Evolution#"+(++fileId)+"(out of "+inputFiles.size()+"): File ["+nextFile+"] with debugMode ["+KnowledgeBase.isDebugMode()+"] ... ");
				File inputXmlFile = new File(nextFile);
				VisualResultsTO newResult = MainUtils.processNextFile(inputXmlFile, fileId);
				reportInfo("... sucessfully processed! The applied controls are:" + newResult.getAppliedControls().getValue());
				visualResults.add(newResult);
				//logger.info("=================================================================================");
			}
		}
				
		//OPD.-Jul5,2020: Start saving the results for easier analysis.
		saveResultsFile(inputFilesList.replace(".txt","_injectionResults.csv"),MainUtils.getSummaryResults());
		//logger.info(MainUtils.getSummaryResults());
		
		//OPD Jan16, 2023: Tmp logic to calculate the applied control (probably the applied pointcuts)
		KnowledgeBase.printCountInjections();
		
		/*
		//OPD:Dec14, 2022: Trying to clear the results that are shown in execLog4all and I think this does not qualify! 
		if (MainScreenControllerv2.execLog4all!=null)
		{
			MainScreenControllerv2.execLog4all.appendText(MainUtils.getSummaryResults());
		}*/
		
		return visualResults;
	}
	
	
	public static void processRelevanceSelectionBatch(String allDiagramsFilename, 
			String bpdiFilename, String bpoFilename, String word2vecCorpus, int numberOfSynonyms,
			String generateImages)
					throws Exception
	{	
		
		MainUtils.initConsolidatedRelevanceResults();								//metrics gathered for this run

		logger.debug("=================================================================================");
		logger.debug("word2vec preprocessing - start");
		logger.debug("=================================================================================");
		long startRead = System.currentTimeMillis();
		
		//First, get the list of diagrams, bpdis, and bpos from the config files.
		List<String> allDiagrams = loadInfo(allDiagramsFilename, "sqDiagrams", null);
		List<String> bpdis = loadInfo(bpdiFilename, "bpdi", null);
		List<String> bpos = loadInfo(bpoFilename, "bpos", null);
		
		logger.debug("size of allDiagrams:"+allDiagrams.size());
		logger.debug("size of bpdis:"+bpdis.size());
		logger.debug("size of bpos:"+bpos.size());

		//Next, use semantic logic (as of now word2vec) to get the synonyms for all business personal data items (bpdi) and business processing operations (bpo).
		// That information will be saved in Maps for futher usage.	
		Map<String,Set<String>> bpdiUniverseMap = word2vecFromList(bpdis, word2vecCorpus, numberOfSynonyms);
		Map<String,Set<String>> bpoUniverseMap = word2vecFromList(bpos, word2vecCorpus, numberOfSynonyms);

		logger.debug("=================================================================================");
		logger.debug("word2vec preprocessing - stop");
		logger.debug("=================================================================================");
		long stopRead = System.currentTimeMillis();
		double word2vecTime = MainUtils.calcExecTimeInSec(startRead, stopRead);
		
		//Sort the data to make it easier to visualize/analyze the results.
		Collections.sort(allDiagrams);
		
		int totalRelevant = 0;
		int totalIrrelevant = 0;
		int total = 1;
		
		Map<String,Set<String>> allRelevantBpdi = new TreeMap<>();
		Map<String,Set<String>> allRelevantBpo = new TreeMap<>();
		List<String> allRelevantDiagrams = new ArrayList<>();
		
		for (String nextFile: allDiagrams)
		{
			logger.debug("=================================================================================");
			logger.debug("["+nextFile+"] processing - start");
			logger.debug("=================================================================================");	
			long startProcess = System.currentTimeMillis();
			
			//Adding the below logic to support unix-style comments in the business input files!
			if (nextFile.startsWith("#"))	continue;
			
			//Set<String> relevantBpdi = checkRelevanceOfFileOLD(nextFile, bpdiMap);
			//Set<String> relevantBpo = checkRelevanceOfFileOLD(nextFile, bpoMap);
			Map<String,Set<String>> relevantBpdi = new TreeMap<>();
			Map<String,Set<String>> relevantBpo = new TreeMap<>();
			
			//if (!relevantBpdi.isEmpty() && !relevantBpo.isEmpty())
			if (checkRelevanceOfFile(nextFile, bpdiUniverseMap, bpoUniverseMap, relevantBpdi, relevantBpo, Boolean.valueOf(generateImages)))
			{
				//logger.info("File ["+nextFile+"] is relevant. #bpdi:["+relevantBpdi.size()+"], #bpo:["+relevantBpo.size()+"]");
				totalRelevant++;
				allRelevantDiagrams.add(nextFile);
				
				//Next, save the information in the allRelevant maps to use this consolidated info to create the tpdi and tpo dictionaries!
				// allRelevantBpdi + allRelevantBpo = technical data dictionary
				add2RelevanceMap(allRelevantBpdi, relevantBpdi);
				add2RelevanceMap(allRelevantBpo, relevantBpo);
			}
			else
			{
				//logger.info("File ["+nextFile+"] is NOT relevant");
				totalIrrelevant++;
			}

			logger.debug("=================================================================================");
			logger.debug("["+nextFile+"] processing - end");
			logger.debug("=================================================================================");
			long stopProcess = System.currentTimeMillis();
			double processTime = MainUtils.calcExecTimeInSec(startProcess, stopProcess);
			
			MainUtils.addConsolidatedResults(
					(total++)+","+nextFile+","+word2vecTime+","+processTime+","+relevantBpo.size()+","+relevantBpdi.size()+","+
					!(relevantBpo.isEmpty() || relevantBpdi.isEmpty())+","+relevantBpo.toString().replaceAll(",", " |")+","+relevantBpdi.toString().replaceAll(",", " |")
					);
		}

		logger.debug("=================================================================================");
		logger.debug("Total number of relevant SQ diagrams is ["+totalRelevant+"], while the number of irrelevant ones is ["+totalIrrelevant+"]");

		logger.debug("=================================================================================");
		logger.debug("Saving the technical dictionaries, as well as the list of relevant diagrams!");
		logger.debug("=================================================================================");
		//Finally, remove ALL items (in bpo and bpdi) that were not used (so no technically relevant for this application). These prunned versions will become the tpdi and tpo dictionaries.
		//So, save them! (for the time being, just replacing the bpo and bpdi names (ASSUMPTION) with tpo and tpdi in the SAME location.
		//logger.debug("size of bpdiMap:["+bpdiUniverseMap.size()+"], size of bpoMap: ["+bpoUniverseMap.size()+"] - BEFORE");
		bpdiUniverseMap.keySet().retainAll(allRelevantBpdi.keySet());
		bpoUniverseMap.keySet().retainAll(allRelevantBpo.keySet());
		//logger.debug("size of bpdiMap:["+bpdiUniverseMap.size()+"], size of bpoMap: ["+bpoUniverseMap.size()+"] - AFTER");

		//Saving the initial versions of the technical dictionaries (e.g., business data inventory + all their synonyms regardless of their actual usage).
		saveDictionary(bpdiFilename.replace("bpdItems", "bpdItems_allsyn"), bpdiUniverseMap);
		saveDictionary(bpoFilename.replace("bprocOps", "bprocOps_allsyn"), bpoUniverseMap);
		
		//Saving the pruned technical dictionaries (i.e., only the synonyms that are actually used).
		saveDictionary(bpdiFilename.replace("bpdItems", "tpdItems"), allRelevantBpdi);
		saveDictionary(bpoFilename.replace("bprocOps", "tprocOps"), allRelevantBpo);

		//Saving the relevant scenarios (ncdiagrams -> should be reldiagrams, in this case we assume all relevant are also non compliant)
		saveResultsFile(allDiagramsFilename.replace("alldiagrams", "ncdiagrams"),formatSetInfo(allRelevantDiagrams));
		saveResultsFile(allDiagramsFilename.replace(".txt","_relevanceSelectionResults.csv"),MainUtils.getSummaryResults());	
		//logger.info(MainUtils.getSummaryResults());
	}
	
	public static void saveDictionary(String filename, Map<String,Set<String>> info)
	{
		String text = formatMapInfo(info);
		try
		{
			FileUtils.writeStringToFile(new File(filename), text, "utf-8");
		} 
		catch (IOException e) 
		{
			logger.error("Error when trying to save the dictionary in file ["+filename+"]. Descr:"+e.getMessage());
		}
	}
	
	public static void saveResultsFile (String filename, String text)
	{
		try
		{
			FileUtils.writeStringToFile(new File(filename), text, "utf-8");
		} 
		catch (IOException e) 
		{
			logger.error("Error when trying to save the file ["+filename+"]. Descr:"+e.getMessage());
		}
	}
	
	public static boolean checkRelevanceOfFile(String filename, Map<String,Set<String>> relevantBpdi, Map<String,Set<String>> relevantBpo, 
			Map<String,Set<String>> fileRelevantBpdi, Map<String,Set<String>> fileRelevantBpo, boolean generateImages)
	{
		boolean relevantFile = false;
		
		//String sqDiagramBody;
		List<String> sqDiagramBody;
		try 
		{
			//First, read all the content of the file into a string (so that contains method can work!) -> Changed to List<String>!
			//sqDiagramBody = FileUtils.readFileToString(new File(filename),"utf-8").toLowerCase();
			sqDiagramBody = FileUtils.readLines(new File(filename),"utf-8");
			
			// For each line in the file
			//for (String nextLine : sqDiagramBody)
			for (int x=0; x<sqDiagramBody.size();x++)
			{
				// Save keyword and syns found in the current msg interaction
				Map<String,Set<String>> nlBpdi = checkRelevanceOfFileLine(sqDiagramBody.get(x), relevantBpdi);
				if (!nlBpdi.isEmpty())
				{
					Map<String,Set<String>> nlBpo = checkRelevanceOfFileLine(sqDiagramBody.get(x), relevantBpo);
					if (!nlBpo.isEmpty())	//Only if the line contains BOTH a bpo and a bpdi, the line itself is relevant! (hence, the file!)
					{
						// Save the tags of the diagrams: the relevant information (pdi + po) found to a global set to have them ready for filters 
						add2RelevanceMap(fileRelevantBpdi,nlBpdi);
						add2RelevanceMap(fileRelevantBpo, nlBpo);
						relevantFile = true;
						
						// Replace current msg interaction with the version highlighted in green to show it was relevant. 
						if (generateImages)
						{
							//OPD.- Jun22,2020: Adding logic to create the images of the tagged diagrams. This version recycles the logic used by the injector. 
							// 					For the time being, this is enough to have a first working version of this functionality.
							String taggedLine = Decorator.decoreateTemplateLine(sqDiagramBody.get(x), "green");
							//logger.debug("taggedLine:"+taggedLine);
							sqDiagramBody.set(x,taggedLine);
						}
					}
				}
				
			}//for (String nextLine : sqDiagramBody)

			//To generate the image file of the relevant scenarios with relevant interactions highlighted 
			//OPD.- Jun22,2020: Adding logic to create the images of the tagged diagrams.
			if (generateImages) 
			{
				String pngFile = new File(filename).getAbsoluteFile().getParent() + "/png/" + new File(filename).getName(); 
				generateUML(String.join("\n",sqDiagramBody), pngFile.replace(".txt", "_tagged.png"));
			}
			
		} 
		catch (IOException e) 
		{
			logger.error("Unable to load file ["+filename+"], so skipping it (meaning empty set of relevant keys!)",e);
			//e.printStackTrace();
		}
				
		return relevantFile;
	}


	public static Map<String,Set<String>> checkRelevanceOfFileLine(String nextLine, Map<String,Set<String>> relevantMap)
	{	
		Map<String,Set<String>> results = new TreeMap<>();
		nextLine = nextLine.toLowerCase().trim();

		//Small patch to avoid the source code lines that are not relevant for relevance ;-).
		if (nextLine.startsWith("#") || nextLine.toLowerCase().startsWith("participant") || nextLine.startsWith("@") || nextLine.length()==0)
			return results;
		
		//Next, iterate the relevantset
		for (Map.Entry<String,Set<String>> pair : relevantMap.entrySet()) 
		{
			//For each key, iterate its values. If ANY of them exists within the file, mark it (by adding the key in the set!) as existing!
			for (String nextValue: pair.getValue())
			{
				if (nextLine.toLowerCase().contains(nextValue.toLowerCase()))
				{
					add2RelevanceMap(results, pair.getKey(), nextValue);
				}
			}
		}//for (Map.Entry<String,Set<String>> pair : relevantSet.entrySet()) 

		return results; //IF no keys were matched (as per their synonym values!), empty set is returned.
	}
	
	public static void add2RelevanceMap(Map<String,Set<String>> relevantMap, String key, String value)
	{
		if (!relevantMap.keySet().stream().anyMatch(key::equalsIgnoreCase))
		{
			relevantMap.put(key, new TreeSet<String>());
		}
	
		Set<String> synSet = relevantMap.get(key);
		synSet.add(value);
	}
	
	
	public static void add2RelevanceMap(Map<String,Set<String>> relevantMapTo, Map<String,Set<String>> relevantMapFrom)
	{
		for (Map.Entry<String,Set<String>> pair : relevantMapFrom.entrySet()) 
		{
			if (!relevantMapTo.keySet().stream().anyMatch(pair.getKey()::equalsIgnoreCase))
			{
				relevantMapTo.put(pair.getKey(), pair.getValue());
			}
			else
			{
				relevantMapTo.get(pair.getKey()).addAll(pair.getValue());
			}
		
		}
	}
	
	public static Map<String,Set<String>> word2vecFromList(List<String> originalValues, String word2vecCorpus, int numberOfSynonyms)
	{
		
		Map<String,Set<String>> extValues = new TreeMap<>();
		Set<String> allSet = new HashSet<String>();	//This extra set is used to make sure that duplicated words (i.e., synonyms!) do not exist among the keys. If it were the case, the duplicated words are not considered!
		
		for (String nextValue: originalValues)
		{
			String nextValueFormatted = nextValue.replace(" ", "").toLowerCase();
			String nextValueForWord2Vec = nextValue.replace(" ", "::").toLowerCase();
			
			//Adding the below logic to support unix-style comments in the business input files!
			if (nextValue.startsWith("#"))	continue;

				
			if (allSet.stream().anyMatch(nextValueFormatted::equalsIgnoreCase))
			{
				logger.info("(Semantically) duplicated word ["+nextValueFormatted+"] found among the input keys, ignoring it!");
				continue;
			}
			
			//From the time being, replacing the blanks in the business DPI/PO into :: (as required by word2vec) to successfully retrieve the synonyms of composed words!
			List<String> tmpList = getWord2VecNoun("http://vectors.nlpl.eu/explore/embeddings", word2vecCorpus, nextValueForWord2Vec, "csv");
			Set<String> tmpSet = new HashSet<>();
			
			//First, add the key as initial value of the set of synonyms (removing the blanks, as the composed words are currently ASSUMED to be together - like in FenixEdu-).
			tmpSet.add(nextValueFormatted);
			
			//Next, check the size of the list returned by word2vec. If it is size 1, it means that no synonyms were found.
			if (tmpList.size()>1)
			{
				//Next, remove the first two elements (which are the name of the corpus used for training and the searched word).
				tmpList.remove(0);
				tmpList.remove(0);
				
				//Next, iterate the rest of the list (which contains the synonyms with the following structure: "word_TYPEOFWORD    similarity").
				for (String nextSyn: tmpList)
				{
										
					//After getting the real synonym from the text string, add it. 
					//The list of synonyms might contain the SAME word but DIFFERENT type of word (e.g., noun, verb, adverb, adjective), so a set helps to avoid worrying about it.
					//Word2vec supports composed words by adding them with "::" (instead of blank spaces). For the time being, removing those separators to have them together (as currently used in the code!)
					//Also adding the "toLowerCase" to avoid duplicated words that are the SAME except for the case! (I have seen a few ones!)
					String nextSynValue = nextSyn.split(" ")[0].split("_")[0].replaceAll("::", "").toLowerCase();
					tmpSet.add(nextSynValue);
					
					//We also need to keep counting the number of synonyms. If the number has exceeded the desired number, we stop.
					//As the list of synonyms could have the same word with different type of word (e.g., alumnus_NOUN and alumnus_ADJ), we better check the size of the set (rather than a regular counter).
					if (tmpSet.size()==numberOfSynonyms+1) //We add one to consider the original key value! (which is also considered a synonym)
					{
						logger.info("The max number of synonyms has been reached for key ["+nextValueFormatted+"] ");
						break;
					}
				}
			}// end of if (tmpList.size()>1)
			
			//Finally, add the new key/value to the Hashset! (and the allSet used to avoid semantically duplicated keys!)
			extValues.put(nextValueFormatted, tmpSet);
			allSet.addAll(tmpSet);
		}
		
		return extValues;
	}
	
	//OPD, Apr13 2020: Temporarily adding a flag to know if we should review for tokens and only consider the first one (intermediate step to support syns in pdi!)
	//OPD, Jun30,2020: Moving the method from Dictionary to MainUtils, as it makes sense here.
	public static List<String> loadInfo(String filename, String itemType, String tokenDelimitator) throws Exception
	{
		String nextLine = null;
		List<String> newData = new ArrayList<String>();
		
		try 
		{
			List<String> inputLines = FileUtils.readLines(new File(filename));			
			for (int x=0; x<inputLines.size(); x++)
			{
				nextLine = inputLines.get(x).trim();
				if (nextLine.length()>0 && !nextLine.startsWith("#"))
				{
					if (tokenDelimitator!=null)
					{
						nextLine = nextLine.split(tokenDelimitator)[0].trim();
					}
					logger.debug("Next "+itemType+" item is: ["+nextLine+"]");
					newData.add(nextLine);
				}
			}
		}
		catch(Exception e)
		{
			MainUtils.reportFatalError("Error when trying to load the "+itemType+" item: ["+nextLine+ 
					"]; description: ["+e.getMessage()+"] ...\n"+
					"Thus, stopping the program execution!!!\n");
		}	
		return newData;
	}
	
    public static void generateUML(String umlSource, String outputFilename)
    {
		try 
		{
			OutputStream png = new FileOutputStream(outputFilename);
			SourceStringReader reader = new SourceStringReader(umlSource);
			reader.generateImage(png);
			logger.debug("Output UML Sequence diagram with name '" + outputFilename + "' is generated in base directory");
		} 
		catch (IOException exception) 
		{
			logger.error("Failed to write output file ["+outputFilename+"]  " + exception.getMessage());
		}
    }
	
    public static void generateUML(File inputFilename, String outputFilename)
    {
    	List<String> inputLines;
		try 
		{
			inputLines = FileUtils.readLines(inputFilename);
			String umlSource = String.join("\n", inputLines);

			generateUML(umlSource, outputFilename);

		} 
		catch (IOException e) 
		{
			MainUtils.reportNonFatalError("Failed to read input file ["+inputFilename+"]  " + e.getMessage());
		}
	}
	
    public static void reportFatalError(String errorDesc) throws ConfigurationException
    {
		logger.error(errorDesc);
		
		if (MainScreenControllerv2.execLog4all!=null)
		{
            Platform.runLater(() -> MainScreenControllerv2.execLog4all.appendText("FATAL ERROR: "+errorDesc+"\n"));
			throw new ConfigurationException("\nFATAL ERROR: "+errorDesc);
		}
		else
		{
			System.exit(-1);			
		}
    }
    
    public static void reportNonFatalError(String errorDesc)
    {
		logger.error(errorDesc);
		
		if (MainScreenControllerv2.execLog4all!=null)
		{
            Platform.runLater(() -> MainScreenControllerv2.execLog4all.appendText("ERROR: "+errorDesc+"\n"));
		}
    }
    
    
    public static void reportInfo(String infoDesc)
    {
		logger.info(infoDesc);
		
		if (MainScreenControllerv2.execLog4all!=null)
		{
            Platform.runLater(() -> MainScreenControllerv2.execLog4all.appendText("INFO: "+infoDesc+"\n"));
		}
    }
    
    public static String trimBothSides(String message) {
    	return message.trim().replaceFirst("^\\s+", "");
    }
    
    public static PropertiesConfiguration readConfigFile(String configFilename)
    {
    	PropertiesConfiguration config = new PropertiesConfiguration();

    	try 
		{
			config.read(new FileReader(configFilename));
		} 
		catch (Exception e)
		{
			try {
				reportFatalError("Error when loading the configuration file: "+e.getMessage()+"\n");
			} catch (Exception e1) {
				//No need to report anything, as the program will stop (command-line mode!)
			}
		}
    	return config;
    }
    
    //================================================================================
    
	public static SimilarityStrategy strategy = new JaroWinklerStrategy();
	public static StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
	
	public static boolean areSyntacticallySimilar(String s1, String s2, double similarityThreshold) 
	{
		double score = service.score(s1, s2);
		return (score>=similarityThreshold);
	}
	
	public static List<String> getWord2VecNoun(String baseUrl, String modelId, String word, String format)
	{
		return getWord2Vec(baseUrl, modelId, word+"_NOUN", format);
	}
	
	
	public static List<String> getWord2VecVerb(String baseUrl, String modelId, String word, String format)
	{
		return getWord2Vec(baseUrl, modelId, word+"_VERB", format);
	}
	
	public static List<String> getWord2Vec(String baseUrl, String modelId, String word, String format)
	{
		return processGetRestCall(baseUrl+"/MOD_"+modelId+"/"+word+"/api/"+format+"/");
	}

		// http://vectors.nlpl.eu/explore/embeddings/MODEL/WORD/api/FORMAT
	public static List<String> processGetRestCall(String getUrl) 
	{
		List<String> results = new ArrayList<>();
		logger.debug("url:"+getUrl);
		
	   try {

		 URL url = new URL(getUrl);
		 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		 		 
		 conn.setRequestMethod("GET");
		 //conn.setRequestProperty("Accept", "application/json");

		 if (conn.getResponseCode() != 200) 
		 {
			throw new RuntimeException("["+getUrl+"] failed. HTTP error code: "+ conn.getResponseCode());
		 }

		 BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

		 String output;
		 //System.out.println("Output from Server .... \n");
		 while ((output = br.readLine()) != null) {
			logger.debug("data returned from word2vec:"+output);
			results.add(output);
		 }

		 conn.disconnect();
	   } 
	   catch (Exception e) 
	   {
		 e.printStackTrace();
	   } 
		 return results;
	}
	
	public static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            if (! Files.isSymbolicLink(f.toPath())) {
	                deleteDir(f);
	            }
	        }
	    }
	    file.delete();
	}
	
	public static void unzip(Path path, Charset charset) throws IOException{
	    String fileBaseName = FilenameUtils.getBaseName(path.getFileName().toString());
	    Path destFolderPath = Paths.get(path.getParent().toString(), fileBaseName);
	    
	    try (ZipFile zipFile = new ZipFile(path.toFile(), ZipFile.OPEN_READ, charset)){
	        Enumeration<? extends ZipEntry> entries = zipFile.entries();
	        while (entries.hasMoreElements()) {
	            ZipEntry entry = entries.nextElement();
	            Path entryPath = destFolderPath.resolve(entry.getName());
	            if (entryPath.normalize().startsWith(destFolderPath.normalize())){
	                if (entry.isDirectory()) {
	                    Files.createDirectories(entryPath);
	                } else {
	                    Files.createDirectories(entryPath.getParent());
	                    try (InputStream in = zipFile.getInputStream(entry)){
	                        try (OutputStream out = new FileOutputStream(entryPath.toFile())){
	                            IOUtils.copy(in, out);                          
	                        }
	                    }
	                }
	            }
	        }
	    }
	}
	
    public static void _unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + MainClient.DIR_OS_SEPARATOR + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
	
    public static List<String> findFiles(Path path, String fileExtension)
            throws IOException {

            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException("Path must be a directory!");
            }

            List<String> result;

            try (Stream<Path> walk = Files.walk(path)) {
                result = walk
                        .filter(p -> !Files.isDirectory(p))
                        // this is a path, not string,
                        // this only test if path end with a certain path
                        //.filter(p -> p.endsWith(fileExtension))
                        // convert path to string first
                        .map(p -> p.toString().toLowerCase())
                        .filter(f -> f.endsWith(fileExtension))
                        .collect(Collectors.toList());
            }

            return result;
        }
    
	public static void main(String args[])  throws IOException
	{
		// http://vectors.nlpl.eu/explore/embeddings/en/associates/#
		// http://vectors.nlpl.eu/explore/embeddings/en/models/
		
		getWord2VecNoun("http://vectors.nlpl.eu/explore/embeddings", "enwiki_upos_skipgram_300_3_2019", "thesis", "csv");
		getWord2VecNoun("http://vectors.nlpl.eu/explore/embeddings", "gigaword_upos_skipgram_300_2_2018", "thesis", "csv");
		getWord2VecNoun("http://vectors.nlpl.eu/explore/embeddings", "googlenews_upos_skipgram_300_xxx_2013", "thesis", "csv");
		getWord2VecNoun("http://vectors.nlpl.eu/explore/embeddings", "bnc_upos_skipgram_300_10_2017", "thesis", "csv");
		getWord2VecNoun("http://vectors.nlpl.eu/explore/embeddings", "naknowac_upos_skipgram_300_5_2017", "thesis", "csv");
		
		String rulesetFilename = ".\\dictionaries\\fenix_pdItems.txt";
		List<String> inputLines = FileUtils.readLines(new File(rulesetFilename));
		double similarityThreshold = 0.9;
		
		for (int x=0;x<inputLines.size();x++)
		{
			for (int y=0;y<inputLines.size();y++)
			{
				if (inputLines.get(x).startsWith("#") || inputLines.get(y).startsWith("#"))		//Skipping comment lines!
					continue;
				if (inputLines.get(x).equals(inputLines.get(y)))								//Avoid comparing the strings with themselves!
					continue;
				boolean result = areSyntacticallySimilar(inputLines.get(x), inputLines.get(y), similarityThreshold);
				if (result)
				{
					System.out.println("Strings ["+inputLines.get(x)+"] and ["+inputLines.get(y)+"] are syntactically similar! (based on "+similarityThreshold+" threshold)");
				}
			}
		}
	}
	
}
