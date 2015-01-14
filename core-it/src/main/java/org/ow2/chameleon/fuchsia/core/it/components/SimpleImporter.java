package org.ow2.chameleon.fuchsia.core.it.components;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core [IntegrationTests]
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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Collection;
import java.util.HashSet;

/**
 * Importer mock
 */
@Component(name = "SimpleImporterFactory")
@Provides(specifications = {ImporterService.class, SimpleImporter.class})
public class SimpleImporter extends AbstractImporterComponent {

    private final Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) {
        decs.add(importDeclaration);
    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) {
        decs.remove(importDeclaration);
    }

    /**
     * Show number of proxies generated
     * @return
     */
    public int nbProxies() {
        return decs.size();
    }

    /**
     * Name of the importer instantiated
     * @return
     */
    public String getName() {
        return "simpleImporter";
    }

    @Override
    public void stop() {
        super.stop();
    }
}
