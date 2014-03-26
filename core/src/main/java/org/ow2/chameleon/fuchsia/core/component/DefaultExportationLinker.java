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

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.manager.LinkerBinderManager;
import org.ow2.chameleon.fuchsia.core.component.manager.LinkerDeclarationsManager;
import org.ow2.chameleon.fuchsia.core.component.manager.LinkerManagement;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.FuchsiaUtils.getFilter;
import static org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder.TARGET_FILTER_PROPERTY;

/**
 * The {@link DefaultExportationLinker} component is the default implementation of the interface.
 * {@link ExportationLinker}.
 * <p/>
 * The {@link DefaultExportationLinker} component take as mandatory ServiceProperty a filter on the
 * {@link ExportDeclaration} named {@literal {@link #FILTER_EXPORTDECLARATION_PROPERTY }} and a filter on
 * {@link ExporterService} named {@literal {@link #FILTER_EXPORTERSERVICE_PROPERTY }}.
 * <p/>
 * The filters are String with the LDAP syntax OR {@link org.osgi.framework.Filter}.
 *
 * @author Morgan Martinet
 */
@Component(name = FuchsiaConstants.DEFAULT_EXPORTATION_LINKER_FACTORY_NAME)
@Provides(specifications = {ExportationLinker.class, ExportationLinkerIntrospection.class})
public class DefaultExportationLinker implements ExportationLinker, ExportationLinkerIntrospection {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultExportationLinker.class);

    // The OSGi BundleContext, injected by OSGi in the constructor
    private final BundleContext bundleContext;

    private final Object lock = new Object();

    private final LinkerManagement<ExportDeclaration, ExporterService> linkerManagement;
    private final LinkerBinderManager<ExportDeclaration, ExporterService> exportersManager;
    private final LinkerDeclarationsManager<ExportDeclaration, ExporterService> declarationsManager;

    @Controller
    private boolean state;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String linkerName;

    @ServiceProperty(name = FILTER_EXPORTDECLARATION_PROPERTY, mandatory = true)
    @Property(name = FILTER_EXPORTDECLARATION_PROPERTY, mandatory = true)
    private Object exportDeclarationFilterProperty;

    private Filter exportDeclarationFilter;


    @ServiceProperty(name = FILTER_EXPORTERSERVICE_PROPERTY, mandatory = true)
    @Property(name = FILTER_EXPORTERSERVICE_PROPERTY, mandatory = true)
    private Object exporterServiceFilterProperty;

    private Filter exporterServiceFilter;

    public DefaultExportationLinker(BundleContext context) {
        this.bundleContext = context;
        processProperties();

        linkerManagement = new LinkerManagement<ExportDeclaration, ExporterService>(bundleContext, exporterServiceFilter, exportDeclarationFilter);
        exportersManager = linkerManagement.getBindersManager();
        declarationsManager = linkerManagement.getDeclarationsManager();
    }

    @Validate
    public void start() {
        LOG.debug("ExportationLinker " + linkerName + " starting");
    }

    @Invalidate
    public void stop() {
        LOG.debug("ExportationLinker " + linkerName + " stopping");
    }

    /**
     * Get the filters ExporterServiceFilter and ExportDeclarationFilter from the properties, stop the instance if one of.
     * them is invalid.
     */
    private void processProperties() {
        state = true;
        try {
            exporterServiceFilter = getFilter(exporterServiceFilterProperty);
        } catch (InvalidFilterException invalidFilterException) {
            LOG.debug("The value of the Property " + FILTER_EXPORTERSERVICE_PROPERTY + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
            return;
        }

        try {
            exportDeclarationFilter = getFilter(exportDeclarationFilterProperty);
        } catch (InvalidFilterException invalidFilterException) {
            LOG.debug("The value of the Property " + FILTER_EXPORTDECLARATION_PROPERTY + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
            return;
        }
    }

    /**
     * Called by iPOJO when the configuration of the DefaultExportationLinker is updated.
     * <p/>
     * Call #processProperties() to get the updated filters ExporterServiceFilter and ExportDeclarationFilter.
     * Compute and apply the changes in the links relatives to the changes in the filters.
     */
    @Updated
    public void updated() {
        processProperties();
        synchronized (lock) {
            exportersManager.applyFilterChanges(exporterServiceFilter);
            declarationsManager.applyFilterChanges(exportDeclarationFilter);
        }
    }

    /**
     * Bind the {@link ExporterService} matching the exporterServiceFilter.
     * <p/>
     * Check all the already bound {@link ExportDeclaration}s, if the metadata of the ExportDeclaration match the filter
     * exposed by the ExporterService, link them together.
     */
    @Bind(id = "exporterServices", specification = "org.ow2.chameleon.fuchsia.core.component.ExporterService", aggregate = true, optional = true)
    void bindExporterService(ServiceReference<ExporterService> serviceReference) {
        synchronized (lock) {
            try {
                exportersManager.add(serviceReference);
            } catch (InvalidFilterException invalidFilterException) {
                LOG.error("The ServiceProperty \"" + TARGET_FILTER_PROPERTY + "\" of the ExporterService "
                        + bundleContext.getService(serviceReference) + " doesn't provides a valid Filter."
                        + " To be used, it must provides a correct \"" + TARGET_FILTER_PROPERTY + "\" ServiceProperty.",
                        invalidFilterException);
                return;
            }
            if (!exportersManager.matched(serviceReference)) {
                return;
            }
            LOG.debug(linkerName + " : Bind the ExporterService "
                    + exportersManager.getDeclarationBinder(serviceReference)
                    + " with filter " + exportersManager.getTargetFilter(serviceReference));
            exportersManager.createLinks(serviceReference);
        }
    }

    /**
     * Update the Target Filter of the ExporterService.
     * Apply the induce modifications on the links of the ExporterService
     *
     * @param serviceReference
     */
    @Modified(id = "exporterServices")
    void modifiedExporterService(ServiceReference<ExporterService> serviceReference) {
        try {
            exportersManager.modified(serviceReference);
        } catch (InvalidFilterException invalidFilterException) {
            LOG.error("The ServiceProperty \"" + TARGET_FILTER_PROPERTY + "\" of the ExporterService "
                    + bundleContext.getService(serviceReference) + " doesn't provides a valid Filter."
                    + " To be used, it must provides a correct \"" + TARGET_FILTER_PROPERTY + "\" ServiceProperty.",
                    invalidFilterException);
            exportersManager.removeLinks(serviceReference);
            return;
        }
        if(exportersManager.matched(serviceReference)){
            exportersManager.updateLinks(serviceReference);
        } else {
            exportersManager.removeLinks(serviceReference);
        }
    }

    /**
     * Unbind the {@link ExporterService}.
     */
    @Unbind(id = "exporterServices")
    void unbindExporterService(ServiceReference<ExporterService> serviceReference) {
        LOG.debug(linkerName + " : Unbind the ExporterService " + exportersManager.getDeclarationBinder(serviceReference));
        synchronized (lock) {
            exportersManager.removeLinks(serviceReference);
            exportersManager.remove(serviceReference);
        }
    }

    /**
     * Bind the {@link ExportDeclaration} matching the filter ExportDeclarationFilter.
     * <p/>
     * Check if metadata of the ExportDeclaration match the filter exposed by the {@link ExporterService}s bound.
     * If the ExportDeclaration matches the ExporterService filter, link them together.
     */
    @Bind(id = "exportDeclarations", specification = "org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration", aggregate = true, optional = true)
    void bindExportDeclaration(ServiceReference<ExportDeclaration> exportDeclarationSRef) {
        synchronized (lock) {
            declarationsManager.add(exportDeclarationSRef);
            LOG.debug(linkerName + " : Bind the ExportDeclaration "
                    + declarationsManager.getDeclaration(exportDeclarationSRef));

            if (!declarationsManager.matched(exportDeclarationSRef)) {
                return;
            }
            declarationsManager.createLinks(exportDeclarationSRef);
        }
    }


    /**
     * Unbind and bind the {@link ExportDeclaration}.
     */
    @Modified(id = "exportDeclarations")
    void modifiedExportDeclaration(ServiceReference<ExportDeclaration> exportDeclarationSRef) {
        LOG.debug(linkerName + " : Modify the ExportDeclaration "
                + declarationsManager.getDeclaration(exportDeclarationSRef));

        synchronized (lock) {
            declarationsManager.removeLinks(exportDeclarationSRef);
            declarationsManager.modified(exportDeclarationSRef);
            if (!declarationsManager.matched(exportDeclarationSRef)) {
                return;
            }
            declarationsManager.createLinks(exportDeclarationSRef);
        }
    }

    /**
     * Unbind the {@link ExportDeclaration}.
     */
    @Unbind(id = "exportDeclarations")
    void unbindExportDeclaration(ServiceReference<ExportDeclaration> exportDeclarationSRef) {
        LOG.debug(linkerName + " : Unbind the ExportDeclaration "
                + declarationsManager.getDeclaration(exportDeclarationSRef));

        synchronized (lock) {
            declarationsManager.removeLinks(exportDeclarationSRef);
            declarationsManager.remove(exportDeclarationSRef);
        }
    }

    public String getName() {
        return linkerName;
    }

    /**
     * Return the exporterServices linked this DefaultExportationLinker.
     *
     * @return The exporterServices linked to this DefaultExportationLinker
     */
    public Set<ExporterService> getLinkedExporters() {
        return exportersManager.getMatchedDeclarationBinder();
    }

    /**
     * Return the exportDeclarations bind by this DefaultExportationLinker.
     *
     * @return The exportDeclarations bind by this DefaultExportationLinker
     */
    public Set<ExportDeclaration> getExportDeclarations() {
        return declarationsManager.getMatchedDeclaration();
    }
}
