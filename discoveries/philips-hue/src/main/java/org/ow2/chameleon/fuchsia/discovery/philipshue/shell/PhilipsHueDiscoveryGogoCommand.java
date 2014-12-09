/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.fuchsia.discovery.philipshue.shell;

import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.ow2.chameleon.fuchsia.discovery.philipshue.PhilipsDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.ow2.chameleon.fuchsia.tools.shell.util.FuchsiaGogoUtil.getArgumentValue;

@Component(immediate = true)
@Instantiate
@Provides
public class PhilipsHueDiscoveryGogoCommand {

    private static final Logger LOG = LoggerFactory.getLogger(PhilipsHueDiscoveryGogoCommand.class);

    @ServiceProperty(name = "osgi.command.scope", value = "philips")
    private String scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    private String[] function = new String[]{"bridge"};

    @Requires(optional = true)
    private PhilipsDiscoveryService discovery;

    @Descriptor(value = "Sends command to the Philips Bridge API")
    public void bridge(@Descriptor("[search]") String... parameters) {

        String searchCmd = getArgumentValue("search", parameters);

        if(searchCmd!=null){
            discovery.searchForBridges();
            print("search fired.");
        }
    }

    private void print(String message) {
        System.out.println(message);
    }

}
