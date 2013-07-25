package org.ow2.chameleon.fuchsia.core.component;


import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Abstract implementation of an discovery component which provides an {@link DiscoveryService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractDiscoveryComponent implements DiscoveryService {

    private final Map<ImportDeclaration, ServiceRegistration> importDeclarationsRegistered;

    private BundleContext bundleContext;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AbstractDiscoveryComponent(BundleContext bundleContext) {
        logger.debug("Creating discovery");

        this.importDeclarationsRegistered = new HashMap<ImportDeclaration, ServiceRegistration>();
        this.bundleContext = bundleContext;
    }

    /**
     * Start the discovery component, iPOJO Validate instance callback.
     * Must be override !
     */
    protected void start() {
        logger.debug("Starting discovery");

    }

    /**
     * Stop the discovery component, iPOJO Invalidate instance callback.
     * Must be override !
     */
    protected void stop() {
        logger.debug("Stopping discovery");

        synchronized (importDeclarationsRegistered) {
            for (ServiceRegistration registration : importDeclarationsRegistered.values()) {
                if (registration != null) {
                    registration.unregister();
                }
            }
            importDeclarationsRegistered.clear();
        }
    }


    /**
     * Utility method to register an ImportDeclaration has a Service in OSGi.
     * If you use it make sure to use unregisterImportDeclaration(...) to unregister the ImportDeclaration
     *
     * @param importDeclaration the ImportDeclaration to register
     */
    protected void registerImportDeclaration(ImportDeclaration importDeclaration) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        String clazzes[] = new String[]{ImportDeclaration.class.getName()};
        ServiceRegistration registration;
        registration = bundleContext.registerService(clazzes, importDeclaration, props);
        synchronized (importDeclarationsRegistered) {
            importDeclarationsRegistered.put(importDeclaration, registration);
        }
    }


    /**
     * Utility method to unregister an ImportDeclaration of OSGi.
     * Use it only if you have used registerImportDeclaration(...) to register the ImportDeclaration
     *
     * @param importDeclaration the ImportDeclaration to unregister
     */
    protected void unregisterImportDeclaration(ImportDeclaration importDeclaration) {
        ServiceRegistration registration;
        synchronized (importDeclarationsRegistered) {
            registration = importDeclarationsRegistered.remove(importDeclaration);
        }
        if (registration != null) {
            registration.unregister();
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
