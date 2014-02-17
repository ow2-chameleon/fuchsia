package org.ow2.chameleon.fuchsia.core.it.components;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

@Component(name = "SimpleImporterFactory")
@Provides(specifications = {ImporterService.class, SimpleImporter.class})
public class SimpleImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleImporter.class);

    private final Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) {
        decs.add(importDeclaration);
    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) {
        decs.remove(importDeclaration);
    }

    public int nbProxies() {
        return decs.size();
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
