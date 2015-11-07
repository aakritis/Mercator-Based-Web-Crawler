package edu.upenn.cis455.mapreduce.master;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Master Client to create requests for POST request /runmap
 * @author Aakriti Singla
 *
 */
public class MasterClient extends Thread{

	String requestUrl;
	String requestUrlParams;
	/**
	 * Default Constructor
	 */
	public MasterClient() {
		this.requestUrl = "";
		this.requestUrlParams = "";
	}
	/**
	 * Parameterized Constructor
	 * @param requestUrl
	 * @param requestUrlParams
	 */
	public MasterClient (String requestUrl , String requestUrlParams) {
		this.requestUrl = requestUrl;
		this.requestUrlParams = requestUrlParams;
	}

	/**
	 * override run function to send POST request for URL
	 */
	@Override
	public void run() {
		try {
			URL masterUrl = new URL(this.requestUrl);
			// set up connection to send post request
			HttpURLConnection connection =  (HttpURLConnection) masterUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(this.requestUrlParams.getBytes().length));
			connection.setDoOutput(true);

			// write data to output stream
			DataOutputStream writter = new DataOutputStream(connection.getOutputStream());
			writter.writeBytes(this.requestUrlParams);
			writter.flush();
			writter.close();

			// read data from input stream
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String eachLine = "";
			while ((eachLine = reader.readLine()) != null)
				System.out.println("[INFO]" + eachLine);
			reader.close();
			
			// close connection
			connection.disconnect();
		}
		catch(MalformedURLException ex) {
			System.err.println("[ERROR] In Master Client +" + ex);
		}
		catch(IOException ex) {
			System.err.println("[ERROR] In Master Client +" + ex);
		}
	}
}
