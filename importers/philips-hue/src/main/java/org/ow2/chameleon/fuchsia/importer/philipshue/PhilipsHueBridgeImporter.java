package org.ow2.chameleon.fuchsia.importer.philipshue;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer Philips Hue
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

import com.philips.lighting.hue.sdk.bridge.impl.PHBridgeImpl;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.philipshue.util.PhilipsHueBridgeImportDeclarationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class PhilipsHueBridgeImporter extends AbstractImporterComponent {

    private final Logger LOG = LoggerFactory.getLogger(PhilipsHueBridgeImporter.class);

    private final BundleContext context;

    Timer timer;

    private ServiceReference serviceReference;

    private Map<String,ServiceRegistration> lamps=new HashMap<String, ServiceRegistration>();
    private Map<String,ServiceRegistration> bridges=new HashMap<String, ServiceRegistration>();

    @ServiceProperty(name = "target", value = "(discovery.philips.bridge.type=*)")
    private String filter;

    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    public PhilipsHueBridgeImporter(BundleContext context) {
        this.context = context;
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Validate
    public void validate() {
        timer=new Timer();
        LOG.info("Philips hue Importer is up and running");
    }

    @Invalidate
    public void invalidate() {

        LOG.info("Cleaning up instances into Philips hue Importer");

        cleanup();

    }

    private void cleanup(){

        timer.cancel();

        for(Map.Entry<String,ServiceRegistration> bridgeEntry:bridges.entrySet()){
            bridges.remove(bridgeEntry.getKey()).unregister();
        }

        for(Map.Entry<String,ServiceRegistration> bridgeEntry:lamps.entrySet()){
            lamps.remove(bridgeEntry.getKey()).unregister();
        }

    }

    @Override
    protected void useImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("philips hue bridge importer triggered");

        PhilipsHueBridgeImportDeclarationWrapper pojo= PhilipsHueBridgeImportDeclarationWrapper.create(importDeclaration);

        Dictionary<String, Object> props = new Hashtable<String, Object>();

        ServiceRegistration bridgeService=context.registerService(new String[]{PHBridge.class.getName(),PHBridgeImpl.class.getName()},pojo.getBridgeObject(),props);

        timer.schedule(new FetchBridgeLampsTask((PHBridgeImpl) pojo.getBridgeObject()),0,5000);

        super.handleImportDeclaration(importDeclaration);

        bridges.put(pojo.getId(),bridgeService);

    }

    @Override
    protected void denyImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("philips hue bridge importer removal triggered");

        PhilipsHueBridgeImportDeclarationWrapper pojo= PhilipsHueBridgeImportDeclarationWrapper.create(importDeclaration);

        try {

            for(Map.Entry<String,ServiceRegistration> entry:lamps.entrySet()){
                ServiceRegistration sr=lamps.remove(entry.getKey());
                if(sr!=null) {
                    sr.unregister();
                }
            }

        }catch(IllegalStateException e){
            LOG.error("failed unregistering lamp", e);
        }

        try {
            ServiceRegistration sr=bridges.remove(pojo.getId());
            if(sr!=null) {
                sr.unregister();
            };
        }catch(IllegalStateException e){
            LOG.error("failed unregistering bridge", e);
        }

        unhandleImportDeclaration(importDeclaration);
    }


    public String getName() {
        return name;
    }

    class FetchBridgeLampsTask extends TimerTask {

        private PHBridge bridge;

        public FetchBridgeLampsTask(PHBridge bridge){
            this.bridge=bridge;
        }

        @Override
        public void run() {

            for(PHLight light:bridge.getResourceCache().getAllLights()){

                if(!light.isReachable()){

                    ServiceRegistration sr=lamps.remove(light.getIdentifier());
                    if(sr!=null)
                        sr.unregister();

                }else if(!lamps.keySet().contains(light.getIdentifier())){

                    Map<String, Object> metadata = new HashMap<String, Object>();

                    metadata.put("id", light.getIdentifier());
                    metadata.put("discovery.philips.device.name", light.getModelNumber());
                    metadata.put("discovery.philips.device.type", light.getClass().getName());
                    metadata.put("discovery.philips.device.object", light);

                    Dictionary metatableService=new Hashtable(metadata);

                    ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

                    ServiceRegistration sr=context.registerService(ImportDeclaration.class,declaration,metatableService);

                    if(PhilipsHueBridgeImporter.this.lamps.containsKey(light.getIdentifier())){
                        LOG.warn("Lamp with identifier {} alreadu exists",light.getIdentifier());
                    }

                    PhilipsHueBridgeImporter.this.lamps.put(light.getIdentifier(),sr);

                }
            }
        }
    }
}

