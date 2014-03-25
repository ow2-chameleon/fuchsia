package org.ow2.chameleon.fuchsia.core.component;

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


import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationRegistrationManager;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Set;

/**
 * Abstract implementation of an discovery component which provides an {@link DiscoveryService}.
 * Start must be call before registering the service !
 * Stop must be called when the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractDiscoveryComponent implements DiscoveryService, DiscoveryIntrospection {

    private final DeclarationRegistrationManager<ImportDeclaration> declarationRegistrationManager;
    private final BundleContext bundleContext;

    protected AbstractDiscoveryComponent(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        declarationRegistrationManager = new DeclarationRegistrationManager<ImportDeclaration>(bundleContext, ImportDeclaration.class);
    }

    /**
     * Start the discovery component, iPOJO Validate instance callback.
     * Must be override !
     */
    protected void start() {
    }

    /**
     * Stop the discovery component, iPOJO Invalidate instance callback.
     * Must be override !
     */
    protected void stop() {
        declarationRegistrationManager.unregisterAll();
    }

    /**
     * Utility method to register an ImportDeclaration has a Service in OSGi.
     * If you use it make sure to use unregisterImportDeclaration(...) to unregister the ImportDeclaration
     *
     * @param importDeclaration the ImportDeclaration to register
     */
    protected void registerImportDeclaration(ImportDeclaration importDeclaration) {
        declarationRegistrationManager.registerDeclaration(importDeclaration);
    }

    /**
     * Utility method to unregister an ImportDeclaration of OSGi.
     * Use it only if you have used registerImportDeclaration(...) to register the ImportDeclaration
     *
     * @param importDeclaration the ImportDeclaration to unregister
     */
    protected void unregisterImportDeclaration(ImportDeclaration importDeclaration) {
        declarationRegistrationManager.unregisterDeclaration(importDeclaration);
    }

    @Override
    public String toString() {
        return getName();
    }

    public Set<ImportDeclaration> getImportDeclarations() {
        return declarationRegistrationManager.getDeclarations();
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

}
