package org.ow2.chameleon.fuchsia.importer;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import org.ow2.chameleon.fuchsia.device.GenericDevice;
import org.ow2.chameleon.fuchsia.device.GenericFakeDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
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


    private BundleContext m_bundleContext;

    /**
     * Constructor in order to have the bundle context injected
     * @param bundleContext
     */
    public ImporterService(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
    }

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ServiceProperty(name = "target", value = "(id=BL-1234)")
    private String filter;

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

    /**
     * Call if an import declaration match with the LDAP filter
     * @param importDeclaration
     * @throws InvalidSyntaxException
     */
    @Override
    protected void createProxy(ImportDeclaration importDeclaration) throws InvalidSyntaxException {

        ServiceReference[] deviceRef = m_bundleContext.getServiceReferences(GenericDevice.class.getName(),"(device.serialNumber=BL-1234)");
        Object device =  m_bundleContext.getService(deviceRef[0]);
        GenericFakeDevice genericDevice = (GenericFakeDevice) device;
        logger.debug("FakeImporter create a proxy for " + importDeclaration);

        GenericDevice proxy = (GenericDevice) Proxy.newProxyInstance(DelegationProxy.class.getClassLoader(), new Class[] {GenericDevice.class}, new DelegationProxy(genericDevice));
        System.out.println(proxy.getSerialNumber());
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
