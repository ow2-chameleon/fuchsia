package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBindManager;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;

import java.util.Set;

/**
 * Abstract implementation of an proxy-creator which provides an {@link ImporterService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractImporterComponent implements ImporterService, ImporterIntrospection {
    private final DeclarationBindManager<ImportDeclaration> declarationBindManager;

    public AbstractImporterComponent() {
        declarationBindManager = new DeclarationBindManager<ImportDeclaration>(this);
    }

    /**
     * Abstract method, called when a ImportDeclaration can be used by the implementation class.
     */
    protected abstract void useImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException;

    /**
     * Abstract method, is called when the implementation class must stop to use an ImportDeclaration.
     */
    protected abstract void denyImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException;

    /**
     *
     */
    protected abstract Logger getLogger();

    /**
     * Stop the Importer component, iPOJO Invalidate instance callback.
     * Must be override !
     */
    protected void stop() {
        declarationBindManager.unbindAll();
    }

    /**
     * Start the endpoint-creator component, iPOJO Validate instance callback.
     * Must be override !
     */
    protected void start() {
        //
    }

    /**
     * @param importDeclaration The {@link ImportDeclaration} of the service to be imported.
     * @throws org.ow2.chameleon.fuchsia.core.exceptions.BinderException
     */
    public void addImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {
        declarationBindManager.addDeclaration(importDeclaration);
    }

    public void useDeclaration(ImportDeclaration declaration) throws BinderException {
        useImportDeclaration(declaration);
    }

    /**
     * @param importDeclaration The {@link ImportDeclaration} of the service to stop to be imported.
     * @throws org.ow2.chameleon.fuchsia.core.exceptions.BinderException
     */
    public void removeImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {
        declarationBindManager.removeDeclaration(importDeclaration);
    }

    public void denyDeclaration(ImportDeclaration declaration) throws BinderException {
        denyImportDeclaration(declaration);
    }

    public Set<ImportDeclaration> getImportDeclarations() {
        return declarationBindManager.getDeclarations();
    }

    @Override
    public String toString() {
        return getName();
    }
}
