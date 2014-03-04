package org.ow2.chameleon.fuchsia.discovery.upnp;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPDevice;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Discovery UPnP  Detects the presence of a UPnP device (in a basedriver interface level) and publishes it as a importation declaration that can be seen by Fuchsia
 *
 * @author jeremy.savonet@gmail.com
 * @author jander nascimento (botelho at imag.fr)
 */
@Component
@Provides(specifications = {DiscoveryService.class})
public class UPnPDiscovery extends AbstractDiscoveryComponent {

    private static final Logger LOG = LoggerFactory.getLogger(UPnPDiscovery.class);

    private final Map<String, ImportDeclaration> importDeclarations = new HashMap<String, ImportDeclaration>();

    @ServiceProperty(name = "instance.name")
    private String name;

    public UPnPDiscovery(BundleContext bundleContext) {
        super(bundleContext);
        LOG.debug("UPnP discovery: loading..");
    }

    @Validate
    public void start() {
        LOG.debug("UPnP discovery: up and running.");
    }

    @Invalidate
    public void stop() {
        super.stop();

        importDeclarations.clear();

        LOG.debug("UPnP discovery: stopped.");
    }

    public String getName() {
        return name;
    }

    @Bind(id="upnp-device", specification = Constants.ORG_OSGI_SERVICE_UPNP_UPNP_DEVICE,aggregate = true)
    public Object addingService(ServiceReference reference) {



        String deviceID = (String) reference.getProperty(UPnPDevice.FRIENDLY_NAME);
        String deviceType = (String) reference.getProperty(UPnPDevice.TYPE);
        String udn = (String) reference.getProperty(UPnPDevice.ID);

        createImportationDeclaration(deviceID, deviceType, udn, reference);

        return getBundleContext().getService(reference);
    }

    @Unbind(id="upnp-device", specification = Constants.ORG_OSGI_SERVICE_UPNP_UPNP_DEVICE,aggregate = true)
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

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.DEVICE_ID, deviceId);
        metadata.put(Constants.DEVICE_TYPE, deviceType);
        metadata.put(Constants.DEVICE_TYPE_SUB, deviceSubType);

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        importDeclarations.put(deviceId, declaration);

        registerImportDeclaration(declaration);
    }

}
