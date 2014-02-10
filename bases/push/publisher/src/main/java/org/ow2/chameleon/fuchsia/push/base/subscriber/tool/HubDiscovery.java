package org.ow2.chameleon.fuchsia.push.base.subscriber.tool;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;

public class HubDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(HubDiscovery.class);

	public String hasHub(Document doc) throws Exception {
		String hub = null;
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		XPathExpression xPathExpression;
		
		try{
			xPathExpression = xPath.compile("/feed/link[@rel='hub']/@href");
			hub = (String) xPathExpression.evaluate(doc);
			if ((hub==null)||(hub.equals(""))){
				xPathExpression = xPath.compile("//link[@rel='hub']/@href");
				hub = (String) xPathExpression.evaluate(doc);			
			}	
			
			if (hub.equals("")){
				return null;
			}
			
			return hub;
		
		} catch (XPathExpressionException e) {
            LOGGER.error("XPathExpression invalid",e);
			return null;
		}
	}

	public String hasTopic(Document doc){
		String topic = null;
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		XPathExpression xPathExpression;

		try {
			xPathExpression = xPath.compile("/feed/link[@rel='self']/@href");
			topic = (String) xPathExpression.evaluate(doc);
			if ((topic==null)||(topic.equals(""))){
				xPathExpression = xPath.compile("//link[@rel='self']/@href");
				topic = (String) xPathExpression.evaluate(doc);			
			}
			
			if (topic.equals("")){
				return null;
			}
			return topic;
			
		} catch (XPathExpressionException e) {
            LOGGER.error("Invalid XpathExpression",e);
		    return null;
		}
		
	}
	
	public String getContents(String feed) throws Exception {
		String response = null;
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(feed);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = httpclient.execute(httpget, responseHandler);
		response = (responseBody);

		httpclient.getConnectionManager().shutdown();

		return response;
	}

	public String getHub(String feedurl) throws Exception {
		
		DocumentBuilderFactory Factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = Factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(
				getContents(feedurl))));
		
		return (hasHub(doc));
		
	}

	public HashMap<String, String> getHubs(ArrayList<String> feedurls) {
		Iterator<String> i = feedurls.iterator();
		HashMap<String, String> hashtable = new HashMap<String, String>();
		while (i.hasNext()) {
			String feedurl = i.next();
			try {
				hashtable.put(feedurl, getHub(feedurl));
			} catch (Exception e) {
                LOGGER.error("Failed to fetch hubs",e);
			}
		}
		return hashtable;
	}
}