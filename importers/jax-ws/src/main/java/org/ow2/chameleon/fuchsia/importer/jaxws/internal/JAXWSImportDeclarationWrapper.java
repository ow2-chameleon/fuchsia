package org.ow2.chameleon.fuchsia.importer.jaxws.internal;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer JAX-WS
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


import org.osgi.framework.Filter;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;
import org.ow2.chameleon.fuchsia.importer.jaxws.JAXWSImporter;

import java.util.Map;

import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;

public class JAXWSImportDeclarationWrapper {

    private static Filter declarationFilter = buildFilter();

    private String endpoint;
    private String clazz;

    private JAXWSImportDeclarationWrapper() {

    }

    private static Filter buildFilter() {
        Filter filter;
        String stringFilter = String.format("(&(%s=*)(%s=*)(%s=*))", ID, JAXWSImporter.ENDPOINT_URL, JAXWSImporter.CLASSNAME);
        try {
            filter = FuchsiaUtils.getFilter(stringFilter);
        } catch (InvalidFilterException e) {
            throw new IllegalStateException(e);
        }
        return filter;
    }

    public static JAXWSImportDeclarationWrapper create(ImportDeclaration importDeclaration) throws BinderException {
        Map<String, Object> metadata = importDeclaration.getMetadata();

        if (!declarationFilter.matches(metadata)) {
            throw new BinderException("Not enough information in the metadata to be used by the JAX WS importer");
        }
        JAXWSImportDeclarationWrapper wrapper = new JAXWSImportDeclarationWrapper();

        wrapper.endpoint = (String) metadata.get(JAXWSImporter.ENDPOINT_URL);
        wrapper.clazz = (String) metadata.get(JAXWSImporter.CLASSNAME);

        return wrapper;

    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getClazz() {
        return clazz;
    }
}
