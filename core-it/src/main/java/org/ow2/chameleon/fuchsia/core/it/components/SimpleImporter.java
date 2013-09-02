package org.ow2.chameleon.fuchsia.core.it.components;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.List;

@Component(name = "SimpleImporterFactory")
@Provides(specifications = {ImporterService.class})
public class SimpleImporter extends AbstractImporterComponent {
    @Override
    public void createProxy(ImportDeclaration importDeclaration) {
        //
    }

    @Override
    public void destroyProxy(ImportDeclaration importDeclaration) {
        //
    }

    public List<String> getConfigPrefix() {
        return null;
    }

    public String getName() {
        return "simpleImporter";
    }

    @Override
    public void stop() {
        super.stop();
    }
}
