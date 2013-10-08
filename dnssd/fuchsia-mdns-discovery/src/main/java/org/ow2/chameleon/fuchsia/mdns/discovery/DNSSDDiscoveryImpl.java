package org.ow2.chameleon.fuchsia.mdns.discovery;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.mdns.discovery.MDNSConstants;
import org.ow2.chameleon.fuchsia.mdns.discovery.topology.NetworkTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;


@Component(name = "DNSSDDiscoveryFactory")
public class DNSSDDiscoveryImpl extends AbstractDiscoveryComponent implements NetworkTopologyDiscovery.Factory.ClassDelegate, ServiceListener {

    private final HashMap<String, ImportDeclaration> importDeclarations = new HashMap<String, ImportDeclaration>();

    @Property(name = "dnssd.service.type",value = MDNSConstants.DNSSD_SERVICE_TYPE)
    private String dnssdServiceType;

    protected DNSSDDiscoveryImpl(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Validate
    public void start(){

        NetworkTopologyDiscovery.Factory.setClassDelegate(this);

        configureNetworkCardServiceListener();

    }

    @Invalidate
    public void stop(){

        getLogger().info("stopping service of mDNS/DNSsd discovery");

        for(ImportDeclaration declaration:importDeclarations.values()){
               super.unregisterImportDeclaration(declaration);
        }

    }

    public NetworkTopologyDiscovery newNetworkTopologyDiscovery() {
        return new NetworkTopology();
    }

    public void serviceAdded(ServiceEvent event) {
        getLogger().info("adding declaration for the mDNS/DNSsd service {}", event.getName());

        createImportationDeclaration(event);

    }

    public void serviceRemoved(ServiceEvent event) {

        getLogger().info("removing declaration for the mDNS/DNSsd service {}",event.getName());

        ImportDeclaration declaration=importDeclarations.remove(event.getName());
        if(declaration!=null) {
            unregisterImportDeclaration(declaration);
            getLogger().info("import declaration removed for the mDNS/DNSsd service {}",event.getName());
        }else {
            getLogger().info("Impossible to remove declaration {}, it was not registered by the discovery",event.getName());
        }

    }

    public void serviceResolved(ServiceEvent event) {
        getLogger().info("resolving declaration for the mDNS/DNSsd service {}", event.getName());
        getLogger().warn("no action implemented for this kind of event");
    }

    public String getName() {
        return "mDNSDiscovery";
    }

    private synchronized void createImportationDeclaration(ServiceEvent event) {

        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("discovery.mdns.device.name", event.getName());

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        registerImportDeclaration(declaration);

        importDeclarations.put(event.getName(),declaration);

    }

    private void configureNetworkCardServiceListener(){
        //Attach listener service to all network card fetched by the network topology
        for (InetAddress address : NetworkTopologyDiscovery.Factory
                .getInstance().getInetAddresses()) {

            try {
                JmDNS current = JmDNS.create(address);

                current.addServiceListener(dnssdServiceType, this);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
