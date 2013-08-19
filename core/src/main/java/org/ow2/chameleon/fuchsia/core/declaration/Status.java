package org.ow2.chameleon.fuchsia.core.declaration;

import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represent the status at a moment t of an {@link Declaration}.
 * This status contains the list of {@link ServiceReference} of the Services bound to the {@link Declaration}.
 *
 * @author Morgan Martinet
 */
public class Status {

    /**
     * The list of ServiceReference.
     * (immutable)
     */
    private final List<ServiceReference> m_serviceReferences;

    /**
     * Creates a status object from the given list of serviceReferences.
     * The given list is copied to a new list made immutable.
     *
     * @param serviceReferences the list of ServiceReference
     */
    private Status(List<ServiceReference> serviceReferences) {
        m_serviceReferences = Collections.unmodifiableList(new ArrayList<ServiceReference>(serviceReferences));
    }

    /**
     * Creates a status instance from the given serviceReferences.
     * The given list is copied to a new list made immutable.
     *
     * @param serviceReferences the list of ServiceReference
     * @return the new instance of status
     */
    public static Status from(List<ServiceReference> serviceReferences) {
        if (serviceReferences == null) {
            throw new IllegalArgumentException("Cannot create a status with `null` as serviceReferences");
        }
        return new Status(serviceReferences);
    }

    /**
     * @return true if the Declaration is bound to at least one Service, false otherwise
     */
    public Boolean isBound() {
        return !m_serviceReferences.isEmpty();
    }

    /**
     * @return The list of ServiceReference of the Services bound to the Declaration
     */
    public List<ServiceReference> getServiceReferences() {
        return m_serviceReferences;
    }
}
