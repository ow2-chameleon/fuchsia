package org.ow2.chameleon.fuchsia.discovery.mdns;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Discovery mDNS
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.discovery.mdns.topology.NetworkTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@Component
@Provides(specifications = DiscoveryService.class)
public class DNSSDDiscovery extends AbstractDiscoveryComponent implements NetworkTopologyDiscovery.Factory.ClassDelegate, ServiceListener {

    private static final Logger LOG = LoggerFactory.getLogger(DNSSDDiscovery.class);

    private final Map<String, ImportDeclaration> importDeclarations = new HashMap<String, ImportDeclaration>();

    @Property(name = "dnssd.service.type", value = MDNSConstants.DNSSD_SERVICE_TYPE)
    private String dnssdServiceType;

    @Property(name = "dnssd.service.name")
    private String dnssdServiceName;

    protected DNSSDDiscovery(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Validate
    public void start() {

        NetworkTopologyDiscovery.Factory.setClassDelegate(this);

        configureNetworkCardServiceListener();

    }

    @Invalidate
    public void stop() {

        LOG.info("stopping service of mDNS/DNSsd discovery");

        for (ImportDeclaration declaration : importDeclarations.values()) {
            super.unregisterImportDeclaration(declaration);
        }

    }

    public NetworkTopologyDiscovery newNetworkTopologyDiscovery() {
        return new NetworkTopology();
    }

    public void serviceAdded(ServiceEvent event) {
        LOG.info("adding declaration for the mDNS/DNSsd service {}", event.getName());

        createImportationDeclaration(event);

    }

    public void serviceRemoved(ServiceEvent event) {

        LOG.info("removing declaration for the mDNS/DNSsd service {}", event.getName());

        ImportDeclaration declaration = importDeclarations.remove(event.getName());
        if (declaration != null) {
            unregisterImportDeclaration(declaration);
            LOG.info("import declaration removed for the mDNS/DNSsd service {}", event.getName());
        } else {
            LOG.info("Impossible to remove declaration {}, it was not registered by the discovery", event.getName());
        }

    }

    public void serviceResolved(ServiceEvent event) {
        LOG.info("resolving declaration for the mDNS/DNSsd service {}", event.getName());
        LOG.warn("no action implemented for this kind of event");
    }

    public String getName() {
        return "mDNSDiscovery";
    }

    private synchronized void createImportationDeclaration(ServiceEvent event) {

        Map<String, Object> metadata = new HashMap<String, Object>();

        StringBuilder bufAddress = new StringBuilder();

        String[] addresses = event.getInfo().getHostAddresses();
        if (addresses.length > 0) {
            for (String address : addresses) {
                bufAddress.append(address);
                bufAddress.append(':');
                bufAddress.append(event.getInfo().getPort());
                bufAddress.append(' ');
            }
        } else {
            bufAddress.append("(null):");
            bufAddress.append(event.getInfo().getPort());
        }

        StringBuilder bufProperty = new StringBuilder();

        Enumeration<String> propertiesNameEnum = event.getInfo().getPropertyNames();

        while (propertiesNameEnum.hasMoreElements()) {
            String name = propertiesNameEnum.nextElement();
            bufProperty.append(name + "=" + event.getInfo().getPropertyString(name) + ",");
        }

        metadata.put("id", event.getName());
        metadata.put("discovery.mdns.device.name", event.getName());

        ServiceInfo serviceInfo=event.getDNS().getServiceInfo(dnssdServiceType, dnssdServiceName);
        String hosts[]=serviceInfo.getHostAddresses();

        if(hosts!=null && hosts.length>0)
            metadata.put("discovery.mdns.device.host", hosts[0]);

        metadata.put("discovery.mdns.device.port", serviceInfo.getPort());

        metadata.put("discovery.mdns.device.properties", bufProperty.toString());
        metadata.put("scope", "generic");

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        registerImportDeclaration(declaration);

        importDeclarations.put(event.getName(), declaration);

    }

    private void configureNetworkCardServiceListener() {
        //Attach listener service to all network card fetched by the network topology
        for (InetAddress address : NetworkTopologyDiscovery.Factory
                .getInstance().getInetAddresses()) {

            try {
                JmDNS current = JmDNS.create(address);

                current.addServiceListener(dnssdServiceType, this);

            } catch (IOException e) {
                LOG.error("Failed to publish in mDNS", e);
            }

        }
    }

}
