package org.ow2.chameleon.fuchsia.core.component;


import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationRegistrationManager;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.slf4j.Logger;

import java.util.Set;

/**
 * Abstract implementation of an export manager component which provides an {@link ExportManagerService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractExportManagerComponent implements ExportManagerService, ExportManagerIntrospection {

    private final DeclarationRegistrationManager<ExportDeclaration> declarationRegistrationManager;

    private final BundleContext bundleContext;


    protected AbstractExportManagerComponent(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        declarationRegistrationManager = new DeclarationRegistrationManager<ExportDeclaration>(bundleContext, ExportDeclaration.class);
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
        declarationRegistrationManager.unregisterAll();
    }

    /**
     * Utility method to register an ExportDeclaration has a Service in OSGi.
     * If you use it make sure to use unregisterExportDeclaration(...) to unregister the ExportDeclaration
     *
     * @param exportDeclaration the ExportDeclaration to register
     */
    protected void registerExportDeclaration(ExportDeclaration exportDeclaration) {
        declarationRegistrationManager.registerDeclaration(exportDeclaration);
    }

    /**
     * Utility method to unregister an ExportDeclaration of OSGi.
     * Use it only if you have used registerExportDeclaration(...) to register the ExportDeclaration
     *
     * @param exportDeclaration the ExportDeclaration to unregister
     */
    protected void unregisterExportDeclaration(ExportDeclaration exportDeclaration) {
        declarationRegistrationManager.unregisterDeclaration(exportDeclaration);
    }

    @Override
    public String toString() {
        return getName();
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    public abstract Logger getLogger();

    public Set<ExportDeclaration> getExportDeclarations() {
        return declarationRegistrationManager.getDeclarations();
    }
}
