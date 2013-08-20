package org.ow2.chameleon.fuchsia.core;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Controller;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;
import org.ow2.chameleon.fuchsia.core.exceptions.BadImportRegistration;
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
 * The {@link DefaultImportationLinker} component is the default implementation of the interface ImportationLinker.
 * <p/>
 * The {@link DefaultImportationLinker} component take as mandatory ServiceProperty a filter on the {@link ImportDeclaration} named
 * {@literal {@link #FILTER_IMPORTDECLARATION_PROPERTY }} and a filter on {@link ImporterService} named
 * {@literal {@link #FILTER_IMPORTERSERVICE_PROPERTY }}.
 * <p/>
 * The filters are String with the LDAP syntax OR {@link Filter}.
 * <p/>
 * An optional ServiceProperty @literal {@link #UNIQUE_IMPORTATION_PROPERTY}} can disallow the DefaultImportationLinker to give an
 * ImportDeclaration to more than one ImporterService. This Property is set to False by default.
 * WARNING : this property is actually based on the number of ImporterService actually bind to the ImportDeclaration,
 * if an other ImportationLinker has already bind the ImportDeclaration to an ImporterService, the DefaultImportationLinker will not give the
 * ImportDeclaration to any of its ImporterService. The others ImportationLinker can bind theirs ImportDeclaration to many
 * ImporterService if they are not configured for an unique import by ImportDeclaration.
 *
 * @author Morgan Martinet
 */
@Component(name = FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
@Provides(specifications = ImportationLinker.class)
public class DefaultImportationLinker implements ImportationLinker {

    @Controller
    private boolean state;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String linker_name;

    @ServiceProperty(name = FILTER_IMPORTDECLARATION_PROPERTY, mandatory = true)
    private Object importDeclarationFilterProperty;

    private Filter importDeclarationFilter;

    @Property(name = FILTER_IMPORTDECLARATION_PROPERTY, mandatory = true)
    public void computeImportDeclarationFilter(Object filterProperty) {
        if (!state) {
            return;
        }
        try {
            importDeclarationFilter = getFilter(filterProperty);
            state = true;
        } catch (InvalidFilterException invalidFilterException) {
            logger.debug("The value of the Property " + FILTER_IMPORTDECLARATION_PROPERTY + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
        }
    }

    @ServiceProperty(name = FILTER_IMPORTERSERVICE_PROPERTY, mandatory = true)
    private Object importerServiceFilterProperty;

    private Filter importerServiceFilter;

    @Property(name = FILTER_IMPORTERSERVICE_PROPERTY, mandatory = true)
    public void computeImporterServiceFilter(Object filterProperty) {
        if (!state) {
            return;
        }
        try {
            importerServiceFilter = getFilter(filterProperty);
            state = true;
        } catch (InvalidFilterException invalidFilterException) {
            logger.debug("The value of the Property " + FILTER_IMPORTERSERVICE_PROPERTY + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
        }
    }

    @ServiceProperty(name = UNIQUE_IMPORTATION_PROPERTY, mandatory = false)
    private boolean uniqueImportationProperty = false;

    private final Object lock = new Object();

    private final Map<ImporterService, Filter> importerServices = new HashMap<ImporterService, Filter>();

    private final Map<ServiceReference, ImporterService> importerServiceReferences = new HashMap<ServiceReference, ImporterService>();

    private final Set<ImportDeclaration> importDeclarations = new HashSet<ImportDeclaration>();

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

    public DefaultImportationLinker() {
        //
    }

    /**
     * Bind all the {@link ImporterService} matching the importerServiceFilter.
     * <p/>
     * Foreach ImporterService, check all the already bound importDeclarations.
     * If the metadata of the importDeclaration match the filter exposed by the importer
     * bind the importDeclaration to the importer
     */
    @Bind(id = "importerServices", aggregate = true, optional = true)
    void bindImporterService(ImporterService importerService, Map<String, Object> properties, ServiceReference serviceReference) {
        if (!importerServiceFilter.matches(properties)) {
            return;
        }
        logger.debug(linker_name + " : Bind the ImporterService " + importerService);

        Filter filter = null;
        try {
            filter = getFilter(properties.get(TARGET_FILTER_PROPERTY));
        } catch (InvalidFilterException invalidFilterException) {
            logger.error("The ServiceProperty \"" + TARGET_FILTER_PROPERTY + "\" of the ImporterService "
                    + importerService + " doesn't provides a valid Filter."
                    + " To be used, it must provides a correct \"" + TARGET_FILTER_PROPERTY + "\" ServiceProperty.",
                    invalidFilterException);
            return;
        }

        synchronized (lock) {
            logger.debug(linker_name + " : Add the ImporterService " + importerService
                    + " with filter " + filter.toString());

            importerServices.put(importerService, filter);
            importerServiceReferences.put(serviceReference, importerService);
            for (ImportDeclaration importDeclaration : importDeclarations) {
                tryToBind(importDeclaration, serviceReference);
            }
        }
    }

    /**
     * Unbind the {@link ImporterService}.
     */
    @Unbind(id = "importerServices")
    void unbindImporterService(ImporterService importerService, ServiceReference serviceReference) {
        logger.debug(linker_name + " : Unbind the ImporterService " + importerService);
        synchronized (lock) {
            for (ImportDeclaration importDeclaration : importDeclarations) {
                if (importDeclaration.getStatus().getServiceReferences().contains(importerService)) {
                    tryToUnbind(importDeclaration, serviceReference);
                }
            }
            importerServices.remove(importerService);
            importerServiceReferences.remove(serviceReference);
        }
    }

    /**
     * Bind all the {@link ImportDeclaration} matching the filter importDeclarationFilter.
     * <p/>
     * Foreach ImportDeclaration, check if metadata match the filter given exposed by the importerServices bound.
     */
    @Bind(id = "importDeclarations", aggregate = true, optional = true)
    void bindImportDeclaration(ImportDeclaration importDeclaration) {
        if (!importDeclarationFilter.matches(importDeclaration.getMetadata())) {
            return;
        }
        logger.debug(linker_name + " : Bind the ImportDeclaration " + importDeclaration);

        synchronized (lock) {
            importDeclarations.add(importDeclaration);
            for (ServiceReference serviceReference : importerServiceReferences.keySet()) {
                tryToBind(importDeclaration, serviceReference);
            }

        }
    }

    /**
     * Unbind the {@link ImportDeclaration}.
     */
    @Unbind(id = "importDeclarations")
    void unbindImportDeclaration(ImportDeclaration importDeclaration) {
        logger.debug(linker_name + " : Unbind the ImportDeclaration " + importDeclaration);
        synchronized (lock) {
            for (ServiceReference serviceReference : importDeclaration.getStatus().getServiceReferences()) {
                tryToUnbind(importDeclaration, serviceReference);
            }
            importDeclarations.remove(importDeclaration);
        }
    }

    /**
     * Try to bind the importDeclaration with the importerService referenced by the ServiceReference,
     * return true if they have been bind together, false otherwise.
     *
     * @param importDeclaration The ImportDeclaration
     * @param importerServiceReference The ServiceReference of the ImporterService
     *
     * @return true if they have been bind together, false otherwise.
     */
    private boolean tryToBind(ImportDeclaration importDeclaration, ServiceReference importerServiceReference) {
        // if the uniqueImportationProperty is set to true and the importDeclaration is already bind, just return.
        if (uniqueImportationProperty && importDeclaration.getStatus().isBound()) {
            return false;
        }
        ImporterService importerService = importerServiceReferences.get(importerServiceReference);
        Filter filter = importerServices.get(importerService);
        if (filter.matches(importDeclaration.getMetadata())) {
            try {
                importerService.addImportDeclaration(importDeclaration);
            } catch (BadImportRegistration bir) {
                logger.debug(importerService + " throw an exception when giving to it the ImportDeclaration "
                        + importDeclaration, bir);
                return false;
            } catch (Exception e) {
                logger.debug(importerService + " throw an exception with giving to it the ImportDeclaration "
                        + importDeclaration, e);
                return false;
            }
            logger.debug(importDeclaration + " match the filter of " + importerService + " : bind them together");
            importDeclaration.bind(importerServiceReference);
            return true;
        }
        logger.debug(importDeclaration + " doesn't match the filter of " + importerService
                + "(" + importDeclaration.getMetadata().toString() + ")");
        return false;
    }

    /**
     * Try to unbind the importDeclaration from the importerService referenced by the ServiceReference,
     * return true if they have been cleanly unbind, false otherwise.
     *
     * @param importDeclaration The ImportDeclaration
     * @param importerServiceReference The ServiceReference of the ImporterService
     *
     * @return true if they have been cleanly unbind, false otherwise.
     */
    private boolean tryToUnbind(ImportDeclaration importDeclaration, ServiceReference importerServiceReference) {
        ImporterService importerService = importerServiceReferences.get(importerServiceReference);
        importDeclaration.unbind(importerServiceReference);
        try {
            importerService.removeImportDeclaration(importDeclaration);
        } catch (BadImportRegistration bir) {
            logger.debug(importerService + " throw an exception when removing of it the ImportDeclaration "
                    + importDeclaration, bir);
            return false;
        } catch (Exception e) {
            logger.debug(importerService + " throw an exception with removing of it the ImportDeclaration "
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
        return new HashSet<ImporterService>(importerServices.keySet());
    }

    /**
     * Return the importDeclarations bind by this DefaultImportationLinker
     *
     * @return The importDeclarations bind by this DefaultImportationLinker
     */
    public Set<ImportDeclaration> getImportDeclarations() {
        return new HashSet<ImportDeclaration>(importDeclarations);
    }
}
