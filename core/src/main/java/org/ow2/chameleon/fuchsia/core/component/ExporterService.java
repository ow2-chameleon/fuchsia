package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

public interface ExporterService {
    /**
     * Export the service describe in the exportDeclaration.
     *
     * @param exportDeclaration The {@link ExportDeclaration} of the service to be exported.
     */
    void addExportDeclaration(ExportDeclaration exportDeclaration);

    /**
     * Stop the exportation of the given exportDeclaration
     *
     * @param exportDeclaration The {@link ExportDeclaration} of the service to stop be exported.
     */
    void removeExportDeclaration(ExportDeclaration exportDeclaration);

    String getName();
}
