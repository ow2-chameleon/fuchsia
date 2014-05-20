package org.ow2.chameleon.fuchsia.push.base.subscriber.tool;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Base PUbSubHubbub Publisher
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ow2.chameleon.fuchsia.push.base.subscriber.exception.HubDiscoveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HubDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(HubDiscovery.class);

    public String hasHub(Document doc) {
        String hub = null;

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        XPathExpression xPathExpression;

        try {
            xPathExpression = xPath.compile("/feed/link[@rel='hub']/@href");
            hub = xPathExpression.evaluate(doc);
            if ((hub == null) || (hub.length() == 0)) {
                xPathExpression = xPath.compile("//link[@rel='hub']/@href");
                hub = xPathExpression.evaluate(doc);
            }

            if (hub.length() == 0) {
                return null;
            }

            return hub;

        } catch (XPathExpressionException e) {
            LOGGER.error("XPathExpression invalid", e);
            return null;
        }
    }

    public String hasTopic(Document doc) {
        String topic;

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        XPathExpression xPathExpression;

        try {
            xPathExpression = xPath.compile("/feed/link[@rel='self']/@href");
            topic = xPathExpression.evaluate(doc);
            if ((topic == null) || (topic.length() == 0)) {
                xPathExpression = xPath.compile("//link[@rel='self']/@href");
                topic = xPathExpression.evaluate(doc);
            }

            if (topic.length() == 0) {
                return null;
            }
            return topic;

        } catch (XPathExpressionException e) {
            LOGGER.error("Invalid XpathExpression", e);
            return null;
        }

    }

    public String getContents(String feed) throws IOException {
        String response = null;

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(feed);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpclient.execute(httpget, responseHandler);
        response = (responseBody);

        httpclient.getConnectionManager().shutdown();

        return response;
    }

    public String getHub(String feedurl) throws HubDiscoveryException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new HubDiscoveryException("Exception thrown while instantiating the DocumentBuilder.", e);
        }
        Document doc = null;
        try {
            doc = builder.parse(new InputSource(new StringReader(getContents(feedurl))));
        } catch (Exception e) {
            throw new HubDiscoveryException("Exception thrown while parsing the feed.", e);
        }

        return hasHub(doc);
    }

    public Map<String, String> getHubs(List<String> feedurls) {
        Iterator<String> i = feedurls.iterator();
        Map<String, String> hashtable = new HashMap<String, String>();
        while (i.hasNext()) {
            String feedurl = i.next();
            try {
                hashtable.put(feedurl, getHub(feedurl));
            } catch (HubDiscoveryException e) {
                LOGGER.error("Failed to fetch hubs", e);
            }
        }
        return hashtable;
    }
}
