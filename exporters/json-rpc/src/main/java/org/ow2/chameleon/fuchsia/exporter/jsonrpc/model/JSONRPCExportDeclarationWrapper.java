package org.ow2.chameleon.fuchsia.exporter.jsonrpc.model;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Exporter JSON-RPC
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
import static org.ow2.chameleon.fuchsia.exporter.jsonrpc.model.Constants.*;

public class JSONRPCExportDeclarationWrapper {

    private static Filter declarationFilter = buildFilter();

    private String id;
    private String instanceName;
    private String instanceClass;
    private String urlContext;

    private JSONRPCExportDeclarationWrapper() {

    }

    private static Filter buildFilter() {
        Filter filter;
        String stringFilter = String.format("(&(%s=*)(%s=*)(%s=*))",
                ID, FUCHSIA_EXPORT_JSONRPC_CLASS, FUCHSIA_EXPORT_JSONRPC_INSTANCE);
        try {
            filter = FuchsiaUtils.getFilter(stringFilter);
        } catch (InvalidFilterException e) {
            throw new IllegalStateException(e);
        }
        return filter;
    }

    public static JSONRPCExportDeclarationWrapper create(ExportDeclaration exportDeclaration) throws BinderException {
        Map<String, Object> metadata = exportDeclaration.getMetadata();
        if (!declarationFilter.matches(metadata)) {
            throw new BinderException("Not enough information in the metadata to be used by the JsonRPC Exporter");
        }

        JSONRPCExportDeclarationWrapper wrapper = new JSONRPCExportDeclarationWrapper();

        wrapper.id = (String) metadata.get(ID);
        wrapper.instanceClass = (String) metadata.get(FUCHSIA_EXPORT_JSONRPC_CLASS);
        wrapper.instanceName = (String) metadata.get(FUCHSIA_EXPORT_JSONRPC_INSTANCE);

        String url = (String) metadata.get(FUCHSIA_EXPORT_JSONRPC_URL_CONTEXT);
        if ((url != null) && !"null".equals(url)) {
            wrapper.urlContext = url;
        } else {
            wrapper.urlContext = "/JSONRPC";
        }

        return wrapper;

    }

    public String getId() {
        return id;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getInstanceClass() {
        return instanceClass;
    }

    public String getUrlContext() {
        return urlContext;
    }
}
