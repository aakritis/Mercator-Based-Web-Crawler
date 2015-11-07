package test.edu.upenn.cis455;

import static org.junit.Assert.*;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import edu.upenn.cis455.xpathengine.HTTPClient;
import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class TestCases extends TestCase
{
	XPathEngineImpl xpathengine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
	String xpaths[];
	String url;
	
	@Before
	public void intialize()
	{
	}
	
	/**
	 * Test case for ideal case of setXpaths
	 */
	@Test
	public void test_setXpaths()
	{
		xpaths = new String[1];
		xpaths[0] = "/html/body[div[@id=\"page\"]";
		xpathengine.setXPaths(xpaths);
		assertFalse(xpathengine.isValid(0));
	}

	/**
	 * Test case to show the functionality of setXpaths
	 */
	@Test
	public void test_setXpaths_should_fail() {
		xpaths = new String[1];
		xpaths[0] = "/html/head/body";
		xpathengine.setXPaths(xpaths);
		xpathengine.isValid(0);
	}
	
	/**
	 * Testing isValid for xpaths that are infact valid
	 */
	@Test
	public void test_isValid()
	{
		xpaths = new String[5];
		xpaths[0] = "/a/b[foo[text()=\"#$(/][]\"]][bar]/hi[@asdf=\"#$(&[]\"][this][is][crazy[text()=\"He said\"You are such a d***\"\"]]";
		xpaths[1] = "/test[ a/b1[ c1[p]/d[p] ] /n1[a]/n2 [c2/d[p]/e[text()=\"/asp[&123(123*/]\"]]]";
		xpaths[2] = "/note/hello4/this[@val=\"text1\"]/that[@val=\"text2\"][something/else]";
		xpaths[3] = "/imas/production[@name=\"765 Production\"]";
		xpaths[4] = "/imas/production[idol[fn[text()=\"chihaya\"]][ln[contains(text(),\"ki\")]]]/idol[ln[text()=\"futami\"]]";
		xpathengine.setXPaths(xpaths);
		assertTrue(xpathengine.isValid(0));
	}
	
	/**
	 * Testing isValid for xpaths that are infact not valid
	 */
	@Test
	public void test_isValid_false()
	{
		xpaths = new String[16];
		xpaths[0] = "/catalog]";
		xpaths[1] = "/catalog[";
		xpaths[2] = "/catalog/cd[@title=Empire Burlesque\"]";
		xpaths[3] = "/catalog/cd[@title=\"Empire Burlesque\"][artist=\"Bob Dylan\"]";
		xpaths[4] = "/catalog/cd[@title=Empire Burlesque\"]]";
		xpaths[5] = "/catalog/cd[@year=\"1988\"][@price=\"9.90\"]/country[text()=\"UK\"]]";
		xpaths[6] = "/catalog/cd[[@title=Empire Burlesque\"]";
		xpaths[7] = "/catalog/cd[@year=\"1988\"][[@price=\"9.90\"]/country[text()=\"UK\"]";
		xpaths[8] = "/catalog/!badelem";
		xpaths[9] = "/@frenchbread/unicorns";
		xpaths[10] = "/abc/123bad";
		xpaths[11] = "/hello world";
		xpaths[12] = "/check(these)chars";
		xpaths[13] = "/abc/ab[@,illegalattribute=\"hello\"]";
		xpaths[14] = "/abc/ab[@<illegalattribute=\"hello\"]";
		xpaths[15] = "/abc/ab[text()=\"abc\"  pqr]";
		xpathengine.setXPaths(xpaths);
		for (int i=0; i<16; i++) {
			assertFalse(xpathengine.isValid(i));
		}
	}
	
	/**
	 * Test case for testing functionality of evaluate in the case of HTML pages
	 * @throws IOException
	 */
	@Test
	public void test_HTML() throws IOException
	{
		url = "http://www.cis.upenn.edu/~cis455/assignments.html";
		HTTPClient client = new HTTPClient(url);
		Document doc = null;
		try{
			doc = client.getDocument();
		}catch(Exception e)
		{
			System.out.println(e);
		}
		xpaths = new String[2];
		xpaths[0] = "/html/head/title[text()=\"Homework assignments for CIS 455 / 555\"]";
		xpaths[1] = "/html/head/title[contains(text(),\"Homework\")]";
		xpathengine.setXPaths(xpaths);
		boolean result[] = xpathengine.evaluate(doc);
		for (int i=0; i<xpaths.length;i++)
			assertTrue(result[i]);
	}
	
	/**
	 * Test case for testing the functionality of evaluate in the case of xml pages
	 * @throws IOException
	 */
	@Test
	public void test_XML() throws IOException
	{
		url = "http://static.akame.cdn.moe/public/imas.xml";
		HTTPClient client = new HTTPClient(url);
		Document doc = null;
		try{
			doc = client.getDocument();
		}catch(Exception e)
		{
			System.out.println(e);
		}
		xpaths = new String[8];
		xpaths[0] = "/imas/production[@name=\"765 Production\"]";
		xpaths[1] = "/imas/production[@name=\"765 Production\"]/idol[fn[text()=\"chihaya\"]]/age[text()=\"16\"]";
		xpaths[2] = "/imas/production[idol[fn[text()=\"chihaya\"]][ln[contains(text(),\"ki\")]]]/idol[ln[text()=\"futami\"]]";
		xpaths[3] = "/imas/production[idol[fn[text()=\"chihaya\"]][ln[contains(text(),\"ki\")]]]/idol[ln[text()=\"hoshii\"]]";
		xpaths[4] = "/imas/production[idol/ln[text()=\"futami\"]]/idol/age[text() = \"21\"]";
		xpaths[5] = "/imas/production[idol/ln[text()=\"ganaha\"]]/idol/age[text() = \"15\"]";
		xpaths[6] = "/imas/production[idol/ln[text()=\"futami\"]]/idol/c";
		xpaths[7] = "/imas/production[idol/ln[text()=\"hidaka\"]]/idol/c";
		xpathengine.setXPaths(xpaths);
		boolean result[] = xpathengine.evaluate(doc);
		assertTrue(result[0]);
		assertTrue(result[1]);
		assertTrue(result[2]);
		assertFalse(result[3]);
		assertTrue(result[4]);
		assertFalse(result[5]);
		assertFalse(result[6]);
		assertTrue(result[7]);
	}
	
}