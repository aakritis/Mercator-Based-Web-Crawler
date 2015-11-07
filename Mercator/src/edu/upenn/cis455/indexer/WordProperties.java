package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.HashMap;

public class WordProperties 
{
	HashMap<String,Integer>tag_count;
	HashMap<String,HashMap<String,Integer>> word_tag_count;
	HashMap<String,Integer> term_frequency;
	HashMap<String,ArrayList>word_position;
	public WordProperties()
	{
		word_tag_count=new HashMap<String,HashMap<String,Integer>>();
		term_frequency=new HashMap<String,Integer>();
		word_position=new HashMap<String,ArrayList>();
	}
	

}
