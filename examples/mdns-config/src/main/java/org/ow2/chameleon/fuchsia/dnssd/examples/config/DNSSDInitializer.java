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
            .of("DNSSDDiscoveryFactory")
            .named("DNSSDDiscovery")
            .with("dnssd.service.type").setto("_printer._tcp.local.");

    Instance pushLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .named("UPnPDeviceLinker")
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(discovery.mdns.device.name=*)")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=UPnPDeviceImporter)");

}
