package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Set;

/**
 *
 */
public interface DiscoveryIntrospection {

    Set<ImportDeclaration> getImportDeclarations();
}
