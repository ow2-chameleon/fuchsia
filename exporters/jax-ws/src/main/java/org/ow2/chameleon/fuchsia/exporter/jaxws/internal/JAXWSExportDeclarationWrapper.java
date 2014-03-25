package org.ow2.chameleon.fuchsia.exporter.jaxws.internal;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Exporter JAX-WS
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
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

import java.util.Map;

import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;
import static org.ow2.chameleon.fuchsia.exporter.jaxws.internal.Constants.CXF_EXPORT_TYPE;
import static org.ow2.chameleon.fuchsia.exporter.jaxws.internal.Constants.CXF_EXPORT_WEB_CONTEXT;

public class JAXWSExportDeclarationWrapper {

    private static Filter declarationFilter = buildFilter();

    private String id;
    private String clazz;
    private String webcontext;
    private String filter;

    private JAXWSExportDeclarationWrapper() {

    }

    private static Filter buildFilter() {
        Filter filter;
        String stringFilter = String.format("(&(%s=*)(%s=*)(%s=*))", ID, CXF_EXPORT_TYPE, CXF_EXPORT_WEB_CONTEXT);
        try {
            filter = FuchsiaUtils.getFilter(stringFilter);
        } catch (InvalidFilterException e) {
            throw new IllegalStateException(e);
        }
        return filter;
    }

    public static JAXWSExportDeclarationWrapper create(ExportDeclaration exportDeclaration) throws BinderException {
        Map<String, Object> metadata = exportDeclaration.getMetadata();
        if (!declarationFilter.matches(metadata)) {
            throw new BinderException("Not enough information in the metadata to be used by the CXF Exporter");
        }

        JAXWSExportDeclarationWrapper wrapper = new JAXWSExportDeclarationWrapper();

        wrapper.id = (String) metadata.get(ID);
        wrapper.clazz = (String) metadata.get(CXF_EXPORT_TYPE);
        wrapper.webcontext = (String) metadata.get(CXF_EXPORT_WEB_CONTEXT);

        Object filterObject = metadata.get(Constants.CXF_EXPORT_FILTER);
        if (filterObject != null) {
            wrapper.filter = (String) filterObject;
        }
        return wrapper;
    }

    public String getId() {
        return id;
    }

    public String getClazz() {
        return clazz;
    }

    public String getWebcontext() {
        return webcontext;
    }

    public String getFilter() {

        return filter;

    }
}
