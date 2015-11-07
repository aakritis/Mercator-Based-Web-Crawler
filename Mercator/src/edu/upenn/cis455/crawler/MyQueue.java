package edu.upenn.cis455.crawler;

import java.util.ArrayList;

import com.sleepycat.je.Environment;
//import com.sleepycat.je.Transaction;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.Indices;
import edu.upenn.cis455.storage.URLQueue;

@SuppressWarnings("unused")
public class MyQueue {

	private int start_pointer;
	private int end_pointer;
	private DBWrapper wrapper;
	private String directory;
	private Indices pk_index;
	private Environment env;

	public MyQueue(String dir) {
		start_pointer = 0;
		end_pointer = 0;
		directory = dir;
		opendb();
		pk_index = new Indices(wrapper.getStore());
		env = wrapper.getEnvironment();
	}

	/**
	 * Function to insert to the end of the queue
	 * @param url is the URL to be inserted
	 */
	public synchronized void enqueue(String url) {
		System.out.println("[ENQUEUE] Enqueue URL request + " + url);
		if (start_pointer == 0) 
			start_pointer++;
		end_pointer++;
		// Add at index end_pointer
		URLQueue url_queue = new URLQueue();
		url_queue.setIndex(end_pointer);
		url_queue.setURL(url);
		//Transaction txn = env.beginTransaction(null, null);
		try {
			pk_index.urlqueue_pk.put(url_queue);
			//txn.commit();
		}
		catch(Exception e) {
			System.out.println("Transaction failed");
			/*
			if (txn != null) {
				txn.abort();
				txn = null;
			}
			*/
		}
		notify();
	}

	/**
	 * Function to get the url at the front of the queue
	 * @return the url
	 * @throws InterruptedException 
	 */
	public synchronized String dequeue() throws InterruptedException {
		String ret_url = "";
		if (isempty()) // If the queue is empty, return null
			wait();
		// Remove the one at index start_pointer
		Integer pk = new Integer(start_pointer);
		//Transaction txn = env.beginTransaction(null, null);
		try {
			ret_url = pk_index.urlqueue_pk.get(pk).getURL();
			//txn.commit();
		}
		catch(Exception e) {
			System.out.println("Transaction failed");
			/*
			if (txn != null) {
				txn.abort();
				txn = null;
			}
			*/
		}
		notify();
		start_pointer++;
		return ret_url;
	}

	/**
	 * Function to check if the URL queue is empty
	 * @return true of the queue is empty and false otherwise
	 */
	public synchronized boolean isempty() {
		if (start_pointer > end_pointer)
			return true;
		return false;
	}

	/**
	 * Function that opens the database
	 */
	public void opendb() {
		wrapper = new DBWrapper();
		wrapper.setup(directory);
	}

	/**
	 * Function that closes the database
	 */
	public void closedb() {
		wrapper.shutdown();
	}
}
