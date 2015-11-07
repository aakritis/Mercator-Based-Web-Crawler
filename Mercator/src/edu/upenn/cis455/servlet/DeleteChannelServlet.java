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

public class DeleteChannelServlet extends HttpServlet{

	private DBWrapper wrapper;
	private String directory;
	private Indices pk_index;
	private String username;
	private Environment env = null;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
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
		out.println("<!DOCTYPE html><html><head><style>.input {border: 3px solid;width: 40em;}.breaks {clear: both;}.sub {position: absolute;display: block;width: 10em;text-align: center;float: left;background: lightblue;border: 3px solid}.labels {display: block;padding-top: .1em;padding-right: .25em;width: 15em;text-align: right;float: left;}</style></head>");
		out.println("<body>");
		directory = request.getSession().getServletContext().getInitParameter("BDBstore");
		opendb();
		pk_index = new Indices(wrapper.getStore());
		Users p = pk_index.users_pk.get(username);
		ArrayList<String> channels = p.getChannels();
		if (channels.size() > 0) {
			out.println("<p>Your channels are:");
			for (String channel : channels)
				out.println("<br><br>"+channel);
			out.println("<p>Enter Channel Name to delete</p><br><form action=\"deletechannel\" method=\"post\"><div id=\"uname\"><label class=\"labels\">Channel Name:</label> <input name=\"cname\" id=\"cname\" /></div><div class=\"breaks\"><br></div><div class=\"btn\"><p><button class=\"sub\" type=\"submit\" value=\"Submit\">Delete Channel</button></p></div></form></body></html>");
		}
		else {
			out.println("<p>You have no channels to delete</p><br><br>Go back to <a href=\"HomePage.html\">HomePage</a></body></html>");
		}
		out.close();
		wrapper.shutdown();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String channel_name = null;
		channel_name = request.getParameter("cname");
		directory = request.getSession().getServletContext().getInitParameter("BDBstore");
		opendb();
		pk_index = new Indices(wrapper.getStore());
		env = wrapper.getEnvironment();
		Users p = pk_index.users_pk.get(username);
		ArrayList<String> channels = p.getChannels();
		if (!channels.contains(channel_name)) {
			out.println("<!DOCTYPE html><html><head></head><body><font style=\"color: red\">Channel Not Found</font><br><br>Go back to <a href=\"HomePage.html\">HomePage</a></body></html>");
			out.close();
			return;
		}
		else 
			channels.remove(channel_name);
		p.setChannels(channels);
		Transaction txn = env.beginTransaction(null, null);
		try {
			pk_index.users_pk.put(p);
			txn.commit();
		}
		catch(Exception e) {
			System.out.println("Transaction failed");
			if (txn != null) {
				txn.abort();
				txn = null;
			}
		}
		pk_index.channels_pk.delete(channel_name);
		wrapper.shutdown(); // Close the database
		out.println("<!DOCTYPE html><html><head></head><body><font style=\"color: green\">Channel delete successful</font><br><br>Go back to <a href=\"HomePage.html\">HomePage</a></body></html>");
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
