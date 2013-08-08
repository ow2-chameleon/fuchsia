package org.ow2.chameleon.fuchsia.fake.discovery;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.fake.device.GenericDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This component according to its configuration create a fake device AND publish an
 * ImportDeclaration (this could / should be two different components, probably better
 * to separate all concepts)
 *
 * @author jeremy.savonet@gmail.com
 */

@Component(name = "Fuchsia-FakeDiscovery-Factory")
@Provides(specifications = DiscoveryService.class)
@Instantiate(name = "Fuchsia-FakeDiscovery")
public class FakeDiscoveryBridge extends AbstractDiscoveryComponent {

    @ServiceProperty(name = "instance.name")
    private String name;

    private final Map<String, Factory> m_factories = new HashMap<String, Factory>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private ReaderForListingFile m_readerForListingFile;

    private final ScheduledThreadPoolExecutor pool_register = new ScheduledThreadPoolExecutor(1);
    private final ScheduledThreadPoolExecutor pool_unregister = new ScheduledThreadPoolExecutor(1);

    private final List<ImportDeclaration> importDeclarations = new ArrayList<ImportDeclaration>();
    private Integer index_importDeclaration_register;
    private Integer index_importDeclaration_unregister;

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private HashMap<String, Map<String,Object>> m_metadata;

    public FakeDiscoveryBridge(BundleContext bundleContext) {
        super(bundleContext);
        logger.debug("Creating fake discovery !");
    }


    /**
     * Factory binding with filter on generic device
     * @param factory
     */
    @Bind(id = "factories", aggregate = true, optional = true, filter = "(component.providedServiceSpecifications=org.ow2.chameleon.fuchsia.fake.device.GenericDevice)")
    public void bindFactory(Factory factory) {
        System.out.println("Bind factory : " + factory.toString());
        String deviceType = factory.getName();
        lock.writeLock().lock();
        try {
            m_factories.put(deviceType, factory);
        } finally {
            lock.writeLock().unlock();
        }

    }

    @Unbind(id = "factories")
    public void unbindFactory(Factory factory) {
        String deviceType = factory.getName();
        lock.writeLock().lock();
        try {
            m_factories.remove(deviceType);
        } finally {
            lock.writeLock().unlock();
        }

    }

    private Factory getFactory(String name) {
        lock.readLock().lock();
        try {
            return m_factories.get(name);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Validate
    public void start() {
        super.start();
        m_readerForListingFile = new ReaderForListingFile();
        m_metadata = m_readerForListingFile.parserXML();
        logger.debug("Start fake discovery !");

        for(String key : m_metadata.keySet()) {

            //Create the fake device => instantiate new generic device
            System.out.println("Create the fake device : " + m_metadata.get(key).toString());
            createFakeDevice("iCasa.GenericFakeDevice",(String)m_metadata.get(key).get("id"),(String)m_metadata.get(key).get("type"),(String)m_metadata.get(key).get("subtype"));

            //publish an importDeclaration
            importDeclarations.add(ImportDeclarationBuilder.create().withMetadata(m_metadata.get(key)).build());
        }
        index_importDeclaration_register = -1;
        index_importDeclaration_unregister = -1;

        pool_register.execute(new Registrator());
        pool_unregister.execute(new Unregistrator());

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
     * Method to create instance of Generic device through a factory name
     * @param factoryName
     * @param deviceId
     * @param deviceType
     * @param deviceSubType
     */
    public void createFakeDevice(String factoryName, String deviceId, String deviceType, String deviceSubType) {

        Factory factory = getFactory(factoryName);
        if (factory == null) {
            throw new IllegalStateException("Unknown device type: " + factoryName);
        }

        // Create the device
        Dictionary<String, String> configProperties = new Hashtable<String, String>();
        configProperties.put(GenericDevice.DEVICE_SERIAL_NUMBER, deviceId);
        configProperties.put(GenericDevice.DEVICE_TYPE, deviceType);
        configProperties.put(GenericDevice.DEVICE_TYPE, deviceSubType);

        try {
            factory.createComponentInstance(configProperties);
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            unacceptableConfiguration.printStackTrace();
        } catch (MissingHandlerException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
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
