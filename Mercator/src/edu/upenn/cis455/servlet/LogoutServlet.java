package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.*;

public class LogoutServlet extends HttpServlet{

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		session.invalidate();
		PrintWriter out = response.getWriter();
		out.println("<html><body>Logout Successful<br><br>Go back to <a href=\"Login.html\">Login Page</a></body></html>");
	} 

}
