package org.ow2.chameleon.fuchsia.raspberry.pi.internal;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer Raspberry Pi GPIO
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

import java.util.Map;

import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;
import static org.ow2.chameleon.fuchsia.raspberry.pi.internal.Constants.IMPORTER_GPIO_NAME;
import static org.ow2.chameleon.fuchsia.raspberry.pi.internal.Constants.IMPORTER_GPIO_PIN;

/**
 * Makes easier to access the Declaration metadata.
 */
public class GPIOImportDeclarationWrapper {

    private static Filter declarationFilter = buildFilter();

    private String id;
    private String pin;
    private String name;

    private GPIOImportDeclarationWrapper() {

    }

    private static Filter buildFilter() {
        Filter filter;
        String stringFilter = String.format("(&(%s=*)(%s=*)(%s=*))", ID, IMPORTER_GPIO_NAME, IMPORTER_GPIO_PIN);
        try {
            filter = FuchsiaUtils.getFilter(stringFilter);
        } catch (InvalidFilterException e) {
            throw new IllegalStateException(e);
        }
        return filter;
    }


    public static GPIOImportDeclarationWrapper create(ImportDeclaration importDeclaration) throws BinderException {
        Map<String, Object> metadata = importDeclaration.getMetadata();
        if (!declarationFilter.matches(metadata)) {
            throw new BinderException("Not enough information provided to create GPIO Importer");
        }

        GPIOImportDeclarationWrapper wrapper = new GPIOImportDeclarationWrapper();

        wrapper.id = (String) metadata.get(ID);
        wrapper.pin = (String) metadata.get(IMPORTER_GPIO_PIN);
        wrapper.name = (String) metadata.get(IMPORTER_GPIO_NAME);

        return wrapper;
    }

    public String getId() {
        return id;
    }

    public String getPin() {
        return pin;
    }

    public String getName() {
        return name;
    }
}
