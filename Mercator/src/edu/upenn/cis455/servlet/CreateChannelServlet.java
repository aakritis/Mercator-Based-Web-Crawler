package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

import edu.upenn.cis455.storage.*;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class CreateChannelServlet extends HttpServlet{

	private DBWrapper wrapper;
	private String directory;
	private Indices pk_index;
	private String username;
	private Environment env = null;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect("NewChannel.html");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		if (session == null) {
			response.sendRedirect("LoggedOut_Error.html");
			return;
		}
		username = (String) session.getAttribute("uname");
		if (username == null) {
			response.sendRedirect("LoggedOut_Error.html");
			return;
		}
		PrintWriter out = response.getWriter();
		XPathEngineImpl xpathengine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
		ArrayList<String> xpaths = new ArrayList<String>();
		Enumeration e = request.getParameterNames();
		String xslt_url = null;
		String channel_name = null;
		while(e.hasMoreElements()) {
			String param = e.nextElement().toString();
			if (param.equals("cname")) {
				channel_name = request.getParameter(param);
			}
			else if (param.equals("xslt")) {
				xslt_url = URLDecoder.decode(request.getParameter(param), "UTF-8");
				if (!xslt_url.startsWith("http"))
					xslt_url = "http://" + xslt_url;
			}
			else if (param.startsWith("xpath_")) {
				xpaths.add(URLDecoder.decode(request.getParameter(param), "UTF-8"));
			}
		}
		String[] xpaths_array = new String[xpaths.size()];
		xpaths_array = xpaths.toArray(xpaths_array);
		xpathengine.setXPaths(xpaths_array);
		for (int i=0; i<xpaths.size(); i++) {
			if (!xpathengine.isValid(i)) {
				// Invalid XPath
				response.sendRedirect("NewChannel_InvalidXPath_Error.html");
				return;
			}
		}
		directory = request.getSession().getServletContext().getInitParameter("BDBstore");
		opendb();
		pk_index = new Indices(wrapper.getStore());
		env = wrapper.getEnvironment();
		Users p = pk_index.users_pk.get(username);
		ArrayList<String> channels = p.getChannels();
		channels.add(channel_name);
		p.setChannels(channels);
		Transaction txn = env.beginTransaction(null, null);
		try {
			pk_index.users_pk.put(p);
			txn.commit();
		}
		catch(Exception e1) {
			System.out.println("Transaction failed");
			if (txn != null) {
				txn.abort();
				txn = null;
			}
		}
		Channels c = new Channels();
		c.setUsername(username);
		c.setChannelName(channel_name);
		c.setXsltURL(xslt_url);
		c.setXPaths(xpaths);
		c.setMatchedURLS(new ArrayList<String>());
		Transaction txn2 = env.beginTransaction(null, null);
		try {
			pk_index.channels_pk.put(c); // Assuming that only unique channels are entered
			txn2.commit();
		}
		catch(Exception e1) {
			System.out.println("Transaction failed");
			if (txn2 != null) {
				txn2.abort();
				txn2 = null;
			}
		}
		wrapper.shutdown(); // Close the database
		response.sendRedirect("NewChannelSuccess.html");
		out.close();
	}

	/**
	 * Function that opens the database
	 */
	public void opendb() {
		wrapper = new DBWrapper();
		wrapper.setup(directory);
	}

}
