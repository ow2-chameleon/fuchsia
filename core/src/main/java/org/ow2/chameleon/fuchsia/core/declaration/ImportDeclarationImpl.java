package org.ow2.chameleon.fuchsia.core.declaration;

import org.apache.felix.ipojo.annotations.Component;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ImportDeclarationImpl} is the reference implementation of the {@link ImportDeclaration} interface of Fuchsia
 *
 * @author jnascimento
 * @author Morgan Martinet
 */
public class ImportDeclarationImpl implements ImportDeclaration {

    // List of importerServices bind to this ImportDeclaration
    private final List<ImporterService> importerServicesBound;

    // The metadata of the ImportDeclaration
    private final Map<String, Object> metadata;

    // The extra-metadata of the ImportDeclaration. Must be empty in the ImportDeclarationImpl because the discovery
    // mustn't give any extraMetadata, all the information from the discovery must be in the metadata
    // the extraMetadata are provided in the ImportDeclarationDecorator
    private final Map<String, Object> extraMetadata;

    public ImportDeclarationImpl(Map<String, Object> metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Can't create a ImportDeclaration without metadata.");
        }
        this.metadata = Collections.unmodifiableMap(new HashMap<String, Object>(metadata));
        this.extraMetadata = Collections.unmodifiableMap(new HashMap<String, Object>());
        this.importerServicesBound = new ArrayList<ImporterService>();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getExtraMetadata() {
        return extraMetadata;
    }

    public Status getStatus() {
        synchronized (importerServicesBound) {
            return new Status(new ArrayList<ImporterService>(importerServicesBound));
        }
    }

    public void bind(ImporterService importerService) {
        synchronized (importerServicesBound) {
            importerServicesBound.add(importerService);
        }
    }

    public void unbind(ImporterService importerService) {
        synchronized (importerServicesBound) {
            importerServicesBound.remove(importerService);
        }
    }


}
