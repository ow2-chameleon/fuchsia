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
import org.ow2.chameleon.fuchsia.core.declaration.Status;
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
 * <p/>
 * An optional ServiceProperty {@literal {@link #UNIQUE_IMPORTATION_PROPERTY}} can disallow the DefaultImportationLinker
 * to give an ImportDeclaration to more than one ImporterService. This Property is set to False by default.
 * WARNING : this property is actually based on the number of ImporterService actually bind to the ImportDeclaration,
 * if an other ImportationLinker has already bind the ImportDeclaration to an ImporterService, the
 * DefaultImportationLinker will not give the ImportDeclaration to any of its ImporterService. The others
 * ImportationLinker can bind theirs ImportDeclaration to many ImporterService if they are not configured for an
 * unique import by ImportDeclaration.
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


    @ServiceProperty(name = UNIQUE_IMPORTATION_PROPERTY, mandatory = false)
    @Property(name = UNIQUE_IMPORTATION_PROPERTY, mandatory = false)
    private boolean uniqueImportationProperty = false;

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
    @Bind(id = "importDeclarations", aggregate = true, optional = true)
    void bindImportDeclaration(ImportDeclaration importDeclaration) {
        logger.debug(linker_name + " : Bind the ImportDeclaration " + importDeclaration);
        synchronized (lock) {
            declarationsManager.add(importDeclaration);
            if (!declarationsManager.matched(importDeclaration)) {
                return;
            }
            declarationsManager.createLinks(importDeclaration);
        }
    }

    /**
     * Unbind the {@link ImportDeclaration}.
     */
    @Unbind(id = "importDeclarations")
    void unbindImportDeclaration(ImportDeclaration importDeclaration) {
        logger.debug(linker_name + " : Unbind the ImportDeclaration " + importDeclaration);
        synchronized (lock) {
            declarationsManager.removeLinks(importDeclaration);
            declarationsManager.remove(importDeclaration);
        }
    }

    public boolean canBeLinked(ImportDeclaration importDeclaration, ServiceReference<ImporterService> importerServiceSRef) {
        // if the uniqueImportationProperty is set to true, if the importDeclaration is already bind and
        // the importerService is not one binded to the importDeclaration, return false.
        Status status = importDeclaration.getStatus();
        if (uniqueImportationProperty && status.isBound() && !status.getServiceReferences().contains(importerServiceSRef)) {
            return false;
        }
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
        private final Map<ImportDeclaration, Boolean> declarations;

        DeclarationsManager() {
            declarations = new HashMap<ImportDeclaration, Boolean>();
        }

        void add(ImportDeclaration importDeclaration) {
            boolean matchFilter = importDeclarationFilter.matches(importDeclaration.getMetadata());
            declarations.put(importDeclaration, matchFilter);
        }

        Boolean remove(ImportDeclaration importDeclaration) {
            return declarations.remove(importDeclaration);
        }

        Boolean matched(ImportDeclaration importDeclaration) {
            return declarations.get(importDeclaration);
        }

        void removeLinks(ImportDeclaration importDeclaration) {
            for (ServiceReference serviceReference : importDeclaration.getStatus().getServiceReferences()) {
                unlink(importDeclaration, serviceReference);
            }
        }

        void createLinks(ImportDeclaration importDeclaration) {
            for (ServiceReference<ImporterService> serviceReference : importersManager.getMatchedServiceReference()) {
                if (canBeLinked(importDeclaration, serviceReference)) {
                    link(importDeclaration, serviceReference);
                }
            }
        }

        Set<ImportDeclaration> getMatchedImportDeclaration() {
            Set<ImportDeclaration> bindedSet = new HashSet<ImportDeclaration>();
            for (Map.Entry<ImportDeclaration, Boolean> e : declarations.entrySet()) {
                if (e.getValue()) {
                    bindedSet.add(e.getKey());
                }
            }
            return bindedSet;
        }

        void applyFilterChanges() {
            Set<ImportDeclaration> added = new HashSet<ImportDeclaration>();
            Set<ImportDeclaration> removed = new HashSet<ImportDeclaration>();

            for (Map.Entry<ImportDeclaration, Boolean> e : declarations.entrySet()) {
                boolean matchFilter = importDeclarationFilter.matches(e.getKey().getMetadata());
                if (matchFilter != e.getValue() && matchFilter) {
                    added.add(e.getKey());
                } else if (matchFilter != e.getValue() && !matchFilter) {
                    removed.add(e.getKey());
                }
                e.setValue(matchFilter);
            }
            for (ImportDeclaration importDeclaration : removed) {
                removeLinks(importDeclaration);
            }
            for (ImportDeclaration importDeclaration : added) {
                createLinks(importDeclaration);
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

            private void update(ServiceReference<ImporterService> serviceReference) throws InvalidFilterException {
                properties.clear();
                for (String key : serviceReference.getPropertyKeys()) {
                    properties.put(key, serviceReference.getProperty(key));
                }
                match = importerServiceFilter.matches(properties);
                targetFilter = getFilter(properties.get(TARGET_FILTER_PROPERTY));
            }
        }

        private final Map<ServiceReference<ImporterService>, ImporterDescriptor> importers;

        ImportersManager() {
            importers = new HashMap<ServiceReference<ImporterService>, ImporterDescriptor>();
        }

        void add(ServiceReference<ImporterService> serviceReference) throws InvalidFilterException {
            ImporterDescriptor importerDescriptor = new ImporterDescriptor(serviceReference);
            importers.put(serviceReference, importerDescriptor);
        }

        ImporterDescriptor remove(ServiceReference<ImporterService> serviceReference) {
            return importers.remove(serviceReference);
        }

        Boolean matched(ServiceReference<ImporterService> serviceReference) {
            return importers.get(serviceReference).match;
        }

        ImporterService getImporterService(ServiceReference<ImporterService> serviceReference) {
            return bundleContext.getService(serviceReference);
        }

        Filter getTargetFilter(ServiceReference<ImporterService> serviceReference) {
            return importers.get(serviceReference).targetFilter;
        }

        void removeLinks(ServiceReference<ImporterService> serviceReference) {
            for (ImportDeclaration importDeclaration : declarationsManager.getMatchedImportDeclaration()) {
                if (importDeclaration.getStatus().getServiceReferences().contains(serviceReference)) {
                    unlink(importDeclaration, serviceReference);
                }
            }
        }

        void createLinks(ServiceReference<ImporterService> serviceReference) {
            for (ImportDeclaration importDeclaration : declarationsManager.getMatchedImportDeclaration()) {
                if (canBeLinked(importDeclaration, serviceReference)) {
                    link(importDeclaration, serviceReference);
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

        void modified(ServiceReference<ImporterService> serviceReference) throws InvalidFilterException {
            importers.get(serviceReference).update(serviceReference);
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
