package org.ow2.chameleon.fushia.importer;

import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is an importer service.
 * It's goal its to  receive the import declaration and instantiate the proxy.
 *
 * @author jeremy.savonet@gmail.com
 */

@Component(name = "Fuchsia-FakeImporterService-Factory")
@Provides(specifications = org.ow2.chameleon.fuchsia.core.component.ImporterService.class)
@Instantiate(name = "Fuchsia-FakeImporter")
public class ImporterService extends AbstractImporterComponent {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @ServiceProperty(name = "instance.name")
    private String name;

    @Override
    @Invalidate
    protected void stop() {
        System.out.println("STOP FAKE IMPORTER SERVICE");
        super.stop();
    }

    @Override
    @Validate
    protected void start() {
        System.out.println("START FAKE IMPORTER SERVICE");
        super.start();
    }

    @Override
    protected void createProxy(ImportDeclaration importDeclaration) {
        logger.debug("FakeImporter create a proxy for " + importDeclaration);
    }

    @Override
    protected void destroyProxy(ImportDeclaration importDeclaration) {
        logger.debug("FakeImporter destroy a proxy for " + importDeclaration);
    }

    public List<String> getConfigPrefix() {
        List<String> l = new ArrayList<String>();
        l.add("fake");
        return l;
    }

    public String getName() {
        return name;
    }
}
