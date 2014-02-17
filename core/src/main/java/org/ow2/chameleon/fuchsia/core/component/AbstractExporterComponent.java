package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

import java.util.HashSet;
import java.util.Set;

import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of an exporter which provides an {@link ExporterService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractExporterComponent implements ExporterService {
    private final Set<ExportDeclaration> exportDeclarations;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractExporterComponent.class);

    public AbstractExporterComponent() {
        exportDeclarations = new HashSet<ExportDeclaration>();
    }

    /**
     * Abstract method, is called when the sub class must export the service describe in the given ExportDeclaration
     * <p/>
     * TODO : Does it should be able to throw an exception if there's problem ? which one ?
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

        synchronized (exportDeclarations) {
            // destroy all the proxies
            for (ExportDeclaration exportDeclaration : exportDeclarations) {
                try {
                    denyExportDeclaration(exportDeclaration);
                } catch (BinderException e) {
                    LOG.error("An exception has been thrown while denying the exportDeclaration "
                            + exportDeclaration
                            + "Stopping in progress.", e);
                }
            }
            // Clear the map
            exportDeclarations.clear();
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
     *  ExportService implementation *
	 *---------------------------------*/

    /**
     * @param exportDeclaration The {@link ExportDeclaration} of the service to be exported.
     */
    public void addExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {
        synchronized (exportDeclarations) {
            if (exportDeclarations.contains(exportDeclaration)) {
                // Already register
                // FIXME :  Clone Registration ??
                throw new UnsupportedOperationException("Duplicate ExportDeclaration are for the moment " +
                        "not supported by the AbstractExporterComponent");
            } else {
                //First registration, create the proxy
                useExportDeclaration(exportDeclaration);
                exportDeclarations.add(exportDeclaration);
            }
        }
    }

    /**
     * @param exportDeclaration The {@link ExportDeclaration} of the service to stop to be exported.
     */
    public void removeExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {
        synchronized (exportDeclarations) {
            denyExportDeclaration(exportDeclaration);
            exportDeclarations.remove(exportDeclaration);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
