package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Set;

/**
 *
 */
public interface ImporterIntrospection {

    Set<ImportDeclaration> getImportDeclarations();

    // TODO : get URLs of the imported services
}
