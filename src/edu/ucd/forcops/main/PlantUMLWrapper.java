package edu.ucd.forcops.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.sourceforge.plantuml.BackSlash;
import net.sourceforge.plantuml.BlockUml;
//import net.sourceforge.plantuml.CharSequence2;
import net.sourceforge.plantuml.Option;
import net.sourceforge.plantuml.SourceFileReader2;
import net.sourceforge.plantuml.core.UmlSource;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.sequencediagram.AbstractMessage;
import net.sourceforge.plantuml.sequencediagram.Event;
import net.sourceforge.plantuml.sequencediagram.GroupingLeaf;
import net.sourceforge.plantuml.sequencediagram.GroupingStart;
import net.sourceforge.plantuml.sequencediagram.Message;
import net.sourceforge.plantuml.sequencediagram.Participant;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagramFactory;
import net.sourceforge.plantuml.skin.ArrowConfiguration;
import net.sourceforge.plantuml.skin.ArrowDirection;

public class PlantUMLWrapper 
{
	private static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	Option option = null;
	
	BlockUml umlItem = null;
	//List<CharSequence2> rawUmlData = null;
	List<String> rawUmlData = null;
	SequenceDiagram sd = null;
	Collection<Participant> cp = null;
	List<Event> le = new ArrayList<Event>();
	
	int numberOfNonMsgLines = 0;
	
	public PlantUMLWrapper(String[] sourceText)
	{		
		String[] pumlSource = new String[sourceText.length+2];
		
		pumlSource[0] = "@startuml";
		System.arraycopy(sourceText, 0 , pumlSource, 1, sourceText.length);
		pumlSource[pumlSource.length-1] = "@enduml";
		
		BlockUml bUml = new BlockUml(pumlSource);
		updateUmlItemBasedData(bUml);
	}
	
	public PlantUMLWrapper(SourceFileReader2 _sfr, Option _option) throws Exception
	{
		
		List<BlockUml> lblocks=_sfr.getBlocks();
		
		if (lblocks.size()>1)
		{
			MainUtils.reportFatalError("Scenario not supported yet!\n");
		}
		else
		{
			updateUmlItemBasedData(lblocks.get(0));
			option = _option;
		}
	}

	public int getNumberOfNonMsgLines()
	{
		return numberOfNonMsgLines;
	}
	
	public Set<String> getExtendedActorSet()
	{
		Set<String> pactors = new HashSet<String>();
		
		for (Participant p: this.getCp())
		{
			pactors.addAll(Dictionary.getActorSynonymsByValue(getActorLongName(p)));
		}
		
		return pactors;
	}

	public Set<String> getOriginalActorSet()
	{
		Set<String> pactors = new HashSet<String>();
		
		for (Participant p: this.getCp())
		{
			pactors.add(getActorLongName(p));
		}
		
		return pactors;
	}
	
	public String getActorLongName(Participant p)
	{
		StringBuilder sblName = new StringBuilder();
		Iterator<CharSequence> it = p.getDisplay(false).iterator();
		while (it.hasNext())
		{
			if (sblName.length()>0)
			{
				sblName.append(".");
			}
			sblName.append(it.next().toString());
		}
		String lname = sblName.toString().replaceAll("\\.\\.", "\\.");
		//logger.debug("lname->"+lname);
		
		return lname;
	}
	
	public String getActorShortName(Participant p)
	{
		return p.getCode();
	}
	
	//Originally, this method is intended to be called only the first time the wrapper is created (i.e., as part of reading the original PlantUML text).
	public void updateUmlItemBasedData(BlockUml _umlItem)
	{
		umlItem = _umlItem;
		//rawUmlData = umlItem.getData();
		sd = (SequenceDiagram) umlItem.getDiagram();
		// new ArrayList<>(...) is needed because Arrays.asList creates an UNMODIFIABLE array!!!
		rawUmlData = new ArrayList<>(Arrays.asList(sd.getSource().getPlainString().split("\r"+BackSlash.CHAR_NEWLINE)));
		cp = sd.participants();
		le.clear();
		le.addAll(sd.events());

		//OPD.- Feb5,2019: From the time being, ONLY supporting the participants listed at the BEGINING, i.e., before the messages.
		numberOfNonMsgLines = 0;
		for (int x=1; x<rawUmlData.size(); x++)
		{
			//OPD.- Feb5,2019: Adding other types of non-message lines, starting with scale.
			String nextLine = rawUmlData.get(x).trim().toLowerCase();
			if (nextLine.startsWith("participant ") || nextLine.startsWith("scale ") )
			{
				numberOfNonMsgLines++;
			}
		}	
		logger.debug("numberOfNonMsgLines in diagram: "+numberOfNonMsgLines);
	}
	

