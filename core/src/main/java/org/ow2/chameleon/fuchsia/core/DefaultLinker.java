package org.ow2.chameleon.fuchsia.core;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BadImportRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@link DefaultLinker} component is the default implementation of the interface Linker.
 * <p/>
 * TODO : finish documentation
 * The {@link DefaultLinker} component take has configuration a filter on the {@link ImportDeclaration} ...
 *
 * @author Morgan Martinet
 */
@Component(name = "FuchsiaDefaultLinkerFactory")
@Provides(specifications = Linker.class)
public class DefaultLinker implements Linker {

    @ServiceProperty(name = "instance.name")
    private String linker_name;

    @ServiceProperty(name = PROPERTY_FILTER_IMPORTDECLARATION)
    private Object importDeclarationFilter;

    //@ServiceProperty(name = PROPERTY_FILTER_IMPORTERSERVICE)
    private Object importerServiceFilter;

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
     * Bind all the {@link ImporterService}.
     * <p/>
     * Foreach ImporterService, check all the already bound declarations.
     * If the metadata of the importDeclaration match the filter exposed by the importer
     * bind the importDeclaration to the importer
     */
    @Bind(id = "importerServices", aggregate = true, optional = true)
    void bindImporterService(ImporterService importerService, Map<String, Object> properties) {
        logger.debug(linker_name + " : Bind the ImporterService " + importerService);
        Filter filter;
        Object propTarget = properties.get("target");
        if (propTarget instanceof String) {
            try {
                filter = FrameworkUtil.createFilter((String) propTarget);
            } catch (InvalidSyntaxException e) {
                // FIXME
                logger.error(linker_name + " : The target properties of the ImporterService "
                        + properties.get("instance.name") +
                        " contains a String that the syntax doesn't respect the LDAP syntax");
                return;
            }
        } else if (propTarget instanceof Filter) {
            filter = (Filter) propTarget;
        } else {
            // FIXME
            logger.error(linker_name + " : The target properties of the ImporterService  "
                    + properties.get("instance.name") + " must be a String using LDAP syntax or a org.osgi.framework.Filter");
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
     * Bind all the {@link ImportDeclaration}
     * <p/>
     * Foreach ImportDeclaration, check if metadata match the filter given exposed by the importerServices bound.
     */
    @Bind(id = "importDeclarations", aggregate = true, optional = true)
    void bindImportDeclaration(ImportDeclaration importDeclaration) {
        logger.debug(linker_name + " : Bind the ImportDeclaration " + importDeclaration);
        synchronized (lock) {
            importDeclarations.add(importDeclaration);
            for (ImporterService importerService : importerServices.keySet()) {
                tryToBind(importDeclaration, importerService);
            }

        }
    }

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
            importDeclaration.bind(importerService);
            logger.debug(importDeclaration + " match the filter of " + importerService + " : they are bind together");
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
