package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.*;

public class DisplayChannelServlet extends HttpServlet{

	private String channelname;
	DBWrapper wrapper;
	Indices pk_index;
	String directory;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		directory = request.getSession().getServletContext().getInitParameter("BDBstore");
		opendb();
		PrintWriter out = response.getWriter();
		channelname = request.getParameter("channelname");
		Channels channel = pk_index.channels_pk.get(channelname);
		String xslt_url = channel.getXsltURL();
		String xslt_line = "<?xml-stylesheet type=\"text/xsl\" href=\""+xslt_url+"\"?>";
		ArrayList<String> urls = channel.getMatchedURLS();
		if (urls.size() == 0) {
			out.println("<html><body><p>No matches for this channel!</p></body></html>");
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"rsl.xsl\"><xsl:template match=\"/\"><documentcollection>");
			//sb.append(xslt_line+"\n");
			for (String url : urls) {
				Crawled_URLS crawled_url = pk_index.crawled_urls_pk.get(url);
				String document = crawled_url.getContent();
				String url_from_db = crawled_url.getURL(); // url_from_db is same as url
				long last_crawled_time = crawled_url.getLastCrawledTime();
				String last_crawled = getDate(last_crawled_time);
				sb.append("<document crawled=\""+last_crawled+"\" location=\""+url_from_db+"\">\n");
				if (document.trim().length() == 0) {
					sb.append("\n");
				}
				String[] lines = document.split("\n");
				boolean first = true;
				for (int i=0; i<lines.length; i++) {
					if (lines[0].contains("<?xml") && first) {
						first = false;
						continue;
					}
					sb.append(lines[i]+"\n");
				}
				sb.append("</document>\n");
			}
			sb.append("</documentcollection></xsl:template></xsl:stylesheet>");
			out.println(sb.toString());
		}
		out.close();
	} 

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//Not needed
	}

	/**
	 * Function that opens the database
	 */
	public void opendb() {
		wrapper = new DBWrapper();
		wrapper.setup(directory);
		pk_index = new Indices(wrapper.getStore());
	}
	
	String getDate(long lastCrawlTime)
	{
		//Date date = new Date(lastCrawlTime);
		SimpleDateFormat localTimeFormat = new SimpleDateFormat("HH:mm:ss");
        String time = localTimeFormat.format(lastCrawlTime);
        SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = localDateFormat.format(lastCrawlTime);
		String timestamp = date + "T" + time;
		return timestamp;
	}
}
