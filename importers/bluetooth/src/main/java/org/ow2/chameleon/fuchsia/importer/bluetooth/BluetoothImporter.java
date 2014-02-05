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

@Component()
@Provides(specifications = org.ow2.chameleon.fuchsia.core.component.ImporterService.class)
// FIXME ADD LOCKS !!
public class BluetoothImporter extends AbstractImporterComponent {
    // FIXME scope metadata
    @ServiceProperty(name = TARGET_FILTER_PROPERTY, value = "(&(" + PROTOCOL_NAME + "=bluetooth)(scope=generic))")
    private String filter;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    private final BundleContext m_bundleContext;
    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Factory> bluetoothProxiesFactories;

    private final Set<ImportDeclaration> unresolvedImportDeclarations;
    private final Map<ImportDeclaration, ComponentInstance> resolvedImportDeclarations;

    /**
     * Constructor in order to have the bundle context injected
     *
     * @param bundleContext
     */
    public BluetoothImporter(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
        bluetoothProxiesFactories = new HashMap<String, Factory>();
        unresolvedImportDeclarations = new HashSet<ImportDeclaration>();
        resolvedImportDeclarations = new HashMap<ImportDeclaration, ComponentInstance>();
    }

    @Override
    @Invalidate
    protected void stop() {
        logger.info("Stop Dynamo Fuchsia Importer");
        super.stop();
    }

    @Override
    @Validate
    protected void start() {
        logger.info("Start Dynamo Fuchsia Importer");
        super.start();
    }

    /**
     * Call if an import declaration match with the LDAP filter
     *
     * @param importDeclaration : the matching import declaration
     */
    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) {
        logger.warn("useImportDeclaration called for : " + importDeclaration.toString());
        String fn = (String) importDeclaration.getMetadata().get("bluetooth.device.friendlyname");
        Factory factory = bluetoothProxiesFactories.get(fn);
        if (factory != null) {
            ComponentInstance proxy = createProxy(importDeclaration, factory);
            resolvedImportDeclarations.put(importDeclaration, proxy);
        } else {
            unresolvedImportDeclarations.add(importDeclaration);
        }
    }

    private ComponentInstance createProxy(ImportDeclaration importDeclaration, Factory f) {
        logger.warn("CreateProxy called for : " + importDeclaration.toString());

        ComponentInstance ci = null;
        Dictionary conf = new Hashtable();
        conf.put("metadata", importDeclaration.getMetadata());

        if (f != null) {
            try {
                ci = f.createComponentInstance(conf);
            } catch (UnacceptableConfiguration unacceptableConfiguration) {
                logger.error("Cannot create instance of Factory " + " : ", unacceptableConfiguration);
            } catch (MissingHandlerException e) {
                logger.error("Cannot create instance of Factory " + " : ", e);
            } catch (ConfigurationException e) {
                logger.error("Cannot create instance of Factory " + " : ", e);
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
        logger.debug("Bluetooth Importer  destroy a proxy for " + importDeclaration);
        // FIXME : destroy proxy
    }


    @Bind(aggregate = true, optional = true, filter = "(protocol=bluetooth)")
    private void bindBluetoothProxyFactories(Factory f, ServiceReference<Factory> sr) {
        logger.warn("Found one factory : " + f.getName());
        String friendlyName = (String) sr.getProperty("device_name");
        bluetoothProxiesFactories.put(friendlyName, f);
        ImportDeclaration iDec = null;
        Iterator<ImportDeclaration> iterator = unresolvedImportDeclarations.iterator();
        while (iterator.hasNext()) {
            iDec = iterator.next();
            // FIXME remove magic string
            String fn = (String) iDec.getMetadata().get("bluetooth.device.friendlyname");
            if (fn.startsWith(friendlyName)) {
                ComponentInstance proxy = createProxy(iDec, f);
                iterator.remove();
                resolvedImportDeclarations.put(iDec, proxy);
            }
        }
    }

    @Unbind
    private void unbindBluetoothProxyFactories(Factory f, ServiceReference<Factory> sr) {
        bluetoothProxiesFactories.remove((String) sr.getProperty("device_name"));
        // FIXME destroy proxy
    }

    public List<String> getConfigPrefix() {
        List<String> l = new ArrayList<String>();
        l.add("bluetooth,*");
        return l;
    }

    public String getName() {
        return name;
    }
}
