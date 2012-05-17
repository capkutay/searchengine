import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


public class Driver {
private static Logger log = Logger.getRootLogger();
	
	private static void setupLogger()
	{
		ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout("%p: %m%n"));
		consoleAppender.setThreshold(Level.INFO);
//		consoleAppender.setThreshold(Level.OFF);

		log.addAppender(consoleAppender);
		
		try
		{
			PatternLayout pattern = new PatternLayout("[%d{hh:mm:ss:SSS} %3L %-5p] (%t) %m%n");
			FileAppender fileAppender = new FileAppender(pattern, "debug.log", false);
			fileAppender.setThreshold(Level.ALL);
//			fileAppender.setThreshold(Level.OFF);

			log.addAppender(fileAppender);
			
		}
		catch(IOException ex)
		{
			consoleAppender.setThreshold(Level.ALL);	
			log.warn("Unable to create file appender. Sending all debug output to console.");
		}
	}	
	
	/*
	 * arguments: -u seedurl -q <file> containing search queries
	 */
	public static void main(String[] args){
		setupLogger();
		ArgumentParser ap = new ArgumentParser(args);
		String seedURL = ap.getValue("-u");
		String queryFile = ap.getValue("-q");
		PrintWriter writer = null;
		InvertedIndex index = null;
		
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter("searchresults.txt")));
			InvertedIndexBuilder builder = new InvertedIndexBuilder();
			index = builder.createInvertedIndex(seedURL);
			index.writeInvertedIndex();
			Search search = new Search(index);
			search.processSearch(queryFile, writer);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			index.shutDown();
		}
	}

}
