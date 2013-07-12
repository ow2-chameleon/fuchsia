package org.ow2.chameleon.fuchsia.core.declaration;

import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.List;

/**
 * This class contains the status at one moment t of an {@link ImportDeclaration} bounds the {@link ImporterService}s.
 *
 * @author Morgan Martinet
 */
public class Status {
    // true if the ImportDeclaration bound to at least one ImporterService
    private final Boolean bound;

    // the list of ImporterService the ImportDeclaration is bound to.
    private final List<ImporterService> importerServices;

    public Status(List<ImporterService> importerServices) {
        this.importerServices = importerServices;
        this.bound = (this.importerServices.size() > 0);
    }

    /**
     * @return true if the ImportDeclaration is bound to at least one ImporterService, false otherwise
     */
    public Boolean isBound() {
        return bound;
    }

    /**
     * @return The list of ImporterService which are bound to the ImportDeclaration
     */
    public List<ImporterService> getImporterServices() {
        return importerServices;
    }
}
