package edu.upenn.cis455.crawler;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sleepycat.je.Environment;
//import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;

import edu.upenn.cis455.storage.*;
import edu.upenn.cis455.xpathengine.*;
import edu.upenn.cis455.mapreduce.master.MasterClient;
// creating obj to access Hashmap and num of workers
import edu.upenn.cis455.mapreduce.worker.WorkerServlet;

public class CrawlerThread implements Runnable {

	private MyQueue urlsqueue;
	private static int max_size;
	private static int max_number_of_files = 1000;
	private NumberOfFilesCrawled number_of_files_crawled;
	private DBWrapper wrapper;
	private String directory;
	private Environment env;
	private Indices pk_index;

	// map ranges -- Hasshing Stufff 
	HashMap<Integer, BigInteger> mapRanges;


	public CrawlerThread(MyQueue queue, int num1, int num2, String dir) {
		number_of_files_crawled = new NumberOfFilesCrawled();
		urlsqueue = queue;
		max_size = num1;
		if (num2 > 0)
			max_number_of_files = num2;
		directory = dir;
		opendb();
		flush_seen_urls();
		flush_robots();
		// method stub for generating ranges 
		this.mapRanges();
	}

	/**
	 * Function to erase the URLS seen in the previous iteration from the database
	 */
	public void flush_seen_urls() {
		EntityCursor<URLS> old_urls = pk_index.urls_pk.entities();
		ArrayList<String> old_url_strs = new ArrayList<String>();
		for (URLS old_url : old_urls) {
			old_url_strs.add(old_url.getURL());		
		}
		old_urls.close();
		for (String old_url_str : old_url_strs) {
			//Transaction txn = env.beginTransaction(null, null);
			try {
				//System.out.println("[MAY THREE] Inside Transaction for URLs (2)");
				pk_index.urls_pk.delete(null, old_url_str);
				//txn.commit();
			}
			catch(Exception e) {
				// System.err.println("[MAY THREE] Inside ERROR Transaction for URLs (2)");
				e.printStackTrace();
				continue;
				/*
				if (txn != null) {
					txn.abort();
					txn = null;
				}
				 */
			}
		}
	}

	/**
	 * Function to erase the domains information of the previous iteration from the database
	 */
	public void flush_robots() {
		EntityCursor<Robots> robots = pk_index.robots_pk.entities();
		ArrayList<String> robots_domains = new ArrayList<String>();
		for (Robots robot : robots) {
			robots_domains.add(robot.getDomain());		
		}
		robots.close();
		for (String domain : robots_domains) {
			// Transaction txn = env.beginTransaction(null, null);
			try {
				// System.out.println("[MAY THREE] Inside Transaction for Robots.txt (3)");
				pk_index.robots_pk.delete(null, domain);
				// txn.commit();
			}
			catch(Exception e) {
				// System.err.println("[MAY THREE] Inside ERROR Transaction for Robots.txt (3)");
				e.printStackTrace();
				continue;
				/*
				if (txn != null) {
					txn.abort();
					txn = null;
				}
				 */
			}
		}
	}

	/**
	 * Function that opens the database
	 */
	public void opendb() {
		wrapper = new DBWrapper();
		wrapper.setup(directory);
		pk_index = new Indices(wrapper.getStore());
		env = wrapper.getEnvironment();
	}

	/**
	 * Function that closes the database
	 */
	public void closedb() {
		wrapper.shutdown();
	}

