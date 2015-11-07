package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.*;

import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

import edu.upenn.cis455.storage.*;

public class SignUpServlet extends HttpServlet{

	private String password1;
	private String password2;
	private String username;
	private String name;
	private DBWrapper wrapper;
	private String directory;
	private Indices pk_index;
	private Environment env = null;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		username = request.getParameter("username");
		password1 = request.getParameter("password1");
		password2 = request.getParameter("password2");
		name = request.getParameter("name");
		directory = request.getSession().getServletContext().getInitParameter("BDBstore");
		opendb();
		if (check_if_username_exists()) {
			// Username already exists, show error
			response.sendRedirect("SignUpError_UName_Exists.html");
		}
		else if (!password1.equals(password2)) {
			// Passwords dont match
			response.sendRedirect("SignUpError_Pwds_Dont_Match.html");
		}
		else {
			// Store username and password in db
			store_new_user();
			response.sendRedirect("SignUpSuccess.html");
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect("SignUp.html");
	}

	/**
	 * Function that opens the database
	 */
	public void opendb() {
		wrapper = new DBWrapper();
		wrapper.setup(directory);
	}

	/**
	 * Function that closes the database
	 */
	public void closedb() {
		ArrayList<String> c = pk_index.users_pk.get(username).getChannels();
		for (String channel : c) {
			System.out.println(c);
		}
		wrapper.shutdown();
	}

	/**
	 * Function to check if the username already exists in the database
	 * @return true if username already exists and false otherwise
	 */
	public boolean check_if_username_exists() {
		pk_index = new Indices(wrapper.getStore());
		try {
			String uname = pk_index.users_pk.get(username).getUsername(); 
		}
		catch(NullPointerException e) {
			return false;
		}
		return true;
	}

	/**
	 * Function to add the new user to the database
	 */
	public void store_new_user() {
		pk_index = new Indices(wrapper.getStore());
		env = wrapper.getEnvironment();
		Users p = new Users();
		p.setUsername(username);
		p.setName(name);
		p.setPassword(password1);
		ArrayList<String> channels = new ArrayList();
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
		//System.out.println("Insertion to Password successful");
		closedb();
	}

}
