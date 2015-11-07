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

import edu.upenn.cis455.storage.*;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class ViewChannelServlet extends HttpServlet{

	private DBWrapper wrapper;
	private String directory;
	private Indices pk_index;
	private String username;
	private Environment env = null;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		directory = request.getSession().getServletContext().getInitParameter("BDBstore");
		opendb();
		username = (String) request.getSession().getAttribute("uname");
		if (username == null) {
			response.sendRedirect("LoggedOut_Error.html");
			return;
		}
		PrintWriter out = response.getWriter();
		Users p = pk_index.users_pk.get(username);
		ArrayList<String> channels = p.getChannels();
		if (channels.size() > 0) {
			out.println("<p>Your channels are:");
			for (String channel : channels)
				out.println("<br><br>"+"<a href=\"displaychannel?channelname="+channel+"\">"+channel+"</a>");
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
