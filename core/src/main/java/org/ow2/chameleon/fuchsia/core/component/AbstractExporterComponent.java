package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract implementation of an exporter which provides an {@link ExporterService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractExporterComponent implements ExporterService {
    private final Set<ExportDeclaration> exportDeclarations;

    public AbstractExporterComponent() {
        exportDeclarations = new HashSet<ExportDeclaration>();
    }

    /**
     * Abstract method, is called when the sub class must export the service describe in the given ExportDeclaration
     * <p/>
     * TODO : Does it should be able to throw an exception if there's problem ? which one ?
     */
    protected abstract void useExportDeclaration(final ExportDeclaration exportDeclaration);

    /**
     * Abstract method, is called when the sub class must stop the export of the service describe in the given
     * ExportDeclaration.
     */
    protected abstract void denyExportDeclaration(final ExportDeclaration exportDeclaration);

    /**
     * Stop the exporter, iPOJO Invalidate instance callback.
     * Must be override !
     */
    protected void stop() {

        synchronized (exportDeclarations) {
            // destroy all the proxies
            for (ExportDeclaration exportDeclaration : exportDeclarations) {
                denyExportDeclaration(exportDeclaration);
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
    public void addExportDeclaration(ExportDeclaration exportDeclaration) {
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
    public void removeExportDeclaration(ExportDeclaration exportDeclaration) {
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
