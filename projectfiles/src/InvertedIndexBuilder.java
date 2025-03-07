import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class InvertedIndexBuilder {

	private ArrayList<URL> totalLinks = new ArrayList<URL>();
	private static Logger logger = Logger.getRootLogger();   
	private CustomLock lock = new CustomLock();
	private CustomLock linkLock = new CustomLock();
	private WorkQueue indexThreadPool;
	private int pending;
	private HashSet<TreeMap<String, HashMap<String, ArrayList<Integer>>>> subIndices = 
			new HashSet<TreeMap<String, HashMap<String, ArrayList<Integer>>>>(); 

	public InvertedIndexBuilder(){
		indexThreadPool = new WorkQueue(10);
		pending = 0;
	}

	private synchronized int getPending(){
		return pending;
	}

	private synchronized void updatePending(boolean increment){
		if(increment){
			pending++;
		} else {
			pending--;	
			if(getPending() <= 0){
				notifyAll();
			}
		}
	}
	/*
	 * Maps words to its position in a file
	 */
	private  void addURLToIndex(String url){
		TreeMap<String, HashMap<String, ArrayList<Integer>>> subIndex = new TreeMap<String, HashMap<String, ArrayList<Integer>>>();
		indexThreadPool.execute(new IndexWorker(url, subIndex));
	}

	public void safeShutDown(){
		while(getPending() > 0)
		{			
			synchronized(this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		indexThreadPool.shutdown();
	}
	
	private InvertedIndex merge(){
		InvertedIndex masterIndex = new InvertedIndex();
		
		
		
		return masterIndex;
	}

	private TreeMap<String, HashMap<String, ArrayList<Integer>>> mergeIndices() {
		TreeMap<String, HashMap<String, ArrayList<Integer>>> masterIndex = 
				new TreeMap<String, HashMap<String, ArrayList<Integer>>>(); 

		for (TreeMap<String, HashMap<String, ArrayList<Integer>>> map : subIndices){
			for (Map.Entry<String, HashMap<String, ArrayList<Integer>>> subEntry : map.entrySet()){
				HashMap<String, ArrayList<Integer>> indexMatch = masterIndex.get(subEntry.getKey());
				if (indexMatch == null)
					masterIndex.put(subEntry.getKey(), subEntry.getValue());
				else{
					indexMatch.putAll(subEntry.getValue());
					masterIndex.put(subEntry.getKey(), indexMatch);
				}
			}
		}
		return masterIndex;
	}
	
	/*
	 * Merge indices to create inverted index
	 */

	public InvertedIndex createInvertedIndex(String seedurl){

		addURLToIndex(seedurl);

		//wait for threads to finish
		while(getPending() > 0)
		{			
			synchronized(this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if(subIndices.isEmpty()){
			System.out.println("Still empty");
		}
		TreeMap<String, HashMap<String, ArrayList<Integer>>> index = mergeIndices();
		indexThreadPool.shutdown();
		return new InvertedIndex(index);
	}

	private class IndexWorker implements Runnable {

		TreeMap<String, HashMap<String, ArrayList<Integer>>> subIndex;
		String url;

		public IndexWorker(String url, TreeMap<String, HashMap<String, ArrayList<Integer>>> subIndex){
			this.url = url;
			this.subIndex = subIndex;
			updatePending(true);
			
			//debug
			logger.debug("BUILDING INDEX STARTING WORK ON: " + url);
			logger.debug("PENDING THREADS: " + getPending());

		}

		private void addWordToSubindex(String term, String url, int position){

			HashMap<String, ArrayList<Integer>> tmpWordMap;
			boolean containsKey = subIndex.containsKey(term);
			if (!containsKey){
				tmpWordMap = new HashMap<String, ArrayList<Integer>>();
				subIndex.put(term, tmpWordMap);
			} else {
				tmpWordMap = subIndex.get(term);
			}

			ArrayList<Integer> wordPositions;

			if (!tmpWordMap.containsKey(url)){
				wordPositions = new ArrayList<Integer>();
				tmpWordMap.put(url, wordPositions);
			} else {
				wordPositions = tmpWordMap.get(url);
			}
			wordPositions.add(position);		
		}


		public void run() {
			try{
				System.out.println("Starting to process: " + url);
				HTMLFetcher fetch = null;
				BufferedReader br = null;
				try {
					//create a socket and retrieve html
					logger.debug("-----FETCHING: " + url);
					fetch = new HTMLFetcher(url);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//parse links on the html
				ArrayList<String> newLinks = HTMLParser.parseLinks(fetch.getHTML());
				System.out.println("Found " + newLinks.size() + " links on " + url);
				linkLock.acquireReadLock();
				int numURLs = totalLinks.size();
				linkLock.releaseReadLock();
				
				//verify we have less than 30 links on our workqueue
				if (numURLs < 30){
					linkLock.acquireWriteLock();
					int i = 1;
					
					for (String newLink : newLinks){
						System.out.println("newLink #" + i + ":" + newLink + " on " + url);
						i++;
						logger.debug("******READING IN LINKS *****:" +newLink);
						URL newURL = null;
						
						//verify whether link is absolute or relative
						//if absolute
						if (newLink.startsWith("http")) {
							newURL = new URL(newLink);							
						}
						else { // if link is relative
							newURL = new URL(new URL(url), newLink);
						}
						System.out.println("NEW URL: " +newURL.toString());

						boolean containsURL = false;
						//check if two urls map to same webpage
						for (URL url : totalLinks){
							if(url.sameFile(newURL) || totalLinks.contains(newURL)){
								System.out.println("Found duplicate for " + newURL + " found on " + url);
								containsURL = true;
								logger.debug("FOUND DUPLICATE URL");
								break;
							} 
						}
						if (!containsURL){
							totalLinks.add(newURL);	
//							logger.debug("ADDING LINK TO WORKQUEUE:" + newLink);
							addURLToIndex(newURL.toString());
							numURLs = totalLinks.size();
							logger.debug("####TOTAL LINKS#####: " + numURLs);
							if(numURLs >= 30){
								logger.debug("!!====MORE THAN 30 LINKS, BREAK====!!");
								break;
							}
						}
					}
					linkLock.releaseWriteLock();
				}

				//get HTML that has already been read in from socket,
				//remove tags, and pass into a buffered reader
				br = new BufferedReader(new StringReader(HTMLParser.parseHTML(fetch.getHTML())));

				int pos = 1;
				String line = null;

				while ((line = br.readLine()) != null){
					line = line.toLowerCase();
					String[] terms = line.split("\\s+");

					for (String term : terms){
						//removes any non letter character
						//splits string at any white space character
						//including tabs and multiple spaces
						term = term.replaceAll("\\W", "").trim();
						if (term.equals("")){
							continue;
						}
						//for each term, map it to a single file and a single position in that file
						addWordToSubindex(term, url, pos);
						pos++;	
					}
				}
			}

			catch (IOException e) {
				e.printStackTrace();
			}

			logger.debug("FINISHED WORK ON: " + url);
			try{Thread.sleep(10);}
			catch(Exception e){}
			lock.acquireWriteLock();
			subIndices.add(subIndex);
			lock.releaseWriteLock();
			updatePending(false);

			logger.debug("PENDING: " + getPending());

		}

	}
	private static void setupLogger()
	{
		ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout("%p: %m%n"));
		consoleAppender.setThreshold(Level.INFO);
		//		consoleAppender.setThreshold(Level.OFF);

		logger.addAppender(consoleAppender);

		try
		{
			PatternLayout pattern = new PatternLayout("[%d{hh:mm:ss:SSS} %3L %-5p] (%t) %m%n");
			FileAppender fileAppender = new FileAppender(pattern, "debug.log", false);
			fileAppender.setThreshold(Level.ALL);
			//			fileAppender.setThreshold(Level.OFF);

			logger.addAppender(fileAppender);

		}
		catch(IOException ex)
		{
			consoleAppender.setThreshold(Level.ALL);	
			logger.warn("Unable to create file appender. Sending all debug output to console.");
		}
	}	

	public static void main(String[] args){
		setupLogger();
		InvertedIndexBuilder builder = new InvertedIndexBuilder();
		builder.addURLToIndex("http://www.cs.usfca.edu/sls-cs-spring-2012.html");
		builder.safeShutDown();
//		builder.createInvertedIndex("http://www.cs.usfca.edu/~cliang2");

	}

}