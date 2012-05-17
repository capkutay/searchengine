import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;


public class Search {
	
	InvertedIndex index;
	public Search(InvertedIndex index){
		this.index = index;
	}
	
	public void processSearch(String path, PrintWriter writer){
		
		ArrayList<String> queryList = QueryListFactory.createQueryList(path);
		//creates worker threads to populate list of search results, waits for threads to finish
		index.processQueries(queryList);
		//get search results
		HashMap<Integer, TreeSet<Map.Entry<String, Integer>>> results = index.getSearchResults();
		int i = 0;
		//FOR EACH QUERYLINE, WRITE SEARCH
		for (String queryLine: queryList){
			System.out.println(results.get(i));
			writer.println(queryLine);
//			if (results.get(i).isEmpty()){
//				writer.println("No match for: " + queryLine);
//			}

			for (Map.Entry<String, Integer> entry : results.get(i++)){
				writer.println("\"" + entry.getKey() + "\"");
			}
			writer.print("\n");
		}
		writer.flush();
	}
	

}
