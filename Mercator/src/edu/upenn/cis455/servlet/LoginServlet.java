package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.*;

public class LoginServlet extends HttpServlet{

	private String username;
	private String db_username;
	private String password;
	private String db_password;
	DBWrapper wrapper;
	Indices pk_index;
	String directory;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect("Login.html");
	} 

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		username = request.getParameter("username");
		password = request.getParameter("password");
		directory = request.getSession().getServletContext().getInitParameter("BDBstore");
		opendb();
		if (username.trim().equals("") || !check_if_username_exists()) {
			response.sendRedirect("LoginError_UName_Error.html");
		}
		else if (!check_if_password_matches()) {
			response.sendRedirect("LoginError_Pwd_Error.html");
		}
		else {
			HttpSession session = request.getSession();
			session.setAttribute("uname", username);
			//request.getSession().getServletContext().setAttribute("uname", username);
			wrapper.shutdown();
			response.sendRedirect("HomePage.html");
		}
	}

	/**
	 * Function that opens the database
	 */
	public void opendb() {
		wrapper = new DBWrapper();
		wrapper.setup(directory);
	}

	/**
	 * Function to check if the username already exists in the database
	 * @return true if username already exists and false otherwise
	 */
	public boolean check_if_username_exists() {
		pk_index = new Indices(wrapper.getStore());
		try {
			db_username = pk_index.users_pk.get(username).getUsername(); 
		}
		catch(NullPointerException e) {
			return false;
		}
		return true;
	}

	/**
	 * Function to check if password entered by user matches that in the database
	 * @return true if the user entered the correct password and false otherwise
	 */
	public boolean check_if_password_matches() {
		Users p = pk_index.users_pk.get(username);
		db_password = p.getPassword();
		if (password.equals(db_password))
			return true;
		else 
			return false;
	}

}
