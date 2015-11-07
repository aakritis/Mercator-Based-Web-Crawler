package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Channels {

	@PrimaryKey
	private String channel_name;
	private String xslt_url;
	private String username;
	ArrayList<String> xpaths;
	ArrayList<String> matched_urls;

	public Channels() {
		matched_urls = new ArrayList<String>();
		xpaths = new ArrayList<String>();
	}
	
	public void setChannelName(String data) {
		channel_name = data;
	}

	public String getChannelName() {
		return channel_name;
	}
	
	public void setMatchedURLS(ArrayList<String> docs) {
		matched_urls = docs;
	}
	
	public ArrayList<String> getMatchedURLS() {
		return matched_urls;
	}

	public void setXPaths(ArrayList<String> paths) {
		xpaths = paths;
	}

	public ArrayList<String> getXPaths() {
		return xpaths;
	}

	public void setXsltURL(String data) {
		xslt_url = data;
	}

	public String getXsltURL() {
		return xslt_url;
	}
	
	public void setUsername(String data) {
		username = data;
	}

	public String getUsername() {
		return username;
	}

} 

