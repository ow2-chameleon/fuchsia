package org.ow2.chameleon.fuchsia.core;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.util.BadImportRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@link Linker} are used by the FuchsiaMachine to make the link between the
 * {@link ImportDeclaration} and the {@link org.ow2.chameleon.fuchsia.core.component.ImporterService}.
 * A FuchsiaMachine can have multiple {@link Linker} with different configurations.
 *
 * @author Morgan Martinet
 */
@Component(name = "FuchsiaLinkerFactory", publicFactory = false)
@Instantiate(name = "FuchsiaLinker")
public class Linker {

    private final Object lock = new Object();

    private final Map<ImporterService, Filter> importerServices = new HashMap<ImporterService, Filter>();

    private final Set<ImportDeclaration> importDeclarations = new HashSet<ImportDeclaration>();

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Validate
    public void start() {
        logger.debug("Fuchsia linker starting");
    }

    @Invalidate
    public void stop() {
        logger.debug("Fuchsia linker stopping");
    }

    public Linker() {
        //
    }

    /**
     * Bind all the {@link ImporterService}.
     * <p/>
     * foreach ImporterService, check all the already bound declarations.
     * If the metadata of the importDeclaration match the filter exposed by the importer
     * bind the importDeclaration to the importer
     */
    @Bind(aggregate = true, optional = true)
    void bindImporterService(ImporterService importerService, Map<String, Object> properties) {
        logger.debug("Bind an ImporterService");
        Filter filter;
        Object propTarget = properties.get("target");
        if (propTarget instanceof String) {
            try {
                filter = FrameworkUtil.createFilter((String) propTarget);
            } catch (InvalidSyntaxException e) {
                // FIXME
                // The target properties of the ImporterService " + properties.get("instance.name"))
                // contains a String that the syntax doesn't fit with the LDAP syntax
                return;
            }
        } else if (propTarget instanceof Filter) {
            filter = (Filter) propTarget;
        } else {
            // FIXME
            // The target properties of the ImporterService " + properties.get("instance.name"))
            // must be a String (LDAP syntax) or a org.osgi.framework.Filter
            return;
        }

        synchronized (lock) {
            importerServices.put(importerService, filter);
            for (ImportDeclaration importDeclaration : importDeclarations) {
                tryToBind(importDeclaration, importerService);
            }
        }
    }

    @Unbind
    void unbindImporterService(ImporterService importerService) {
        logger.debug("Unbind an ImporterService");
        synchronized (lock) {
            importerServices.remove(importerService);
            for (ImportDeclaration importDeclaration : importDeclarations) {
                if (importDeclaration.getStatus().getImporterServices().contains(importerService)) {
                    try {
                        importerService.removeImportDeclaration(importDeclaration);
                    } catch (BadImportRegistration bir) {
                        // TODO
                    }
                    importDeclaration.unbind(importerService);
                }
            }
        }
    }

    /**
     * Bind all the {@link ImportDeclaration}
     * <p/>
     * Foreach ImportDeclaration, check if metadata match the filter given exposed by the importerServices bound.
     */
    @Bind(aggregate = true, optional = true)
    void bindImportDeclaration(ImportDeclaration importDeclaration) {
        logger.debug("Bind an ImportDeclaration");
        synchronized (lock) {
            importDeclarations.add(importDeclaration);
            for (ImporterService importerService : importerServices.keySet()) {
                tryToBind(importDeclaration, importerService);
            }

        }
    }

    @Unbind
    void unbindImportDeclaration(ImportDeclaration importDeclaration) {
        logger.debug("Unbind an ImportDeclaration");
        synchronized (lock) {
            for (ImporterService importerService : importDeclaration.getStatus().getImporterServices()) {
                try {
                    importerService.removeImportDeclaration(importDeclaration);
                } catch (BadImportRegistration bir) {
                    // TODO
                }
            }
            importDeclarations.remove(importDeclaration);
        }
    }

    private boolean tryToBind(ImportDeclaration importDeclaration, ImporterService importerService) {
        Filter filter = importerServices.get(importerService);
        if (filter.matches(importDeclaration.getMetadata())) {
            try {
                importerService.addImportDeclaration(importDeclaration);
            } catch (BadImportRegistration bir) {
                // TODO
            }
            importDeclaration.bind(importerService);
            return true;
        }
        return false;
    }
}
