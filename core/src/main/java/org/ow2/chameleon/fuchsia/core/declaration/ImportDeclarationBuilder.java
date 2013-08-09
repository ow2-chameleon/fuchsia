package org.ow2.chameleon.fuchsia.core.declaration;

import java.util.HashMap;
import java.util.Map;

/**
 * A Builder for ImportDeclaration. There's two way to use it :
 * - Build a ImportDeclaration with metadata
 * - Build a ImportDeclaration from another ImportDeclaration with extra-metadata
 * <p/>
 * If you doing it wrong, {@link IllegalStateException} are thrown.
 */
public class ImportDeclarationBuilder {
    private Map<String, Object> metadata;
    private Map<String, Object> extraMetadata;
    private ImportDeclaration importDeclaration;

    private ImportDeclarationBuilder() {
        this.metadata = null;
        this.importDeclaration = null;
        this.extraMetadata = new HashMap<String, Object>();
    }

    private ImportDeclarationBuilder(Map<String, Object> metadata) {
        this();
        this.metadata = metadata;
    }

    private ImportDeclarationBuilder(ImportDeclaration importDeclaration) {
        this();
        this.importDeclaration = importDeclaration;
    }

    public static ImportDeclarationBuilder empty() {
        return new ImportDeclarationBuilder();
    }

    public static ImportDeclarationBuilder fromMetadata(Map<String, Object> metadata) {
        return new ImportDeclarationBuilder(metadata);
    }

    public static ImportDeclarationBuilder fromImportDeclaration(ImportDeclaration importDeclaration) {
        return new ImportDeclarationBuilder(importDeclaration);
    }

    public ValueSetter key(String key) {
        if (importDeclaration != null) {
            throw new IllegalStateException();
        }

        return new ValueSetter(key);
    }

    public ExtraValueSetter extraKey(String key) {
        if (importDeclaration != null) {
            throw new IllegalStateException();
        }
        return new ExtraValueSetter(key);
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

    private void addMetadata(String key, Object value) {
        if (importDeclaration != null) {
            throw new IllegalStateException();
        }
        if (metadata == null) {
            metadata = new HashMap<String, Object>();
        }
        metadata.put(key, value);
    }

    private void addExtraMetadata(String key, Object value) {
        if (importDeclaration == null) {
            throw new IllegalStateException();
        }
        extraMetadata.put(key, value);
    }

    protected class ValueSetter {
        private final String key;

        ValueSetter(String key) {
            this.key = key;
        }

        public ImportDeclarationBuilder value(Object value) {
            addMetadata(key, value);
            return ImportDeclarationBuilder.this;
        }
    }

    protected class ExtraValueSetter {
        private final String key;

        ExtraValueSetter(String key) {
            this.key = key;
        }

        public ImportDeclarationBuilder value(Object value) {
            addExtraMetadata(key, value);
            return ImportDeclarationBuilder.this;
        }
    }
}

