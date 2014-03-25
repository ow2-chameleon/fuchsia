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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Morgan Martinet
 */
class DeclarationDecorator implements Declaration, ImportDeclaration, ExportDeclaration {

    private final Declaration declaration;

    // The extra-metadata of the Declaration, set by the ImportationLinker
    private final Map<String, Object> extraMetadata;

    DeclarationDecorator(Declaration declaration, Map<String, Object> extraMetadata) {
        if (declaration == null) {
            throw new IllegalArgumentException("Cannot decorate a null object");
        }

        this.declaration = declaration;
        if (extraMetadata == null) {
            this.extraMetadata = Collections.unmodifiableMap(new HashMap<String, Object>());
        } else {
            this.extraMetadata = Collections.unmodifiableMap(extraMetadata);
        }
    }

    public Map<String, Object> getMetadata() {
        return declaration.getMetadata();
    }

    public Map<String, Object> getExtraMetadata() {
        // Aggregate the extraMetadata of the decorated object and the extraMetadata of the decorator
        Map<String, Object> em = new HashMap<String, Object>(declaration.getExtraMetadata());
        em.putAll(this.extraMetadata);
        return Collections.unmodifiableMap(em);
    }

    public Status getStatus() {
        return declaration.getStatus();
    }

    public void bind(ServiceReference serviceReference) {
        declaration.bind(serviceReference);
    }

    public void unbind(ServiceReference serviceReference) {
        declaration.unbind(serviceReference);
    }

    public void handle(ServiceReference serviceReference) {
        declaration.handle(serviceReference);
    }

    public void unhandle(ServiceReference serviceReference) {
        declaration.unhandle(serviceReference);
    }

    public String toString() {
        Status status = declaration.getStatus();
        return String.format("[Declaration:%s(%s)(%d(%d))]",
                getMetadata().toString(), getExtraMetadata().toString(),
                status.getServiceReferencesBounded().size(), status.getServiceReferencesHandled().size());
    }

}
