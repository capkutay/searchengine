import java.io.*;

public class HTMLFetcher extends Fetcher
{
	public HTMLFetcher(String url) throws Exception 
	{
		super(url);
	}

	protected String craftRequest()
	{
		StringBuffer output = new StringBuffer();
		output.append("GET " + url.resource + " HTTP/1.1\n");
		output.append("Host: " + url.domain + "\n");
		output.append("Connection: close\n");
		output.append("\r\n");
		
		return output.toString();
	}
	
	public static void main(String[] args) throws Exception 
	{
		System.out.print("Input URL: ");
		
		BufferedReader reader = 
			new BufferedReader(new InputStreamReader(System.in));
		
		String url = reader.readLine();
		reader.close();
		
		HTMLFetcher fetch = new HTMLFetcher(url);
		System.out.println(HTMLParser.parseHTML(fetch.getHTML()));
	}
}