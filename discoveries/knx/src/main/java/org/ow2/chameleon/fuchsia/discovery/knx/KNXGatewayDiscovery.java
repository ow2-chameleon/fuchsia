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
package org.ow2.chameleon.fuchsia.discovery.knx;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.discovery.knx.listener.KNXGatewaySearchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;

import java.util.*;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component
@Instantiate
public class KNXGatewayDiscovery extends AbstractDiscoveryComponent implements KNXGatewaySearchListener {

    private static final Logger LOG = LoggerFactory.getLogger(KNXGatewayDiscovery.class);
    private Set response= Collections.synchronizedSet(new HashSet<SearchResponse>());
    private List<UpdateGatewaysThreads> threads=new ArrayList<UpdateGatewaysThreads>();

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    public KNXGatewayDiscovery(BundleContext context){
        super(context);
    }

    @Validate
    public void start(){

        LOG.info("Starting KNX gateway discovery..");

        UpdateGatewaysThreads ifaceMonitor=new UpdateGatewaysThreads(response,this);
        threads.add(ifaceMonitor);
        ifaceMonitor.start();

        LOG.info("KNX gateway discovery started");


    }

    @Invalidate
    public void stop(){

        LOG.info("Stopping KNX gateway discovery..");

        for(UpdateGatewaysThreads th:threads){
            try {
                th.setActive(false);
                LOG.info("Waiting thread {} to finish...",th.getName());
                th.join();
                LOG.info("Thread {} to finished",th.getName());
            } catch (InterruptedException e) {
                LOG.warn("Failed waiting for thread {}",th.getName());
            }
        }

        LOG.info("KNX gateway discovery stopped");

    }

    public String getName() {
        return name;
    }

    public void gatewayFound(SearchResponse sr) {

        LOG.info("gateway {} found, registering in fuchsia..",sr.getDevice().getSerialNumberString());

        final String PREFIX="discovery.knx.gateway.%s";

        Map<String, Object> metadata = new HashMap<String, Object>();

        metadata.put("id", "knx-gateway-"+sr.getDevice().getSerialNumberString());
        metadata.put(String.format(PREFIX,"ip"), sr.getControlEndpoint().getAddress().getHostAddress());
        metadata.put(String.format(PREFIX,"port"), sr.getControlEndpoint().getPort());
        metadata.put(String.format(PREFIX,"sn"), sr.getDevice().getSerialNumberString());
        metadata.put(String.format(PREFIX,"macaddr"), sr.getDevice().getSerialNumberString());
        metadata.put(String.format(PREFIX,"installation"), sr.getDevice().getInstallation());
        metadata.put(String.format(PREFIX,"project.number"), sr.getDevice().getProject());
        metadata.put(String.format(PREFIX,"project.name"), sr.getDevice().getProjectInstallID());

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        registerImportDeclaration(declaration);

        LOG.info("KNX gateway {} registered.",metadata.get("ID"));
    }
}

