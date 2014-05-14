package org.ow2.chameleon.fuchsia.importer.mqtt;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer MQTT
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

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Component
@Provides
public class MQTTImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTImporter.class);

    @ServiceProperty(name = "instance.name")
    private String name;

    @ServiceProperty(name = "target", value = "(&(id=*)(mqtt.queue=*)(scope=generic))")
    private String filter;

    @Requires(filter = "(factory.name=org.ow2.chameleon.fuchsia.importer.mqtt.MQTTOutputRouter)")
    Factory jointFactory;

    Map<String,InstanceManager> managedInstances=new HashMap<String, InstanceManager>();

    private ServiceReference serviceReference;

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        try {

            Properties instanceProperties=new Properties();

            for(Map.Entry<String,Object> element:importDeclaration.getMetadata().entrySet()){
                instanceProperties.put(element.getKey(), element.getValue());
            }

            InstanceManager im=(InstanceManager)jointFactory.createComponentInstance(instanceProperties);

            String id=(String)importDeclaration.getMetadata().get(Constants.ID);

            super.handleImportDeclaration(importDeclaration);

            managedInstances.put(id, im);

        } catch (Exception e) {
            throw new BinderException(e);
        }

    }

    @Invalidate
    public void stop(){
        super.stop();
    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        String id=(String)importDeclaration.getMetadata().get(Constants.ID);

        InstanceManager instance=managedInstances.get(id);

        if(instance!=null){
            instance.dispose();
            unhandleImportDeclaration(importDeclaration);
        }else {
            LOG.warn("Failed to destroy managed instance {}, such instance was not registered by this importer",id);
        }

    }

    public String getName() {
        return name;
    }

}
