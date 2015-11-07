package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.mapreduce.master.WorkerStatusParams;


/**
 * Master Servlet
 * @author Aakriti Singla
 *
 */
@SuppressWarnings("unused")
public class MasterServlet extends HttpServlet {

	static final long serialVersionUID = 455555001;

	boolean isIdle = true;
	boolean isWaiting = true;
	// map ranges 
	HashMap<Integer, BigInteger> mapRanges;
	// array to store hashed urls to workers 
	HashMap<String , ArrayList<String>> mapWorkerSeedURL = new HashMap<String,ArrayList<String>>();

	// hash map to store /workerstatus request params
	HashMap<String, WorkerStatusParams> mapworkerStatusParams = new HashMap<String, WorkerStatusParams>();
	// hash map to store /runmap request params 
	HashMap<String, String> maprunMapRequestParams = new HashMap<String, String>();
	
	// initialization
	public void init () {
		System.out.println("In init of MasterServlet");
	}

	/**
	 * doGet function for Master Servlet
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		System.out.println("[Info] In doGet of Master Servlet");
		// various URL requests to handle 
		String urlRequest = request.getRequestURI();
		System.out.println("[Info] In doGet Master Servlet + " + urlRequest);
		/* switch(urlRequest) {
		case "/master/workerstatus" :
			// call function for workerstatus request
			this.storeWorkerStatus(request, response);
			break;
		case "/master/status" :
			// call function for status request
			this.displayStatusRequest(request, response);
			break;
		}*/
		if (urlRequest.contains("/master/workerstatus"))
			this.storeWorkerStatus(request, response);
		else if (urlRequest.contains("/master/status"))
			this.displayStatusRequest(request, response);
	}
	/**
	 * function to store worker status for GET /workerstatus request
	 * @param request
	 * @param response
	 * @throws java.io.IOException
	 */
	public void storeWorkerStatus(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		PrintWriter out = response.getWriter();
		System.out.println("inside storeworker status");
		response.setContentType("text/html");
		
		//String ipAddress = (String) request.getAttribute("IPAddress");
		
		// extract IPAddress for the client 
		String ipAddress = request.getHeader("X-FORWARDED-FOR");  
		if (ipAddress == null) {  
			ipAddress = request.getRemoteAddr();  
		}

		// store data for workerstatus
		WorkerStatusParams worker = new WorkerStatusParams();
		worker.port = Integer.parseInt(request.getParameter("port"));
		//worker.job = request.getParameter("job");
		worker.status = request.getParameter("status");
		//worker.keysRead = Integer.parseInt(request.getParameter("keysRead"));
		//worker.keysWritten = Integer.parseInt(request.getParameter("keysWritten"));
		worker.timeRecieved = System.currentTimeMillis();

		
		this.mapworkerStatusParams.put(ipAddress+":"+worker.port, worker);
		System.out.println("Worker status: "+ worker.status);
		out.close();

		// check if all are waiting 
		for(String workerS : this.mapworkerStatusParams.keySet()) {
			if(!(this.mapworkerStatusParams.get(workerS).status.equalsIgnoreCase("waiting")))
				this.isWaiting = false;
		}
		// call /runreduce on each active worker
		/*
		System.out.println("Value of isWaitin + " + isWaiting);
		System.out.println("Hash Map + " + this.mapworkerStatusParams);
		if(this.isWaiting){
			// send parameters to all workers
			for(String workers: this.mapworkerStatusParams.keySet()) {
				String requestUrl = "http://" + workers + "/worker/runreduce";
				String requestParams = "";
				requestParams += "job=" + this.maprunMapRequestParams.get("classname");
				requestParams += "&" + "output=" + this.maprunMapRequestParams.get("outputdir");
				requestParams += "&" + "numThreads=" + this.maprunMapRequestParams.get("numreducethreads");
				// send request and request params to MasterClient

				MasterClient clientThread = new MasterClient (requestUrl, requestParams);
				clientThread.start();
			}
		}
		*/
		
		this.isWaiting = true;
	}

	/**
	 * displayStatusRequest for processing of GET /status request
	 * @param request
	 * @param response
	 * @throws java.io.IOException
	 */
	public void displayStatusRequest(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		// print writer object for displaying status table webpage 
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.println("<html><body>");
		out.println("&nbsp;&nbsp;&nbsp;&nbsp;<h1 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>Status Display</font></h1><hr/>");
		out.println("<table align = 'center'><tr><th>Active Worker</th><th>Status</th><th>Job</th>><th>keysRead</th>><th>keysWritten</th></tr>");

		// for loop for accessing keySet in HashMap
		for(String worker : this.mapworkerStatusParams.keySet()) {
			Long timeGap = System.currentTimeMillis() - this.mapworkerStatusParams.get(worker).timeRecieved;
			String status = this.mapworkerStatusParams.get(worker).status;
			if(! status.toLowerCase().equals("idle"))
				this.isIdle = false;
			if(timeGap < 30000)
				//out.println("<tr><td>"+this.mapworkerStatusParams.get(worker).port+"</td><td>" + this.mapworkerStatusParams.get(worker).status + "</td><td>"+ this.mapworkerStatusParams.get(worker).job + "</td><td>" + this.mapworkerStatusParams.get(worker).keysRead + "</td><td>" + this.mapworkerStatusParams.get(worker).keysWritten +"</td></tr>");
				out.println("<tr><td>"+this.mapworkerStatusParams.get(worker).port+"</td><td>" + this.mapworkerStatusParams.get(worker).status + "</td></tr>");
			else
				// remove the worker whose timegap > 30000
				this.mapworkerStatusParams.remove(worker);
		}
		out.println("</table></br>");

		// web-form - display only when all workers are idle and no job is being processed
		if(this.isIdle){
			out.println("<html><body>");

			out.println("<form action='master' method = 'POST'>");
			out.println("<h1 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>Submit Job</font></h1><br/>");
			// creating text box for Class Name of Job
			out.println("<table align = 'center'><tr>");
			//out.println("<td align = 'center'><font color = '#009933'><label>Class Name</label></font></td>");
			out.println("<td align = 'center'><font color = '#009933'><label>Seed URLs</label></font></td>");
			//out.println("<td align = 'center'><input type='text' name='classname' id='classname' /></td></tr>");
			out.println("<td align = 'center'><input type='text' name='seeds' id='seeds' /></td></tr>");
			// creating text box for Input directory - relative to storage directory 
			//out.println("<tr><td align='center'><font color = '#009933'><label>Input Directory</label></td>");
			//out.println("<td align='center'><input type='text' name='inputdir' id='inputdir' /></td></tr>");
			// creating text box for Output directory - relative to storage directory 
			//out.println("<tr><td align='center'><font color = '#009933'><label>Output Directory</label></td>");
			//out.println("<td align='center'><input type='text' name='outputdir' id='outputdir' /></td></tr>");
			// creating text box for Number of map threads 
			//out.println("<tr><td align='center'><font color = '#009933'><label>Numbet of map threads</label></td>");
			//out.println("<td align='center'><input type='text' name='nummapthreads' id='nummapthreads' /></td></tr>");
			// creating text box for Number of reduce threads 
			//out.println("<tr><td align='center'><font color = '#009933'><label>Numbet of reduce threads</label></td>");
			//out.println("<td align='center'><input type='text' name='numreducethreads' id='numreducethreads' /></td></tr>");

			out.println("</table><p align='center'><input type='submit' class='submit' value='Submit'/></p>");
			out.println("</form>");
		}
		out.println("</body></html>");
	}

	/**
	 * doPost function for Master Servlet
	 * Create a /runmap request
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		// send /runmap request - set required params for POST request
		System.out.println("[Info] In doPost of Master Servlet");
		// display redirect page for /status request 
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.println("<html><body>");
		out.println("&nbsp;&nbsp;&nbsp;&nbsp;<h1 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>Jobs Sent to the Workers</font></h1><hr/>");
		out.println("<form action='/master/status' method = 'GET'>");
		out.println("<h3 align='middle' padding = '0' margin = '0'><font color = '#0033CC'>Get Status</font></h3><br/>");
		//out.println("<p align='center'><input type='submit' class='submit' value='Redirect to Status Display'/></p>");
		out.println("</form>");
		out.println("</body></html>");

		// store parameters for POST request in hash map to access later
		/*
		this.maprunMapRequestParams.put("classname", request.getParameter("classname"));
		this.maprunMapRequestParams.put("inputdir",request.getParameter("inputdir"));
		this.maprunMapRequestParams.put("nummapthreads",request.getParameter("nummapthreads"));
		this.maprunMapRequestParams.put("numworkers",Integer.toString(this.mapworkerStatusParams.size()));
		*/
		
		this.maprunMapRequestParams.put("seeds", request.getParameter("seeds"));
		
		// adding extra parameters
		/*
		this.maprunMapRequestParams.put("outputdir", request.getParameter("outputdir"));
		this.maprunMapRequestParams.put("numreducethreads",request.getParameter("numreducethreads"));
		*/

		/*
		// send parameters to all workers
		for(String workers: this.mapworkerStatusParams.keySet()) {
			//String requestUrl = "http://" + workers + "/worker/runmap";
			String requestUrl = "http://" + workers + "/worker/runcrawler";
			String requestParams = "";
			requestParams += "seeds=" + this.maprunMapRequestParams.get("seeds");
			//requestParams += "&" + "input=" + this.maprunMapRequestParams.get("inputdir");
			//requestParams += "&" + "numThreads=" + this.maprunMapRequestParams.get("nummapthreads");
			//requestParams += "&" + "numWorkers=" + this.maprunMapRequestParams.get("numworkers");
			int iWorker = 1;
			for (String subworker: this.mapworkerStatusParams.keySet()) {
				requestParams += "&" + "worker" + iWorker + "=" + subworker;
				iWorker++;

			}
			// send request and request params to MasterClient 
			MasterClient clientThread = new MasterClient (requestUrl, requestParams);
			clientThread.start();
		}
		*/
		// method stub for generating ranges 
		this.mapRanges();
		
		// call function to split seed urls based on semi colons and hash it to workers
		String[] list_seed_urls = request.getParameter("seeds").split(";");
		
		for (String subseed : list_seed_urls) {
			int getWorkerID = this.getWorkerId(subseed);
			String worker = "worker"+getWorkerID;
			if ( this.mapWorkerSeedURL.get(worker) == null )
				this.mapWorkerSeedURL.put(worker, new ArrayList<String>());
			this.mapWorkerSeedURL.get(worker).add(subseed);
		}
		
		// send parameters to all workers 
		for (String key : this.mapWorkerSeedURL.keySet()) {
			System.out.println("[DEBUG] For Worker : " + key);
			for (String value : this.mapWorkerSeedURL.get(key))
				System.out.println("			: " + value);
		}
	}
	
	/**
	 * function to get ranges for each worker
	 * @param numWorkers
	 */
	public void mapRanges () {
		this.mapRanges = new HashMap<Integer, BigInteger>();

		String minValue = "0000000000000000000000000000000000000000";
		String maxValue = "ffffffffffffffffffffffffffffffffffffffff";

		BigInteger maxIntVal = new BigInteger(maxValue, 16);
		BigInteger minIntVal = new BigInteger(minValue, 16);
		BigInteger numWorkerInt = new BigInteger(Integer.toString(this.mapworkerStatusParams.keySet().size()));

		// get range for given values
		BigInteger rangeVal = maxIntVal.divide(numWorkerInt);

		for (int index = 1; index <= this.mapworkerStatusParams.keySet().size(); index++) {
			minIntVal = minIntVal.add(rangeVal);
			this.mapRanges.put(index, minIntVal);
		}
	}
	
	/**
	 * Get Hash for the given key 
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private String fetchHash(String key) throws NoSuchAlgorithmException {
		MessageDigest message;
		message = MessageDigest.getInstance("SHA-1");
		message.update(key.getBytes());
		byte byteData[] = message.digest();

		StringBuffer writer = new StringBuffer();
		for (int j = 0; j < byteData.length; j++) {
			writer.append(Integer.toString((byteData[j] & 0xff) + 0x100, 16).substring(1));
		}
		// System.out.println("[DEBUG] In fetchHash writer +" + writer);
		return writer.toString();
	}
	/**
	 * to get mapping worker id
	 * @param key
	 * @return
	 */
	private int getWorkerId(String key) {
		try {
			String hash = this.fetchHash(key);
			BigInteger hashVal = new BigInteger(hash, 16);
			for(int index = 1; index <= this.mapworkerStatusParams.keySet().size(); index++) {
				if((hashVal.compareTo(this.mapRanges.get(index))) < 0 ) {
					return index;
				}
			}
		} 
		catch (NoSuchAlgorithmException e) {
			System.err.println("[ERROR] In get Worker ID + " + e);
		}
		return 0;
	}

}

