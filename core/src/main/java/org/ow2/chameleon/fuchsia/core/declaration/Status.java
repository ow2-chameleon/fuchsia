package org.ow2.chameleon.fuchsia.core.declaration;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.osgi.framework.ServiceReference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * This class represent the status at a moment t of an {@link Declaration}.
 * This status contains two lists. The list of {@link ServiceReference} of the Services bound to the
 * {@link Declaration}.
 * And the list of {@link ServiceReference} of the Services handling the {@link Declaration}.
 *
 * @author Morgan Martinet
 */
public final class Status {

    /**
     * The list of ServiceReference which are bound to the declaration.
     * (immutable)
     */
    private final Set<ServiceReference> serviceReferencesBound;

    /**
     * The list of ServiceReference which are handling the declaration.
     * (immutable)
     */
    private final Set<ServiceReference> serviceReferencesHandled;

    /**
     * Creates a status object from the given sets of serviceReferencesBound and serviceReferencesHandled.
     * The given sets are copied to new lists made immutables.
     *
     * @param serviceReferencesBound   the set of ServiceReference which are bound to the declaration.
     * @param serviceReferencesHandled the set of ServiceReference which are handling the declaration.
     */
    private Status(Set<ServiceReference> serviceReferencesBound, Set<ServiceReference> serviceReferencesHandled) {
        this.serviceReferencesBound = Collections.unmodifiableSet(new HashSet<ServiceReference>(serviceReferencesBound));
        this.serviceReferencesHandled = Collections.unmodifiableSet(new HashSet<ServiceReference>(serviceReferencesHandled));
    }

    /**
     * Creates a status instance from the given serviceReferences.
     * The given list is copied to a new set made immutable.
     *
     * @param serviceReferencesBound   the set of ServiceReference which are bound to the declaration.
     * @param serviceReferencesHandled the set of ServiceReference which are handling the declaration.
     * @return the new instance of status
     */
    public static Status from(Set<ServiceReference> serviceReferencesBound, Set<ServiceReference> serviceReferencesHandled) {
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
        return !serviceReferencesBound.isEmpty();
    }

    /**
     * @return true if the Declaration is handled by at least one Service, false otherwise
     */
    public Boolean isHandled() {
        return !serviceReferencesHandled.isEmpty();
    }

    /**
     * @return The list of ServiceReference of the Services bound to the Declaration
     */
    public Set<ServiceReference> getServiceReferencesBounded() {
        return serviceReferencesBound;
    }

    /**
     * @return The list of ServiceReference of the Services bound to the Declaration
     */
    public Set<ServiceReference> getServiceReferencesHandled() {
        return serviceReferencesHandled;
    }

}
