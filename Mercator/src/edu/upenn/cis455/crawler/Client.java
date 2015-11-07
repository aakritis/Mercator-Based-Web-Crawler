package edu.upenn.cis455.crawler;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Client {

	private URL url;
	private String url_str;
	private String query_str;
	private String host;
	private int port;
	private String method;
	private String body;
	private HashMap<String, String> request_headers;
	private HashMap<String, String> response_map;
	private Socket clientSocket;
	private PrintWriter output = null;
	private BufferedReader input = null;

	/**
	 * Constructor
	 * @param link is the request url
	 * @param method is the request method
	 */
	public Client(String link, String method) {
		this.url_str = link;
		this.method = method;
		this.request_headers = new HashMap<String, String>();
		request_headers.put("User-Agent","cis455crawler");
		request_headers.put("Host", host+":"+port);
		this.response_map = new HashMap<String, String>();
		try {
			this.url = new URL(url_str);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		host = url.getHost();
		port = url.getPort();
		port = (port <= 0 ? 80 : port);
		query_str = url.getPath().equals("") ? "/" : url.getPath();
		try{
			if (!url.getProtocol().equalsIgnoreCase("https"))
			{
				InetAddress address = InetAddress.getByName(url.getHost());
				clientSocket = new Socket(address.getHostAddress(), port);
				clientSocket.setSoTimeout(10000);
				output =  new PrintWriter(clientSocket.getOutputStream(), true);
				input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Function to add a header to be sent along with the request
	 * @param key
	 * @param value
	 */
	public void set_request_header(String key, String value) {
		request_headers.put(key, value);
	}

	/**
	 * Function to get one of the response header values
	 * @param key
	 * @return
	 */
	public String get_response_header(String key) {
		return response_map.get(key);
	}

	/**
	 * Function to send the request to the url
	 */
	public void send_request() {
		if (url.getProtocol().equalsIgnoreCase("https"))
		{
			System.out.println("[MAY FOUR] In client request sending HTTPS request");
			send_https_request();
			return;
		}
		String request = "";
		request += method + " " + query_str + " HTTP/1.0\r\n";
		for (Map.Entry<String, String> entry : request_headers.entrySet()) {
			request += entry.getKey() + ": " + entry.getValue() + "\r\n";
		}
		request += "\r\n";
		output.println(request);
		String responseline = "";
		try {
			responseline = input.readLine();
			int status = Integer.parseInt(responseline.split(" ")[1]);
			response_map.put("status", ""+status);
			String line = "";
			while ((line = input.readLine()).length() != 0) { // Process Headers
				String[] line_parts = line.split(":", 2);
				response_map.put(line_parts[0].toLowerCase(), line_parts[1]);
			}
			if (status!=200 && status != 301 && status != 307)
			{
				input = null;
				return;
			}
			if (method.equalsIgnoreCase("get")) {
				body = "";
				while ((line = input.readLine()) != null) { // Get body of response
					body += line + "\n";
				}
			}
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}
	
	/**
	 * Function to send a https request to the url
	 */
	private void send_https_request()
	{
		HttpsURLConnection con = null;
		int status=0;
		try{
			con = (HttpsURLConnection) url.openConnection();
			con.setRequestMethod(method);
			con.setRequestProperty("User-Agent", "cis455crawler");
			con.setInstanceFollowRedirects(false);
			for (Map.Entry<String, String> entry : request_headers.entrySet())
				con.setRequestProperty(entry.getKey(), entry.getValue());
			status = con.getResponseCode();
		}
		catch(Exception e) {
			System.out.println(e);
		}
		response_map.put("status", ""+status);
		if (status!=200 && status!=301 && status!=304 && status!=307)
		{
			input = null;
			return;
		}
		response_map.put("content-length", ""+con.getContentLength());
		response_map.put("content-type", con.getContentType());
		if (request_headers.containsKey("if-modified-since"))
			response_map.put("last-modified", getDate(con.getLastModified()));
		if(con.getHeaderField("location") != null)
			response_map.put("location", con.getHeaderField("location"));
		try {
			input = new BufferedReader(new InputStreamReader((con.getInputStream())));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		String output;
		boolean first = true;
		try {
			while ((output = input.readLine()) != null) {
				if (first) {
					sb.append(output);
					first = false;
					continue;
				}
				sb.append("\n"+output);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		body = sb.toString();
	}

	/**
	 * Function to return a buffered reader that is used to read the response
	 * @return
	 */
	public BufferedReader getBufferedReader()
	{
		return input;
	}

	/**
	 * Function to convert the time in milliseconds to a formatted string
	 * @param time
	 * @return
	 */
	private String getDate(long time)
	{
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String ret_date = sdf.format(date).toString();
		return ret_date;
	}

	/**
	 * Function to get the body of a response
	 * @return null in case it is a HEAD request and the body in case it is a GET request
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Function to get the Document corresponding to the body
	 * @return
	 */
	public Document getDocument() {
		Document doc = null ;
		send_request();
		String content_type = response_map.get("content-type");
		if (content_type == null) {
			if (url_str.trim().endsWith("html"))
				return get_doc_html(body);
			else if (url_str.trim().endsWith("xml"))
				return get_doc_xml(body);
			else
				return doc;
		}
		else {
			if (content_type.contains("html/plain"))
				return get_doc_html(body);
			else if (content_type.contains("xml"))
				return get_doc_xml(body);
			else
				return doc;
		}
	}

	/**
	 * Function to convert the HTML body to a document
	 * @param body
	 * @return
	 */
	private Document get_doc_html(String body)
	{
		Tidy tidy=new Tidy();
		tidy.setXHTML(true);
		tidy.setTidyMark(false);
		tidy.setShowWarnings(false);
		tidy.setQuiet(true);
		Document doc = tidy.parseDOM(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), null);
		return doc;
	}

	/**
	 * Function to convert the XML body to a document 
	 * @param body
	 * @return
	 */
	private Document get_doc_xml(String body)
	{
		DocumentBuilder db = null;
		try{
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch(Exception e) {
			System.out.println(e);
		}
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(body));
		Document doc = null;
		try{
			doc = db.parse(is);
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return doc;
	}
	
}