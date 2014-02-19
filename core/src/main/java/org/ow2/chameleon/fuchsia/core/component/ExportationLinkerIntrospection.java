package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

import java.util.Set;

/**
 * Created by mo on 19/02/14.
 */
public interface ExportationLinkerIntrospection {
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
