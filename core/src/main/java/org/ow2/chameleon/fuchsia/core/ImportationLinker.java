package org.ow2.chameleon.fuchsia.core;


/**
 * The components providing this service are used by Fuchsia to make the link between the
 * {@link org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration} and the {@link org.ow2.chameleon.fuchsia.core.component.ImporterService}.
 * You can use multiples {@link ImportationLinker} with different configurations.
 * <p/>
 * A default implementation of {@link ImportationLinker} is provided by the {@link DefaultImportationLinker} component.
 * If the {@link DefaultImportationLinker} doesn't fit to your needs, you can use your own implementation
 * of this interface, by subclassing {@link DefaultImportationLinker} or by implementing this {@link ImportationLinker} interface.
 *
 * @author Morgan Martinet
 */
public interface ImportationLinker {

    final static String FILTER_IMPORTDECLARATION_PROPERTY = "fuchsia.linker.filter.importDeclaration";

    final static String FILTER_IMPORTERSERVICE_PROPERTY = "fuchsia.linker.filter.importerService";

    final static String UNIQUE_IMPORTATION_PROPERTY = "fuchsia.linker.uniqueImportation";

    String getName();

}
