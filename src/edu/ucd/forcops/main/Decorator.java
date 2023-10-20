package edu.ucd.forcops.main;

public class Decorator {

	public static String getAddColor()
	{
		return KnowledgeBase.isDebugMode() ? "blue" : "black";
	}
	
	public static String getDelColor()
	{
		// red has to be returned ALL the time to identify the content that needs to be removed.
		return KnowledgeBase.isDebugMode() ? "red" : "red";		
	}
	
	public static boolean isSourceLineActor(String line)
	{
		return line.toLowerCase().contains("participant");
	}
	
	public static boolean isSourceLineOperation(String line)
	{
		return line.toLowerCase().contains("<") || 
				line.toLowerCase().contains(">");
	}
	
	public static boolean isSourceLineIfElse(String line)
	{
		return line.toLowerCase().contains("alt") || 
				line.toLowerCase().contains("else") || 
				line.toLowerCase().contains("end");
	}
	
	public static boolean isSourceLineIf(String line)
	{
		return line.toLowerCase().contains("alt");
	}
	
	public static boolean isSourceLineElse(String line)
	{
		return line.toLowerCase().contains("else");
	}
	
	public static String decorateActor(String line, String color)
	{
		String[] tokens = line.split(" ");
		String bold = KnowledgeBase.isDebugMode()?"**":"";
		return tokens[0] + " \""+bold+"<color "+color+">"+tokens[1]+bold+"\" as " + tokens[1];
	}
	
	public static String decorateIf(String line, String color)
	{
		return line.replace("alt","alt <font color="+color+">");
	}
	
	public static String decorateElse(String line, String color)
	{
		return line.replace("else","else <font color="+color+">");
	}
	
	public static String decorateOperation(String line, String color)
	{	
		return line.replaceAll("(<)(color )([\\w]+)(>)", "[$2$3]")
				   .replaceFirst(": ", ": <color "+color+">");
	}
	
	public static boolean isDeletedLine(String line)
	{
		return line.toLowerCase().contains("<color "+getDelColor()+">") || 
				line.toLowerCase().contains("<font color="+getDelColor()+">");
	}
	
	public static boolean isAddedLine(String line)
	{
		return line.toLowerCase().contains("<color "+getAddColor()+">") 
				|| line.toLowerCase().contains("<font color="+getAddColor()+">");
	}
	
	public static String decoreateTemplateLine(String line, String color)
	{
		if (isSourceLineOperation(line)) return decorateOperation(line, color);
		else if (isSourceLineActor(line)) return decorateActor(line, color);
		else if (isSourceLineIf(line)) return decorateIf(line, color);
		else if (isSourceLineElse(line)) return decorateElse(line, color);
		else return line;
	}
	
	public static String[] decorateTemplateAdvice(String[] template_advice)
	{
		String[] decoratedTemplateAdvice = new String[template_advice.length];

		for (int x=0; x<template_advice.length;x++)
		{
			//OPD.- Dec 18, 2018: Using "getAddColor" as this method will (theoretically!) ONLY add content.
			//					  (as a foresee other method will highlight the removed content!)
			decoratedTemplateAdvice[x] = Decorator.decoreateTemplateLine(template_advice[x], Decorator.getAddColor());
		}

		return decoratedTemplateAdvice;
	}
}