	public void run() {
		while (number_of_files_crawled.count() < max_number_of_files) {
			System.out.println("[MAY THREE] Number of files crawled till now in While loop +" + number_of_files_crawled.count());
			String url = null;
			try {
				url = urlsqueue.dequeue();
				if (url.trim().length() == 0)
					continue;
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			if (check_if_url_seen(url)) { // If the URL has been seen before in this particular iteration of the crawler, move on to the next one
				System.out.println("URL : "+url+" : Already Crawled Before");
				continue;
			}
			URL url_href = null;
			try {
				url_href = new URL(url);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				continue;
			}
			boolean crawled_before = false;
			long last_crawled_time = 0;
			if (!check_robots(url)) { // Check the domain's robots.txt to see if the url can be crawled
				//System.out.println("[ROBOTS] --- Robots.txt blocked crawler --- URL : "+url);
				continue;
			}
			// System.out.println("[MAY FOUR] After Robots.txt + Creating Request + ");
			Client client = new Client(url, "HEAD");
			if (check_if_url_crawled(url)) { // URL has been crawled in a previous iteration
				crawled_before = true;
				Crawled_URLS url_crawled_db = pk_index.crawled_urls_pk.get(url);
				last_crawled_time = url_crawled_db.getLastCrawledTime();
				String last_crawled = getDate(last_crawled_time);
				client.set_request_header("If-Modified-Since", last_crawled);
			}
			client.send_request();
			if (client.get_response_header("location")!=null) {
				String redirect_location = client.get_response_header("location");
				if (redirect_location.startsWith("/")) {
					redirect_location = url_href.getProtocol() + "://" + url_href.getHost() + redirect_location;
				}
				else if(redirect_location.startsWith("www")) {
					redirect_location = url_href.getProtocol() + "://" + redirect_location;
				}
				else if(!redirect_location.startsWith("http")) {
					System.out.println("[ERROR] Unknown URL protocol");
					continue;
				}
				// Add the redirected link back to the queue
				//urlsqueue.enqueue(redirect_location);
				this.workerEnqueue(redirect_location);
				continue;
			}
			if (client.get_response_header("status").equals("304") && crawled_before) {
				System.out.println("URL : "+url+" : Not Modified");
				// If content type is html, fetch content from db, extract urls and add them to the end of the queue
				Crawled_URLS crawled_url = pk_index.crawled_urls_pk.get(url);
				String old_content = crawled_url.getContent();
				if (crawled_url.getContentType().contains("html")) {
					// Extract links from content and add them to the end of the queue
					ArrayList<String> hrefs = get_hrefs(old_content, url_href);
					for (String href : hrefs) {
						if (href.trim().length() == 0)
							continue;
						//urlsqueue.enqueue(href);
						this.workerEnqueue(href);
					}
				}
				/*
				else if (crawled_url.getContentType().endsWith("xml")) {
					update_channels(url);
				}
				 */
				// Update the last crawled time in the database
				crawled_url.setLastCrawledTime(new Date().getTime());
				//Transaction txn = env.beginTransaction(null, null);
				try {
					// System.out.println("[MAY THREE] Inside Transaction for Crawled URLs (1)");
					pk_index.crawled_urls_pk.put(crawled_url);
					//txn.commit();
				}
				catch(Exception e) {
					// System.err.println("[MAY THREE] Inside ERROR Transaction for Crawled URLs (1)");
					e.printStackTrace();
					continue;
					/*
					if (txn != null) {
						txn.abort();
						txn = null;
					}
					 */
				}
				continue;
			}
			if (!client.get_response_header("status").equals("200")) {
				System.out.println("[ERROR] --- Status code is "+ client.get_response_header("status") +" --- URL : "+url);
				continue;
			}
			int content_length = Integer.parseInt(client.get_response_header("content-length"));
			String content_type = client.get_response_header("content-type");
			// If the size of the page is greater than the maximum allowed size, we skip it
			if (content_length > max_size) {
				System.out.println("[ERROR] --- File size exceeds maximum size --- URL : "+url);
				continue;
			}
			// If the content type of the file is neither html nor a variant of xml, we arent interested in such files
			if (!content_type.contains("html") && !content_type.endsWith("xml")) {
				System.out.println("[ERROR] --- Content type neither html nor a variant of xml --- URL : "+url);
				continue;
			}
			System.out.println("URL : "+url+" : Downloading");
			client = new Client(url, "GET");
			number_of_files_crawled.increment();
			last_crawled_time = new Date().getTime();
			client.send_request();
			String content = client.getBody();
			Crawled_URLS crawled_url;
			if (crawled_before) { // If it has been crawled before but the content has been modified
				crawled_url = pk_index.crawled_urls_pk.get(url);
			}
			else { // If it is the first time we are crawling the url
				crawled_url = new Crawled_URLS();
				crawled_url.setURL(url);
				crawled_url.setContentType(content_type);
			}
			crawled_url.setLastCrawledTime(last_crawled_time);
			crawled_url.setContent(content);
			//Transaction txn = env.beginTransaction(null, null);
			try {
				// System.out.println("[MAY THREE] Inside Transaction for Crawled URLs (2)");
				pk_index.crawled_urls_pk.put(crawled_url);
				//txn.commit();
			}
			catch(Exception e) {
				// System.err.println("[MAY THREE] Inside ERROR Transaction for Crawled URLs (2)");
				e.printStackTrace();
				continue;
				/*
				if (txn != null) {
					txn.abort();
					txn = null;
				}*/
			}
			// Also update last crawled time of the domain
			String domain = url_href.getHost();
			Robots robots_file = pk_index.robots_pk.get(domain);
			robots_file.setLastCrawledTime(last_crawled_time);
			//Transaction txn2 = env.beginTransaction(null, null);
			try {
				// System.out.println("[MAY THREE] Inside Transaction for Robots.txt (1)");
				pk_index.robots_pk.put(robots_file);
				//txn2.commit();
			}
			catch(Exception e) {
				// System.err.println("[MAY THREE] Inside ERROR Transaction for Robots.txt (1)");
				e.printStackTrace();
				continue;
				/*
				if (txn2 != null) {
					txn2.abort();
					txn2 = null;
				}*/
			}
			if (!crawled_before) { // If the page is seen for the first time, add the URL to the list of seen urls for this iteration of the crawler
				URLS new_url = new URLS();
				new_url.setURL(url);
				//Transaction txn3 = env.beginTransaction(null, null);
				try {
					// System.out.println("[MAY THREE] Inside Transaction for URLs (1)");
					pk_index.urls_pk.put(new_url);
					//txn3.commit();
				}
				catch(Exception e) {
					// System.out.println("[MAY THREE] Inside ERROR Transaction for URLs (1)");
					e.printStackTrace();
					continue;
					/*
					if (txn3 != null) {
						txn3.abort();
						txn3 = null;
					}
					 */
				}
			}
			if (content_type.contains("html")) {
				// Extract links from content and add them to the end of the queue
				ArrayList<String> hrefs = get_hrefs(content, url_href);
				for (String href : hrefs) {
					if (href.trim().length() == 0)
						continue;
					//urlsqueue.enqueue(href);
					this.workerEnqueue(href);
				}
			}
			/*
			else {
				// Match xml documents against all the xpaths for all channels and store them where there is a match
				update_channels(url);
			}
			 */
		}
		System.out.println("[MAY THREE] Number of files crawled at end of loop +" + number_of_files_crawled.count());
	}

	/**
	 * Function to convert a string to a document
	 * @param body
	 * @return
	 */
	private Document string_to_doc(String body)
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
			return null;
		}
		return doc;
	}

	/**
	 * Function to match an xml document with all the xpaths of all the channels and store them corresponding to channels that match
	 * @param url containing the xml content
	 */
	private void update_channels(String url) {
		String content = "";
		content = pk_index.crawled_urls_pk.get(url).getContent();
		Document doc = string_to_doc(content);
		if (doc == null) {
			return;
		}
		XPathEngineImpl xpathengine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
		EntityCursor<Channels> channels = pk_index.channels_pk.entities();
		ArrayList<String> matched_urls;
		ArrayList<String> xpaths;
		ArrayList<Channels> all_channels = new ArrayList<Channels>();
		for (Channels channel : channels) {
			all_channels.add(channel);
		}
		channels.close();
		for (Channels channel : all_channels) {
			matched_urls = channel.getMatchedURLS();
			xpaths = channel.getXPaths();
			for (String xpath : xpaths) {
				// If atleast one of the xpaths matches the document, add it to the channel
				String[] paths = {xpath};
				xpathengine.setXPaths(paths);
				boolean result = xpathengine.evaluate(doc)[0];
				if (result) {
					if (!matched_urls.contains(url))
						matched_urls.add(url);
					break;
				}
			}
			/*
			channel.setMatchedURLS(matched_urls);
			Transaction txn = env.beginTransaction(null, null);
			try {

				pk_index.channels_pk.put(channel);
				txn.commit();
			}
			catch(Exception e) {
				e.printStackTrace();
				if (txn != null) {
					txn.abort();
					txn = null;
				}
			}
			 */
		}
	}

	/**
	 * Function to extract the hrefs and variants from a html page
	 * @param html
	 * @param urlObj
	 * @return
	 */
	private ArrayList<String> get_hrefs(String html, URL url) {
		ArrayList<String> links = new ArrayList<String>();
		Pattern p = Pattern.compile("href=\"(.*?)\"", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);
		while (m.find()) {
			String link = m.group(1);
			// If it is a relative href link, normalize it
			if(link.startsWith("/")) {
				String path = url.getPath();
				if(path.endsWith(".html") || path.endsWith(".htm"))
					path = path.substring(0,path.lastIndexOf("/"));
				else if(path.endsWith("/"))
					path = path.substring(0, path.length() - 1);
				link = url.getProtocol() +"://" + url.getHost()+ path  + link;				
			} 
			else if(!link.startsWith("www.") && !link.startsWith("http")) {
				String path = url.getPath();
				if(path.endsWith(".html") || path.endsWith(".htm"))
					path = path.substring(0,path.lastIndexOf("/"));
				else if(path.endsWith("/"))
					path = path.substring(0, path.length() - 1);
				link = url.getProtocol() +"://" + url.getHost()+ path  + "/" + link;
			}
			// If it is a non-absolute link
			else if(!link.startsWith("http"))
				link = "http://" + link; 
			links.add(link);
		}		   
		return links;
	}

	/**
	 * Function to see if the robots.txt of the domain allows us to crawl the page represented by the url
	 * @param url
	 * @return true if we are allowed crawl and false otherwise
	 */
	public boolean check_robots(String url) {
		URL link = null;
		String domain;
		long last_crawled_time = 0;
		long crawl_delay = 0;
		ArrayList<String> allowed_links = new ArrayList<String>();
		ArrayList<String> disallowed_links = new ArrayList<String>();
		try {
			link = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		domain = link.getHost();
		if (check_if_robots_exists(domain)) { // If the robots.txt for this particular domain has already been parsed in this iteration
			Robots r = pk_index.robots_pk.get(domain);
			last_crawled_time = r.getLastCrawledTime();
			crawl_delay = r.getCrawlDelay();
			allowed_links = r.getAllowedLinks();
			disallowed_links = r.getDisallowedLinks();
		}
		else {
			// Send get request to domain/robots.txt, parse that and fill above values. Then create a Robots object, set above values and store in database
			String req_url = link.getProtocol() + "://" + domain + "/robots.txt";
			System.out.println("[ROBOTS] Request URL for ROBOTS.txt + " + req_url );
			Client client = new Client(req_url, "GET");
			client.send_request();
			String response_code = client.get_response_header("status");
			// System.out.println("[ROBOTS] Response Header for Robots.txt + " + response_code);
			if (response_code.equals("200")) {
				String[] file_lines = client.getBody().split("\n");
				boolean ua_for_me = false; // True if 'User-agent: <mycrawler>' is present in the robots.txt file and false otherwise
				boolean start_storing = false;
				for (String line : file_lines) {
					if (start_storing) {
						if (line.contains("User-agent: cis455crawler")) {
							allowed_links = new ArrayList<String>();
							disallowed_links = new ArrayList<String>();
							crawl_delay = 0;
							continue;
						}
						else if (line.contains("User-agent:")) {
							start_storing = false;
							continue;
						}
						else if (line.contains("Allow:")) {
							String rule = line.split(":",2)[1];
							try {
								if (rule.contains("*") || rule.contains("?") || rule.contains("$"))
									continue;
								allowed_links.add(URLDecoder.decode(rule, "UTF-8").trim());
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
						else if (line.contains("Disallow:")) {
							String rule = line.split(":",2)[1];
							try {
								if (rule.contains("*") || rule.contains("?") || rule.contains("$"))
									continue;
								disallowed_links.add(URLDecoder.decode(rule, "UTF-8").trim());
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
						else if (line.contains("Crawl-delay") && start_storing) {
							int seconds = Integer.parseInt(line.split(":",2)[1].trim());
							//crawl_delay = (long) seconds * 1000;
							crawl_delay = 1000;
						}
					}
					if (line.contains("User-agent: cis455crawler")) {
						start_storing = true;
					}
					if (line.contains("User-agent: *") && !ua_for_me) {
						start_storing = true;
					}
				}
			}
			else {
				System.out.println("[ROBOTS] Robots.txt not present for : "+req_url+". Therefore adding null values.");
			}
			Robots r = new Robots();
			r.setAllowedLinks(allowed_links);
			r.setDisallowedLinks(disallowed_links);
			r.setCrawlDelay(crawl_delay);
			r.setLastCrawledTime(0);
			Robots new_robots = new Robots();
			new_robots.setDomain(domain);
			new_robots.setCrawlDelay(crawl_delay);
			new_robots.setLastCrawledTime(last_crawled_time);
			new_robots.setAllowedLinks(allowed_links);
			new_robots.setDisallowedLinks(disallowed_links);
			// Transaction txn = env.beginTransaction(null, null);
			try {
				// System.out.println("[MAY THREE] Inside Transaction for Robots.txt (2)");
				// System.out.println("[MAY FOUR] Before Adding Robots.txt");
				pk_index.robots_pk.put(new_robots);
				// System.out.println("[MAY FOUR] After Adding Robots.txt");
				// txn.commit();
			}
			catch(Exception e) {
				// System.err.println("[MAY FOUR] Error Adding Robots.txt in DB");
				// System.err.println("[MAY THREE] Inside ERROR Transaction for Robots.txt (2)");
				// e.printStackTrace();
				/*
				if (txn != null) {
					txn.abort();
					txn = null;
				}
				 */
			}
		}
		long current_time = new Date().getTime();
		if (current_time - last_crawled_time < crawl_delay) {
			//urlsqueue.enqueue(url);
			this.workerEnqueue(url);
			return false;
		}
		try {
			String query_string = url.split(domain)[1];
			for (String allowed_link : allowed_links) {
				if (query_string.startsWith(allowed_link)) { // If its an allowed link, return true
					return true;
				}
			}
			for (String disallowed_link : disallowed_links) {
				if (query_string.startsWith(disallowed_link)) { // If it is a disallowed link, return false
					return false;
				}
			}
		}
		catch(Exception e)
		{
			return true;
		}
		// If none of the Allow or Disallow rules matched, consider it as allow 
		return true;
	}

	/**
	 * Function to check if the robots.txt file for the domain already exists in our database
	 * @param domain
	 * @return true if robots.txt for the given domain already exists and false otherwise
	 */
	public boolean check_if_robots_exists(String domain) {
		try {
			String dom = pk_index.robots_pk.get(domain).getDomain();
		}
		catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Function to check if the URL already exists in our URLS table, i.e whether the URL has been seen in this iteration
	 * @param url
	 * @return true if URL already exists and false otherwise
	 */
	public boolean check_if_url_seen(String url) {
		try {
			String link = pk_index.urls_pk.get(url).getURL();
		}
		catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Function to check if the URL has already been crawled before
	 * @param url
	 * @return true if URL has been crawled and false otherwise
	 */
	public boolean check_if_url_crawled(String url) {
		try {
			String link = pk_index.crawled_urls_pk.get(url).getURL();
		}
		catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Function to convert the time in milliseconds to a particular string format
	 * @param time
	 * @return
	 */
	String getDate(long time)
	{
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String ret_date = sdf.format(date).toString();
		return ret_date;
	}

	// Hashing Functions 
	public void mapRanges () {
		this.mapRanges = new HashMap<Integer, BigInteger>();

		String minValue = "0000000000000000000000000000000000000000";
		String maxValue = "ffffffffffffffffffffffffffffffffffffffff";

		BigInteger maxIntVal = new BigInteger(maxValue, 16);
		BigInteger minIntVal = new BigInteger(minValue, 16);
		BigInteger numWorkerInt = new BigInteger(Integer.toString(WorkerServlet.numWorkers));

		// get range for given values
		BigInteger rangeVal = maxIntVal.divide(numWorkerInt);

		for (int index = 1; index <= WorkerServlet.numWorkers; index++) {
			minIntVal = minIntVal.add(rangeVal);
			// System.out.println("[MAY THREE] Range value added in MAP RANGES + " + index + "   " + minIntVal);
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
			// System.out.println("[MAY THREE] No of Workers + " + WorkerServlet.numWorkers);
			for(int index = 1; index <= WorkerServlet.numWorkers; index++) {
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

	@SuppressWarnings("unused")
	private void workerEnqueue (String url) {
		int getWorkerID = this.getWorkerId(url);
		String worker = "worker"+getWorkerID;
		String workers = WorkerServlet.mapworkersIPPort.get(worker);
		//String requestUrl = "http://" + workers + "/HW2/worker/runcrawler";
		//String requestUrl = "http://" + workers + "/Crawler/worker/runcrawler";
		String requestUrl = "http://" + workers + "/Mercator/worker/runcrawler";
		// System.out.println("[MAY THREE] Sending Crawler Request + " + requestUrl);
		String requestParams = "";
		int iSubWorker = 1;
		int index = 1;
		int iWorker = 1;
		for (String subworker: WorkerServlet.mapworkersIPPort.keySet()) {
			if (index == 1){
				requestParams += "worker" + iWorker + "=" + WorkerServlet.mapworkersIPPort.get(subworker);
				index = -1;
			}
			else 
				requestParams += "&" + "worker" + iWorker + "=" + WorkerServlet.mapworkersIPPort.get(subworker);
			iSubWorker++;

		}
		requestParams += "&" + "numWorkers=" + WorkerServlet.mapworkersIPPort.keySet().size();
		requestParams += "&" + "seed=" + url;
		System.out.println("[ENQUEUE WORKER] Enqueue + " + url);
		// send request and request params to MasterClient 
		MasterClient clientThread = new MasterClient (requestUrl, requestParams);
		clientThread.start();

	}
}
