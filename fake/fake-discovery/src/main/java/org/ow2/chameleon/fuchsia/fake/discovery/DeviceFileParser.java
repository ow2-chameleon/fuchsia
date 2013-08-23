package org.ow2.chameleon.fuchsia.fake.discovery;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * This class contains utilities to parse *.cfg file containing devices description !
 *
 * @author jeremy.savonet@gmail.com
 */
public class DeviceFileParser {

    private File m_file;
    Properties prop;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeviceFileParser(File m_file) {
        this.m_file = m_file;
    }

    /**
     * This method parses the CFG file and returns an HASHMAP containing metadatas
     */
    public synchronized HashMap<String,Object> cfgFileParser() {
        HashMap<String,Object> metadatas = new HashMap<String,Object>();

        prop = new Properties();
        try {
            InputStream is = new FileInputStream(m_file);
            prop.load(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        metadatas.put("id",getProperty("ID"));
        metadatas.put("type",getProperty("DeviceType"));
        metadatas.put("subtype",getProperty("DeviceSubType"));
        prop.clear();
        return metadatas;
    }

    /**
     * Method to get property value through the key !
     * @param key : the key of the value in the cfg file !
     * @return the value of the desired key.
     *
     * i.e :  device.serialNumber = BL-1234AA :
     *  <li>device.serialNumber is the key</li>
     *  <li>BL-1234AA is the value</li>
     */
    private String getProperty(String key) {
        String value = prop.getProperty(key);
        return value;
    }
}
