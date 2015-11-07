package edu.upenn.cis455.indexer;
import java.io.*;
import java.util.Map;

import edu.washington.cs.knowitall.morpha.MorphaStemmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BasicIndexer 
{
	String title;
	String h1; String h2; String h3; String h4; String h5; String h6; String anchor;
	Elements test; int tag_size; int title_size; int body_size; int max_occurrence;
	String tags[]={"h1","h2","h3","h4","h5","h6","b","i","u","meta","a","title"};
	String stopwords[] ={"a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also","although","always","am","among", "amongst", "amoungst", "amount",  "an", "and", "another", "any","anyhow","anyone","anything","anyway", "anywhere", "are", "around", "as",  "at", "back","be","became", "because","become","becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom","but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven","else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own","part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", "the"};
	WordProperties word_prop;
	public BasicIndexer()
	{
		title=null;
		h1=null;
		h2=null; 
		h3=null;
		h4=null;
		h5=null;
		h6=null;
		anchor=null;
		tag_size=0;
		title_size=0;
		body_size=0;
		max_occurrence=0;
		word_prop=new WordProperties();
	}
	/**
	 * countWords function is used to find normalized term frequency,position of the words.
	 * @param html_file
	 */
	public void countWords(Document html_file)
	{
		try
		{
			// find the number of title tags in our html file
			title_size = html_file.select("title").size();
			StringBuffer content=new StringBuffer("");
			// extract all the text from all the title tags in the given html page
			for(int i=0;i<title_size;i++)
			{
				Element each_title_tag= html_file.select("title").get(i);
				content.append(each_title_tag.text().toLowerCase());			
			}	
			//find the number of body tags in the html file
			body_size=html_file.select("body").size();
			// extract all the text from the body tag
			for(int i=0;i<body_size;i++)
			{
				Element each_body_tag=html_file.select("body").get(i);
				content.append(each_body_tag.text().toLowerCase());
			}
			String final_content=content.toString().replaceAll("[^a-zA-Z0-9]+","");
			//tokenize the given alpha numeric string
			StringTokenizer st=new StringTokenizer(final_content);
			int position=0;
			while(st.hasMoreTokens())
			{
				
				String token=st.nextToken();
				position++;
				String lemmatized_token=MorphaStemmer.morpha(token, false);
				// check if the given token is in the list of stop words
				if(Arrays.asList(stopwords).contains(lemmatized_token))
				{
					continue;
				}	
				else
				{
					// check if the word already exists in the term_frequency hash map
					if(word_prop.term_frequency.containsKey(lemmatized_token))
					{
						// word exists
						int tf=word_prop.term_frequency.get(lemmatized_token);
						word_prop.term_frequency.put(lemmatized_token,tf+1);
						ArrayList<Integer> pos=word_prop.word_position.get(lemmatized_token);
						pos.add(position);
						word_prop.word_position.put(lemmatized_token,pos);
						if(max_occurrence<(tf+1))
						{
							max_occurrence=tf+1;
						}	
					}	
					else
					{
						// word does not exist in the hash map
						word_prop.term_frequency.put(lemmatized_token,1);
						ArrayList<Integer> pos=new ArrayList<Integer>();
						pos.add(position);
						word_prop.word_position.put(lemmatized_token, pos);
					}	
				}	
				
			}	
			
			
		}
		catch(Exception e)
		{
			System.out.println("Exception occurred while parsing the html string.."+e);
		}
	}
	/**
	 * function to calculate the normalized term frequencies of the words
	 */
	public void calculateNormalizedTermFrequencies()
	{
		try
		{
			 for(Map.Entry m: word_prop.term_frequency.entrySet())
			 {
				 String key=(String)m.getKey();
				 int value=(Integer)m.getValue();
				 word_prop.term_frequency.put(key,value/max_occurrence);
			 }	 
			
		}
		catch(Exception e)
		{
			System.out.println("Exception occurred while finding normalized term frequencies...");
		}
	}
	/**
	 * function parseInput is used to parse the html string
	 * @param inputString
	 * @return
	 */
	public boolean parseInput(String inputString)
	{
		boolean flag=true;
		try
		{
			Document html_file=Jsoup.parse(inputString);

			//update tag counts
			countTags(html_file);
			//update word counts
			countWords(html_file);
			calculateNormalizedTermFrequencies();
			
			/*
			//find the title in html content
			title_size = html_file.select("title").size();
			//iterate for all title tags
			for(int i=0;i<title_size;i++)
			{
				// extracting each title from given htm string
				Element each_title_tag=html_file.select("title").get(i);
				//finding the text of each title
				String each_title_text=each_title_tag.text().toLowerCase();
				System.out.println("Text here....."+each_title_text);
			}	
			h1 = html_file.body().getElementsByTag("h1").text();
			//h2 = html_file.body().getElementsByTag("h2").text();
			System.out.println("Title size..."+html_file.select("title").size());
			test=html_file.select("h1");
			 */
		}
		catch(Exception e)
		{
			System.out.println("Exception occurred while parsing the html string...."+e);
		}

		return flag;
	}
	/**
	 * countTags function is used to update the tag counts corresponding to given words
	 * @param html_file
	 */
	public void countTags(Document html_file)
	{
		// function to count tags
		for(String tag:tags)
		{
			tag_size=html_file.select(tag).size();
			for(int i=0;i<tag_size;i++)
			{
				// extracting each tag from string
				Element each_tag=html_file.select(tag).get(i);
				//finding the text of each tag
				String each_tag_text=each_tag.text().toLowerCase();
				// remove special characters
				String alphaNumeric_string = each_tag_text.replaceAll("[^a-zA-Z0-9]+","");
				//tokenize the given alpha numeric string
				StringTokenizer st=new StringTokenizer(alphaNumeric_string);
				while(st.hasMoreTokens())
				{
					String token=st.nextToken();
					String lemmatized_token=MorphaStemmer.morpha(token, false);
					// check if the given token is in the list of stop words
					if(Arrays.asList(stopwords).contains(lemmatized_token))
					{
						continue;
					}	
					else
					{
						boolean key_present=word_prop.word_tag_count.containsKey(lemmatized_token);
						if(key_present)
						{
							// word exists in the hash map
							HashMap<String,Integer> tag_value=word_prop.word_tag_count.get(lemmatized_token);
							// check if the given tag corresponding to this word is already present in my hash map
							if(tag_value.containsKey(tag))
							{
								//the hash map corresponding to the given word already contains the tag
								tag_value.put(tag, tag_value.get(tag)+1);
							}	
							else
							{
								// the hash map corresponding to the given word does not contain the tag
								tag_value.put(tag,1);
							}	
							word_prop.word_tag_count.put(lemmatized_token,tag_value);
						}	
						else
						{
							//word does not exist in the hash map
							word_prop.tag_count=new HashMap<String,Integer>();
							word_prop.tag_count.put(tag,1);
							word_prop.word_tag_count.put(lemmatized_token,word_prop.tag_count);
						}	
					}	

				}	

			}   
		}	
	}

	public static void main(String args[])
	{
		String inputString="<!DOCTYPE html>"+"<html>"+"<head>"+"<title><h1>Page Title</h1></title><title>This is second page title</title>"+"</head>"+"<body>"+"<h1>This is a Heading</h1><h1>This is another h1 heading</h1>"+"<h2>This is another heading tag</h2>"+"<p>This is a paragraph.<b>here u go with the bold tag</b></p>"+"</body>"+"</html>";
		String inputFile="/home/cis455/workspace/test.html";
		BasicIndexer indexer=new BasicIndexer();
		boolean isParsed=indexer.parseInput(inputString);
	}

}
