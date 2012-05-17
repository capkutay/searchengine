import java.io.File;
import java.util.ArrayList;


public abstract class DirectoryTraverser {
/*
 * Create ArrayList of files
 */
	public static void getTextFiles(String path, ArrayList<File> textFileList){
		
		File folder = new File(path);
		
		//check to see if path is a valid directory
		if(!folder.isDirectory()){
			System.out.println("Invalid directory.");
			System.exit(1);
		}
		
		File[] files = folder.listFiles();
		
		for(File file : files){
			String name = file.getName();
			
			if(file.isDirectory()){
				getTextFiles(file.getAbsolutePath(), textFileList);
				//verify file is a textfile
			} else if(name.toLowerCase().endsWith(".txt")){
				textFileList.add(file);
			} 
		}
	}	
}
	


