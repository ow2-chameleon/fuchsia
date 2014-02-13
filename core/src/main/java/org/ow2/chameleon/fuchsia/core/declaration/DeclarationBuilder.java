package org.ow2.chameleon.fuchsia.core.declaration;

import java.util.HashMap;
import java.util.Map;

/**
 * A Builder for Declaration. There's two way to use it :
 * - Build a Declaration with metadata
 * - Build a Declaration from another Declaration with extra-metadata
 * <p/>
 * If you doing it wrong, {@link IllegalStateException} are thrown.
 */
class DeclarationBuilder<BUILDER extends DeclarationBuilder<BUILDER,DECLARATION>, DECLARATION extends Declaration> {
    private Map<String, Object> metadata;
    private Map<String, Object> extraMetadata;
    private DECLARATION declaration;

    DeclarationBuilder() {
        this.metadata = null;
        this.declaration = null;
        this.extraMetadata = new HashMap<String, Object>();
    }

    DeclarationBuilder(Map<String, Object> metadata) {
        this();
        this.metadata = metadata;
    }

    DeclarationBuilder(DECLARATION declaration) {
        this();
        this.declaration = declaration;
    }

    public ValueSetter key(String key) {
        if (declaration != null) {
            throw new IllegalStateException();
        }

        return new ValueSetter(key);
    }

    public ExtraValueSetter extraKey(String key) {
        if (declaration == null) {
            throw new IllegalStateException();
        }
        return new ExtraValueSetter(key);
    }

    public BUILDER withExtraMetadata(Map<String, Object> extraMetadata) {
        if (declaration == null) {
            throw new IllegalStateException();
        }
        this.extraMetadata = extraMetadata;
        return (BUILDER) this;
    }

    public DECLARATION build() {
        if (metadata != null && declaration == null) {
            return (DECLARATION) new DeclarationImpl(metadata);
        } else if (metadata == null && declaration != null) {
            return (DECLARATION) new DeclarationDecorator(declaration, extraMetadata);
        } else {
            throw new IllegalStateException();
        }
    }

    private void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<String, Object>();
        }
        metadata.put(key, value);
    }

    private void addExtraMetadata(String key, Object value) {
        extraMetadata.put(key, value);
    }

    public class ValueSetter {
        private final String key;

        ValueSetter(String key) {
            this.key = key;
        }

        public BUILDER value(Object value) {
            addMetadata(key, value);
            return (BUILDER) DeclarationBuilder.this;
        }
    }

    public class ExtraValueSetter {
        private final String key;

        ExtraValueSetter(String key) {
            this.key = key;
        }

        public BUILDER value(Object value) {
            addExtraMetadata(key, value);
            return (BUILDER) DeclarationBuilder.this;
        }
    }
}

