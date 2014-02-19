package org.ow2.chameleon.fuchsia.core.component;


import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

import java.util.Set;

/**
 * The components providing this service are used by Fuchsia to make the link between the
 * {@link ExportDeclaration} and the {@link ExporterService}.
 * You can use multiples {@link ExportationLinker} with different configurations.
 * <p/>
 * A default implementation of {@link ExportationLinker} is provided by the {@link DefaultExportationLinker} component.
 * If the {@link DefaultExportationLinker} doesn't fit to your needs, you can use your own implementation
 * of this interface, by subclassing {@link DefaultExportationLinker} or by implementing this {@link ExportationLinker} interface.
 *
 * @author Morgan Martinet
 */
public interface ExportationLinker {

    final static String FILTER_EXPORTDECLARATION_PROPERTY = "fuchsia.linker.filter.exportDeclaration";

    final static String FILTER_EXPORTERSERVICE_PROPERTY = "fuchsia.linker.filter.exporterService";

    final static String UNIQUE_EXPORTATION_PROPERTY = "fuchsia.linker.uniqueExportation";


    String getName();

    /**
     * Return the exporterServices linked the ExportationLinker
     *
     * @return The exporterServices linked to the ExportationLinker
     */
    Set<ExporterService> getLinkedExporters();

    /**
     * Return the exportDeclarations bind by the ExportationLinker
     *
     * @return The exportDeclarations bind by the ExportationLinker
     */
    Set<ExportDeclaration> getExportDeclarations();

}
