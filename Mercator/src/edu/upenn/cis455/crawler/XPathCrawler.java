package edu.upenn.cis455.crawler;

import java.util.ArrayList;

import com.sleepycat.je.Environment;
//import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.Indices;
import edu.upenn.cis455.storage.URLS;

@SuppressWarnings("unused")
public class XPathCrawler implements Runnable {  

	private String seed_url;
	private String directory;
	private int max_size;
	private int max_number_of_files;
	private MyQueue urlsqueue;
	private ArrayList<Thread> threadpool = new ArrayList<Thread>();
	private boolean isfirsturl = true;

	// changes as per project
	public XPathCrawler(String dir, int size, int num) {
		directory = dir;
		max_size = size * 1024 * 1024;
		max_number_of_files = num;
		urlsqueue = new MyQueue(directory);
		/*
		try {
			Thread t = new Thread(this);
			t.start();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		CrawlerThread crawler = new CrawlerThread(urlsqueue, max_size, max_number_of_files, directory);
		for(int i=0; i<20; i++) {
			Thread t = new Thread(crawler);
			threadpool.add(t);
		}
		for (Thread t : threadpool) { //Start all the worker threads
			t.start();
		}
		 */
	}

	/*
	public XPathCrawler(String url, String dir, int size, int num) {
		if (!url.startsWith("http"))
			url = "http://" + url;
		seed_url = url;
		directory = dir;
		max_size = size * 1024 * 1024;
		max_number_of_files = num;
		urlsqueue = new MyQueue(directory);
		urlsqueue.enqueue(url); // Add the seed URL to the queue
		try {
			Thread t = new Thread(this);
			t.start();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		CrawlerThread crawler = new CrawlerThread(urlsqueue, max_size, max_number_of_files, directory);
		for(int i=0; i<20; i++) {
			Thread t = new Thread(crawler);
			threadpool.add(t);
		}
		for (Thread t : threadpool) { //Start all the worker threads
			t.start();
		}
	}
	*/

	public void run() {
		while (true) {
			if (urlsqueue.isempty()) {
				for (Thread t : threadpool) {
					while (t.isAlive()) {
						if (t.getState().toString().equals("WAITING")) {
							t.stop();
						}
					}
				}
				break;
			}
		}
	}

	// enqueue incoming seed url 
	public void enqueueSeedUrl (String url) {
		if (!url.startsWith("http"))
			url = "http://" + url;
		seed_url = url;
		urlsqueue.enqueue(url); // Add the seed URL to the queue
		if(this.isfirsturl == true) {
			System.out.println("[MAY FOUR] Initializing THreads for XPath Crawler + ");
			try {
				Thread t = new Thread(this);
				t.start();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			CrawlerThread crawler = new CrawlerThread(urlsqueue, max_size, max_number_of_files, directory);
			// trying single threaded
			for(int i=0; i < 10; i++) {
				Thread t = new Thread(crawler);
				threadpool.add(t);
			}
			for (Thread t : threadpool) { //Start all the worker threads
				t.start();
			}
			this.isfirsturl = false;
		}
	}

	public String getDBDirectory() {
		return directory;
	}

	public String getSeedURL() {
		return seed_url;
	}

	public int getMaxSize() {
		return max_size;
	}

	public int getMaxFiles() {
		return max_number_of_files;
	}

	public void setMaxFiles(int num) {
		max_number_of_files = num;
	}

	/*
	public static void main(String args[]) {
		if (args.length < 3) {
			System.err.println("Missing command line arguments");
			System.exit(0);
		}
		int num = 1000;
		if (args.length == 4) {
			num = Integer.parseInt(args[3]);
		}
		XPathCrawler crawler = new XPathCrawler(args[0], args[1], Integer.parseInt(args[2]), num);	
	}
	 */
}