	//This method will be useful to update the wrapper AFTER (gradually) applying the privacy patterns.
	public void updateUmlItemBasedData(String[] plantUmlText)
	{
		updateUmlItemBasedData(new BlockUml(plantUmlText));		
	}
	
	//This method will be useful to update the wrapper AFTER (gradually) applying the privacy patterns.
	public void updateUmlItemBasedData(List<String> plantUmlText)
	{
		updateUmlItemBasedData((String[]) plantUmlText.toArray(new String[plantUmlText.size()]));
	}
	
	public Option getOption() {
		return option;
	}

	public void setOption(Option option) {
		this.option = option;
	}
	
	public BlockUml getUmlItem() {
		return umlItem;
	}

	public void setUmlItem(BlockUml umlItem) {
		this.umlItem = umlItem;
	}

	public List<String> getRawUmlData() {
		return rawUmlData;
	}

	public void setRawUmlData(String[] newRawUmlData) {
		this.rawUmlData = new ArrayList<>(Arrays.asList(newRawUmlData));
	}
	
	public void setRawUmlData(List<String> newRawUmlData) {
		this.rawUmlData = newRawUmlData;
	}
	
	//OPD.- Dec 21,2018: IMPORTANT NOTE to call the method "" after finishing modifying the "RawUmlData" of the wrapper.
	public void addRawUmlData(String[] newData, int index)
	{
		for (int x=0; x<newData.length; x++)
		{
			addRawUmlData(newData[x], index+x);
		}
	}

	//OPD.- Dec 21,2018: IMPORTANT NOTE to call the method "" after finishing modifying the "RawUmlData" of the wrapper.
	public void addRawUmlData(String newData, int index)
	{
		rawUmlData.add(index, newData);
		//updateUmlItemBasedData((String[]) rawUmlData.toArray(new String[rawUmlData.size()]));
	}

	//OPD.- Dec 21,2018: IMPORTANT NOTE to call the method "" after finishing modifying the "RawUmlData" of the wrapper.
	public void addRawUmlData(String newData)
	{
		rawUmlData.add(newData);
	}

	//OPD.- Dec 21,2018: IMPORTANT NOTE to call the method "" after finishing modifying the "RawUmlData" of the wrapper.
	public void replaceRawUmlData(String newData, int index)
	{
		rawUmlData.set(index, newData);
	}
	
	//OPD.- Dec 21,2018: IMPORTANT NOTE to call the method "" after finishing modifying the "RawUmlData" of the wrapper.
	public void delRawUmlData(String oldData)
	{
		rawUmlData.remove(oldData);
	}
	
	//OPD.- Dec 21,2018: IMPORTANT NOTE to call the method "" after finishing modifying the "RawUmlData" of the wrapper.
	public void delRawUmlData(int oldIndex)
	{
		rawUmlData.remove(oldIndex);
	}
	
	public SequenceDiagram getSd() {
		return sd;
	}

	public void setSd(SequenceDiagram sd) {
		this.sd = sd;
	}

	public Collection<Participant> getCp() {
		return cp;
	}

	public void setCp(Collection<Participant> cp) {
		this.cp = cp;
	}

	public List<Event> getLe() {
		return le;
	}

	public void setLe(List<Event> le) {
		this.le = le;
	}
	
	//========================================
	

	public String getPlainText() {
		return sd.getSource().getPlainString();
	}
	
