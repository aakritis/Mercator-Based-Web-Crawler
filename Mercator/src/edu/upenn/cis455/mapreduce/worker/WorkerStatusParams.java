package edu.upenn.cis455.mapreduce.worker;

public class WorkerStatusParams {
	/**
	 * Inner Class WorkerStatusParams for query string parameters
	 * port, status, job, keysRead, keysWritten 
	 * @author Aakriti Singla
	 *
	 */
	// IP Address can be fetched from the request
	int port;
	public String status;
	// String job;
	// int keysRead;
	// public int keysWritten;
	// time recieved 
	Long timeRecieved;

	/**
	 * Default Constructor with default values
	 */
	public WorkerStatusParams(){
		this.port = 8080;
		this.status = "idle";
		// this.job = "";
		// this.keysRead = 0;
		// this.keysWritten = 0;
		this.timeRecieved = 12345678910L;
	}
}
