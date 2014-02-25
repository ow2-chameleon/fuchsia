package org.ow2.chameleon.fuchsia.examples.filebased.config;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class FileBasedInitializer {

    //This configuration starts a discovery to detect all local printer based on DNSSD/mDNS protocol
    Instance fileBasedDiscoveryExport = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryExport")
            .named("FileBasedDiscoveryExport");

    Instance fileBasedDiscoveryImport = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImport")
            .named("FileBasedDiscoveryImport");

}