	public void testChanges()
	{
		//Testing the saving AFTER modifying the code!

		//*
		// Non-successful code: It DOES modify the diagram, but it DOES NOT update the source code (which is what is ultimately saved to disk!).
		String[] strings = {
				"@startuml",
				"Staff-> Test: testingLogic(studentID)",
				"@enduml"
		};
		SequenceDiagramFactory sdf = new SequenceDiagramFactory();
		SequenceDiagram sd2 = sdf.createEmptyDiagram();
		UmlSource umls2 = new UmlSource(BlockUml.convert(strings), true);
		sd2 = (SequenceDiagram) sdf.createSystem(umls2);
		System.out.println(sd2.participants().size());
		
		sd.getOrCreateParticipant("Test", Display.getWithNewlines("Test"));
		sd.addMessage((AbstractMessage)sd2.events().get(0));
		//*/
		
		//=====================
		String[] strings2 = {
				"Staff-> Test: testingLogic(studentID)",
		};
		//rawUmlData.add(1, BlockUml.convert(strings2).get(0));
		rawUmlData.add(1, strings2[0]);
		//BlockUml umlItem2 = new BlockUml(rawUmlData,umlItem.getLocalDefines());
		BlockUml umlItem2 = new BlockUml((String[])rawUmlData.toArray());
		sd = (SequenceDiagram) umlItem2.getDiagram();
		cp = sd.participants();
		le = sd.events();
		
	}
	
	public void printInfo()
	{

		//System.out.println("lblock #"+x);
		for (Participant p: cp)
		{
			System.out.println("p.getCode() -> "+p.getCode());
		}
		
		
		int eventId=0;
		for (Event e: le)
		{
			eventId++;
			System.out.println("========================================================");
			//System.out.println("e.toString() -> "+e.toString()+", e.getClass() -> "+e.getClass().getName());
			
			if (e instanceof Message) 
			{
				Message m = (Message) e;
				ArrowConfiguration ac = m.getArrowConfiguration();
				ArrowDirection ad = ac.getArrowDirection();
				System.out.println("m.toString() -> "+m.toString());
				System.out.println("m.isSelfMessage() -> "+m.isSelfMessage());
				System.out.println("m.getLabel() -> "+m.getLabel());
				System.out.println("m.getLabelNumbered() -> "+m.getLabelNumbered());
				//body.name() + "(" + dressing1.name() + " " + decoration1 + ")
				//(" + dressing2.name() + " " + decoration2 + ")" + isSelf + " " + color;
				System.out.println("m.getArrowConfiguration()-> "+ac);
				System.out.println("m.getArrowConfiguration().name() -> "+ad.name());
				System.out.println("rawUmlData.get(eventId).toString() -> "+rawUmlData.get(eventId).toString());
				if (rawUmlData.get(eventId).toString().contains("<"))
				{
					MainUtils.reportNonFatalError("Right to Left direction that needs to be fixed!");
				}
				System.out.println("m.getArrowConfiguration().isAsync() -> "+ac.isAsync());
				System.out.println("m.getArrowConfiguration().isDotted() -> "+ac.isDotted());
				System.out.println("m.getArrowConfiguration().isSelfArrow() -> "+ac.isSelfArrow());
				System.out.println("m.getMessageNumber() -> "+m.getMessageNumber());
				System.out.println("m.getParticipant1() -> "+m.getParticipant1());
				System.out.println("m.getParticipant2() -> "+m.getParticipant2());
			}
			else if (e instanceof GroupingStart)
			{
				GroupingStart gs = (GroupingStart) e;
				System.out.println("gs.toString() -> "+gs.toString());
			}
			else if (e instanceof GroupingLeaf)
			{
				GroupingLeaf gl = (GroupingLeaf) e;
				System.out.println("gl.toString() -> "+gl.toString());
			}
			else
			{
				MainUtils.reportNonFatalError("========================================================");
				MainUtils.reportNonFatalError("Unsupported Event Type:"+e.getClass());
				MainUtils.reportNonFatalError("========================================================");
			}
		}
		
		System.out.println(umlItem.toString());
	}

	
}
