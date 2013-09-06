package org.ow2.chameleon.fuchsia.upnp.discovery;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.*;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.ow2.chameleon.everest.client.EverestClient;
import org.ow2.chameleon.everest.services.EverestService;
import org.ow2.chameleon.everest.services.IllegalActionOnResourceException;
import org.ow2.chameleon.everest.services.Path;
import org.ow2.chameleon.everest.services.ResourceNotFoundException;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Discovery UPnP  TODO Documentation
 *
 * @author jeremy.savonet@gmail.com
 */
@Component(name = "Fuchsia-UPnPDiscovery-Factory")
@Provides(specifications = {DiscoveryService.class,UPnPFuchsiaDiscoveryImpl.class})
//@Instantiate(name = "Fuchsia-UPnPDiscovery")
public class UPnPFuchsiaDiscoveryImpl extends AbstractDiscoveryComponent implements ServiceTrackerCustomizer {

    @ServiceProperty(name = "instance.name")
    private String name;

    @Requires (optional = false)
    EverestService m_everestService;

    private EverestClient m_everestClient;

    private final HashMap<String,ImportDeclaration> importDeclarations = new HashMap<String, ImportDeclaration>();
    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private HashMap<String, Map<String,Object>> m_metadata;


    /**
     * A service tracker to detect apparition and disparition of UPnPDevice services instances
     */
    private ServiceTracker m_serviceTracker;

    /**
     * The bundle context
     */
    private BundleContext m_context;

    public UPnPFuchsiaDiscoveryImpl(BundleContext bundleContext) throws ResourceNotFoundException, IllegalActionOnResourceException {
        super(bundleContext);
        m_context=bundleContext;
        m_everestClient = new EverestClient(m_everestService);
        logger.debug("Creating UPnP discovery !");
    }

    @Validate
    public void start() {
        super.start();
        logger.debug("Start UPnP discovery !");
        String listenerFilter = "(" + Constants.OBJECTCLASS + "=" + UPnPDevice.class.getName() + ")";
        Filter filter;
        try {
            filter = m_context.createFilter(listenerFilter);
            m_serviceTracker = new ServiceTracker(m_context, filter, this);
            m_serviceTracker.open();
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Invalidate
    public void stop() {
        super.stop();
        synchronized (this) {
            for(ImportDeclaration importDeclaration : importDeclarations.values()) {
                removeUPnPDevice((String) importDeclaration.getMetadata().get("id"));
                unregisterImportDeclaration(importDeclaration);
            }
        }
        importDeclarations.clear();
        if (m_serviceTracker !=null)
            m_serviceTracker.close();

    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public String getName() {
        return name;
    }

    public Object addingService(ServiceReference reference) {
        logger.debug("ADDING OSGI SERVICE : " + reference.toString());
        String deviceID = (String)reference.getProperty(UPnPDevice.UDN);
        String deviceType = (String)reference.getProperty(UPnPDevice.UDN);
        String deviceSubTYype = (String)reference.getProperty(UPnPDevice.UDN);

        createUPnPDeviceInstanceAndPublishIDec(deviceID, deviceType, deviceSubTYype, reference);

        return m_context.getService(reference);
    }

    //TODO implements
    public void modifiedService(ServiceReference reference, Object service) {

    }

    //TODO implements
    public void removedService(ServiceReference reference, Object service) {
//        String deviceID = (String)reference.getProperty(UPnPDevice.UDN);
//        roseMachine.removeRemote(deviceID);
    }

    /**
     * Create an instance of the device through the factory and publish the Idec in order to create the proxy
     * @param deviceId the deviceID will be used as key
     * @param deviceType
     * @param deviceSubTYype
     * @param reference the service reference
     */
    public synchronized void createUPnPDeviceInstanceAndPublishIDec(String deviceId, String deviceType, String deviceSubTYype, ServiceReference reference) {

        HashMap<String,Object> metadata = new HashMap<String, Object>();
        metadata.put("id",deviceId);
        metadata.put("deviceType",deviceType);
        metadata.put("deviceSubType",deviceSubTYype);

        Map props = new Properties();
        props.put(RemoteConstants.ENDPOINT_ID, deviceId);
        props.put(RemoteConstants.SERVICE_IMPORTED_CONFIGS, "upnp");
        props.put("objectClass", new String[] { "someObject" });
        props.put("sensor.service.id", reference.getProperty(Constants.SERVICE_ID));
        props.put(UPnPDevice.UDN, reference.getProperty(UPnPDevice.UDN));

        EndpointDescription epd = new EndpointDescription(props);
        logger.debug("[DEBUG DISCOVERY] EndPoint : " + epd.toString());
        metadata.put("endpoint",epd);
        createUPnPDevice("fuchsia.GenericFakeDevice", deviceId, deviceType);

        //publish an importDeclaration
        importDeclarations.put(deviceId, ImportDeclarationBuilder.fromMetadata(metadata).build());
        registerImportDeclaration(importDeclarations.get(metadata.get("id")));
    }

    public Set<ImportDeclaration> getImportDeclarations() {
        Set<ImportDeclaration> idecs = new HashSet<ImportDeclaration>();
        synchronized (this) {
           for(ImportDeclaration idec : importDeclarations.values())
               idecs.add(idec);
        }
        return idecs;
    }

    /**
     * Method to create an instance of Generic device through a factory name
     * @param factoryName
     * @param deviceId
     * @param deviceType
     */
    public void createUPnPDevice(String factoryName, String deviceId, String deviceType) {

        // Create the instance
        try {
            m_everestClient.create(Path.from("/ipojo/factory/" + factoryName + "/null").toString()).with("instance.name","GenericDeviceInst-"+deviceId).with("device.serialNumber",deviceType).doIt();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalActionOnResourceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to create an instance of Generic device through a factory name
     */
    public void removeUPnPDevice(String deviceId) {

        // Delete the instance
        try {
            m_everestClient.delete(Path.from("/ipojo/instance/GenericDeviceInst-" + deviceId).toString()).doIt();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalActionOnResourceException e) {
            e.printStackTrace();
        }
    }

}
