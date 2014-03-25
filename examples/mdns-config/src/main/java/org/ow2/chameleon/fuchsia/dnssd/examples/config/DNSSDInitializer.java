package org.ow2.chameleon.fuchsia.dnssd.examples.config;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Example DNS-SD Configuration
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

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class DNSSDInitializer {

    //This configuration starts a discovery to detect all local printer based on DNSSD/mDNS protocol
    Instance dnssdDiscovery = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.mdns.DNSSDDiscovery")
            .named("DNSSDDiscovery")
            .with("dnssd.service.type").setto("_printer._tcp.local.");

}
