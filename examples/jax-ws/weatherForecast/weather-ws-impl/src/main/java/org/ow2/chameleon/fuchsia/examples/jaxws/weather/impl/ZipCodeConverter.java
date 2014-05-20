package org.ow2.chameleon.fuchsia.examples.jaxws.weather.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Class to convert zipcodes into Yahoo API WOEIDs using Yahoo Query Language (YQL).
 *
 * @author jaspervalero
 * @example Here is an example of this class in use:
 * <p/>
 * <listing version="3.0">
 * <p/>
 * private function convertZip():void
 * {
 * _zc = new ZipcodeConverter("95348");
 * _zc.addEventListener(Event.COMPLETE, conversionComplete);
 * }
 * <p/>
 * private function conversionComplete(e:Event):void
 * {
 * _woeid = _zc.woeid;
 * }
 * </listing>
 */
public class ZipCodeConverter {

    private String _woeid;

    /**
     * Constructor function which passes in a zipcode to be converted.
     *
     * @param zipcode The 5-digit zipcode you want converted to a WOEID.
     */
    public ZipCodeConverter(String zipcode) {
        query(zipcode);
    }

    // Query zipcode from Yahoo to find associated WOEID
    private void query(String zipcode) {
                        /* Setup YQL query statement using dynamic zipcode. The statement searches geo.places
                        for the zipcode and returns XML which includes the WOEID. For more info about YQL go
                        to: http://developer.yahoo.com/yql/ */
        String qry = URLEncoder.encode("SELECT woeid FROM geo.places WHERE text=" + zipcode + " LIMIT 1");

        // Generate request URI using the query statement
        URL url;
        try {
            // get URL content
            url = new URL("http://query.yahooapis.com/v1/public/yql?q=" + qry);
            URLConnection conn = url.openConnection();

            InputStream content = conn.getInputStream();
            parseResponse(content);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Extract WOEID after XML loads
    private void parseResponse(InputStream inputStream) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName("place");

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    _woeid = getValue("woeid", element);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }


    /**
     * This is the WOEID used by the Yahoo API.
     *
     * @return Returns the WOEID for the queried zipcode.
     */
    public String getWoeid() {
        return _woeid;
    }
}
