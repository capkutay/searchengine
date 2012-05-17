import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;


public class InvertedIndex {

	public HashMap<Integer, TreeSet<Map.Entry<String, Integer>>> searchResults  = new HashMap<Integer, TreeSet<Map.Entry<String, Integer>>>();
	private TreeMap<String, HashMap<String, ArrayList<Integer>>> invertedIndex;
	private CustomLock lock = new CustomLock();
	private CustomLock resultsLock = new CustomLock();
	private WorkQueue searchThreadPool;
	private int pending;
	private static Logger logger = Logger.getRootLogger();   



	public InvertedIndex(TreeMap<String, HashMap<String, ArrayList<Integer>>> invertedIndex){
		this.invertedIndex = invertedIndex;
		searchThreadPool = new WorkQueue(10);
		pending = 0;
	}
	
	public InvertedIndex(){
	
		searchThreadPool = new WorkQueue(10);
		pending = 0;
	}


	/*
	 * Write output file using print writer wrapped around buffered writer
	 */

	public void writeInvertedIndex(){

		try {

			PrintWriter write = new PrintWriter(new BufferedWriter(new FileWriter("invertedindex.txt")));
			for (String key : invertedIndex.keySet()){
				write.print(key + "\n");
				for (String fileKey : invertedIndex.get(key).keySet()){
					write.print("\"" + fileKey + "\"");
					ArrayList<Integer> tmp = invertedIndex.get(key).get(fileKey);
					for (Integer i : tmp){
						write.print(", " + i);
					}
					write.print("\n");
				}
				write.print("\n");
			}

			write.flush();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}



	/*
	 * given a query returns a set of entries with containing filename and frequencies
	 */
	private HashSet<Map.Entry<String, ArrayList<Integer>>> partialLookUp(String query) {

		//get invertedIndex data structures where the query matches the key
		lock.acquireReadLock();
		//Map.Entry<String, HashMap<String,ArrayList<Integer>>> entry = invertedIndex.ceilingEntry(query);
		Map.Entry<String, HashMap<String,ArrayList<Integer>>> entry = this.getHigherEntries(query, true);
		lock.releaseReadLock();
		HashSet<Map.Entry<String, ArrayList<Integer>>> matchingFiles = new HashSet<Map.Entry<String, ArrayList<Integer>>>();
		while (entry != null && entry.getKey().startsWith(query)) {

			HashMap<String, ArrayList<Integer>> matchLocations = entry.getValue();
			matchingFiles.addAll(matchLocations.entrySet());	
			lock.acquireReadLock();
			//			entry = invertedIndex.higherEntry(entry.getKey());
			entry = this.getHigherEntries(entry.getKey(), false);

			lock.releaseReadLock();
		}
		//lock.releaseReadLock();
		//lock.releaseReadLock();
		return matchingFiles;
	}


	/*
	 * Private method that given a query returns a copy of ceiling or higher entries 
	 */
	private Map.Entry<String, HashMap<String,ArrayList<Integer>>> getHigherEntries(String query, boolean ceiling){
		if (ceiling){
			Map.Entry<String, HashMap<String,ArrayList<Integer>>> ceilingEntries = invertedIndex.ceilingEntry(query);
			return ceilingEntries;
		}
		Map.Entry<String, HashMap<String,ArrayList<Integer>>> higherEntries = invertedIndex.higherEntry(query);
		return higherEntries;
	}


	/*
	 * Thread safe method to return number of pending workers
	 */

	private synchronized int getPending(){
		return pending;
	}
	
	/*
	 * Allows threads to increment or decrementing the pending worker variable
	 */
	private synchronized void updatePending(boolean increment){
		if(increment){
			pending++;			
		} else{
			pending--;
			if(getPending() <= 0){
				notifyAll();
			}
		}
	}

	public HashMap<Integer, TreeSet<Map.Entry<String, Integer>>> getSearchResults(){
		return searchResults;
	}

	public void processQueries(ArrayList<String> queryList){
		int i = 0;
		for (String queryLine: queryList){

			searchThreadPool.execute(new SearchWorker(queryLine, i++));
		}

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
	}

	public void shutDown(){
		searchThreadPool.shutdown();
	}
	private class SearchWorker implements Runnable {

		String queryLine;
		int i;

		public SearchWorker(String queryLine, int i){
			this.queryLine = queryLine;
			this.i = i;

			//updatePending(boolean increment), pass in false if decrementing 
			updatePending(true);
		}


		public void run() { 
			//			logger.debug("Starting work on" + queryLine);
			String[] queries = queryLine.split("\\s+");

			//master set of search results (unsorted)
			HashSet<Map.Entry<String, ArrayList<Integer>>> fileSet = 
					new HashSet<Map.Entry<String, ArrayList<Integer>>>();

			//return a partial look up on the query and add it to the unsorted set of files
			for (String query : queries) {	
				fileSet.addAll(partialLookUp(query));
			}
			HashMap<String, Integer> totalAppearances = new HashMap<String, Integer>();
			final HashMap<String, Integer> firstAppearances = new HashMap<String, Integer>();

			for (Map.Entry<String, ArrayList<Integer>> entry : fileSet) {
				Integer size = totalAppearances.get(entry.getKey());
				if (size == null){
					//combine first appearances 
					firstAppearances.put(entry.getKey(), entry.getValue().get(0));
					//combine sizes of array lists for each key that begins with the query term
					totalAppearances.put(entry.getKey(), entry.getValue().size());
				} else {
					if (entry.getValue().get(0) < firstAppearances.get(entry.getKey())){
						firstAppearances.put(entry.getKey(), entry.getValue().get(0));
					}
					totalAppearances.put(entry.getKey(), entry.getValue().size() + size);
				}
			}
			//create a sorted set of total entries
			TreeSet<Map.Entry<String, Integer>> sortedTotalAppearances = new TreeSet<Map.Entry<String, Integer>>(
					new Comparator<Map.Entry<String, Integer>>(){
						//comparator compares total entries for that key, if frequencies are equal, it compares the first appearance
						public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
							int value1 = o2.getValue() - o1.getValue();
							if(value1 != 0){
								return value1;									
							}
							return firstAppearances.get(o1.getKey()) - firstAppearances.get(o2.getKey());
						}
					});
			//add all appearances into sorted data structure
			sortedTotalAppearances.addAll(totalAppearances.entrySet());

			resultsLock.acquireWriteLock();
			searchResults.put(i, sortedTotalAppearances);
			resultsLock.releaseWriteLock();
			logger.debug("Adding index: " + i);

			logger.debug("Search: Finished work on: " + queryLine);
			//updatePending(boolean increment), pass in false if decrementing
			updatePending(false);
			
		}
	}
}