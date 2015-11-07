package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sleepycat.je.Environment;

import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.storage.Crawled_URLS;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.Indices;

public class CrawlerTestCases {

	private String seed_url;
	private String db_directory = "./database/";
	private String max_file_size;
	private String max_number_of_files;
	private DBWrapper wrapper;
	private Environment env;
	private Indices pk_index;

	/*
	@Test
	public void test1() throws InterruptedException {
		seed_url = "https://dbappserv.cis.upenn.edu/crawltest.html";
		db_directory = "./database/";
		max_file_size = ""+1000;
		max_number_of_files = ""+1;
		String args[] = {seed_url, db_directory, max_file_size, max_number_of_files};
		XPathCrawler.main(args);
		Thread.sleep(10000);
		opendb();
		Crawled_URLS crawled_url = pk_index.crawled_urls_pk.get(seed_url);
		assertEquals(crawled_url.getURL(), seed_url);
		wrapper.shutdown();
	}
	
	@Test
	public void test2() throws InterruptedException {
		seed_url = "https://dbappserv.cis.upenn.edu/crawltest.html";
		db_directory = "./database/";
		max_file_size = ""+1000;
		max_number_of_files = ""+1;
		String args[] = {seed_url, db_directory, max_file_size, max_number_of_files};
		XPathCrawler.main(args);
		Thread.sleep(10000);
		opendb();
		Crawled_URLS crawled_url = pk_index.crawled_urls_pk.get(seed_url);
		assertNotEquals(crawled_url.getURL(), "https://dbappserv.cis.upenn.edu/crawltest/misc/eurofxref-daily.xml");
		wrapper.shutdown();
	}
	
	public void opendb() {
		wrapper = new DBWrapper();
		wrapper.setup(db_directory);
		pk_index = new Indices(wrapper.getStore());
		env = wrapper.getEnvironment();
	}
	*/
}
