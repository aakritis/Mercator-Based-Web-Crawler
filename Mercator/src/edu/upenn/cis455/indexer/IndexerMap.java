package edu.upenn.cis455.indexer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class IndexerMap extends Mapper 
{
	/**
	 * input for the map is the (key,value) pair: (documentId,url) 
	 * @param key
	 * @param value
	 */
	public void map()
	{
	    // key is the documentId
		// value is the url
		//fetch the document using the documentId
		
	}

}
