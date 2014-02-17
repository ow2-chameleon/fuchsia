package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBindManager;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

/**
 * Abstract implementation of an exporter which provides an {@link ExporterService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractExporterComponent implements ExporterService {
    private final DeclarationBindManager<ExportDeclaration> declarationBindManager;

    public AbstractExporterComponent() {
        declarationBindManager = new DeclarationBindManager<ExportDeclaration>(this);
    }

    /**
     * Abstract method, is called when the sub class must export the service describe in the given ExportDeclaration
     */
    protected abstract void useExportDeclaration(final ExportDeclaration exportDeclaration) throws BinderException;

    /**
     * Abstract method, is called when the sub class must stop the export of the service describe in the given
     * ExportDeclaration.
     */
    protected abstract void denyExportDeclaration(final ExportDeclaration exportDeclaration) throws BinderException;

    /**
     * Stop the exporter, iPOJO Invalidate instance callback.
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
     * @param exportDeclaration The {@link ExportDeclaration} of the service to be exported.
     */
    public void addExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {
        declarationBindManager.addDeclaration(exportDeclaration);
    }

    public void useDeclaration(ExportDeclaration declaration) throws BinderException {
        useExportDeclaration(declaration);
    }

    /**
     * @param exportDeclaration The {@link ExportDeclaration} of the service to stop to be exported.
     */
    public void removeExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {
        declarationBindManager.removeDeclaration(exportDeclaration);
    }

    public void denyDeclaration(ExportDeclaration declaration) throws BinderException {
        denyExportDeclaration(declaration);
    }

    @Override
    public String toString() {
        return getName();
    }
}
