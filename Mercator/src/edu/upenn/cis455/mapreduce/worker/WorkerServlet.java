package edu.upenn.cis455.mapreduce.worker;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.upenn.cis455.crawler.XPathCrawler;

/**
 * Worker Servlet 
 * @author Aakriti Singla
 *
 */
public class WorkerServlet extends HttpServlet {

	static final long serialVersionUID = 455555002;
	public WorkerStatusParams workerParams;
	public String masterServer;
	public String rootStorageDir;
	HashMap<String, String> maprunMapParams = new HashMap<String, String>();
	HashMap<String, String> maprunReduceParams = new HashMap<String, String>();
	public static HashMap<String, String> mapworkersIPPort = new HashMap<String, String>();
	int workerIndex = 1;
	// for pushdata request
	int workerCount = 0;
	int numberWorkers = 0;
	public static int numWorkers = 0;
	BlockingQueue<String> syncFileLineQueue;
	//MapReduceContext contextObj;
	XPathCrawler crawler = null;

	public boolean isMapReduceThreadAlive = true;
	public boolean isWorkersMapped = false;
	
	/**
	 * Inner Class
	 * WorkerStatusClient to invoke /workerstatus request 
	 * @author Aakriti Singla
	 *
	 */
	public class WorkerStatusClient extends Thread {
		boolean isWorkerStatusRequest = true;
		// public String masterServer;
		// public WorkerStatusParams workerParams;


		/**
		 * Parameterized Constructor to initialize /workerstatus request
		 * @param isWorkerStatusRequest
		 */
		public WorkerStatusClient(boolean isWorkerStatusRequest) {
			this.isWorkerStatusRequest = isWorkerStatusRequest;
			// this.masterServer = masterServer;
			// this.workerParams = workerParams;
		}

		/**
		 * Override run function
		 */
		@Override
		public void run() {
			do {
				try {
					this.isWorkerStatusRequest = false;
					// System.out.println("[MAY FOUR] In run function for Worker Status Thread");
					//String requestUrl = "http://" + masterServer + "/HW2/master/workerstatus" + "?port=" + workerParams.port + "&job=" + workerParams.job  + "&status=" + workerParams.status + "&keysRead=" + workerParams.keysRead + "&keysWritten=" + workerParams.keysWritten + "&timeRecieved=" + workerParams.timeRecieved;
					//String requestUrl = "http://" + masterServer + "/HW2/master/workerstatus" + "?port=" + workerParams.port + "&status=" + workerParams.status + "&timeRecieved=" + workerParams.timeRecieved;
					//String requestUrl = "http://" + masterServer + "/Crawler/master/workerstatus" + "?port=" + workerParams.port + "&status=" + workerParams.status + "&timeRecieved=" + workerParams.timeRecieved;
					String requestUrl = "http://" + masterServer + "/Mercator/master/workerstatus" + "?port=" + workerParams.port + "&status=" + workerParams.status + "&timeRecieved=" + workerParams.timeRecieved;
					// System.out.println("[DEBUG] Sendng request to master + " + requestUrl);
					URL masterUrl = new URL(requestUrl);
					HttpURLConnection connection = (HttpURLConnection) masterUrl.openConnection();
					connection.setRequestMethod("GET");
					connection.setDoOutput(true);
					connection.getResponseCode();
					connection.disconnect();
					try {
						Thread.sleep(10000);
					} 
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.err.println("[ERROR] In WorkerStatusClient -Interupt +" + e);
					}
				}
				catch(MalformedURLException ex) {
					System.err.println("[ERROR] In WorkerStatusClient +" + ex);
				}
				catch(IOException ex) {
					System.err.println("[ERROR] In WorkerStatusClient +" + ex);
				}

			} while (this.isWorkerStatusRequest);
		}
	}
	/**
	 * Init function to start Worker Servlet
	 */
	@Override
	public void init (final ServletConfig config) throws ServletException {

		System.out.println("[DEBUG] Initializing Worker Servlet");
		
		this.masterServer = config.getInitParameter("master");
		// this.rootStorageDir = config.getInitParameter("storagedir");

		// store hash map for /runmap request
		// this.maprunMapParams.put("rootstoragedir", this.rootStorageDir);

		this.workerParams = new WorkerStatusParams();
		this.workerParams.port = Integer.parseInt(config.getInitParameter("port"));
		// this.workerParams.job = null;
		// this.workerParams.keysRead = 0;
		// this.workerParams.keysWritten = 0;
		this.workerParams.timeRecieved = System.currentTimeMillis();

		// calling WorkerStatusClient to invoke /workerstatus request every 10 secs
		Thread workerstatus = new WorkerStatusClient(true);
		workerstatus.start();
		
		// starting crawler object
		// String dbBase = "/home/cis455/workspace/Crawler/database";
		/*
		ServletContext context = this.getServletContext();
		
		Enumeration name = context.getInitParameterNames();
		while(name.hasMoreElements())
			System.out.println("Name +" +  name.nextElement());
		String dbBase = context.getInitParameter("BDBstore");
		*/
		String dbBase = config.getInitParameter("BDBstore");
		// System.out.println("[MAY THREE] Database Captured + " + dbBase);
		int size = 100000;
		int num = 2000;
		crawler = new XPathCrawler(dbBase, size, num);		
	}
	/**
	 * doGet for Worker Servlet
	 */
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		System.out.println("[Info] In doGet of Worker Servlet");
	}
	
	/**
	 * doPost for Worker Servlet
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
		System.out.println("[Info] In doPost of Worker Servlet");
		// various URL requests to handle 
		String urlRequest = request.getRequestURI();
		// System.out.println("[DEBUG] value of urlRequest + " + urlRequest);
		
		/*
		switch(urlRequest) {
		//case "/HW2/worker/runcrawler" :
		case "/Mercator/worker/runcrawler" :
			// call function for runmap request
			System.out.println("[DEBUG] value of urlRequest + " + urlRequest);
			// function call
			this.runcrawler(request,response);
			break;		
		}
		*/
		
		if (urlRequest.contains("/worker/runcrawler"))
			// System.out.println("[DEBUG] value of urlRequest + " + urlRequest);
			this.runcrawler(request,response);

	}
	
	public void runcrawler(HttpServletRequest request, HttpServletResponse response){
		int numWorkers = Integer.parseInt(request.getParameter("numWorkers"));
		
		// set static variable to access in Crawler
		WorkerServlet.numWorkers = numWorkers;
		if (this.isWorkersMapped == false){
			for(int index = 1; index <= numWorkers ; index++)
				// storing hash maps for workers and their ip:port
				// set static variable to access in Crawler
				WorkerServlet.mapworkersIPPort.put("worker"+index, request.getParameter("worker"+index));
			this.isWorkersMapped = true;
		}
		String crawlSeedUrl = request.getParameter("seed");
		crawler.enqueueSeedUrl(crawlSeedUrl);
		
		// this.setCurrentWorkerID();
		this.workerParams.status = "crawling";
	}
	/**
	 * to determine current worker id 
	 */
	/*
	public void setCurrentWorkerID() {
		for (String worker : this.mapworkersIPPort.keySet())
			if(this.mapworkersIPPort.get(worker).contains(Integer.toString(this.workerParams.port)))
				this.workerIndex = Integer.parseInt(worker.split("worker")[1]);
	}
	*/
}

