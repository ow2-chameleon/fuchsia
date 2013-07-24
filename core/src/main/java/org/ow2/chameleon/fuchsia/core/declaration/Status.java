package org.ow2.chameleon.fuchsia.core.declaration;

import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains the status at a moment t of an {@link ImportDeclaration} bounds the {@link ImporterService}s.
 *
 * @author Morgan Martinet
 */
public class Status {

    /**
     * The list of importers.
     * (immutable)
     */
    private final List<ImporterService> m_importers;

    /**
     * Creates a status object from the given list of importers.
     * The given list is copied to a new list made immutable.
     *
     * @param importers the list of importers
     */
    private Status(List<ImporterService> importers) {
        m_importers = Collections.unmodifiableList(new ArrayList<ImporterService>(importers));
    }

    /**
     * Creates a status instance from the given importers.
     * The given list is copied to a new list made immutable.
     *
     * @param importers the list of importers
     * @return the new instance of status
     */
    public static Status from(List<ImporterService> importers) {
        if (importers == null) {
            throw new IllegalArgumentException("Cannot create a status with `null` as importers");
        }
        return new Status(importers);
    }

    /**
     * @return true if the ImportDeclaration is bound to at least one ImporterService, false otherwise
     */
    public Boolean isBound() {
        return !m_importers.isEmpty();
    }

    /**
     * @return The list of ImporterService bound to the ImportDeclaration
     */
    public List<ImporterService> getImporterServices() {
        return m_importers;
    }
}
