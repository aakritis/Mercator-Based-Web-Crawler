package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/*
 * Check if URL is alreeady crawled for first iteration
 * Flushed offf once the next iteration starts
 */
@Entity
public class URLS {

	@PrimaryKey
	private String url;
	
	public void setURL(String data) {
		url = data;
	}

	public String getURL() {
		return url;
	}
	
} 

