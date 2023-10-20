package edu.ucd.forcops.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.ISourceFileReader;
import net.sourceforge.plantuml.Option;
import net.sourceforge.plantuml.SourceFileReader2;

public class PlantUMLHelper 
{
	public static PlantUMLWrapper read2(File _inputXmlFile) throws Exception
	{
		Option _option = createOptions(_inputXmlFile);
		SourceFileReader2 _sfr = read(_option, _inputXmlFile);
		
		return new PlantUMLWrapper(_sfr, _option);
	}
	
	public static Option createOptions(File _inputXmlFile) throws InterruptedException, IOException
	{
		int indexExtension = _inputXmlFile.getName().lastIndexOf(".");
		String extension = _inputXmlFile.getName().substring(indexExtension);
		File outputXmlFile = new File(_inputXmlFile.getAbsolutePath().replace(extension, "_o["+KnowledgeBase.isDebugMode()+"]"+extension));
		String[] argsArray = {"-ofile",outputXmlFile.getAbsolutePath(),"-checkmetadata"};
		return new Option(argsArray);

	}
	
	public static SourceFileReader2 read(Option _option, File _inputXmlFile) throws IOException
	{
		SourceFileReader2 sourceFileReader = new SourceFileReader2(_option.getDefaultDefines(_inputXmlFile), 
				_inputXmlFile, _option.getOutputFile(), _option.getConfig(), _option.getCharset(), _option.getFileFormatOption());
		
		sourceFileReader.setCheckMetadata(_option.isCheckMetadata());
					
		return sourceFileReader;
	}
	
	public static void save(Option _option, ISourceFileReader _sourceFileReader) throws IOException 
	{
		StringBuilder sb = new StringBuilder();
		
		for (BlockUml blockUml : _sourceFileReader.getBlocks()) 
		{
			sb.append(blockUml.getFlashData());
		}

		save(_option, sb.toString());
	}
	
	public static void save2(PlantUMLWrapper _pWrap) throws IOException
	{
		PlantUMLHelper.save(_pWrap.getOption(), _pWrap.getPlainText());
	}
	
	public static void save(Option _option, String _sourceUmlText) throws IOException 
	{
		String charset = _option.getCharset();
		File file = _option.getOutputFile();
		final PrintWriter pw = charset == null ? new PrintWriter(file) : new PrintWriter(file, charset);
		pw.print(_sourceUmlText);
		pw.close();
	}
}
