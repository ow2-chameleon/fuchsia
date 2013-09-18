package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;

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
public abstract class AbstractImporterComponent implements ImporterService, ImporterIntrospection {
    private final Set<ImportDeclaration> importDeclarations;

    public AbstractImporterComponent() {
        importDeclarations = new HashSet<ImportDeclaration>();
    }

    /**
     * Abstract method, called when a ImportDeclaration can be used by the implementation class.
     */
    protected abstract void useImportDeclaration(final ImportDeclaration importDeclaration) throws ImporterException;

    /**
     * Abstract method, is called when the implementation class must stop to use an ImportDeclaration.
     */
    protected abstract void denyImportDeclaration(final ImportDeclaration importDeclaration) throws ImporterException;

    /**
     * Stop the Importer component, iPOJO Invalidate instance callback.
     * Must be override !
     */
    protected void stop() {

        synchronized (importDeclarations) {
            // deny all the ImportDeclarations
            for (ImportDeclaration importDeclaration : importDeclarations) {
                try {
                    denyImportDeclaration(importDeclaration);
                } catch (ImporterException e) {
                    e.printStackTrace();
                }
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
     * @throws ImporterException
     */
    public void addImportDeclaration(final ImportDeclaration importDeclaration) throws ImporterException {
        synchronized (importDeclarations) {
            if (importDeclarations.contains(importDeclaration)) {
                // Already register
                throw new IllegalStateException("Duplicate ImportDeclaration : " +
                        "this ImportDeclaration has already been treated.");
            }
            // First registration, give it to the implementation class and keep it in memory
            useImportDeclaration(importDeclaration);
            importDeclarations.add(importDeclaration);
        }
    }

    /**
     * @param importDeclaration The {@link ImportDeclaration} of the service to stop to be imported.
     * @throws ImporterException
     */
    public void removeImportDeclaration(final ImportDeclaration importDeclaration) throws ImporterException {
        synchronized (importDeclarations) {
            if (!importDeclarations.contains(importDeclaration)) {
                throw new IllegalStateException("The given ImportDeclaration has never been added"
                        + "or have already been removed.");
            }
            importDeclarations.remove(importDeclaration);
        }
        denyImportDeclaration(importDeclaration);
    }

    public Set<ImportDeclaration> getImportDeclarations() {
        synchronized (importDeclarations) {
            return new HashSet<ImportDeclaration>(importDeclarations);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
