package org.ow2.chameleon.fuchsia.core.declaration;

import java.util.HashMap;
import java.util.Map;

/**
 * A Builder for ImportDeclaration. There's two way to use it :
 * - Build a ImportDeclaration with metadata
 * - Build a ImportDeclaration from another ImportDeclaration with extra-metadata
 *
 * If you doing it wrong, IllegalStateException are thrown.
 */
public class ImportDeclarationBuilder {
    private Map<String, Object> metadata;
    private Map<String, Object> extraMetadata;
    private ImportDeclaration importDeclaration;

    public static ImportDeclarationBuilder create() {
        return new ImportDeclarationBuilder();
    }

    private ImportDeclarationBuilder() {
        this.metadata = null;
        this.importDeclaration = null;
        this.extraMetadata = new HashMap<String, Object>();
    }



    public ImportDeclarationBuilder withMetadata(Map<String, Object> metadata) {
        if (importDeclaration != null) {
            throw new IllegalStateException();
        }
        this.metadata = metadata;
        return this;
    }

    public ImportDeclarationBuilder from(ImportDeclaration importDeclaration) {
        if (metadata != null) {
            throw new IllegalStateException();
        }
        this.importDeclaration = importDeclaration;
        return this;
    }

    public ImportDeclarationBuilder withExtraMetadata(Map<String, Object> extraMetadata) {
        if (importDeclaration == null) {
            throw new IllegalStateException();
        }
        this.extraMetadata = extraMetadata;
        return this;
    }

    public ImportDeclaration build() {
        if (metadata != null && importDeclaration == null) {
            return new ImportDeclarationImpl(metadata);
        } else if (metadata == null && importDeclaration != null) {
            return new ImportDeclarationDecorator(importDeclaration, extraMetadata);
        } else {
            throw new IllegalStateException();
        }
    }
}