package org.ow2.chameleon.fuchsia.core.component;


import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

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

    String FILTER_EXPORTDECLARATION_PROPERTY = "fuchsia.linker.filter.exportDeclaration";

    String FILTER_EXPORTERSERVICE_PROPERTY = "fuchsia.linker.filter.exporterService";

    String getName();

}
