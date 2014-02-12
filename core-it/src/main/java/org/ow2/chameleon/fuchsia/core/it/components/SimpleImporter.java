package org.ow2.chameleon.fuchsia.core.it.components;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "SimpleImporterFactory")
@Provides(specifications = {ImporterService.class})
public class SimpleImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleImporter.class);

    @Override
    public void useImportDeclaration(ImportDeclaration importDeclaration) {
        //
    }

    @Override
    public void denyImportDeclaration(ImportDeclaration importDeclaration) {
        //
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    public String getName() {
        return "simpleImporter";
    }

    @Override
    public void stop() {
        super.stop();
    }
}
