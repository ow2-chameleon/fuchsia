package org.ow2.chameleon.fuchsia.upnp.importer;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.fake.device.GenericDevice;
import org.ow2.chameleon.fuchsia.fake.device.GenericFakeDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component(name = "Fuchsia-UPnPImporter-Factory")
@Provides(specifications = {ImporterService.class,UPnPFuchsiaImporterImpl.class })
//@Instantiate(name = "FuchsiaUPnPImporter")
public class UPnPFuchsiaImporterImpl extends AbstractImporterComponent {

    private final BundleContext m_bundleContext;

    private HashMap<ImportDeclaration, GenericDevice> listOfCreatedProxies;
    /**
     * Constructor in order to have the bundle context injected
     * @param bundleContext
     */
    public UPnPFuchsiaImporterImpl(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
        listOfCreatedProxies = new HashMap<ImportDeclaration, GenericDevice>();
    }

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ServiceProperty(name = "target", value = "(id=*)")
    private String filter;

    @ServiceProperty(name = "instance.name")
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
    protected void createProxy(ImportDeclaration importDeclaration) {

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

        GenericDevice proxy = (GenericDevice) Proxy.newProxyInstance(DelegationProxy.class.getClassLoader(), new Class[]{GenericDevice.class}, new DelegationProxy(genericDevice));
        listOfCreatedProxies.put(importDeclaration,proxy);
        logger.debug(proxy.getSerialNumber());
    }

    public synchronized GenericDevice getObjectProxy(ImportDeclaration iDec) {
         return listOfCreatedProxies.get(iDec);
    }

    /**
     * Call when an import declaration is leaving the OSGi register
     * @param importDeclaration : the leaving import declaration
     */
    @Override
    protected void destroyProxy(ImportDeclaration importDeclaration) {
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
