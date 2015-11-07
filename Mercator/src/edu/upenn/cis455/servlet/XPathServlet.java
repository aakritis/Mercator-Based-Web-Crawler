package edu.upenn.cis455.servlet;

import java.io.IOException;
import javax.servlet.http.*;

public class XPathServlet extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect("Query.html");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect("Query.html");
	}

}







