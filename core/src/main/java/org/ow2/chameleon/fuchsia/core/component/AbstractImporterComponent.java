package org.ow2.chameleon.fuchsia.core.component;

import org.osgi.framework.InvalidSyntaxException;
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

    public AbstractImporterComponent() {
        importDeclarations = new HashSet<ImportDeclaration>();
    }

    /**
     * Abstract method, is called when the sub class must create the proxy or do other stuff, like APAM things... dun know
     * <p/>
     * TODO : Does it should be able to throw an exception if there's problem ? which one ?
     */
    protected abstract void createProxy(final ImportDeclaration importDeclaration);

    /**
     * Abstract method, is called when the sub class must destroy the proxy.
     * Do not forget to unregister the proxy, call: <code>proxy.unregister()</code>
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
        }
    }

    /**
     * Start the endpoint-creator component, iPOJO Validate instance callback.
     * Must be override !
     */
    protected void start() {
        //
    }


	/*---------------------------------*
     *  ImportService implementation *
	 *---------------------------------*/

    /**
     * @param importDeclaration The {@link ImportDeclaration} of the service to be imported.
     * @throws BadImportRegistration
     */
    public void addImportDeclaration(ImportDeclaration importDeclaration) throws BadImportRegistration {
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


    /**
     * @param importDeclaration The {@link ImportDeclaration} of the service to stop to be imported.
     * @throws BadImportRegistration
     */
    public void removeImportDeclaration(ImportDeclaration importDeclaration) throws BadImportRegistration {
        synchronized (importDeclarations) {
            destroyProxy(importDeclaration);
            importDeclarations.remove(importDeclaration);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
