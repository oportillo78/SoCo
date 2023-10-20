package edu.ucd.forcops.main;

import java.io.File;
import java.util.List;

public class CountElementsInPlantUML {

	public static void main(String[] args) throws Exception 
	{
		String inputFileList = "./configurations/fenix_v2_ncdiagrams.txt";
		inputFileList = "./configurations/uniXYZ_ncdiagrams.txt";
		List<String> inputFiles = MainUtils.loadInfo(inputFileList, "inputFiles", null);
		
		System.out.println("file,participants,messages");
		for (String nextFile: inputFiles)
		{
			PlantUMLWrapper pWrap = PlantUMLHelper.read2(new File(nextFile));
			
			int participants = pWrap.getCp().size();
			int messages = pWrap.getLe().size();
			
			System.out.println(nextFile+","+participants+","+messages);
		}

	}

}
