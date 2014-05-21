package org.ow2.chameleon.fuchsia.exporter.protobuffer.internal;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Exporter Protobuffer
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
import static org.ow2.chameleon.fuchsia.exporter.protobuffer.internal.Constants.*;

public final class ProtobufferExportDeclarationWrapper {

    private static Filter declarationFilter = buildFilter();

    String id;
    String address;
    String clazz;
    String message;
    String filter;
    String service;

    private ProtobufferExportDeclarationWrapper() {

    }

    private static Filter buildFilter() {
        Filter filter;
        String stringFilter = String.format("(&(%s=*)(%s=*)(%s=*)(%s=*)(%s=*))",
                ID, RPC_EXPORT_ADDRESS, RPC_EXPORT_CLASS, RPC_EXPORT_MESSAGE, RPC_EXPORT_SERVICE);
        try {
            filter = FuchsiaUtils.getFilter(stringFilter);
        } catch (InvalidFilterException e) {
            throw new IllegalStateException(e);
        }
        return filter;
    }

    public static ProtobufferExportDeclarationWrapper create(ExportDeclaration exportDeclaration) throws BinderException {


        Map<String, Object> metadata = exportDeclaration.getMetadata();

        if (!declarationFilter.matches(metadata)) {
            throw new BinderException("Not enough information in the metadata to be used by the protobuffer export");
        }

        ProtobufferExportDeclarationWrapper wrapper = new ProtobufferExportDeclarationWrapper();

        wrapper.id = (String) metadata.get(ID);
        wrapper.address = (String) metadata.get(RPC_EXPORT_ADDRESS);
        wrapper.clazz = (String) metadata.get(RPC_EXPORT_CLASS);
        wrapper.message = (String) metadata.get(RPC_EXPORT_MESSAGE);
        wrapper.service = (String) metadata.get(RPC_EXPORT_SERVICE);

        Object filterObject = metadata.get(RPC_EXPORT_FILTER);
        if (filterObject == null) {
            wrapper.filter = null;
        } else {
            wrapper.filter = (String) filterObject;
        }

        return wrapper;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getClazz() {
        return clazz;
    }

    public String getMessage() {
        return message;
    }

    public String getFilter() {
        return filter;
    }

    public String getService() {
        return service;
    }
}
