package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

import java.util.Set;

/**
 *
 */
public interface ExporterIntrospection {
    Set<ExportDeclaration> getExportDeclarations();
}

