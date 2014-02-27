package org.ow2.chameleon.fuchsia.core.component;


import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationRegistrationManager;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Set;

/**
 * Abstract implementation of an discovery component which provides an {@link DiscoveryService}.
 * Start must be call before registering the service !
 * Stop must be called when the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractDiscoveryComponent implements DiscoveryService, DiscoveryIntrospection {

    private final DeclarationRegistrationManager<ImportDeclaration> declarationRegistrationManager;
    private final BundleContext bundleContext;

    protected AbstractDiscoveryComponent(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        declarationRegistrationManager = new DeclarationRegistrationManager<ImportDeclaration>(bundleContext, ImportDeclaration.class);
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
     * Utility method to register an ImportDeclaration has a Service in OSGi.
     * If you use it make sure to use unregisterImportDeclaration(...) to unregister the ImportDeclaration
     *
     * @param importDeclaration the ImportDeclaration to register
     */
    protected void registerImportDeclaration(ImportDeclaration importDeclaration) {
        declarationRegistrationManager.registerDeclaration(importDeclaration);
    }

    /**
     * Utility method to unregister an ImportDeclaration of OSGi.
     * Use it only if you have used registerImportDeclaration(...) to register the ImportDeclaration
     *
     * @param importDeclaration the ImportDeclaration to unregister
     */
    protected void unregisterImportDeclaration(ImportDeclaration importDeclaration) {
        declarationRegistrationManager.unregisterDeclaration(importDeclaration);
    }

    @Override
    public String toString() {
        return getName();
    }

    public Set<ImportDeclaration> getImportDeclarations() {
        return declarationRegistrationManager.getDeclarations();
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

}
