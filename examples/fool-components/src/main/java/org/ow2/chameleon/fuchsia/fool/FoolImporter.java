package org.ow2.chameleon.fuchsia.fool;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component(name = "Fuchsia-FoolImporter-Factory")
@Provides(specifications = ImporterService.class)
@Instantiate(name = "Fuchsia-FoolImporter")
public class FoolImporter extends AbstractImporterComponent {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FoolImporter.class);

    @ServiceProperty(name = TARGET_FILTER_PROPERTY, value = "(&(fool-number=1)(fool=fool))")
    private String filter;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {
        LOG.debug("FoolImporter create a proxy for " + importDeclaration);
    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws ImporterException {
        LOG.debug("FoolImporter destroy a proxy for " + importDeclaration);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    @Validate
    protected void stop() {
        super.stop();
    }

    @Override
    @Invalidate
    protected void start() {
        super.start();
    }

    public String getName() {
        return name;
    }
}
