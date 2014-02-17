package org.ow2.chameleon.fuchsia.core;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Controller;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.FuchsiaUtils.getFilter;
import static org.ow2.chameleon.fuchsia.core.component.ImporterService.TARGET_FILTER_PROPERTY;

/**
 * The {@link DefaultImportationLinker} component is the default implementation of the interface
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
@Provides(specifications = {ImportationLinker.class,ImportationLinkerIntrospection.class})
public class DefaultImportationLinker implements ImportationLinker, ImportationLinkerIntrospection {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultImportationLinker.class);

    // The OSGi BundleContext, injected by OSGi in the constructor
    private final BundleContext bundleContext;

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
            importersManager.applyFilterChanges();
            declarationsManager.applyFilterChanges();
        }
    }

    private final Object lock = new Object();

    private final ImportersManager importersManager = new ImportersManager();

    private final DeclarationsManager declarationsManager = new DeclarationsManager();

    @Validate
    public void start() {
        LOG.debug(linkerName + " starting");
    }

    @Invalidate
    public void stop() {
        LOG.debug(linkerName + " stopping");
    }

    public DefaultImportationLinker(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        processProperties();
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
                    + importersManager.getImporterService(serviceReference)
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
        for (ImportDeclaration importDeclaration : declarationsManager.getMatchedImportDeclaration()) {
            boolean isAlreadyLinked = importDeclaration.getStatus().getServiceReferencesBounded().contains(serviceReference);
            boolean canBeLinked = canBeLinked(importDeclaration, serviceReference);
            if (isAlreadyLinked && !canBeLinked) {
                unlink(importDeclaration, serviceReference);
            } else if (!isAlreadyLinked && canBeLinked) {
                link(importDeclaration, serviceReference);
            }
        }
    }

    /**
     * Unbind the {@link ImporterService}.
     */
    @Unbind(id = "importerServices")
    void unbindImporterService(ServiceReference<ImporterService> serviceReference) {
        LOG.debug(linkerName + " : Unbind the ImporterService " + importersManager.getImporterService(serviceReference));
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
                    + declarationsManager.getImportDeclaration(importDeclarationSRef));

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
                + declarationsManager.getImportDeclaration(importDeclarationSRef));

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
                + declarationsManager.getImportDeclaration(importDeclarationSRef));

        synchronized (lock) {
            declarationsManager.removeLinks(importDeclarationSRef);
            declarationsManager.remove(importDeclarationSRef);
        }
    }

    /**
     * Return true if the ImportDeclaration can be linked to the ImporterService
     *
     * @param importDeclaration   The ImportDeclaration
     * @param importerServiceSRef The ServiceReference<ImporterService> of the ImporterService
     * @return true if the ImportDeclaration can be linked to the ImporterService
     */
    public boolean canBeLinked(ImportDeclaration importDeclaration, ServiceReference<ImporterService> importerServiceSRef) {
        // Evaluate the target filter of the ImporterService on the ImportDeclaration
        Filter filter = importersManager.getTargetFilter(importerServiceSRef);
        return filter.matches(importDeclaration.getMetadata());
    }

    /**
     * Try to link the importDeclaration with the importerService referenced by the ServiceReference,
     * return true if they have been link together, false otherwise.
     *
     * @param importDeclaration   The ImportDeclaration
     * @param importerServiceSRef The ServiceReference<ImporterService> of the ImporterService
     * @return true if they have been link together, false otherwise.
     */
    private boolean link(ImportDeclaration importDeclaration, ServiceReference<ImporterService> importerServiceSRef) {
        ImporterService importerService = importersManager.getImporterService(importerServiceSRef);
        LOG.debug(importDeclaration + " match the filter of " + importerService + " : bind them together");
        importDeclaration.bind(importerServiceSRef);
        try {
            importerService.addImportDeclaration(importDeclaration);
        } catch (BinderException e) {
            importDeclaration.unbind(importerServiceSRef);
            LOG.debug(importerService + " throw an exception when giving to it the ImportDeclaration "
                    + importDeclaration, e);
            return false;
        }
        return true;
    }

    /**
     * Try to unlink the importDeclaration from the importerService referenced by the ServiceReference,
     * return true if they have been cleanly unlink, false otherwise.
     *
     * @param importDeclaration        The ImportDeclaration
     * @param importerServiceReference The ServiceReference of the ImporterService
     * @return true if they have been cleanly unlink, false otherwise.
     */
    private boolean unlink(ImportDeclaration importDeclaration, ServiceReference<ImporterService> importerServiceReference) {
        ImporterService importerService = importersManager.getImporterService(importerServiceReference);
        importDeclaration.unbind(importerServiceReference);
        try {
            importerService.removeImportDeclaration(importDeclaration);
        } catch (BinderException e) {
            LOG.debug(importerService + " throw an exception when removing of it the ImportDeclaration "
                    + importDeclaration, e);
            return false;
        }
        return true;
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
        return importersManager.getMatchedImporterService();
    }

    /**
     * Return the importDeclarations bind by this DefaultImportationLinker
     *
     * @return The importDeclarations bind by this DefaultImportationLinker
     */
    public Set<ImportDeclaration> getImportDeclarations() {
        return declarationsManager.getMatchedImportDeclaration();
    }

    /**
     * Internal class which manages the ImportDeclarations binded to the DefaultImportationLinker.
     * <p/>
     * Provides methods to add/remove/retrieve informations/do operations on the ImportDeclarations
     * <p/>
     * This class doesn't use the ImportDeclaration objects but the ServiceReference of the ImportDeclaration.
     */
    class DeclarationsManager {
        private final Map<ServiceReference<ImportDeclaration>, Boolean> declarations;

        DeclarationsManager() {
            declarations = new HashMap<ServiceReference<ImportDeclaration>, Boolean>();
        }

        /**
         * Add the importDeclarationSRef to the DeclarationsManager.
         * Calculate the matching of the ImportDeclaration with the ImportDeclarationFilter of the
         * DefaultImportationLinker.
         *
         * @param importDeclarationSRef the ServiceReference<ImportDeclaration> of the ImportDeclaration
         */
        void add(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            ImportDeclaration importDeclaration = getImportDeclaration(importDeclarationSRef);
            boolean matchFilter = importDeclarationFilter.matches(importDeclaration.getMetadata());
            declarations.put(importDeclarationSRef, matchFilter);
        }

        /**
         * Remove the importDeclarationSRef from the DeclarationsManager.
         *
         * @param importDeclarationSRef the ServiceReference<ImportDeclaration> of the ImportDeclaration
         * @return
         */
        void remove(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            declarations.remove(importDeclarationSRef);
        }

        /**
         * Calculate the matching of the ImportDeclaration modified with the ImportDeclarationFilter of the
         * DefaultImportationLinker.
         *
         * @param importDeclarationSRef the ServiceReference<ImportDeclaration> of the ImportDeclaration
         */
        void modified(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            ImportDeclaration importDeclaration = getImportDeclaration(importDeclarationSRef);
            boolean matchFilter = importDeclarationFilter.matches(importDeclaration.getMetadata());
            declarations.put(importDeclarationSRef, matchFilter);
        }

        /**
         * Return true if the ImportDeclaration match the ImportDeclarationFilter, false otherwise.
         *
         * @param importDeclarationSRef the ServiceReference<ImportDeclaration> of the ImportDeclaration
         * @return true if the ImportDeclaration match the ImportDeclarationFilter, false otherwise.
         */
        Boolean matched(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            return declarations.get(importDeclarationSRef);
        }

        /**
         * Return the ImportDeclaration corresponding to the importDeclarationSRef
         *
         * @param importDeclarationSRef the ServiceReference<ImportDeclaration> of the ImportDeclaration
         * @return the ImportDeclaration corresponding to the importDeclarationSRef
         */
        ImportDeclaration getImportDeclaration(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            return bundleContext.getService(importDeclarationSRef);
        }

        /**
         * Create all the links possible between the ImportDeclaration and all the ImporterService matching the
         * ImporterServiceFilter of the DefaultImportationLinker.
         *
         * @param importDeclarationSRef the ServiceReference<ImportDeclaration> of the ImportDeclaration
         */
        void createLinks(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            ImportDeclaration importDeclaration = getImportDeclaration(importDeclarationSRef);
            for (ServiceReference<ImporterService> serviceReference : importersManager.getMatchedServiceReference()) {
                if (canBeLinked(importDeclaration, serviceReference)) {
                    link(importDeclaration, serviceReference);
                }
            }
        }

        /**
         * Remove all the existing links of the ImportDeclaration.
         *
         * @param importDeclarationSRef the ServiceReference<ImportDeclaration> of the ImportDeclaration
         */
        void removeLinks(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            ImportDeclaration importDeclaration = getImportDeclaration(importDeclarationSRef);
            for (ServiceReference serviceReference : importDeclaration.getStatus().getServiceReferencesBounded()) {
                // FIXME : In case of multiples Linker, we will remove the link of all the ServiceReference
                // FIXME : event the ones which dun know nothing about
                unlink(importDeclaration, serviceReference);
            }
        }

        /**
         * Return a set of all the ImportDeclaration matching the ImportDeclarationFilter of the
         * DefaultImportationLinker.
         *
         * @return a set of all the ImportDeclaration matching the ImportDeclarationFilter of the
         *         DefaultImportationLinker.
         */
        Set<ImportDeclaration> getMatchedImportDeclaration() {
            Set<ImportDeclaration> bindedSet = new HashSet<ImportDeclaration>();
            for (Map.Entry<ServiceReference<ImportDeclaration>, Boolean> e : declarations.entrySet()) {
                if (e.getValue()) {
                    bindedSet.add(getImportDeclaration(e.getKey()));
                }
            }
            return bindedSet;
        }


        /**
         * Compute and apply all the modifications bring by the modification of the ImportDeclarationFilter.
         * <p/>
         * Find all the ImportDeclaration that are now matching the filter and all that are no more matching the filter.
         * <ul>
         * <li>Remove all the links of the ones which are no more matching the ImportDeclarationFilter.</li>
         * <li>Create the links of the ones which are now matching the ImportDeclarationFilter.</li>
         * </ul>
         */
        void applyFilterChanges() {
            Set<ServiceReference<ImportDeclaration>> added = new HashSet<ServiceReference<ImportDeclaration>>();
            Set<ServiceReference<ImportDeclaration>> removed = new HashSet<ServiceReference<ImportDeclaration>>();

            for (Map.Entry<ServiceReference<ImportDeclaration>, Boolean> e : declarations.entrySet()) {
                Map<String, Object> metadata = getImportDeclaration(e.getKey()).getMetadata();
                boolean matchFilter = importDeclarationFilter.matches(metadata);
                if (matchFilter != e.getValue() && matchFilter) {
                    added.add(e.getKey());
                } else if (matchFilter != e.getValue() && !matchFilter) {
                    removed.add(e.getKey());
                }
                e.setValue(matchFilter);
            }
            for (ServiceReference<ImportDeclaration> importDeclarationSRef : removed) {
                removeLinks(importDeclarationSRef);
            }
            for (ServiceReference<ImportDeclaration> importDeclarationSRef : added) {
                createLinks(importDeclarationSRef);
            }
        }
    }

    /**
     * Internal class which manages the {@link ImporterService}s binded to the {@link DefaultImportationLinker}.
     * <p/>
     * Provides methods to add/remove/retrieve informations/do operations on the ImporterServices.
     * <p/>
     * This class doesn't use the ImporterService objects but the ServiceReference of the ImporterService.
     */
    class ImportersManager {

        /**
         * Stock some informations processed/retrieved from the ImporterService
         */
        private class ImporterDescriptor {
            Filter targetFilter;
            boolean match;
            Map<String, Object> properties;

            private ImporterDescriptor(ServiceReference<ImporterService> serviceReference) throws InvalidFilterException {
                properties = new HashMap<String, Object>();
                update(serviceReference);
            }

            /**
             * Extract the information of the importerServiceSRef to update the ImporterDescriptor
             * information.
             *
             * @param importerServiceSRef the ServiceReference<ImporterService> of the ImporterService
             * @throws InvalidFilterException
             */
            private void update(ServiceReference<ImporterService> importerServiceSRef) throws InvalidFilterException {
                properties.clear();
                for (String key : importerServiceSRef.getPropertyKeys()) {
                    properties.put(key, importerServiceSRef.getProperty(key));
                }
                match = importerServiceFilter.matches(properties);
                targetFilter = getFilter(properties.get(TARGET_FILTER_PROPERTY));
            }
        }

        private final Map<ServiceReference<ImporterService>, ImporterDescriptor> importers;

        ImportersManager() {
            importers = new HashMap<ServiceReference<ImporterService>, ImporterDescriptor>();
        }

        /**
         * Add the importerServiceSRef to the ImportersManager, create the corresponding
         * ImporterDescriptor.
         *
         * @param importerServiceSRef the ServiceReference<ImporterService> of the ImporterService
         * @throws InvalidFilterException
         */
        void add(ServiceReference<ImporterService> importerServiceSRef) throws InvalidFilterException {
            ImporterDescriptor importerDescriptor = new ImporterDescriptor(importerServiceSRef);
            importers.put(importerServiceSRef, importerDescriptor);
        }

        /**
         * Remove the importerServiceSRef of the ImportersManager
         *
         * @param importerServiceSRef the ServiceReference<ImporterService> of the ImporterService
         */
        void remove(ServiceReference<ImporterService> importerServiceSRef) {
            importers.remove(importerServiceSRef);
        }

        /**
         * Update the ImporterDescriptor of the  importerServiceSRef
         *
         * @param importerServiceSRef the ServiceReference<ImporterService> of the ImporterService
         */
        void modified(ServiceReference<ImporterService> importerServiceSRef) throws InvalidFilterException {
            importers.get(importerServiceSRef).update(importerServiceSRef);
        }

        /**
         * Return true if the ImporterService has match the ImportDeclarationFilter or false other.
         *
         * @param importerServiceSRef the ServiceReference<ImporterService> of the ImporterService
         * @return true if the ImporterService has match the ImportDeclarationFilter, false otherwise.
         */
        Boolean matched(ServiceReference<ImporterService> importerServiceSRef) {
            return importers.get(importerServiceSRef).match;
        }

        /**
         * Return the ImporterService of the importerServiceSRef
         *
         * @param importerServiceSRef the ServiceReference<ImporterService> of the ImporterService
         * @return the ImporterService of the importerServiceSRef
         */
        ImporterService getImporterService(ServiceReference<ImporterService> importerServiceSRef) {
            return bundleContext.getService(importerServiceSRef);
        }

        /**
         * Return the Target Filter of the ImporterService
         *
         * @param importerServiceSRef the ServiceReference<ImporterService> of the ImporterService
         * @return the Target Filter of the ImporterService.
         */
        Filter getTargetFilter(ServiceReference<ImporterService> importerServiceSRef) {
            return importers.get(importerServiceSRef).targetFilter;
        }

        /**
         * Create all the links possible between the ImporterService and all the ImportDeclaration matching the
         * ImportDeclarationFilter of the DefaultImportationLinker.
         *
         * @param importerServiceSRef the ServiceReference<ImporterService> of the ImporterService
         */
        void createLinks(ServiceReference<ImporterService> importerServiceSRef) {
            for (ImportDeclaration importDeclaration : declarationsManager.getMatchedImportDeclaration()) {
                if (canBeLinked(importDeclaration, importerServiceSRef)) {
                    link(importDeclaration, importerServiceSRef);
                }
            }
        }

        /**
         * Remove all the existing links of the ImporterService.
         *
         * @param importerServiceSRef the ServiceReference<ImporterService> of the ImporterService
         */
        void removeLinks(ServiceReference<ImporterService> importerServiceSRef) {
            for (ImportDeclaration importDeclaration : declarationsManager.getMatchedImportDeclaration()) {
                if (importDeclaration.getStatus().getServiceReferencesBounded().contains(importerServiceSRef)) {
                    unlink(importDeclaration, importerServiceSRef);
                }
            }
        }

        /**
         * Return a set of all ImporterService matching the ImporterServiceFilter of the DefaultImportationLinker.
         *
         * @return a Set of all ImporterService matching the ImporterServiceFilter of the DefaultImportationLinker.
         */
        Set<ImporterService> getMatchedImporterService() {
            Set<ImporterService> bindedSet = new HashSet<ImporterService>();
            for (Map.Entry<ServiceReference<ImporterService>, ImporterDescriptor> e : importers.entrySet()) {
                if (e.getValue().match) {
                    bindedSet.add(getImporterService(e.getKey()));
                }
            }
            return bindedSet;
        }

        /**
         * Return a set of all ServiceReference<ImporterService> matching the ImporterServiceFilter of the
         * DefaultImportationLinker.
         *
         * @return a Set of all ServiceReference<ImporterService> matching the ImporterServiceFilter of the
         *         DefaultImportationLinker.
         */
        Set<ServiceReference<ImporterService>> getMatchedServiceReference() {
            Set<ServiceReference<ImporterService>> bindedSet = new HashSet<ServiceReference<ImporterService>>();
            for (Map.Entry<ServiceReference<ImporterService>, ImporterDescriptor> e : importers.entrySet()) {
                if (e.getValue().match) {
                    bindedSet.add(e.getKey());
                }
            }
            return bindedSet;
        }

        /**
         * Compute and apply all the modifications bring by the modification of the ImporterServiceFilter.
         * <p/>
         * Find all the ImporterService that are now matching the filter and all that are no more matching the filter.
         * <ul>
         * <li>Remove all the links of the ones which are no more matching the ImporterServiceFilter.</li>
         * <li>Create the links of the ones which are now matching the ImporterServiceFilter.</li>
         * </ul>
         */
        void applyFilterChanges() {
            Set<ServiceReference<ImporterService>> added = new HashSet<ServiceReference<ImporterService>>();
            Set<ServiceReference<ImporterService>> removed = new HashSet<ServiceReference<ImporterService>>();

            for (Map.Entry<ServiceReference<ImporterService>, ImporterDescriptor> e : importers.entrySet()) {
                boolean matchFilter = importerServiceFilter.matches(e.getValue().properties);
                if (matchFilter != e.getValue().match && matchFilter) {
                    added.add(e.getKey());
                } else if (matchFilter != e.getValue().match && !matchFilter) {
                    removed.add(e.getKey());
                }
                e.getValue().match = matchFilter;
            }
            for (ServiceReference<ImporterService> importerServiceReference : removed) {
                removeLinks(importerServiceReference);
            }
            for (ServiceReference<ImporterService> importerServiceReference : added) {
                createLinks(importerServiceReference);
            }
        }
    }
}
