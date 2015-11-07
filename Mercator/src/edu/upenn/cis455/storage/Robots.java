package edu.upenn.cis455.storage;

import java.util.ArrayList;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Robots {

	@PrimaryKey
	private String domain;
	private Long last_crawled_time;
	private Long crawl_delay;
	private ArrayList<String> allowed_links;
	private ArrayList<String> disallowed_links;
	
	public void setLastCrawledTime(long time) {
		last_crawled_time = new Long(time);
	}

	public long getLastCrawledTime() {
		return last_crawled_time.longValue();
	}

	public void setDomain(String data) {
		domain = data;
	}

	public String getDomain() {
		return domain;
	}
	
	public void setCrawlDelay(long time) {
		crawl_delay = new Long(time);
	}

	public long getCrawlDelay() {
		return crawl_delay.longValue();
	}
	
	public void setAllowedLinks(ArrayList<String> links) {
		allowed_links = links;
	}
	
	public ArrayList<String> getAllowedLinks() {
		return allowed_links;
	}
	
	public void setDisallowedLinks(ArrayList<String> links) {
		disallowed_links = links;
	}
	
	public ArrayList<String> getDisallowedLinks() {
		return disallowed_links;
	}
} 

