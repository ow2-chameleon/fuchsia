package org.ow2.chameleon.fuchsia.examples.filebased.config;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Example FileBased Configuration
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
public class FileBasedInitializer {

    //This configuration starts a discovery to detect all local printer based on DNSSD/mDNS protocol
    Instance fileBasedDiscoveryExport = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryExport")
            .named("FileBasedDiscoveryExport");

    Instance fileBasedDiscoveryImport = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImport")
            .named("FileBasedDiscoveryImport");

}
