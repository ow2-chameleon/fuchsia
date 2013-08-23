package org.ow2.chameleon.fuchsia.fake.discovery;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.core.services.ExtensionBasedDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

/**
 * This class is extends a functionality from Chameleon CORE !
 * It aims at providing a  device file deployer. By that, we mean that we can catch all *.cfg file added, removed or changed
 * in monitored directories.
 *
 * @author jeremy.savonet@gmail.com
 */
@Component
@Provides
@Instantiate
public class CustomDeviceFileDeployer extends ExtensionBasedDeployer {

    @Requires (optional = false)
    FakeDiscoveryBridge m_fakeDiscoveryBridge;

    HashMap<String, HashMap<String,Object>> m_metadatas;
    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private HashMap<String, Object> m_metadata;

    public CustomDeviceFileDeployer() {
        super("cfg");
        m_metadatas = new HashMap<String, HashMap<String, Object>>();
    }

    /**
     * Once a file is created, parse it and create the device instance !
     * @param file : the cfg file containing the device description to parse
     */
    public void onFileCreate(File file) {
        DeviceFileParser deviceFileParser = new DeviceFileParser(file);
        m_metadata =  deviceFileParser.cfgFileParser();
        m_fakeDiscoveryBridge.createFakeDeviceInstanceAndPublishIDec(m_metadata);
        m_metadatas.put(file.getName(),m_metadata);
    }

    /**
     * Once a file is updated, reparse it and update the instance !
     * @param file : the cfg file containing the device description to parse
     */
    public void onFileChange(File file) {
        DeviceFileParser deviceFileParser = new DeviceFileParser(file);
        m_metadata =  deviceFileParser.cfgFileParser();
        m_fakeDiscoveryBridge.reconfigureFakeDeviceInstanceAndRePublishIDec(m_metadata);
    }

    /**
     * Once a file is deleted, remove the instance !
     * @param file : the file removed
     */
    public void onFileDelete(File file) {
        m_metadata =  m_metadatas.get(file.getName());
        m_fakeDiscoveryBridge.removeFakeDeviceInstanceAndIDec(m_metadata);
    }

    public void open(Collection<File> files) {
        //Do nothing
    }

    public void close() {
        //Do nothing
    }
}
