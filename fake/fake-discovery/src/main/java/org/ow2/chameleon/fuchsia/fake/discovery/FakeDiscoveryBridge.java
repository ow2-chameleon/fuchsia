package org.ow2.chameleon.fuchsia.fake.discovery;

import org.ow2.chameleon.fuchsia.fake.device.GenericDevice;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
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
 * This component according to its configuration create a fake device AND publish an
 * ImportDeclaration.
 *
 * <strong>Note : We used the Everest Core in order to facilitate the creation/update/deletion of iPOJO instances.</strong> *
 * @author jeremy.savonet@gmail.com
 */

@Component(name = "Fuchsia-FakeDiscovery-Factory")
@Provides(specifications = {DiscoveryService.class,FakeDiscoveryBridge.class})
@Instantiate(name = "Fuchsia-FakeDiscovery")
public class FakeDiscoveryBridge extends AbstractDiscoveryComponent {

    @ServiceProperty(name = "instance.name")
    private String name;

    @Requires (optional = false)
    EverestService m_everestService;

    private EverestClient m_everestClient;

    private final ScheduledThreadPoolExecutor pool_register = new ScheduledThreadPoolExecutor(1);
    private final ScheduledThreadPoolExecutor pool_unregister = new ScheduledThreadPoolExecutor(1);

    private final HashMap<String,ImportDeclaration> importDeclarations = new HashMap<String, ImportDeclaration>();
    private Integer index_importDeclaration_register;
    private Integer index_importDeclaration_unregister;

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private HashMap<String, Map<String,Object>> m_metadata;

    public FakeDiscoveryBridge(BundleContext bundleContext) throws ResourceNotFoundException, IllegalActionOnResourceException {
        super(bundleContext);
        m_everestClient = new EverestClient(m_everestService);
        logger.debug("Creating fake discovery !");
    }

    @Validate
    public void start() {
        super.start();
        logger.debug("Start fake discovery !");
    }

    @Invalidate
    public void stop() {
        super.stop();
        pool_unregister.shutdown();
        pool_register.shutdown();

        importDeclarations.clear();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public String getName() {
        return name;
    }

    /**
     * Create an instance of the device through the factory and publish the Idec in order to create the proxy
     * @param metadata
     */
    public synchronized void createFakeDeviceInstanceAndPublishIDec(HashMap<String,Object> metadata) {

        //Create the fake device => instantiate new generic device
        logger.info("Create the fake device : " + metadata.toString());
        createFakeDevice("fuchsia.GenericFakeDevice",(String)metadata.get("id"),(String)metadata.get("type"),(String)metadata.get("subtype"));

        //publish an importDeclaration
        importDeclarations.put((String) metadata.get("id"), ImportDeclarationBuilder.fromMetadata(metadata).build());

        registerImportDeclaration(importDeclarations.get((String)metadata.get("id")));

        index_importDeclaration_register = -1;
        index_importDeclaration_unregister = -1;

        pool_register.execute(new Registrator());
        pool_unregister.execute(new Unregistrator());
    }

    /**
     * Reconfigure an instance of a device through the factory and republish the Idec in order to create the proxy
     * @param metadata
     */
    public synchronized void reconfigureFakeDeviceInstanceAndRePublishIDec(HashMap<String,Object> metadata) {

        //Create the fake device => instantiate new generic device
        logger.info("Reconfigure the fake device : " + metadata.toString());
        updateFakeDevice("fuchsia.GenericFakeDevice", (String)metadata.get("id"), (String)metadata.get("type"), (String)metadata.get("subtype"));

        //remove the importDeclaration and republish it
        unregisterImportDeclaration(importDeclarations.get((String) metadata.get("id")));
        importDeclarations.put((String) metadata.get("id"), ImportDeclarationBuilder.fromMetadata(metadata).build());
        registerImportDeclaration(importDeclarations.get((String)metadata.get("id")));
    }

    /**
     * Remove the instance of the device through the factory and unregister the Idec in order to create the proxy
     * @param metadata
     */
    public synchronized void removeFakeDeviceInstanceAndIDec(HashMap<String,Object> metadata) {

        //Create the fake device => instantiate new generic device
        logger.info("Delete the fake device : " + (String)metadata.get("id").toString());
        deleteFakeDevice("fuchsia.GenericFakeDevice", (String)metadata.get("id"), (String)metadata.get("type"), (String)metadata.get("subtype"));

        //remove the importDeclaration
        unregisterImportDeclaration(importDeclarations.get((String)metadata.get("id")));
        importDeclarations.remove(importDeclarations.get((String) metadata.get("id")));
    }


    /**
     * Method to create an instance of Generic device through a factory name
     * @param factoryName
     * @param deviceId
     * @param deviceType
     * @param deviceSubType
     */
    public void createFakeDevice(String factoryName, String deviceId, String deviceType, String deviceSubType) {

        // Create the device
        Dictionary<String, String> configProperties = new Hashtable<String, String>();
        configProperties.put(GenericDevice.DEVICE_SERIAL_NUMBER, deviceId);
        configProperties.put(GenericDevice.DEVICE_TYPE, deviceType);
        configProperties.put(GenericDevice.DEVICE_TYPE, deviceSubType);

        //Create the instance
        try {
            m_everestClient.create(Path.from("/ipojo/factory/"+factoryName+"/null").toString()).with("instance.name","GenericDeviceInst-"+deviceId).with("device.serialNumber",deviceId).doIt();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalActionOnResourceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to create instance of Generic device through a factory name
     * @param factoryName
     * @param deviceId
     * @param deviceType
     * @param deviceSubType
     */
    public void updateFakeDevice(String factoryName, String deviceId, String deviceType, String deviceSubType) {
         //TODO implements
    }

    /**
     * Method to delete the instance of Generic device through a factory name
     * @param factoryName
     * @param deviceId
     * @param deviceType
     * @param deviceSubType
     */
    public void deleteFakeDevice(String factoryName, String deviceId, String deviceType, String deviceSubType) {

        // Create the device
        Dictionary<String, String> configProperties = new Hashtable<String, String>();
        configProperties.put(GenericDevice.DEVICE_SERIAL_NUMBER, deviceId);
        configProperties.put(GenericDevice.DEVICE_TYPE, deviceType);
        configProperties.put(GenericDevice.DEVICE_TYPE, deviceSubType);

        //delete the instance
        try {
            m_everestClient.delete(Path.from("/ipojo/instance/"+"GenericDeviceInst-"+deviceId).toString()).doIt();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalActionOnResourceException e) {
            e.printStackTrace();
        }
    }


    protected class Registrator implements Runnable {

        final Random random = new Random();

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        public void run() {
            while (index_importDeclaration_register < importDeclarations.size()) {
                try {
                    Thread.sleep((random.nextInt(5) + 1) * 3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.debug("Registrator : " + index_importDeclaration_register);
                registerImportDeclaration(importDeclarations.get(index_importDeclaration_register + 1));
                index_importDeclaration_register = index_importDeclaration_register + 1;
            }

        }
    }

    protected class Unregistrator implements Runnable {

        final Random random = new Random();

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        public void run() {
            while (index_importDeclaration_unregister < importDeclarations.size()) {
                while (index_importDeclaration_unregister.equals(index_importDeclaration_register)) {
                    try {
                        Thread.sleep((random.nextInt(5) + 1) * 2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                logger.debug("Unregistrator : " + index_importDeclaration_unregister);

                unregisterImportDeclaration(importDeclarations.get(index_importDeclaration_unregister + 1));
                index_importDeclaration_unregister = index_importDeclaration_unregister + 1;
            }

        }
    }
}
