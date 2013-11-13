package org.ow2.chameleon.fuchsia.upnp.discovery;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Discovery UPnP  Detects the presence of a UPnP device (in a basedriver interface level) and publishes it as a importation declaration that can be seen by Fuchsia
 *
 * @author jeremy.savonet@gmail.com
 * @author jander nascimento (botelho at imag.fr)
 */
@Component(name = "Fuchsia-UPnPDiscovery-Factory")
@Provides(specifications = {DiscoveryService.class})
public class UPnPFuchsiaDiscoveryImpl extends AbstractDiscoveryComponent {

    private final HashMap<String, ImportDeclaration> importDeclarations = new HashMap<String, ImportDeclaration>();

    @ServiceProperty(name = "instance.name")
    private String name;

    public UPnPFuchsiaDiscoveryImpl(BundleContext bundleContext) {
        super(bundleContext);
        getLogger().debug("UPnP discovery: loading..");
    }

    @Validate
    public void start() {
        getLogger().debug("UPnP discovery: up and running.");
    }

    @Invalidate
    public void stop() {
        super.stop();

        importDeclarations.clear();

        getLogger().debug("UPnP discovery: stopped.");
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    public String getName() {
        return name;
    }

    @Bind(specification = Constants.BIND_SPECIFICATION_FOR_UPnPDevice)
    public Object addingService(ServiceReference reference) {

        String deviceID = (String) reference.getProperty(Constants.DEVICE_ID);
        String deviceType = (String) reference.getProperty(Constants.DEVICE_TYPE);
        String deviceSubType = (String) reference.getProperty(Constants.DEVICE_TYPE_SUB);

        createImportationDeclaration(deviceID, deviceType, deviceSubType, reference);

        return getBundleContext().getService(reference);
    }

    @Unbind(specification = Constants.BIND_SPECIFICATION_FOR_UPnPDevice)
    public void removedService(ServiceReference reference) {

        String deviceID = (String) reference.getProperty(Constants.DEVICE_ID);

        ImportDeclaration importDeclaration=importDeclarations.get(deviceID);

        unregisterImportDeclaration(importDeclaration);

    }

    public Set<ImportDeclaration> getImportDeclarations() {
        return Collections.unmodifiableSet(new HashSet<ImportDeclaration>(importDeclarations.values()));
    }

    /**
     * Create an import declaration and delegates its registration for an upper class
     */
    public synchronized void createImportationDeclaration(String deviceId, String deviceType, String deviceSubType, ServiceReference reference) {

        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.DEVICE_ID, deviceId);
        metadata.put(Constants.DEVICE_TYPE, deviceType);
        metadata.put(Constants.DEVICE_TYPE_SUB, deviceSubType);

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        importDeclarations.put(deviceId, declaration);

        registerImportDeclaration(declaration);
    }

}
