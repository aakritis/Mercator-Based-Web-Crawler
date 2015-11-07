package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

@SuppressWarnings("unused")
public class Indices {

	public PrimaryIndex<String, Users> users_pk;
	public PrimaryIndex<String, Channels> channels_pk;
	public PrimaryIndex<Integer, URLQueue> urlqueue_pk;
	public PrimaryIndex<String, Robots> robots_pk;
	public PrimaryIndex<String, Crawled_URLS> crawled_urls_pk;
	public PrimaryIndex<String, URLS> urls_pk;

	public Indices(EntityStore store) throws DatabaseException {
		//users_pk = store.getPrimaryIndex(String.class, Users.class);
		//channels_pk = store.getPrimaryIndex(String.class, Channels.class);
		urlqueue_pk = store.getPrimaryIndex(Integer.class, URLQueue.class);
		robots_pk = store.getPrimaryIndex(String.class, Robots.class);
		crawled_urls_pk = store.getPrimaryIndex(String.class, Crawled_URLS.class);
		urls_pk = store.getPrimaryIndex(String.class, URLS.class);
	}

}