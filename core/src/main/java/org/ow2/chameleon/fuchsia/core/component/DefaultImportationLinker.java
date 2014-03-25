package org.ow2.chameleon.fuchsia.core.component;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.manager.LinkerBinderManager;
import org.ow2.chameleon.fuchsia.core.component.manager.LinkerDeclarationsManager;
import org.ow2.chameleon.fuchsia.core.component.manager.LinkerManagement;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.FuchsiaUtils.getFilter;
import static org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder.TARGET_FILTER_PROPERTY;

/**
 * The {@link DefaultImportationLinker} component is the default implementation of the interface.
 * {@link ImportationLinker}.
 * <p/>
 * The {@link DefaultImportationLinker} component take as mandatory ServiceProperty a filter on the
 * {@link ImportDeclaration} named {@literal {@link #FILTER_IMPORTDECLARATION_PROPERTY }} and a filter on
 * {@link ImporterService} named {@literal {@link #FILTER_IMPORTERSERVICE_PROPERTY }}.
 * <p/>
 * The filters are String with the LDAP syntax OR {@link Filter}.
 *
 * @author Morgan Martinet
 */
@Component(name = FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
@Provides(specifications = {ImportationLinker.class, ImportationLinkerIntrospection.class})
public class DefaultImportationLinker implements ImportationLinker, ImportationLinkerIntrospection {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultImportationLinker.class);

    // The OSGi BundleContext, injected by OSGi in the constructor
    private final BundleContext bundleContext;

    private final Object lock = new Object();

    private final LinkerManagement<ImportDeclaration, ImporterService> linkerManagement;
    private final LinkerBinderManager<ImportDeclaration, ImporterService> importersManager;
    private final LinkerDeclarationsManager<ImportDeclaration, ImporterService> declarationsManager;

    @Controller
    private boolean state;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String linkerName;

    @ServiceProperty(name = FILTER_IMPORTDECLARATION_PROPERTY, mandatory = true)
    @Property(name = FILTER_IMPORTDECLARATION_PROPERTY, mandatory = true)
    private Object importDeclarationFilterProperty;

    private Filter importDeclarationFilter;


    @ServiceProperty(name = FILTER_IMPORTERSERVICE_PROPERTY, mandatory = true)
    @Property(name = FILTER_IMPORTERSERVICE_PROPERTY, mandatory = true)
    private Object importerServiceFilterProperty;

    private Filter importerServiceFilter;

    public DefaultImportationLinker(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

        processProperties();
        linkerManagement = new LinkerManagement<ImportDeclaration, ImporterService>(bundleContext, importerServiceFilter, importDeclarationFilter);
        importersManager = linkerManagement.getBindersManager();
        declarationsManager = linkerManagement.getDeclarationsManager();
    }

    @Validate
    public void start() {
        LOG.debug("ImportationLinker " + linkerName + " starting");
    }

    @Invalidate
    public void stop() {
        LOG.debug("ImportationLinker " + linkerName + " stopping");
    }

    /**
     * Get the filters ImporterServiceFilter and ImportDeclarationFilter from the properties, stop the instance if one of
     * them is invalid.
     */
    private void processProperties() {
        state = true;
        try {
            importerServiceFilter = getFilter(importerServiceFilterProperty);
        } catch (InvalidFilterException invalidFilterException) {
            LOG.debug("The value of the Property " + FILTER_IMPORTERSERVICE_PROPERTY + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
            return;
        }

        try {
            importDeclarationFilter = getFilter(importDeclarationFilterProperty);
        } catch (InvalidFilterException invalidFilterException) {
            LOG.debug("The value of the Property " + FILTER_IMPORTDECLARATION_PROPERTY + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
            return;
        }
    }

    /**
     * Called by iPOJO when the configuration of the DefaultImportationLinker is updated.
     * <p/>
     * Call #processProperties() to get the updated filters ImporterServiceFilter and ImportDeclarationFilter.
     * Compute and apply the changes in the links relatives to the changes in the filters.
     */
    @Updated
    public void updated() {
        processProperties();
        synchronized (lock) {
            importersManager.applyFilterChanges(importerServiceFilter);
            declarationsManager.applyFilterChanges(importDeclarationFilter);
        }
    }

    /**
     * Bind the {@link ImporterService} matching the importerServiceFilter.
     * <p/>
     * Check all the already bound {@link ImportDeclaration}s, if the metadata of the ImportDeclaration match the filter
     * exposed by the ImporterService, link them together.
     */
    @Bind(id = "importerServices", specification = "org.ow2.chameleon.fuchsia.core.component.ImporterService", aggregate = true, optional = true)
    void bindImporterService(ServiceReference<ImporterService> serviceReference) {
        synchronized (lock) {
            try {
                importersManager.add(serviceReference);
            } catch (InvalidFilterException invalidFilterException) {
                LOG.error("The ServiceProperty \"" + TARGET_FILTER_PROPERTY + "\" of the ImporterService "
                        + bundleContext.getService(serviceReference) + " doesn't provides a valid Filter."
                        + " To be used, it must provides a correct \"" + TARGET_FILTER_PROPERTY + "\" ServiceProperty.",
                        invalidFilterException);
                return;
            }
            if (!importersManager.matched(serviceReference)) {
                return;
            }
            LOG.debug(linkerName + " : Bind the ImporterService "
                    + importersManager.getDeclarationBinder(serviceReference)
                    + " with filter " + importersManager.getTargetFilter(serviceReference));
            importersManager.createLinks(serviceReference);
        }
    }

    /**
     * Update the Target Filter of the ImporterService.
     * Apply the induce modifications on the links of the ImporterService
     *
     * @param serviceReference
     */
    @Modified(id = "importerServices")
    void modifiedImporterService(ServiceReference<ImporterService> serviceReference) {
        try {
            importersManager.modified(serviceReference);
        } catch (InvalidFilterException invalidFilterException) {
            LOG.error("The ServiceProperty \"" + TARGET_FILTER_PROPERTY + "\" of the ImporterService "
                    + bundleContext.getService(serviceReference) + " doesn't provides a valid Filter."
                    + " To be used, it must provides a correct \"" + TARGET_FILTER_PROPERTY + "\" ServiceProperty.",
                    invalidFilterException);
            importersManager.removeLinks(serviceReference);
            return;
        }
        if(importersManager.matched(serviceReference)){
            importersManager.updateLinks(serviceReference);
        } else {
            importersManager.removeLinks(serviceReference);
        }
    }

    /**
     * Unbind the {@link ImporterService}.
     */
    @Unbind(id = "importerServices")
    void unbindImporterService(ServiceReference<ImporterService> serviceReference) {
        LOG.debug(linkerName + " : Unbind the ImporterService " + importersManager.getDeclarationBinder(serviceReference));
        synchronized (lock) {
            importersManager.removeLinks(serviceReference);
            importersManager.remove(serviceReference);
        }
    }

    /**
     * Bind the {@link ImportDeclaration} matching the filter ImportDeclarationFilter.
     * <p/>
     * Check if metadata of the ImportDeclaration match the filter exposed by the {@link ImporterService}s bound.
     * If the ImportDeclaration matches the ImporterService filter, link them together.
     */
    @Bind(id = "importDeclarations", specification = "org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration", aggregate = true, optional = true)
    void bindImportDeclaration(ServiceReference<ImportDeclaration> importDeclarationSRef) {
        synchronized (lock) {
            declarationsManager.add(importDeclarationSRef);
            LOG.debug(linkerName + " : Bind the ImportDeclaration "
                    + declarationsManager.getDeclaration(importDeclarationSRef));

            if (!declarationsManager.matched(importDeclarationSRef)) {
                return;
            }
            declarationsManager.createLinks(importDeclarationSRef);
        }
    }


    /**
     * Unbind and bind the {@link ImportDeclaration}.
     */
    @Modified(id = "importDeclarations")
    void modifiedImportDeclaration(ServiceReference<ImportDeclaration> importDeclarationSRef) {
        LOG.debug(linkerName + " : Modify the ImportDeclaration "
                + declarationsManager.getDeclaration(importDeclarationSRef));

        synchronized (lock) {
            declarationsManager.removeLinks(importDeclarationSRef);
            declarationsManager.modified(importDeclarationSRef);
            if (!declarationsManager.matched(importDeclarationSRef)) {
                return;
            }
            declarationsManager.createLinks(importDeclarationSRef);
        }
    }

    /**
     * Unbind the {@link ImportDeclaration}.
     */
    @Unbind(id = "importDeclarations")
    void unbindImportDeclaration(ServiceReference<ImportDeclaration> importDeclarationSRef) {
        LOG.debug(linkerName + " : Unbind the ImportDeclaration "
                + declarationsManager.getDeclaration(importDeclarationSRef));

        synchronized (lock) {
            declarationsManager.removeLinks(importDeclarationSRef);
            declarationsManager.remove(importDeclarationSRef);
        }
    }

    public String getName() {
        return linkerName;
    }

    /**
     * Return the importerServices linked this DefaultImportationLinker
     *
     * @return The importerServices linked to this DefaultImportationLinker
     */
    public Set<ImporterService> getLinkedImporters() {
        return importersManager.getMatchedDeclarationBinder();
    }

    /**
     * Return the importDeclarations bind by this DefaultImportationLinker
     *
     * @return The importDeclarations bind by this DefaultImportationLinker
     */
    public Set<ImportDeclaration> getImportDeclarations() {
        return declarationsManager.getMatchedDeclaration();
    }
}
