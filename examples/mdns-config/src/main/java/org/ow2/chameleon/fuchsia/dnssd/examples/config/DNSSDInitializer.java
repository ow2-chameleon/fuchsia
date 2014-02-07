package org.ow2.chameleon.fuchsia.dnssd.examples.config;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;

import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.*;

@Configuration
public class DNSSDInitializer {

    //This configuration starts a discovery to detect all local printer based on DNSSD/mDNS protocol
    Instance dnssdDiscovery = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.mdns.DNSSDDiscovery")
            .named("DNSSDDiscovery")
            .with("dnssd.service.type").setto("_printer._tcp.local.");

}
