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

import static org.ow2.chameleon.fuchsia.core.FuchsiaUtils.getFilter;

/**
 * The {@link DefaultLinker} component is the default implementation of the interface Linker.
 * <p/>
 * The {@link DefaultLinker} component take as mandatory ServiceProperty a filter on the {@link ImportDeclaration} named
 * {@literal {@link #PROPERTY_FILTER_IMPORTDECLARATION}} and a filter on {@link ImporterService} named
 * {@literal {@link #PROPERTY_FILTER_IMPORTERSERVICE}}.
 * <p/>
 * The filters are String with the LDAP syntax OR {@Link Filter}.
 * <p/>
 * An optional ServiceProperty @literal {@link #PROPERTY_UNIQUE_IMPORTATION}} can disallow the DefaultLinker to give an
 * ImportDeclaration to more than one ImporterService. This Property is set to False by default.
 * WARNING : this property is actually based on the number of ImporterService actually bind to the ImportDeclaration,
 * if an other Linker has already bind the ImportDeclaration to an ImporterService, the DefaultLinker will not give the
 * ImportDeclaration to any of its ImporterService. The others Linker can bind theirs ImportDeclaration to many
 * ImporterService if they are not configured for an unique import by ImportDeclaration.
 *
 * @author Morgan Martinet
 */
@Component(name = "FuchsiaDefaultLinkerFactory")
@Provides(specifications = Linker.class)
public class DefaultLinker implements Linker {

    @Controller
    private boolean state;

    @ServiceProperty(name = "instance.name")
    private String linker_name;

    @ServiceProperty(name = PROPERTY_FILTER_IMPORTDECLARATION, mandatory = true)
    private Object importDeclarationFilterProperty;

    private Filter importDeclarationFilter;

    @Property(name = PROPERTY_FILTER_IMPORTDECLARATION, mandatory = true)
    public void computeImportDeclarationFilter(Object filterProperty) {
        if (!state) {
            return;
        }
        try {
            importDeclarationFilter = getFilter(filterProperty);
            state = true;
        } catch (InvalidFilterException invalidFilterException) {
            logger.debug("The value of the Property " + PROPERTY_FILTER_IMPORTDECLARATION + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
        }
    }

    @ServiceProperty(name = PROPERTY_FILTER_IMPORTERSERVICE, mandatory = true)
    private Object importerServiceFilterProperty;

    private Filter importerServiceFilter;

    @Property(name = PROPERTY_FILTER_IMPORTERSERVICE, mandatory = true)
    public void computeImporterServiceFilter(Object filterProperty) {
        if (!state) {
            return;
        }
        try {
            importerServiceFilter = getFilter(filterProperty);
            state = true;
        } catch (InvalidFilterException invalidFilterException) {
            logger.debug("The value of the Property " + PROPERTY_FILTER_IMPORTERSERVICE + " is invalid,"
                    + " the recuperation of the Filter has failed. The instance gonna stop.", invalidFilterException);
            state = false;
        }
    }

    @ServiceProperty(name = PROPERTY_UNIQUE_IMPORTATION, mandatory = false)
    private boolean uniqueImportationProperty = false;

    private final Object lock = new Object();

    private final Map<ImporterService, Filter> importerServices = new HashMap<ImporterService, Filter>();

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

    public DefaultLinker() {
        //
    }

    /**
     * Bind all the {@link ImporterService} matching the importerServiceFilter.
     * <p/>
     * Foreach ImporterService, check all the already bound declarations.
     * If the metadata of the importDeclaration match the filter exposed by the importer
     * bind the importDeclaration to the importer
     */
    @Bind(id = "importerServices", aggregate = true, optional = true)
    void bindImporterService(ImporterService importerService, Map<String, Object> properties) {
        if (!importerServiceFilter.matches(properties)) {
            return;
        }
        logger.debug(linker_name + " : Bind the ImporterService " + importerService);

        Filter filter = null;
        try {
            filter = getFilter(properties.get("target"));
        } catch (InvalidFilterException invalidFilterException) {
            logger.error("The ServiceProperty \"target\" of the ImporterService " + importerService
                    + " doesn't provides a valid Filter."
                    + " To be used, it must provides a correct \"target\" ServiceProperty.", invalidFilterException);
            return;
        }

        synchronized (lock) {
            logger.debug(linker_name + " : Add the ImporterService " + importerService
                    + " with filter " + filter.toString());

            importerServices.put(importerService, filter);
            for (ImportDeclaration importDeclaration : importDeclarations) {
                tryToBind(importDeclaration, importerService);
            }
        }
    }

    /**
     * Unbind the {@link ImporterService}.
     */
    @Unbind(id = "importerServices")
    void unbindImporterService(ImporterService importerService) {
        logger.debug(linker_name + " : Unbind the ImporterService " + importerService);
        synchronized (lock) {
            importerServices.remove(importerService);
            for (ImportDeclaration importDeclaration : importDeclarations) {
                if (importDeclaration.getStatus().getImporterServices().contains(importerService)) {
                    tryToUnbind(importDeclaration, importerService);
                }
            }
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
            for (ImporterService importerService : importerServices.keySet()) {
                tryToBind(importDeclaration, importerService);
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
            for (ImporterService importerService : importDeclaration.getStatus().getImporterServices()) {
                tryToUnbind(importDeclaration, importerService);
            }
            importDeclarations.remove(importDeclaration);
        }
    }

    /**
     * Try to bind the importDeclaration with the importerService, return true if they have been bind together,
     * false otherwise.
     *
     * @param importDeclaration The ImportDeclaration
     * @param importerService   The ImporterService
     * @return true if they have been bind together, false otherwise.
     */
    private boolean tryToBind(ImportDeclaration importDeclaration, ImporterService importerService) {
        // if the uniqueImportationProperty is set to true and the importDeclaration is already bind, just return.
        if (uniqueImportationProperty && importDeclaration.getStatus().isBound()) {
            return false;
        }
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
            importDeclaration.bind(importerService);
            return true;
        }
        logger.debug(importDeclaration + " doesn't match the filter of " + importerService
                + "(" + importDeclaration.getMetadata().toString() + ")");
        return false;
    }

    private boolean tryToUnbind(ImportDeclaration importDeclaration, ImporterService importerService) {
        importDeclaration.unbind(importerService);
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
     * Return the importerServices linked this DefaultLinker
     *
     * @return The importerServices linked to this DefaultLinker
     */
    public Set<ImporterService> getLinkedImporters() {
        return new HashSet<ImporterService>(importerServices.keySet());
    }

    /**
     * Return the importDeclarations bind by this DefaultLinker
     *
     * @return The importDeclarations bind by this DefaultLinker
     */
    public Set<ImportDeclaration> getImportDeclarations() {
        return new HashSet<ImportDeclaration>(importDeclarations);
    }
}
