package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Set;

public interface ImportationLinkerIntrospection extends ImportationLinker {

    /**
     * Return the importerServices linked the ImportationLinker
     *
     * @return The importerServices linked to the ImportationLinker
     */
    Set<ImporterService> getLinkedImporters();

    /**
     * Return the importDeclarations bind by the ImportationLinker
     *
     * @return The importDeclarations bind by the ImportationLinker
     */
    Set<ImportDeclaration> getImportDeclarations();


    // TODO : get Importer Errors ??




}
