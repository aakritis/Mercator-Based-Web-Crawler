package edu.upenn.cis455.crawler;

public class NumberOfFilesCrawled {
	
	private int counter;
	
	public NumberOfFilesCrawled() {
		counter = 0;
	}
	
	public synchronized void increment() {
		counter++;
	}
	
	public synchronized void decrement() {
		counter--;
	}

	public synchronized int count() {
		return counter;
	}
}
