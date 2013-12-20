package org.ow2.chameleon.fuchsia.fake.importer;

import org.ow2.chameleon.fuchsia.fake.device.GenericDevice;
import org.ow2.chameleon.fuchsia.fake.device.GenericFakeDevice;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

/**
 * This class is an importer service.
 * Its goal is to  receive the import declaration and instantiate the proxy.
 *
 * @author jeremy.savonet@gmail.com
 */

@Component(name = "Fuchsia-FakeImporterService-Factory")
@Provides(specifications = org.ow2.chameleon.fuchsia.core.component.ImporterService.class)
@Instantiate(name = "Fuchsia-FakeImporter")
public class ImporterService extends AbstractImporterComponent {

    private final BundleContext m_bundleContext;

    private HashMap<ImportDeclaration, GenericDevice> listOfCreatedProxies;
    /**
     * Constructor in order to have the bundle context injected
     * @param bundleContext
     */
    public ImporterService(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
        listOfCreatedProxies = new HashMap<ImportDeclaration, GenericDevice>();
    }

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ServiceProperty(name = TARGET_FILTER_PROPERTY, value = "(id=*)")
    private String filter;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @Override
    @Invalidate
    protected void stop() {
        logger.info("STOP FAKE IMPORTER SERVICE");
        super.stop();
        listOfCreatedProxies.clear();
    }

    @Override
    @Validate
    protected void start() {
        logger.info("START FAKE IMPORTER SERVICE");
        super.start();
    }

    /**
     * Call if an import declaration match with the LDAP filter
     * @param importDeclaration : the matching import declaration
     */
    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) {

        String id = (String) importDeclaration.getMetadata().get("id");

        ServiceReference[] deviceRef = new ServiceReference[0];
        try {
            deviceRef = m_bundleContext.getServiceReferences(GenericDevice.class.getName(),"(device.serialNumber="+id+")");
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        Object device =  m_bundleContext.getService(deviceRef[0]);
        GenericFakeDevice genericDevice = (GenericFakeDevice) device;
        logger.debug("FakeImporter create a proxy for " + importDeclaration);

        GenericDevice proxy = (GenericDevice) Proxy.newProxyInstance(DelegationProxy.class.getClassLoader(), new Class[] {GenericDevice.class}, new DelegationProxy(genericDevice));
        listOfCreatedProxies.put(importDeclaration,proxy);
        logger.debug(proxy.getSerialNumber());
    }

    /**
     * Call when an import declaration is leaving the OSGi register
     * @param importDeclaration : the leaving import declaration
     */
    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) {
        logger.debug("FakeImporter destroy a proxy for " + importDeclaration);
        listOfCreatedProxies.remove(listOfCreatedProxies.get(importDeclaration));
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
