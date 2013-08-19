package org.ow2.chameleon.fuchsia.core.declaration;

import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link DeclarationImpl} is the reference implementation of the {@link Declaration} interface of Fuchsia
 *
 * @author jnascimento
 * @author Morgan Martinet
 */
class DeclarationImpl implements Declaration, ImportDeclaration, ExportDeclaration {

    // List of importerServices bind to this Declaration
    private final List<ServiceReference> serviceReferencesBound;

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
        this.serviceReferencesBound = new ArrayList<ServiceReference>();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getExtraMetadata() {
        return extraMetadata;
    }

    public Status getStatus() {
        synchronized (serviceReferencesBound) {
            return Status.from(serviceReferencesBound);
        }
    }

    public void bind(ServiceReference serviceReference) {
        synchronized (serviceReferencesBound) {
            serviceReferencesBound.add(serviceReference);
        }
    }

    public void unbind(ServiceReference serviceReference) {
        synchronized (serviceReferencesBound) {
            serviceReferencesBound.remove(serviceReference);
        }
    }

    public String toString() {
        // FIXME
        return metadata.values().toArray()[0].toString();
    }
}
