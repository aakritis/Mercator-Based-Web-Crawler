package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class URLQueue {

	@PrimaryKey
	private Integer index;
	private String url;
	
	public void setIndex(int data) {
		index = new Integer(data);
	}

	public int getIndex() {
		return index.intValue();
	}

	public void setURL(String data) {
		url = data;
	}

	public String getURL() {
		return url;
	}
	
} 

