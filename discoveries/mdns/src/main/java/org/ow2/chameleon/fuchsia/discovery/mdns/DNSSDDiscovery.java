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
import java.net.Inet4Address;
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

    @Property(name = "dnssd.service.marker",value = "")
    private String dnssdServiceMarker;

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

        if(!importDeclarations.containsKey(event.getName())){
            createImportationDeclaration(event);
        }else {
            LOG.warn("Service already registered under the name of {}, skipping declaration registration",event.getName());
        }
    }

    public void serviceRemoved(ServiceEvent event) {

        LOG.info("removing declaration for the mDNS/DNSsd service named {}", event.getName());

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

        if(!importDeclarations.containsKey(event.getName())){
            createImportationDeclaration(event);
        }else {
            LOG.warn("Service already registered under the name of {}, skipping declaration registration",event.getName());
        }
    }

    public String getName() {
        return "mDNSDiscovery";
    }

    private synchronized void createImportationDeclaration(ServiceEvent event) {

        ServiceInfo serviceInfo=event.getInfo();

        String instanceName=serviceInfo.getQualifiedNameMap().get(ServiceInfo.Fields.Instance);

        if(serviceInfo!=null && instanceName!=null && instanceName.equals(dnssdServiceName)){

            ServiceInfo serviceDetail=event.getDNS().getServiceInfo(dnssdServiceType, dnssdServiceName);

            Map<String, Object> metadata = new HashMap<String, Object>();

            metadata.put("id", event.getName());
            metadata.put("discovery.mdns.device.name", event.getName());
            metadata.put("discovery.mdns.device.port", serviceDetail.getPort());
            metadata.put("discovery.mdns.device.marker",dnssdServiceMarker);
            metadata.put("scope", "generic");

            String[] hosts=serviceDetail.getHostAddresses();

            final String suffixModel="discovery.mdns.device.host%s";

            for(int x=0;x<hosts.length;x++){
                String suffix=x==0?"":"."+String.valueOf(x);
                metadata.put(String.format(suffixModel,suffix),hosts[x]);
            }

            String txData=new String(serviceInfo.getTextBytes());

            if(txData.trim().length()!=0)
                metadata.put("discovery.mdns.device.txdata",txData);

            synchronized (importDeclarations){

                ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

                registerImportDeclaration(declaration);

                importDeclarations.put(event.getName(), declaration);

            }

        }

    }

    private void configureNetworkCardServiceListener() {
        //Attach listener service to all network card fetched by the network topology
        for (InetAddress address : NetworkTopologyDiscovery.Factory
                .getInstance().getInetAddresses()) {

            try {

                if(address instanceof Inet4Address){

                    JmDNS current = JmDNS.create((Inet4Address)address);
                    current.addServiceListener(dnssdServiceType, this);

                }
            } catch (IOException e) {
                LOG.error("Failed to publish in mDNS", e);
            }

        }
    }

}
