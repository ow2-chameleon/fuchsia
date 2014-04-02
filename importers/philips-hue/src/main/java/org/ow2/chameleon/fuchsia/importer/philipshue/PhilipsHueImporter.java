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

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.philipshue.util.PhilipsHueImportDeclarationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class PhilipsHueImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(PhilipsHueImporter.class);

    private final BundleContext context;

    private ServiceReference serviceReference;

    private Map<String,ServiceRegistration> lamps=new HashMap<String, ServiceRegistration>();
    private Map<String,ServiceRegistration> bridges=new HashMap<String, ServiceRegistration>();

    @ServiceProperty(name = "target", value = "(discovery.philips.device.name=*)")
    private String filter;

    public PhilipsHueImporter(BundleContext context) {
        this.context = context;
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }

    @Validate
    public void validate() {
        LOG.info("Philips hue Importer is up and running");
    }

    @Invalidate
    public void invalidate() {

        LOG.info("Cleaning up instances into Philips hue Importer");

        cleanup();

    }

    private void cleanup(){

        for(Map.Entry<String,ServiceRegistration> lampEntry:lamps.entrySet()){
            lamps.remove(lampEntry.getKey()).unregister();
        }

        for(Map.Entry<String,ServiceRegistration> bridgeEntry:bridges.entrySet()){
            bridges.remove(bridgeEntry.getKey()).unregister();
        }

    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("philips hue importer triggered");

        PhilipsHueImportDeclarationWrapper pojo= PhilipsHueImportDeclarationWrapper.create(importDeclaration);

        try {

            FuchsiaUtils.loadClass(context,pojo.getType());

            Dictionary<String, Object> props = new Hashtable<String, Object>();

            ServiceRegistration lampService=context.registerService(pojo.getType(),pojo.getObject(),props);

            importDeclaration.handle(serviceReference);

            lamps.put(pojo.getId(),lampService);

        } catch (ClassNotFoundException e) {
            LOG.error("Failed to load type {}, importing process aborted.", pojo.getType(), e);
        }


    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        PhilipsHueImportDeclarationWrapper pojo= PhilipsHueImportDeclarationWrapper.create(importDeclaration);

        try {
            lamps.remove(pojo.getId()).unregister();
        }catch(IllegalStateException e){
            LOG.error("failed unregistering lamp", e);
        }

        importDeclaration.unhandle(serviceReference);
    }


    public String getName() {
        return this.getClass().getSimpleName();
    }
}

