/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
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
package org.ow2.chameleon.fuchsia.importer.knx.dao;

import org.osgi.framework.Filter;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

import java.util.Map;

import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;

public class KNXDeclaration {

    final static String PREFIX="discovery.knx.device.%s";
    final static String ID="id";
    final static String ADDR=String.format(PREFIX,"addr");
    final static String DPT=String.format(PREFIX,"dpt-id");
    final static String LOCALHOST=String.format(PREFIX,"localhost");
    final static String GATEWAY=String.format(PREFIX,"gateway");

    private static Filter declarationFilter = buildFilter();

    private String id;
    private String knxAddress;
    private String dpt;
    private String localhost;
    private String gateway;

    private static Filter buildFilter() {
        Filter filter;

        String stringFilter = String.format("(&(%s=*)(%s=*)(%s=*)(%s=*)(%s=*))",
                ID,ADDR, DPT,LOCALHOST,GATEWAY);

        try {
            filter = FuchsiaUtils.getFilter(stringFilter);
        } catch (InvalidFilterException e) {
            throw new IllegalStateException(e);
        }
        return filter;
    }

    public static KNXDeclaration create(ImportDeclaration importDeclaration) throws BinderException {

        Map<String, Object> metadata = importDeclaration.getMetadata();

        if (!declarationFilter.matches(metadata)) {
            throw new BinderException("Not enough information in the metadata to be used by the KNX importer");
        }
        KNXDeclaration wrapper = new KNXDeclaration();
        wrapper.id=(String)metadata.get(ID);
        wrapper.knxAddress = (String) metadata.get(ADDR);
        wrapper.dpt = (String) metadata.get(DPT);
        wrapper.localhost = (String) metadata.get(LOCALHOST);
        wrapper.gateway = (String) metadata.get(GATEWAY);

        return wrapper;
    }

    public String getKnxAddress() {
        return knxAddress;
    }

    public String getDpt() {
        return dpt;
    }

    public String getLocalhost() {
        return localhost;
    }

    public String getGateway() {
        return gateway;
    }

    public String getId() {
        return id;
    }
}
