package edu.ucd.forcops.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.Date;

public class FileWalker {

	static int num_files = 0;
	static int num_dirs = 0;
	
    public void walk( String path ) {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath() );
                System.out.println( "Dir:" + f.getAbsoluteFile() );

                setFileCreationDate(f.getAbsoluteFile().getAbsolutePath(), new Date());
                num_dirs++;
            }
            else {
                System.out.println( "File:" + f.getAbsoluteFile() );
                
                setFileCreationDate(f.getAbsoluteFile().getAbsolutePath(), new Date());
                num_files++;
            }
        }
    }

    public void setFileCreationDate(String filePath, Date creationDate){

        BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(filePath), BasicFileAttributeView.class);
        FileTime time = FileTime.fromMillis(creationDate.getTime());
        try 
        {
			attributes.setTimes(time, time, time);
		} catch (IOException e) 
        {
			MainUtils.reportNonFatalError("Error trying to set the timestamps of the file. Details are:"+e.getMessage());
		}

    }
    
    public static void main(String[] args) {
    	FileWalker fw = new FileWalker();
        fw.walk("C:\\Users\\omar.portillo\\Downloads\\Soco_Prototype_April2023\\Soco_Prototype" );
        System.out.println("num_files is:"+num_files);
        System.out.println("num_dirs is:"+num_dirs);
    }

}
