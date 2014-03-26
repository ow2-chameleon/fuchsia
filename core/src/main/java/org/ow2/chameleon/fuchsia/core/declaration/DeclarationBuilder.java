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

import java.util.HashMap;
import java.util.Map;

/**
 * A Builder for Declaration. There's two way to use it :
 * - Build a Declaration with metadata
 * - Build a Declaration from another Declaration with extra-metadata
 * <p/>
 * If you doing it wrong, {@link IllegalStateException} are thrown.
 */
class DeclarationBuilder<B extends DeclarationBuilder<B, D>, D extends Declaration> {
    private Map<String, Object> metadata;
    private Map<String, Object> extraMetadata;
    private D declaration;

    DeclarationBuilder() {
        this.metadata = null;
        this.declaration = null;
        this.extraMetadata = new HashMap<String, Object>();
    }

    DeclarationBuilder(Map<String, Object> metadata) {
        this();
        this.metadata = metadata;
    }

    DeclarationBuilder(D declaration) {
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

    public B withExtraMetadata(Map<String, Object> extraMetadata) {
        if (declaration == null) {
            throw new IllegalStateException();
        }
        this.extraMetadata = extraMetadata;
        return (B) this;
    }

    public D build() {
        if (metadata != null && declaration == null) {
            return (D) new DeclarationImpl(metadata);
        } else if (metadata == null && declaration != null) {
            return (D) new DeclarationDecorator(declaration, extraMetadata);
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

        public B value(Object value) {
            addMetadata(key, value);
            return (B) DeclarationBuilder.this;
        }
    }

    public class ExtraValueSetter {
        private final String key;

        ExtraValueSetter(String key) {
            this.key = key;
        }

        public B value(Object value) {
            addExtraMetadata(key, value);
            return (B) DeclarationBuilder.this;
        }
    }
}

