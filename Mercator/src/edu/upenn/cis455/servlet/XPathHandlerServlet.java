package edu.upenn.cis455.servlet;

import edu.upenn.cis455.xpathengine.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;

public class XPathHandlerServlet extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		XPathEngineImpl xpathengine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
		ArrayList<String> xpaths = new ArrayList<String>();
		boolean[] results = null;
		boolean[] valids = null;
		Enumeration e = request.getParameterNames();
		String request_url = null;
		while(e.hasMoreElements()) {
			String param = e.nextElement().toString();
			if (param.equals("URL")) {
				request_url = URLDecoder.decode(request.getParameter(param), "UTF-8");
				if (!request_url.startsWith("http"))
					request_url = "http://" + request_url;
			}
			if (param.startsWith("xpath_")) {
				xpaths.add(URLDecoder.decode(request.getParameter(param), "UTF-8"));
			}
		}
		String[] xpaths_array = new String[xpaths.size()];
		xpaths_array = xpaths.toArray(xpaths_array);
		xpathengine.setXPaths(xpaths_array);
		HTTPClient client = new HTTPClient(request_url);
		Document doc = null;
		try {
			doc = client.getDocument();
		} 
		catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} 
		catch (SAXException e1) {
			e1.printStackTrace();
		}
		results = xpathengine.evaluate(doc);
		valids = xpathengine.getvalids();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><body>");
		for (int i=0; i<xpaths.size(); i++) {
			if (!valids[i]) {
				out.println("<p>"+xpaths.get(i)+":  "+"Invalid XPath"+"</p>");
			}
			else {
				if (results[i])
					out.println("<p>"+xpaths.get(i)+":  "+"Success"+"</p>");
				else
					out.println("<p>"+xpaths.get(i)+":  "+"Failure"+"</p>");
			}
		}
		out.println("</body></html>");
		out.close();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//response.sendRedirect("Query.html");
	}
}








