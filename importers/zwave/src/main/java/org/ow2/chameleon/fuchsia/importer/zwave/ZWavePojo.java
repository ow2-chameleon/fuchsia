/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2015 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.fuchsia.importer.zwave;

import org.osgi.framework.Filter;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

import java.util.Map;

public class ZWavePojo {

    private static Filter declarationFilter = buildFilter();

    private String id;
    private String port;
    private String node;
    private String endpoint;

    public String getType() {
        return type;
    }

    private String type;

    private static Filter buildFilter() {
        Filter filter;
        String stringFilter = String.format("(&(port=*)(node=*)(endpoint=*))");
        try {
            filter = FuchsiaUtils.getFilter(stringFilter);
        } catch (InvalidFilterException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        return filter;
    }

    public static ZWavePojo create(ImportDeclaration importDeclaration) throws BinderException {
        Map<String, Object> metadata = importDeclaration.getMetadata();

        if (!declarationFilter.matches(metadata)) {
            throw new BinderException("Not enough information in the metadata to be used by the phillips hue importer");
        }
        ZWavePojo wrapper = new ZWavePojo();

        wrapper.id = (String) metadata.get("id");
        wrapper.port = (String) metadata.get("port");
        wrapper.endpoint = (String) metadata.get("node");
        wrapper.node = (String)metadata.get("endpoint");
        wrapper.type = metadata.get("type")!=null?(String)metadata.get("type"):null;

        return wrapper;
    }

    public String getPort() {
        return port;
    }

    public String getNode() {
        return node;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getId() {
        return id;
    }
}
