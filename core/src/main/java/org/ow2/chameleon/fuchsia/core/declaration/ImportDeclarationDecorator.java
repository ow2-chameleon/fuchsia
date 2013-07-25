package org.ow2.chameleon.fuchsia.core.declaration;

import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Morgan Martinet
 */
public class ImportDeclarationDecorator implements ImportDeclaration {

    private final ImportDeclaration importDeclaration;

    // The extra-metadata of the ImportDeclaration, set by the Linker
    private final Map<String, Object> extraMetadata;

    public ImportDeclarationDecorator(ImportDeclaration importDeclaration, Map<String, Object> extraMetadata) {
        if (importDeclaration == null) {
            throw new IllegalArgumentException("Cannot decorate a null object");
        }

        this.importDeclaration = importDeclaration;
        if (extraMetadata == null) {
            this.extraMetadata = Collections.unmodifiableMap(new HashMap<String, Object>());
        } else {
            this.extraMetadata = Collections.unmodifiableMap(extraMetadata);
        }
    }

    public Map<String, Object> getMetadata() {
        return importDeclaration.getMetadata();
    }

    public Map<String, Object> getExtraMetadata() {
        // Aggregate the extraMetadata of the decorated object and the extraMetadata of the decorator
        Map<String, Object> extraMetadata = new HashMap<String, Object>(importDeclaration.getExtraMetadata());
        extraMetadata.putAll(this.extraMetadata);
        return Collections.unmodifiableMap(extraMetadata);
    }

    public Status getStatus() {
        return importDeclaration.getStatus();
    }

    public void bind(ImporterService importerService) {
        importDeclaration.bind(importerService);
    }

    public void unbind(ImporterService importerService) {
        importDeclaration.unbind(importerService);
    }

}
