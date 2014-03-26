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

import org.apache.felix.ipojo.Factory;
import org.osgi.framework.ServiceReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * {@link DeclarationImpl} is the reference implementation of the {@link Declaration} interface of Fuchsia.
 *
 * @author jnascimento
 * @author Morgan Martinet
 */
class DeclarationImpl implements Declaration, ImportDeclaration, ExportDeclaration {

    // the lock used to synchronized serviceReferencesBound and serviceReferencesHandled
    private final Object lock;

    // List of importerServices bind to this Declaration
    private final Map<Long, ServiceReference> serviceReferencesBound;

    // List of importerServices which are currently doing something with this Declaration
    // i.e. the importerService has created a proxy from the declaration
    private final Map<Long, ServiceReference> serviceReferencesHandled;

    // The metadata of the Declaration
    private final Map<String, Object> metadata;

    // The extra-metadata of the Declaration. Must be empty in the DeclarationImpl because the discovery
    // mustn't give any extraMetadata, all the information from the discovery must be in the metadata
    // the extraMetadata are provided in the DeclarationDecorator
    private final Map<String, Object> extraMetadata;

    DeclarationImpl(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalArgumentException("Can't create a Declaration without metadata.");
        }
        this.metadata = Collections.unmodifiableMap(new HashMap<String, Object>(metadata));
        this.extraMetadata = Collections.unmodifiableMap(new HashMap<String, Object>());
        this.serviceReferencesBound = new HashMap<Long, ServiceReference>();
        this.serviceReferencesHandled = new HashMap<Long, ServiceReference>();
        this.lock = new Object();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getExtraMetadata() {
        return extraMetadata;
    }

    public Status getStatus() {
        synchronized (lock) {
            return Status.from(new HashSet<ServiceReference>(serviceReferencesBound.values()),
                    new HashSet<ServiceReference>(serviceReferencesHandled.values()));
        }
    }

    public void bind(ServiceReference serviceReference) {
        synchronized (lock) {
            serviceReferencesBound.put(fetchServiceId(serviceReference), serviceReference);
        }
    }

    public void unbind(ServiceReference serviceReference) {
        synchronized (lock) {
            if (!serviceReferencesHandled.containsKey(fetchServiceId(serviceReference))) {
                serviceReferencesBound.remove(fetchServiceId(serviceReference));
            } else {
                String name = (String) serviceReference.getProperty(Factory.INSTANCE_NAME_PROPERTY);
                throw new IllegalStateException(name + " want to unbound a declaration that it is still handling.");
            }
        }
    }

    public void handle(ServiceReference serviceReference) {
        synchronized (lock) {
            if (serviceReferencesBound.containsKey(fetchServiceId(serviceReference))) {
                serviceReferencesHandled.put(fetchServiceId(serviceReference), serviceReference);
            } else {
                String name = (String) serviceReference.getProperty(Factory.INSTANCE_NAME_PROPERTY);
                throw new IllegalStateException(name + " cannot handle a declaration that it's not bound to it");
            }
        }
    }

    public void unhandle(ServiceReference serviceReference) {
        synchronized (lock) {
            serviceReferencesHandled.remove(fetchServiceId(serviceReference));
        }
    }

    public String toString() {
        Status status = getStatus();
        return String.format("[Declaration:%s(%s)(%d(%d))]",
                getMetadata().toString(), getExtraMetadata().toString(),
                status.getServiceReferencesBounded().size(), status.getServiceReferencesHandled().size());
    }

    /**
     * Returns the service id with the propertype.
     *
     * @param serviceReference
     * @return long value for the service id
     */
    private Long fetchServiceId(ServiceReference serviceReference) {
        return (Long) serviceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID);
    }

}
