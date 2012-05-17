import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public final class QueryListFactory {
	
	/*
	 * Parse textfile of queries and put queries in a list
	 */
	public static ArrayList<String> createQueryList(String path){
		ArrayList<String> queryList = new ArrayList<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = null; 

			while ((line = br.readLine()) != null){
				//discard empty queries
				if (line.equals("")){
					continue;
				}
				line = line.toLowerCase();
				//replace all characters except whitespace and word
				line = line.replaceAll("[^\\w\\s+]", "").trim();
				queryList.add(line);
			}

		} catch (FileNotFoundException e) {
			System.out.println("Query File not found.");
			System.exit(1);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		return queryList;
	}

}
