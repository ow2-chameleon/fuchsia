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
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;
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
@Provides(specifications = ImportationLinker.class)
public class DefaultImportationLinker implements ImportationLinker, ImportationLinkerIntrospection {

    // The OSGi BundleContext, injected by OSGi in the constructor
    private final BundleContext bundleContext;

    @Controller
    private boolean state;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String linker_name;

    @ServiceProperty(name = FILTER_IMPORTDECLARATION_PROPERTY, mandatory = true)
    @Property(name = FILTER_IMPORTDECLARATION_PROPERTY, mandatory = true)
    private Object importDeclarationFilterProperty;

    private Filter importDeclarationFilter;


    @ServiceProperty(name = FILTER_IMPORTERSERVICE_PROPERTY, mandatory = true)
    @Property(name = FILTER_IMPORTERSERVICE_PROPERTY, mandatory = true)
    private Object importerServiceFilterProperty;

    private Filter importerServiceFilter;

    @Updated
    public void updated() {
        state = true;
        try {
            importerServiceFilter = getFilter(importerServiceFilterProperty);
        } catch (InvalidFilterException invalidFilterException) {
            logger.debug("The value of the Property " + FILTER_IMPORTERSERVICE_PROPERTY + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
            return;
        }

        try {
            importDeclarationFilter = getFilter(importDeclarationFilterProperty);
        } catch (InvalidFilterException invalidFilterException) {
            logger.debug("The value of the Property " + FILTER_IMPORTDECLARATION_PROPERTY + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
            return;
        }

        synchronized (lock) {
            importersManager.applyFilterChanges();
            declarationsManager.applyFilterChanges();
        }
    }

    private final Object lock = new Object();

    private final ImportersManager importersManager = new ImportersManager();

    private final DeclarationsManager declarationsManager = new DeclarationsManager();

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Validate
    public void start() {
        logger.debug(linker_name + " starting");
    }

    @Invalidate
    public void stop() {
        logger.debug(linker_name + " stopping");
    }

    public DefaultImportationLinker(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Bind all the {@link ImporterService} matching the importerServiceFilter.
     * <p/>
     * Foreach ImporterService, check all the already bound importDeclarations.
     * If the metadata of the importDeclaration match the filter exposed by the importer
     * bind the importDeclaration to the importer
     */
    @Bind(id = "importerServices", specification = "org.ow2.chameleon.fuchsia.core.component.ImporterService", aggregate = true, optional = true)
    void bindImporterService(ServiceReference<ImporterService> serviceReference) {
        synchronized (lock) {
            try {
                importersManager.add(serviceReference);
            } catch (InvalidFilterException invalidFilterException) {
                logger.error("The ServiceProperty \"" + TARGET_FILTER_PROPERTY + "\" of the ImporterService "
                        + bundleContext.getService(serviceReference) + " doesn't provides a valid Filter."
                        + " To be used, it must provides a correct \"" + TARGET_FILTER_PROPERTY + "\" ServiceProperty.",
                        invalidFilterException);
                return;
            }
            if (!importersManager.matched(serviceReference)) {
                return;
            }
            logger.debug(linker_name + " : Bind the ImporterService "
                    + importersManager.getImporterService(serviceReference)
                    + " with filter " + importersManager.getTargetFilter(serviceReference));
            importersManager.createLinks(serviceReference);
        }
    }

    @Modified(id = "importerServices")
    void modifiedImporterService(ServiceReference<ImporterService> serviceReference) {
        try {
            importersManager.modified(serviceReference);
        } catch (InvalidFilterException invalidFilterException) {
            logger.error("The ServiceProperty \"" + TARGET_FILTER_PROPERTY + "\" of the ImporterService "
                    + bundleContext.getService(serviceReference) + " doesn't provides a valid Filter."
                    + " To be used, it must provides a correct \"" + TARGET_FILTER_PROPERTY + "\" ServiceProperty.",
                    invalidFilterException);
            importersManager.removeLinks(serviceReference);
            return;
        }
        for (ImportDeclaration importDeclaration : declarationsManager.getMatchedImportDeclaration()) {
            boolean isAlreadyLinked = importDeclaration.getStatus().getServiceReferences().contains(serviceReference);
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
        logger.debug(linker_name + " : Unbind the ImporterService " + importersManager.getImporterService(serviceReference));
        synchronized (lock) {
            importersManager.removeLinks(serviceReference);
            importersManager.remove(serviceReference);
        }
    }

    /**
     * Bind all the {@link ImportDeclaration} matching the filter importDeclarationFilter.
     * <p/>
     * Foreach ImportDeclaration, check if metadata match the filter given exposed by the importerServices bound.
     */
    @Bind(id = "importDeclarations", specification = "org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration", aggregate = true, optional = true)
    void bindImportDeclaration(ServiceReference<ImportDeclaration> importDeclarationSRef) {
        synchronized (lock) {
            declarationsManager.add(importDeclarationSRef);
            logger.debug(linker_name + " : Bind the ImportDeclaration "
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
        logger.debug(linker_name + " : Modify the ImportDeclaration "
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
        logger.debug(linker_name + " : Unbind the ImportDeclaration "
                + declarationsManager.getImportDeclaration(importDeclarationSRef));

        synchronized (lock) {
            declarationsManager.removeLinks(importDeclarationSRef);
            declarationsManager.remove(importDeclarationSRef);
        }
    }

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
     * @param importerServiceSRef The ServiceReference of the ImporterService
     * @return true if they have been link together, false otherwise.
     */
    private boolean link(ImportDeclaration importDeclaration, ServiceReference<ImporterService> importerServiceSRef) {
        ImporterService importerService = importersManager.getImporterService(importerServiceSRef);
        try {
            importerService.addImportDeclaration(importDeclaration);
        } catch (ImporterException e) {
            logger.debug(importerService + " throw an exception when giving to it the ImportDeclaration "
                    + importDeclaration, e);
            return false;
        }
        logger.debug(importDeclaration + " match the filter of " + importerService + " : bind them together");
        importDeclaration.bind(importerServiceSRef);
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
        } catch (ImporterException e) {
            logger.debug(importerService + " throw an exception when removing of it the ImportDeclaration "
                    + importDeclaration, e);
            return false;
        }
        return true;
    }

    public String getName() {
        return linker_name;
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
     * Internal class which handle the ImportDeclarations and provide some usefuls functions on
     */
    class DeclarationsManager {
        private final Map<ServiceReference<ImportDeclaration>, Boolean> declarations;

        DeclarationsManager() {
            declarations = new HashMap<ServiceReference<ImportDeclaration>, Boolean>();
        }

        void add(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            ImportDeclaration importDeclaration = getImportDeclaration(importDeclarationSRef);
            boolean matchFilter = importDeclarationFilter.matches(importDeclaration.getMetadata());
            declarations.put(importDeclarationSRef, matchFilter);
        }

        void remove(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            declarations.remove(importDeclarationSRef);
        }

        void modified(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            ImportDeclaration importDeclaration = getImportDeclaration(importDeclarationSRef);
            boolean matchFilter = importDeclarationFilter.matches(importDeclaration.getMetadata());
            declarations.put(importDeclarationSRef, matchFilter);
        }

        Boolean matched(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            return declarations.get(importDeclarationSRef);
        }

        ImportDeclaration getImportDeclaration(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            return bundleContext.getService(importDeclarationSRef);
        }

        void createLinks(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            ImportDeclaration importDeclaration = getImportDeclaration(importDeclarationSRef);
            for (ServiceReference<ImporterService> serviceReference : importersManager.getMatchedServiceReference()) {
                if (canBeLinked(importDeclaration, serviceReference)) {
                    link(importDeclaration, serviceReference);
                }
            }
        }

        void removeLinks(ServiceReference<ImportDeclaration> importDeclarationSRef) {
            ImportDeclaration importDeclaration = getImportDeclaration(importDeclarationSRef);
            for (ServiceReference serviceReference : importDeclaration.getStatus().getServiceReferences()) {
                // FIXME : In case of multiples Linker, we will remove the link of all the ServiceReference
                // FIXME : event the ones which dun know nothing about
                unlink(importDeclaration, serviceReference);
            }
        }

        Set<ImportDeclaration> getMatchedImportDeclaration() {
            Set<ImportDeclaration> bindedSet = new HashSet<ImportDeclaration>();
            for (Map.Entry<ServiceReference<ImportDeclaration>, Boolean> e : declarations.entrySet()) {
                if (e.getValue()) {
                    bindedSet.add(getImportDeclaration(e.getKey()));
                }
            }
            return bindedSet;
        }

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

    class ImportersManager {
        private class ImporterDescriptor {
            Filter targetFilter;
            boolean match;
            Map<String, Object> properties;

            private ImporterDescriptor(ServiceReference<ImporterService> serviceReference) throws InvalidFilterException {
                properties = new HashMap<String, Object>();
                update(serviceReference);
            }

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

        void add(ServiceReference<ImporterService> importerServiceSRef) throws InvalidFilterException {
            ImporterDescriptor importerDescriptor = new ImporterDescriptor(importerServiceSRef);
            importers.put(importerServiceSRef, importerDescriptor);
        }

        void remove(ServiceReference<ImporterService> importerServiceSRef) {
            importers.remove(importerServiceSRef);
        }

        void modified(ServiceReference<ImporterService> importerServiceSRef) throws InvalidFilterException {
            importers.get(importerServiceSRef).update(importerServiceSRef);
        }

        Boolean matched(ServiceReference<ImporterService> importerServiceSRef) {
            return importers.get(importerServiceSRef).match;
        }

        ImporterService getImporterService(ServiceReference<ImporterService> importerServiceSRef) {
            return bundleContext.getService(importerServiceSRef);
        }

        Filter getTargetFilter(ServiceReference<ImporterService> importerServiceSRef) {
            return importers.get(importerServiceSRef).targetFilter;
        }

        void createLinks(ServiceReference<ImporterService> importerServiceSRef) {
            for (ImportDeclaration importDeclaration : declarationsManager.getMatchedImportDeclaration()) {
                if (canBeLinked(importDeclaration, importerServiceSRef)) {
                    link(importDeclaration, importerServiceSRef);
                }
            }
        }

        void removeLinks(ServiceReference<ImporterService> importerServiceSRef) {
            for (ImportDeclaration importDeclaration : declarationsManager.getMatchedImportDeclaration()) {
                if (importDeclaration.getStatus().getServiceReferences().contains(importerServiceSRef)) {
                    unlink(importDeclaration, importerServiceSRef);
                }
            }
        }

        Set<ImporterService> getMatchedImporterService() {
            Set<ImporterService> bindedSet = new HashSet<ImporterService>();
            for (Map.Entry<ServiceReference<ImporterService>, ImporterDescriptor> e : importers.entrySet()) {
                if (e.getValue().match) {
                    bindedSet.add(getImporterService(e.getKey()));
                }
            }
            return bindedSet;
        }

        Set<ServiceReference<ImporterService>> getMatchedServiceReference() {
            Set<ServiceReference<ImporterService>> bindedSet = new HashSet<ServiceReference<ImporterService>>();
            for (Map.Entry<ServiceReference<ImporterService>, ImporterDescriptor> e : importers.entrySet()) {
                if (e.getValue().match) {
                    bindedSet.add(e.getKey());
                }
            }
            return bindedSet;
        }

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
