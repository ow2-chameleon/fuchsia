/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2015 OW2 Chameleon
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
package org.ow2.chameleon.fuchsia.importer.zwave;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.SerialInterfaceException;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveController;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component
@Provides(specifications = {ImporterService.class,ImporterIntrospection.class,ZWaveImporterIface.class})
public class ZWaveImporter extends AbstractImporterComponent implements ZWaveImporterIface {

    private static final Logger LOG = LoggerFactory.getLogger(ZWaveImporter.class);

    private final BundleContext context;

    private static final Map<String, ZWaveController> controllers=new HashMap<String, ZWaveController>();

    @ServiceProperty(name = "target", value = "(&(port=*)(scope=generic)(node=*)(endpoint=*))")
    private String filter;

    @Requires(filter="(factory.name=org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveController)")
    private Factory controllerFactory;

    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    public ZWaveImporter(BundleContext context) {
        this.context = context;
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        ZWavePojo data=ZWavePojo.create(importDeclaration);

        ZWaveController controller=controllers.get(data.getPort());

        if(controller==null){
            try {
                controller=new ZWaveController(data.getPort());
                controller.initialize();
/*
                Dictionary<String,String> hs=new Hashtable<String, String>();
                hs.put("serialPortName",data.getPort());
                InstanceManager im= (InstanceManager) controllerFactory.createComponentInstance(hs);
                controller=(ZWaveController)im.getPojoObject();
*/
                controllers.put(data.getPort(),controller);
            } catch (SerialInterfaceException e) {
                e.printStackTrace();
            }
        }

        if(data.getType()!=null){
            startupDevice(data);
        }

        super.handleImportDeclaration(importDeclaration);

    }

    private void startupDevice(ZWavePojo data){

        String filter="";

        Hashtable<String, Object> hs = new Hashtable<String, Object>();

        try {

            hs.put("endpoint", data.getEndpoint());
            hs.put(Factory.INSTANCE_NAME_PROPERTY, data.getId() + "ZWaveDevice");
            hs.put("node",data.getNode());

        } catch (Exception e) {
            LOG.error("Failed to connect to the KNX Bus, ignoring connection and creating object",e);
        }

        try{
            ComponentInstance ci = null;

            filter=String.format("(&(protocol=zwave)(type=%s))", data.getType());

            LOG.info("Looking for factories that match the filter '{}'",filter);

            Collection<ServiceReference<Factory>> srs=context.getServiceReferences(Factory.class,filter);

            if(srs!=null && srs.size()>0){

                if(srs.size()>1){
                    LOG.warn("More than one provider was found ({} to be exact), one will be picked up randomly",srs.size());
                }

                LOG.debug("{} service(s) found as factory for {}",srs.size(),data.getType());
                ServiceReference<Factory> sr=srs.iterator().next();

                Factory fact=context.getService(sr);

                ci=fact.createComponentInstance(hs);

            }else {
                LOG.debug("No services found as factory the device ID", data.getId());
            }

        } catch (InvalidSyntaxException e) {
            LOG.error("Fuchsia internal error. Invalid service filter", filter,e);
        } catch (Exception e) {
            LOG.error("Fuchsia internal error.",e);
        }

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {
        unhandleImportDeclaration(importDeclaration);
    }

    public String getName() {
        return name;
    }

    public static Map<String, ZWaveController> getControllers() {
        return controllers;
    }
}
