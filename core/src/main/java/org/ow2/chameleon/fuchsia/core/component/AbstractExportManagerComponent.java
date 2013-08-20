package org.ow2.chameleon.fuchsia.core.component;


import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.slf4j.Logger;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Abstract implementation of an export manager component which provides an {@link ExportManagerService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractExportManagerComponent implements ExportManagerService {

    private final Map<ExportDeclaration, ServiceRegistration> exportDeclarationsRegistered;

    private final BundleContext bundleContext;


    protected AbstractExportManagerComponent(BundleContext bundleContext) {
        this.exportDeclarationsRegistered = new HashMap<ExportDeclaration, ServiceRegistration>();
        this.bundleContext = bundleContext;
    }

    /**
     * Start the discovery component, iPOJO Validate instance callback.
     * Must be override !
     */
    protected void start() {
    }

    /**
     * Stop the discovery component, iPOJO Invalidate instance callback.
     * Must be override !
     */
    protected void stop() {
        synchronized (exportDeclarationsRegistered) {
            for (ServiceRegistration registration : exportDeclarationsRegistered.values()) {
                if (registration != null) {
                    registration.unregister();
                }
            }
            exportDeclarationsRegistered.clear();
        }
    }


    /**
     * Utility method to register an ExportDeclaration has a Service in OSGi.
     * If you use it make sure to use unregisterExportDeclaration(...) to unregister the ExportDeclaration
     *
     * @param exportDeclaration the ExportDeclaration to register
     */
    protected void registerExportDeclaration(ExportDeclaration exportDeclaration) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        String clazzes[] = new String[]{ExportDeclaration.class.getName()};
        ServiceRegistration registration;
        registration = bundleContext.registerService(clazzes, exportDeclaration, props);
        synchronized (exportDeclarationsRegistered) {
            exportDeclarationsRegistered.put(exportDeclaration, registration);
        }
    }


    /**
     * Utility method to unregister an ExportDeclaration of OSGi.
     * Use it only if you have used registerExportDeclaration(...) to register the ExportDeclaration
     *
     * @param exportDeclaration the ExportDeclaration to unregister
     */
    protected void unregisterExportDeclaration(ExportDeclaration exportDeclaration) {
        ServiceRegistration registration;
        synchronized (exportDeclarationsRegistered) {
            registration = exportDeclarationsRegistered.remove(exportDeclaration);
        }
        if (registration != null) {
            registration.unregister();
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    public abstract Logger getLogger();
}
