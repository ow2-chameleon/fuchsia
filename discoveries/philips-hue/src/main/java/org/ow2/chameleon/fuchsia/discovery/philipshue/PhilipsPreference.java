/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.fuchsia.discovery.philipshue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;
import java.util.prefs.*;

public class PhilipsPreference extends Preferences {

    private static final Logger LOG = LoggerFactory.getLogger(PhilipsHueBridgeDiscovery.class);
    private static final String FILE_NAME="philips.properties";
    private Properties propertiesLocalStore;

    private static Preferences instance;

    public static Preferences getInstance(){

        if(instance==null){
            try {
                LOG.info("Trying to save on the disk username adopted for the Philips AP");

                if(Boolean.getBoolean("philips.java.preferences.disable")){
                    LOG.info("Java preferences disabled, storing file philips.properties on the disk");
                    instance=new PhilipsPreference();
                }else {
                    LOG.info("Using Java preferences to store authentication");
                    instance=Preferences.userRoot().node(PhilipsPreference.class.getName());
                    tryWritePreferenceOnDisk(instance);
                }

            } catch (BackingStoreException e) {
                LOG.warn("Failed using default java preferences to save username, using fallback mechanism(property files)");
                instance=new PhilipsPreference();

            }

        }

        return instance;

    }

    /**
     * This ensures that we are able to use the default preference from JSDK, to check basically if we are in Android or Not
     * @param preference
     * @throws BackingStoreException
     */
    private static void tryWritePreferenceOnDisk(Preferences preference) throws BackingStoreException {
        final String DUMMY_PROP="dummywrite";
        instance.put(DUMMY_PROP,"test");
        instance.flush();
        instance.remove(DUMMY_PROP);
        instance.flush();
    }

    private PhilipsPreference(){
        try {
            LOG.info("Creating local properties file {} to replace java preferences",FILE_NAME);

            propertiesLocalStore = new Properties();

            if(new File(FILE_NAME).exists()){
                LOG.info("Loading properties from the file {}",FILE_NAME);
                FileInputStream fis = new FileInputStream(FILE_NAME);
                propertiesLocalStore.load(fis);
                fis.close();
            }else {
                LOG.info("Properties file {} do not exists, it will be created when first property is added",FILE_NAME);
            }

        } catch (Exception e) {
            LOG.warn("No mechanism found to save username from Philips AP, you will be forced to push the button after every restart");
        }
    }

    @Override
    public void put(String key, String value) {
        LOG.info("Using Fuchsia java preferences fallback to set preference {}",key);
        propertiesLocalStore.put(key, value);
        safePropertiesOnDisk();


    }

    private void safePropertiesOnDisk(){
        try {
            FileOutputStream fos = new FileOutputStream(FILE_NAME);
            propertiesLocalStore.store(fos,null);
            fos.close();
        } catch (Exception e) {
            LOG.warn("Failed to save key/value on the file",FILE_NAME);
        }
    }

    @Override
    public String get(String key, String def) {
        String value=propertiesLocalStore.getProperty(key);
        LOG.info("Using Fuchsia java preferences fallback to get preference {}, resulting in value {}",key,value);
        return value;
    }

    @Override
    public void remove(String key) {
        propertiesLocalStore.remove(key);
        safePropertiesOnDisk();
    }

    @Override
    public void clear() throws BackingStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putInt(String key, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(String key, int def) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putLong(String key, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(String key, long def) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putBoolean(String key, boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putFloat(String key, float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(String key, float def) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putDouble(String key, double value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(String key, double def) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getByteArray(String key, byte[] def) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] keys() throws BackingStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] childrenNames() throws BackingStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Preferences parent() {
        return null;
    }

    @Override
    public Preferences node(String pathName) {
        return null;
    }

    @Override
    public boolean nodeExists(String pathName) throws BackingStoreException {
        return false;
    }

    @Override
    public void removeNode() throws BackingStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public String absolutePath() {
        return null;
    }

    @Override
    public boolean isUserNode() {
        return false;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void flush() throws BackingStoreException {
        LOG.info("Flush unecessary for this implementation");
    }

    @Override
    public void sync() throws BackingStoreException {
        flush();
    }

    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException();
    }

}
