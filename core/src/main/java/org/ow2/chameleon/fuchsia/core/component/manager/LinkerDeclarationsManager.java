package org.ow2.chameleon.fuchsia.core.component.manager;

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
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Internal class which manages the Declarations binded to a Linker.
 * <p/>
 * Provides methods to add/remove/retrieve informations/do operations on the Declarations
 * <p/>
 * This class doesn't use the Declaration objects but the ServiceReference of the Declaration.
 */
public class LinkerDeclarationsManager<D extends Declaration, S extends DeclarationBinder<D>> {
    private final BundleContext bundleContext;
    private final LinkerManagement<D, S> linkerManagement;
    private final Map<ServiceReference<D>, Boolean> declarations;
    private Filter declarationFilter;

    public LinkerDeclarationsManager(BundleContext bundleContext, LinkerManagement linkerManagement, Filter declarationFilter) {
        this.bundleContext = bundleContext;
        this.linkerManagement = linkerManagement;
        this.declarations = new HashMap<ServiceReference<D>, Boolean>();
        this.declarationFilter = declarationFilter;
    }

    /**
     * Add the declarationSRef to the DeclarationsManager.
     * Calculate the matching of the Declaration with the DeclarationFilter of the
     * Linker.
     *
     * @param declarationSRef the ServiceReference<D> of the Declaration
     */
    public void add(ServiceReference<D> declarationSRef) {
        D declaration = getDeclaration(declarationSRef);
        boolean matchFilter = declarationFilter.matches(declaration.getMetadata());
        declarations.put(declarationSRef, matchFilter);
    }

    /**
     * Remove the declarationSRef from the DeclarationsManager.
     *
     * @param declarationSRef the ServiceReference<Declaration> of the Declaration
     * @return
     */
    public void remove(ServiceReference<D> declarationSRef) {
        declarations.remove(declarationSRef);
    }

    /**
     * Calculate the matching of the Declaration modified with the DeclarationFilter of the
     * Linker.
     *
     * @param declarationSRef the ServiceReference<Declaration> of the Declaration
     */
    public void modified(ServiceReference<D> declarationSRef) {
        D declaration = getDeclaration(declarationSRef);
        boolean matchFilter = declarationFilter.matches(declaration.getMetadata());
        declarations.put(declarationSRef, matchFilter);
    }

    /**
     * Return true if the Declaration match the DeclarationFilter, false otherwise.
     *
     * @param declarationSRef the ServiceReference<Declaration> of the Declaration
     * @return true if the Declaration match the DeclarationFilter, false otherwise.
     */
    public Boolean matched(ServiceReference<D> declarationSRef) {
        return declarations.get(declarationSRef);
    }

    /**
     * Return the Declaration corresponding to the declarationSRef
     *
     * @param declarationSRef the ServiceReference<Declaration> of the Declaration
     * @return the Declaration corresponding to the declarationSRef
     */
    public D getDeclaration(ServiceReference<D> declarationSRef) {
        return bundleContext.getService(declarationSRef);
    }

    /**
     * Create all the links possible between the Declaration and all the ImporterService matching the
     * ImporterServiceFilter of the Linker.
     *
     * @param declarationSRef the ServiceReference<Declaration> of the Declaration
     */
    public void createLinks(ServiceReference<D> declarationSRef) {
        D declaration = getDeclaration(declarationSRef);
        for (ServiceReference<S> serviceReference : linkerManagement.getMatchedBinderServiceRef()) {
            if (linkerManagement.canBeLinked(declaration, serviceReference)) {
                linkerManagement.link(declaration, serviceReference);
            }
        }
    }

    /**
     * Remove all the existing links of the Declaration.
     *
     * @param declarationSRef the ServiceReference<Declaration> of the Declaration
     */
    public void removeLinks(ServiceReference<D> declarationSRef) {
        D declaration = getDeclaration(declarationSRef);
        for (ServiceReference serviceReference : declaration.getStatus().getServiceReferencesBounded()) {
            // FIXME : In case of multiples Linker, we will remove the link of all the ServiceReference
            // FIXME : event the ones which dun know nothing about
            linkerManagement.unlink(declaration, serviceReference);
        }
    }

    /**
     * Return a set of all the Declaration matching the DeclarationFilter of the
     * Linker.
     *
     * @return a set of all the Declaration matching the DeclarationFilter of the
     * Linker.
     */
    public Set<D> getMatchedDeclaration() {
        Set<D> bindedSet = new HashSet<D>();
        for (Map.Entry<ServiceReference<D>, Boolean> e : declarations.entrySet()) {
            if (e.getValue()) {
                bindedSet.add(getDeclaration(e.getKey()));
            }
        }
        return bindedSet;
    }


    /**
     * Compute and apply all the modifications bring by the modification of the DeclarationFilter.
     * <p/>
     * Find all the Declaration that are now matching the filter and all that are no more matching the filter.
     * <ul>
     * <li>Remove all the links of the ones which are no more matching the DeclarationFilter.</li>
     * <li>Create the links of the ones which are now matching the DeclarationFilter.</li>
     * </ul>
     */
    public void applyFilterChanges(Filter declarationFilter) {
        this.declarationFilter = declarationFilter;
        Set<ServiceReference<D>> added = new HashSet<ServiceReference<D>>();
        Set<ServiceReference<D>> removed = new HashSet<ServiceReference<D>>();

        for (Map.Entry<ServiceReference<D>, Boolean> e : declarations.entrySet()) {
            Map<String, Object> metadata = getDeclaration(e.getKey()).getMetadata();
            boolean matchFilter = declarationFilter.matches(metadata);
            if (matchFilter != e.getValue() && matchFilter) {
                added.add(e.getKey());
            } else if (matchFilter != e.getValue() && !matchFilter) {
                removed.add(e.getKey());
            }
            e.setValue(matchFilter);
        }
        for (ServiceReference<D> declarationSRef : removed) {
            removeLinks(declarationSRef);
        }
        for (ServiceReference<D> declarationSRef : added) {
            createLinks(declarationSRef);
        }
    }
}