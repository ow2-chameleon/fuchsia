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

import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBindManager;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract implementation of an proxy-creator which provides an {@link ImporterService}.
 * Start must be call before registering the service !
 * Stop must be called while the service is no more available !
 *
 * @author Morgan Martinet
 */
public abstract class AbstractImporterComponent implements ImporterService, ImporterIntrospection {
    private final DeclarationBindManager<ImportDeclaration> declarationBindManager;
    private final Set<ImportDeclaration> waitingImportDeclarationsToHandle;
    private ServiceReference<ImporterService> serviceReference;

    public AbstractImporterComponent() {
        declarationBindManager = new DeclarationBindManager<ImportDeclaration>(this);
        waitingImportDeclarationsToHandle = new HashSet<ImportDeclaration>();
    }

    /**
     * Abstract method, called when a ImportDeclaration can be used by the implementation class.
     */
    protected abstract void useImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException;

    /**
     * Abstract method, is called when the implementation class must stop to use an ImportDeclaration.
     */
    protected abstract void denyImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException;

    /**
     * Stop the Importer component, iPOJO Invalidate instance callback.
     * Must be override !
     */
    protected void stop() {
        declarationBindManager.unbindAll();
    }

    /**
     * Start the endpoint-creator component, iPOJO Validate instance callback.
     * Must be override !
     */
    protected void start() {
        //
    }

    /**
     * @param importDeclaration The {@link ImportDeclaration} of the service to be imported.
     * @throws org.ow2.chameleon.fuchsia.core.exceptions.BinderException
     */
    public void addDeclaration(final ImportDeclaration importDeclaration) throws BinderException {
        declarationBindManager.addDeclaration(importDeclaration);
    }

    public void useDeclaration(ImportDeclaration declaration) throws BinderException {
        useImportDeclaration(declaration);
    }

    /**
     * @param importDeclaration The {@link ImportDeclaration} of the service to stop to be imported.
     * @throws org.ow2.chameleon.fuchsia.core.exceptions.BinderException
     */
    public void removeDeclaration(final ImportDeclaration importDeclaration) throws BinderException {
        declarationBindManager.removeDeclaration(importDeclaration);
    }

    public void denyDeclaration(ImportDeclaration declaration) throws BinderException {
        denyImportDeclaration(declaration);
    }

    public Set<ImportDeclaration> getImportDeclarations() {
        return declarationBindManager.getDeclarations();
    }


    public ServiceReference<ImporterService> getServiceReference() {
        return serviceReference;
    }

    public void setServiceReference(ServiceReference<ImporterService> serviceReference) {
        synchronized (waitingImportDeclarationsToHandle) {
            this.serviceReference = serviceReference;
            for (ImportDeclaration importDeclaration : waitingImportDeclarationsToHandle) {
                importDeclaration.handle(serviceReference);
            }
            waitingImportDeclarationsToHandle.clear();
        }
    }

    public void handleImportDeclaration(ImportDeclaration importDeclaration) {
        synchronized (waitingImportDeclarationsToHandle) {
            if (this.serviceReference == null) {
                waitingImportDeclarationsToHandle.add(importDeclaration);
                return;
            }
        }

        importDeclaration.handle(serviceReference);

    }

    public void unhandleImportDeclaration(ImportDeclaration importDeclaration) {
        synchronized (waitingImportDeclarationsToHandle) {
            if (this.serviceReference == null) {
                waitingImportDeclarationsToHandle.remove(importDeclaration);
                return;
            }
        }
        importDeclaration.unhandle(serviceReference);

    }

    @Override
    public String toString() {
        return getName();
    }
}

