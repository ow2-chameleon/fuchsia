package org.ow2.chameleon.fuchsia.core;


import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Set;

/**
 * The components providing this service are used by Fuchsia to make the link between the
 * {@link org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration} and the {@link org.ow2.chameleon.fuchsia.core.component.ImporterService}.
 * You can use multiples {@link Linker} with different configurations.
 * <p/>
 * A default implementation of {@link Linker} is provided by the {@link DefaultLinker} component.
 * If the {@link DefaultLinker} doesn't fit to your needs, you can use your own implementation
 * of this interface, by subclassing {@link DefaultLinker} or by implementing this {@link Linker} interface.
 *
 * @author Morgan Martinet
 */
public interface Linker {

    final static String PROPERTY_FILTER_IMPORTDECLARATION = "fuchsia.linker.filter.importDeclaration";

    final static String PROPERTY_FILTER_IMPORTERSERVICE = "fuchsia.linker.filter.importerService";

    final static String PROPERTY_UNIQUE_IMPORTATION = "fuchsia.linker.uniqueImportation";


    String getName();

    /**
     * Return the importerServices linked the Linker
     *
     * @return The importerServices linked to the Linker
     */
    Set<ImporterService> getLinkedImporters();

    /**
     * Return the importDeclarations bind by the Linker
     *
     * @return The importDeclarations bind by the Linker
     */
    Set<ImportDeclaration> getImportDeclarations();

}
