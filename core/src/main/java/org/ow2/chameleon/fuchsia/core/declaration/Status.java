package org.ow2.chameleon.fuchsia.core.declaration;

import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represent the status at a moment t of an {@link Declaration}.
 * This status contains two lists. The list of {@link ServiceReference} of the Services bound to the
 * {@link Declaration}.
 * And the list of {@link ServiceReference} of the Services handling the {@link Declaration}.
 *
 * @author Morgan Martinet
 */
public class Status {

    /**
     * The list of ServiceReference which are bound to the declaration.
     * (immutable)
     */
    private final List<ServiceReference> m_serviceReferencesBound;

    /**
     * The list of ServiceReference which are handling the declaration.
     * (immutable)
     */
    private final List<ServiceReference> m_serviceReferencesHandled;

    /**
     * Creates a status object from the given lists of serviceReferencesBound and serviceReferencesHandled.
     * The given lists are copied to new lists made immutables.
     *
     * @param serviceReferencesBound   the list of ServiceReference which are bound to the declaration.
     * @param serviceReferencesHandled the list of ServiceReference which are handling the declaration.
     */
    private Status(List<ServiceReference> serviceReferencesBound, List<ServiceReference> serviceReferencesHandled) {
        m_serviceReferencesBound = Collections.unmodifiableList(new ArrayList<ServiceReference>(serviceReferencesBound));
        m_serviceReferencesHandled = Collections.unmodifiableList(new ArrayList<ServiceReference>(serviceReferencesHandled));
    }

    /**
     * Creates a status instance from the given serviceReferences.
     * The given list is copied to a new list made immutable.
     *
     * @param serviceReferencesBound   the list of ServiceReference which are bound to the declaration.
     * @param serviceReferencesHandled the list of ServiceReference which are handling the declaration.
     * @return the new instance of status
     */
    public static Status from(List<ServiceReference> serviceReferencesBound, List<ServiceReference> serviceReferencesHandled) {
        if (serviceReferencesBound == null && serviceReferencesHandled == null) {
            throw new IllegalArgumentException("Cannot create a status with serviceReferencesBound == null" +
                    "and serviceReferencesHandled == null");
        } else if (serviceReferencesBound == null) {
            throw new IllegalArgumentException("Cannot create a status with serviceReferencesBound == null");
        } else if (serviceReferencesHandled == null) {
            throw new IllegalArgumentException("Cannot create a status with serviceReferencesHandled == null");
        }
        return new Status(serviceReferencesBound, serviceReferencesHandled);
    }

    /**
     * @return true if the Declaration is bound to at least one Service, false otherwise
     */
    public Boolean isBound() {
        return !m_serviceReferencesBound.isEmpty();
    }

    /**
     * @return true if the Declaration is handled by at least one Service, false otherwise
     */
    public Boolean isHandled() {
        return !m_serviceReferencesHandled.isEmpty();
    }

    /**
     * @return The list of ServiceReference of the Services bound to the Declaration
     */
    public List<ServiceReference> getServiceReferencesBounded() {
        return m_serviceReferencesBound;
    }

    /**
     * @return The list of ServiceReference of the Services bound to the Declaration
     */
    public List<ServiceReference> getServiceReferencesHandled() {
        return m_serviceReferencesHandled;
    }

}
