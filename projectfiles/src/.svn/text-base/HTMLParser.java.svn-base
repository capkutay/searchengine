
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class HTMLParser {



	public static String parseHTML(String html){

		return removeHtmlTags(removeScript(removeStyle(html)));

	}

	public static String removeStyle(String code){
		code = code.replaceAll("(?i)<\\s*style\\s*>.*?<\\s*/\\s*style>", " ");
		return code;

	}

	public static String removeScript(String code){
		code = code.replaceAll("(?i)<\\s*script.*?>.*?<\\s*/\\s*script\\s*?>", " ");
		return code;
	}

	public static String removeHtmlTags(String code){
		code = code.replaceAll("(?i)<.*?>", " ");
		return code;
	}
	
	

	public static ArrayList<String> parseLinks(String html){
		//			BufferedReader br = new BufferedReader(new FileReader(path));
		ArrayList<String> links = new ArrayList<String>();
		Pattern p = Pattern.compile("(?i)<a\\s*[^>]*href\\s*=\\s*\"([^\"]*)\"[^>]*>(.*?)\\s*</?a/?\\s*>");
								   //"(?i)<a\\s[^>]*href\\s*=\\s*\"([^\"]*)\"[^>]*>(.*?)</a>"
		Matcher m = p.matcher(html);

		while (m.find()){
			links.add(m.group(1));
		}			


		return links;
	}

	public static void main(String[] args){
		try {
//			HTMLFetcher fetch = new HTMLFetcher("http://www.cs.usfca.edu/~cliang2/");
			HTMLFetcher fetch = new HTMLFetcher("http://www.htmlhelp.com/tools/valet/");
			String html = fetch.getHTML();
			System.out.println(parseHTML(html));
			ArrayList<String> links = parseLinks(html);
			for(String link : links){
				System.out.println(link);
			}
			//			System.out.println("ITEMS FETCHED: " + html);


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
