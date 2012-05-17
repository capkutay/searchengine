import java.io.*;
import java.net.*;

import org.apache.log4j.Logger;

public abstract class Fetcher 
{
	protected static final int PORT = 80;
	protected URLParser url;
	protected String domain;
	protected String resource;
	private StringBuilder html;
	protected int responseCode;
	//	private static Logger logger = Logger.getRootLogger();  

	public Fetcher(String url) throws Exception 
	{
		this.url = new URLParser(url);
		fetch();
	}

	public String getHTML(){
		return html.toString();
	}

	public int getResponseCode(){
		return responseCode;
	}

	protected abstract String craftRequest();

	public void fetch() throws Exception 
	{
		//if (url.resource == null || url.domain == null)
		if (url.resource == null || url.domain == null)
		{
			System.err.printf("There is no domain or resource to fetch on" + url + "\n");
			return;
		}
		html = new StringBuilder();


		//		System.out.println(url.domain + ":" + PORT);
		Socket socket = new Socket(url.domain, PORT);

		PrintWriter writer = new PrintWriter(socket.getOutputStream());

		BufferedReader reader = 
				new BufferedReader(new InputStreamReader(socket.getInputStream()));


		String request = craftRequest();
		//		System.out.println(request);

		writer.println(request);
		writer.flush();

		String line = reader.readLine();

		while(line != null){
			if(line.isEmpty()){
				break;
			}
			line = reader.readLine();
		}
		while (line != null) 
		{
			html.append(line);				
			line = reader.readLine();
		}

		reader.close();
		writer.close();
		socket.close();

		//		System.out.println("[done]");
	}
}