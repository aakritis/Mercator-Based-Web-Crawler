package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;

import edu.upenn.cis455.storage.*;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class DisplayChannelsServlet extends HttpServlet{

	private DBWrapper wrapper;
	private String directory;
	private Indices pk_index;
	private String username;
	private Environment env = null;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		directory = request.getSession().getServletContext().getInitParameter("BDBstore");
		opendb();
		PrintWriter out = response.getWriter();
		EntityCursor<Channels> channels = pk_index.channels_pk.entities();
		ArrayList<String> channel_names = new ArrayList<String>();
		for (Channels channel : channels) {
			channel_names.add(channel.getChannelName());
		}
		channels.close();
		if (channel_names.size() == 0) {
			out.println("<html><body><p>No channels have been created so far</p></body></html>");
			out.close();
		}
		else {
			out.println("<html><body><p>All the available channels are:</p>");
			for (String channel_name : channel_names) {
				out.println("<br>"+"<a href=\"displaychannel?channelname="+channel_name+"\">"+channel_name+"</a>");
			}
			out.println("</body></html>");
			out.close();
		}
	}

	/**
	 * Function that opens the database
	 */
	public void opendb() {
		wrapper = new DBWrapper();
		wrapper.setup(directory);
		env = wrapper.getEnvironment();
		pk_index = new Indices(wrapper.getStore());
	}

}
