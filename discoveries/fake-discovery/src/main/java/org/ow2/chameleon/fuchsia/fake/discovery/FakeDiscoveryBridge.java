package org.ow2.chameleon.fuchsia.fake.discovery;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.*;
import org.ow2.chameleon.fuchsia.fake.device.GenericDevice;
import org.ow2.chameleon.fuchsia.fake.discovery.monitor.Deployer;
import org.ow2.chameleon.fuchsia.fake.discovery.monitor.DirectoryMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

/**
 * This component instantiate a directory monitor (initially pointed to a directory in chameleon called "load") that reads all file placed there (as property files)
 * and instantiate a {@link org.ow2.chameleon.fuchsia.fake.device.GenericDevice} according with the same properties as the ones declared in the property file and
 * published an {@link ImportDeclaration}
 *
 * @author jeremy.savonet@gmail.com
 * @author botelho (at) imag.fr
 */

@Component(name = "Fuchsia-FakeDiscovery-Factory")
@Provides(specifications = {DiscoveryService.class,Deployer.class})
@Instantiate(name = "Fuchsia-FakeDiscovery")
public class FakeDiscoveryBridge extends AbstractDiscoveryComponent implements Deployer {

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @ServiceProperty(name = FakeDiscoveryConstants.FAKE_DISCOVERY_PROPERTY_KEY_MONITORED_DIR_KEY,value = FakeDiscoveryConstants.FAKE_DISCOVERY_PROPERTY_KEY_MONITORED_DIR_VALUE)
    private String monitoredDirectory;

    @ServiceProperty(name = FakeDiscoveryConstants.FAKE_DISCOVERY_PROPERTY_POLLING_TIME_KEY,value = FakeDiscoveryConstants.FAKE_DISCOVERY_PROPERTY_POLLING_TIME_VALUE)
    private Long pollingTime;

    private final HashMap<String,ImportDeclaration> importDeclarationsFile = new HashMap<String, ImportDeclaration>();

    private final HashMap<String,InstanceManager> devicesManaged =new LinkedHashMap<String, InstanceManager>();

    public FakeDiscoveryBridge(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Validate
    public void start() {

        super.start();

        try {
            new DirectoryMonitor(new File(monitoredDirectory),pollingTime).start(getBundleContext());
            getLogger().info("Discovery up and running.");
        } catch (Exception e) {
            getLogger().error("Failed to start {} for the directory {} and polling time {}, with the message '{}'", new String[]{DirectoryMonitor.class.getName(), monitoredDirectory, pollingTime.toString(), e.getMessage()});
        }

    }

    @Invalidate
    public void stop() {
        super.stop();
        getLogger().info("Discovery stopped.");
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    public String getName() {
        return name;
    }

    private InstanceManager createAndRegisterFakeDevice(String deviceId) throws Exception {

        try {
            Collection<ServiceReference<Factory>> factory=getBundleContext().getServiceReferences(Factory.class,String.format("(%s=%s)","factory.name",FakeDiscoveryConstants.FAKE_DEVICE_FACTORY_NAME));

            if(factory.size()==0) {
                getLogger().error("No {} component is available in the platform. Impossible to create a fake device.", GenericDevice.class.getName());
                throw new Exception(String.format("No %s component is available in the platform. Impossible to create a fake device.",GenericDevice.class.getName()));
            }

            if(factory.size()>1) {
                getLogger().warn("Several components implementing {} were found. One will be picket randomly but this may indicate an issue.", GenericDevice.class.getName());
            }

            for (ServiceReference<Factory> factoryServiceReference : factory) {

                Factory deviceIpojoFactory=(Factory)getBundleContext().getService(factoryServiceReference);

                Hashtable deviceProperties=new Hashtable<String,Object>();
                deviceProperties.put(Factory.INSTANCE_NAME_PROPERTY, "GenericDeviceInst-" + deviceId);
                deviceProperties.put(GenericDevice.DEVICE_SERIAL_NUMBER, "SN-" + deviceId);

                InstanceManager deviceInstanceManager=(InstanceManager)deviceIpojoFactory.createComponentInstance(deviceProperties);

                devicesManaged.put(deviceId,deviceInstanceManager);

                return deviceInstanceManager;

            }

        } catch (Exception e) {
           throw new Exception(e);
        }

        return null;

    }

    private ImportDeclaration createAndRegisterImportDeclaration(HashMap<String, Object> metadata){

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        registerImportDeclaration(declaration);

        return declaration;

    }

    public boolean accept(File file) {
        return true;
    }

    private Properties parseFile(File file) throws Exception {

        Properties deviceReificationProperties = new Properties();
        try {
            InputStream is = new FileInputStream(file);
            deviceReificationProperties.load(is);
        } catch (Exception e) {
            throw new Exception(String.format("Error reading file that represents a device. %s",file.getAbsoluteFile()));
        }

        if( !deviceReificationProperties.containsKey(Constants.ID) ||
            !deviceReificationProperties.containsKey(Constants.DEVICE_TYPE)||
            !deviceReificationProperties.containsKey(Constants.DEVICE_TYPE_SUB)){
            throw new Exception(String.format("File cannot represent a device since it does not contain the information base to work as such",file.getAbsoluteFile()));
        }

        return deviceReificationProperties;

    }

    public void onFileCreate(File file) {

        getLogger().info("File created {}", file.getAbsolutePath());

        try {
            Properties deviceReificationProperties=parseFile(file);

            String deviceId=deviceReificationProperties.getProperty(Constants.ID);

            createAndRegisterFakeDevice(deviceId);

            HashMap<String, Object> metadata = new HashMap<String, Object>();
            metadata.put(Constants.DEVICE_ID, deviceId);

            for(Map.Entry<Object,Object> element:deviceReificationProperties.entrySet()){
                Object replacedObject=metadata.put(element.getKey().toString(), element.getValue());

                if(replacedObject!=null){
                    getLogger().warn("ImportationDeclaration: replacing metadata key {}, that contained the value {} by the new value {}",new Object[]{element.getKey(),replacedObject,element.getValue()});
                }
            }

            ImportDeclaration declaration=createAndRegisterImportDeclaration(metadata);

            importDeclarationsFile.put(file.getAbsolutePath(),declaration);

        } catch (Exception e) {
            getLogger().error(e.getMessage());
        }

    }

    //TODO this have to be rechecked, this is an pessimist approach
    public void onFileChange(File file) {

        getLogger().info("File updated {}",file.getAbsolutePath());

        onFileDelete(file);
        onFileCreate(file);

    }

    public void onFileDelete(File file) {

        getLogger().info("File removed {}",file.getAbsolutePath() );

        ImportDeclaration declaration=importDeclarationsFile.get(file.getAbsolutePath());

        if(importDeclarationsFile.remove(file.getAbsolutePath())==null){
            getLogger().error("Failed to unregister device-file mapping ({}),  it did not existed before.",file.getAbsolutePath());
        } else {
            getLogger().info("Device-file mapping removed.");
        }

        try{
            unregisterImportDeclaration(declaration);
        } catch(IllegalStateException e){
            getLogger().error("Failed to unregister import declaration for the device {},  it did not existed before.",declaration.getMetadata());
        }

        try {
            String deviceId=(String)declaration.getMetadata().get(Constants.DEVICE_ID);
            devicesManaged.get(deviceId).dispose();
        } catch(NullPointerException e){
            getLogger().error("Failed to disconnect device {}, this device was not registered by this discovery",(String)declaration.getMetadata().get(Constants.DEVICE_ID));
        }

    }

    public void open(Collection<File> files) {

        for (File file : files) {
            onFileChange(file);
        }

    }

    public void close() { }
}
