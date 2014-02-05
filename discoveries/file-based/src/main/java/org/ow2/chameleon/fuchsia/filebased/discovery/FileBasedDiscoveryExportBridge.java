package org.ow2.chameleon.fuchsia.filebased.discovery;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.filebased.discovery.monitor.Deployer;
import org.ow2.chameleon.fuchsia.filebased.discovery.monitor.DirectoryMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

/**
 * This component instantiate a directory monitor (initially pointed to a directory in chameleon called "load/export") that reads all file placed there (as property files)
 * and publishes an {@link ExportDeclaration}
 *
 * @author jeremy.savonet@gmail.com
 * @author botelho (at) imag.fr
 */

@Component(name = "Fuchsia-FileBaseExportDiscovery-Factory")
@Provides(specifications = {DiscoveryService.class,Deployer.class})
@Instantiate
public class FileBasedDiscoveryExportBridge extends AbstractDiscoveryComponent implements Deployer {

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @ServiceProperty(name = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_EXPORT_PROPERTY_KEY_MONITORED_DIR_KEY,value = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_EXPORT_PROPERTY_KEY_MONITORED_DIR_VALUE)
    private String monitoredExportDirectory;

    @ServiceProperty(name = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_PROPERTY_POLLING_TIME_KEY,value = FileBasedDiscoveryConstants.FILEBASED_DISCOVERY_PROPERTY_POLLING_TIME_VALUE)
    private Long pollingTime;

    private final Map<ExportDeclaration, ServiceRegistration> exportDeclarationsRegistered=new HashMap<ExportDeclaration, ServiceRegistration>();

    private final HashMap<String,ExportDeclaration> exportDeclarationsFile = new HashMap<String, ExportDeclaration>();

    public FileBasedDiscoveryExportBridge(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Validate
    public void start() {

        super.start();

        startMonitorDirectory(monitoredExportDirectory,pollingTime);

        getLogger().info("Filebase Export discovery up and running.");

    }

    private void startMonitorDirectory(String directory, Long poolTime){

        try {

            DirectoryMonitor dm=new DirectoryMonitor(directory,pollingTime,this);

            dm.start(getBundleContext());

        } catch (Exception e) {
            getLogger().error("Failed to start {} for the directory {} and polling time {}, with the message '{}'", new String[]{DirectoryMonitor.class.getName(), directory, poolTime.toString(), e.getMessage()});
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

            HashMap<String, Object> metadata = new HashMap<String, Object>();
            metadata.put(Constants.DEVICE_ID, deviceId);

            for(Map.Entry<Object,Object> element:deviceReificationProperties.entrySet()){
                Object replacedObject=metadata.put(element.getKey().toString(), element.getValue());

                if(replacedObject!=null){
                    getLogger().warn("ImportationDeclaration: replacing metadata key {}, that contained the value {} by the new value {}",new Object[]{element.getKey(),replacedObject,element.getValue()});
                }
            }

            createAndRegisterExportDeclaration(metadata);

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

        ExportDeclaration declaration= exportDeclarationsFile.get(file.getAbsolutePath());

        if(declaration==null) return;

        if(exportDeclarationsFile.remove(file.getAbsolutePath())==null){
            getLogger().error("Failed to unregister device-file mapping ({}),  it did not existed before.",file.getAbsolutePath());
        } else {
            getLogger().info("Device-file mapping removed.");
        }

        try{
            unregisterExportDeclaration(declaration);
        } catch(IllegalStateException e){
            getLogger().error("Failed to unregister import declaration for the device {},  it did not existed before.",declaration.getMetadata());
        }

    }

    public void open(Collection<File> files) {

        for (File file : files) {
            onFileChange(file);
        }

    }

    private ExportDeclaration createAndRegisterExportDeclaration(HashMap<String, Object> metadata){


        ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();


        registerExportDeclaration(declaration);

        return declaration;

    }

    private void registerExportDeclaration(ExportDeclaration declaration){

        synchronized (exportDeclarationsRegistered) {
            if (exportDeclarationsRegistered.containsKey(declaration)) {
                throw new IllegalStateException("The given ExportDeclaration has already been registered.");
            }

            Dictionary<String, Object> props = new Hashtable<String, Object>();
            String clazzes[] = new String[]{ExportDeclaration.class.getName()};
            ServiceRegistration registration;
            registration = super.getBundleContext().registerService(clazzes, declaration, props);

            exportDeclarationsRegistered.put(declaration, registration);
        }

    }

    protected void unregisterExportDeclaration(ExportDeclaration importDeclaration) {
        ServiceRegistration registration;
        synchronized (exportDeclarationsRegistered) {
            registration = exportDeclarationsRegistered.remove(importDeclaration);
            if (registration == null) {
                throw new IllegalStateException("The given ExportDeclaration has never been registered"
                        + "or have already been unregistered.");
            }
        }
        registration.unregister();
    }

    public void close() { }

}
