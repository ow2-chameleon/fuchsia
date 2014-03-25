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
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

import java.util.Set;

/**
 * Abstract implementation of an export manager component which provides an {@link ExportManagerService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractExportManagerComponent implements ExportManagerService, ExportManagerIntrospection {

    private final DeclarationRegistrationManager<ExportDeclaration> declarationRegistrationManager;

    private final BundleContext bundleContext;


    protected AbstractExportManagerComponent(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        declarationRegistrationManager = new DeclarationRegistrationManager<ExportDeclaration>(bundleContext, ExportDeclaration.class);
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
     * Utility method to register an ExportDeclaration has a Service in OSGi.
     * If you use it make sure to use unregisterExportDeclaration(...) to unregister the ExportDeclaration
     *
     * @param exportDeclaration the ExportDeclaration to register
     */
    protected void registerExportDeclaration(ExportDeclaration exportDeclaration) {
        declarationRegistrationManager.registerDeclaration(exportDeclaration);
    }

    /**
     * Utility method to unregister an ExportDeclaration of OSGi.
     * Use it only if you have used registerExportDeclaration(...) to register the ExportDeclaration
     *
     * @param exportDeclaration the ExportDeclaration to unregister
     */
    protected void unregisterExportDeclaration(ExportDeclaration exportDeclaration) {
        declarationRegistrationManager.unregisterDeclaration(exportDeclaration);
    }

    @Override
    public String toString() {
        return getName();
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    public Set<ExportDeclaration> getExportDeclarations() {
        return declarationRegistrationManager.getDeclarations();
    }
}
