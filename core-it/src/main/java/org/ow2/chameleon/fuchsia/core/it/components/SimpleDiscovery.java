package org.ow2.chameleon.fuchsia.core.it.components;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.it.services.SimulateBindingInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "SimpleDiscoveryFactory")
@Provides(specifications = {SimulateBindingInterface.class, DiscoveryService.class})
public class SimpleDiscovery extends AbstractDiscoveryComponent implements SimulateBindingInterface {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SimpleDiscovery(BundleContext bundleContext) {
        super(bundleContext);
    }

    public void bind(ImportDeclaration id) {
        super.registerImportDeclaration(id);
    }

    public void unbind(ImportDeclaration id) {
        super.unregisterImportDeclaration(id);
    }

    @Validate
    @Override
    protected void start() {
        super.start();
    }

    @Invalidate
    @Override
    protected void stop() {
        super.stop();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public String getName() {
        return "name";
    }

}