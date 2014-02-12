package org.ow2.chameleon.fuchsia.importer.bluetooth;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.PROTOCOL_NAME;

@Component
@Provides(specifications = org.ow2.chameleon.fuchsia.core.component.ImporterService.class)
// FIXME ADD LOCKS !!
public class BluetoothImporter extends AbstractImporterComponent {
    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BluetoothImporter.class);

    // FIXME scope metadata
    @ServiceProperty(name = TARGET_FILTER_PROPERTY, value = "(&(" + PROTOCOL_NAME + "=bluetooth)(scope=generic))")
    private String filter;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    private final BundleContext m_bundleContext;

    private ServiceReference serviceReference;

    private final Map<String, Factory> bluetoothProxiesFactories;

    private final Map<String, ImportDeclaration> unresolvedImportDeclarations;
    private final Map<String, ImportDeclaration> resolvedImportDeclarations;
    private final Map<ImportDeclaration, ComponentInstance> proxyComponentInstances;

    /**
     * Constructor in order to have the bundle context injected
     *
     * @param bundleContext
     */
    public BluetoothImporter(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
        bluetoothProxiesFactories = new HashMap<String, Factory>();
        unresolvedImportDeclarations = new HashMap<String, ImportDeclaration>();
        resolvedImportDeclarations = new HashMap<String, ImportDeclaration>();
        proxyComponentInstances = new HashMap<ImportDeclaration, ComponentInstance>();
    }

    @PostRegistration
    protected void registration(ServiceReference serviceReference){
        this.serviceReference = serviceReference;
    }

    @Override
    @Invalidate
    protected void stop() {
        LOG.info("Stop Dynamo Fuchsia Importer");
        super.stop();
    }

    @Override
    @Validate
    protected void start() {
        LOG.info("Start Dynamo Fuchsia Importer");
        super.start();
    }

    /**
     * Call if an import declaration match with the LDAP filter
     *
     * @param importDeclaration : the matching import declaration
     */
    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) {
        LOG.warn("useImportDeclaration called for : " + importDeclaration.toString());
        String fn = (String) importDeclaration.getMetadata().get("bluetooth.device.friendlyname");
        Factory factory = bluetoothProxiesFactories.get(fn);
        if (factory != null) {
            ComponentInstance proxy = createProxy(importDeclaration, factory);
            importDeclaration.handle(this.serviceReference);
            resolvedImportDeclarations.put(fn, importDeclaration);
            proxyComponentInstances.put(importDeclaration, proxy);
        } else {
            unresolvedImportDeclarations.put(fn, importDeclaration);
        }
    }

    private ComponentInstance createProxy(ImportDeclaration importDeclaration, Factory f) {
        LOG.warn("CreateProxy called for : " + importDeclaration.toString());

        ComponentInstance ci = null;
        Dictionary conf = new Hashtable();
        conf.put("metadata", importDeclaration.getMetadata());

        if (f != null) {
            try {
                ci = f.createComponentInstance(conf);
            } catch (UnacceptableConfiguration unacceptableConfiguration) {
                LOG.error("Cannot create instance of Factory " + " : ", unacceptableConfiguration);
            } catch (MissingHandlerException e) {
                LOG.error("Cannot create instance of Factory " + " : ", e);
            } catch (ConfigurationException e) {
                LOG.error("Cannot create instance of Factory " + " : ", e);
            }
        }
        return ci;
    }


    /**
     * Call when an import declaration is leaving the OSGi register
     *
     * @param importDeclaration : the leaving import declaration
     */
    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) {
        LOG.debug("Bluetooth Importer  destroy a proxy for " + importDeclaration);
        String fn = (String) importDeclaration.getMetadata().get("bluetooth.device.friendlyname");
        if(unresolvedImportDeclarations.remove(fn) == null){
            ImportDeclaration idec = resolvedImportDeclarations.remove(fn);
            ComponentInstance proxy = proxyComponentInstances.remove(idec);

            idec.unhandle(serviceReference);
            proxy.dispose();
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }


    @Bind(aggregate = true, optional = true, filter = "(protocol=bluetooth)")
    private void bindBluetoothProxyFactories(Factory f, ServiceReference<Factory> sr) {
        LOG.warn("Found one factory : " + f.getName());
        String friendlyName = (String) sr.getProperty("device_name");
        if(friendlyName == null){
            return;
        }
        bluetoothProxiesFactories.put(friendlyName, f);
        Map.Entry<String, ImportDeclaration> unresolvedImportDeclaration;
        ImportDeclaration iDec = null;
        String fn = null;
        Iterator<Map.Entry<String,ImportDeclaration>> iterator = unresolvedImportDeclarations.entrySet().iterator();
        while (iterator.hasNext()) {
            unresolvedImportDeclaration = iterator.next();
            fn = unresolvedImportDeclaration.getKey();
            if (fn.startsWith(friendlyName)) {
                iDec = unresolvedImportDeclaration.getValue();
                ComponentInstance proxy = createProxy(iDec, f);
                iDec.handle(this.serviceReference);
                iterator.remove();
                resolvedImportDeclarations.put(fn, iDec);
                proxyComponentInstances.put(iDec, proxy);
            }
        }
    }

    @Unbind
    private void unbindBluetoothProxyFactories(Factory f, ServiceReference<Factory> sr) {
        String name = (String) sr.getProperty("device_name");
        bluetoothProxiesFactories.remove(name);
        ImportDeclaration idec = resolvedImportDeclarations.remove(name);
        ComponentInstance proxy = proxyComponentInstances.remove(idec);

        idec.unhandle(serviceReference);
        proxy.dispose();

        unresolvedImportDeclarations.put(name, idec);
    }

    public String getName() {
        return name;
    }
}
