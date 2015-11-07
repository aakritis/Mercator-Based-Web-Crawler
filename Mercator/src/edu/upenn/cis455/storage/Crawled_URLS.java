package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Crawled_URLS {

	@PrimaryKey
	private String url;
	private Long last_crawled_time;
	private String content;
	private String content_type;
	
	public void setLastCrawledTime(long time) {
		last_crawled_time = new Long(time);
	}

	public long getLastCrawledTime() {
		return last_crawled_time.longValue();
	}

	public void setURL(String data) {
		url = data;
	}

	public String getURL() {
		return url;
	}
	
	public void setContentType(String data) {
		content_type = data;
	}

	public String getContentType() {
		return content_type;
	}
	
	public void setContent(String data) {
		content = data;
	}

	public String getContent() {
		return content;
	}
} 

