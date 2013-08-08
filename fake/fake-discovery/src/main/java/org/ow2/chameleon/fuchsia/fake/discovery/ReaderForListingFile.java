package org.ow2.chameleon.fuchsia.fake.discovery;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides methods to read the XML listing file.
 * Also it offers the possibility to construct some metadata with extracted data !
 */
public class ReaderForListingFile {

    /**
     * This method parses the XML file and returns a HASHMAP containing metadata
     */
    public synchronized HashMap<String,Map> parserXML() {
        HashMap<String,Map> metadatas = new HashMap<String, Map>();
        try {

            File stocks = new File("listing.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(stocks);
            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName("device");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    HashMap<String,Object> metadata = new HashMap<String, Object>();
                    metadata.put("id",getValue("id", element));
                    metadata.put("type",getValue("type", element));
                    metadata.put("subtype",getValue("subtype", element));
                    metadatas.put(getValue("id", element),metadata);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return metadatas;
    }

    private synchronized static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }
}
