package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.util.BadImportRegistration;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract implementation of an proxy-creator which provides an {@link ImporterService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 * @author barjo
 * @author Morgan Martinet
 */
public abstract class AbstractImporterComponent implements ImporterService {
    private final Set<ImportDeclaration> importDeclarations;

    private volatile boolean isValid = false;

    public AbstractImporterComponent() {
        importDeclarations = new HashSet<ImportDeclaration>();
    }

    /**
     * Abstract method, is called when the sub class must create the proxy or do other stuff, like APAM things... dun know
     * <p/>
     * TODO : should maybe be able to throw which exception if there's problem ?
     */
    protected abstract void createProxy(final ImportDeclaration importDeclaration);

    /**
     * Abstract method, is called when the sub class must destroy the proxy. Do not forget to unregister the proxy, call:
     * <code>registration.unregister()</code>
     */
    protected abstract void destroyProxy(final ImportDeclaration importDeclaration);

    /**
     * Stop the proxy-creator, iPOJO Invalidate instance callback.
     * Must be override !
     */
    protected void stop() {

        synchronized (importDeclarations) {
            // destroy all the proxies
            for (ImportDeclaration importDeclaration : importDeclarations) {
                destroyProxy(importDeclaration);
            }
            // Clear the map
            importDeclarations.clear();
            isValid = false;
        }
    }

    /**
     * Start the endpoint-creator component, iPOJO Validate instance callback.
     * Must be override !
     */
    protected void start() {
        synchronized (importDeclarations) {
            isValid = true;
        }
    }

    /**
     * @return <code>true</code> if the {@link ImporterService} is in a valid state, <code>false</code> otherwise.
     */
    protected final boolean isValid() {
        return isValid;
    }

	/*---------------------------------*
     *  ImportService implementation *
	 *---------------------------------*/

    /**
     * @param importDeclaration The ImportDeclartion pass to the ImporterService
     */
    public void addImportDeclaration(ImportDeclaration importDeclaration) throws BadImportRegistration {
        if (!isValid) {
            throw new BadImportRegistration("This ImporterService is no more available. !");
        }

        synchronized (importDeclarations) {
            if (importDeclarations.contains(importDeclaration)) {
                // Already register
                // FIXME :  Clone Registration ??
                throw new UnsupportedOperationException("Duplicate ImportDeclaration are for the moment " +
                        "not supported by the AbstractImporterComponent");
            } else {
                //First registration, create the proxy
                createProxy(importDeclaration);
                importDeclarations.add(importDeclaration);
            }
        }
    }

    public void removeImportDeclaration(ImportDeclaration importDeclaration) throws BadImportRegistration {
        if (!isValid) {
            throw new BadImportRegistration("This ImporterService is no more available. !");
        }
        synchronized (importDeclarations) {
            destroyProxy(importDeclaration);
            importDeclarations.remove(importDeclaration);
        }
    }
}
