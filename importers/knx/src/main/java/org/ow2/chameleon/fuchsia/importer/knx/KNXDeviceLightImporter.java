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
package org.ow2.chameleon.fuchsia.importer.knx;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.knx.dao.KNXDeclaration;
import org.ow2.chameleon.fuchsia.importer.knx.util.KNXLinkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;

import javax.xml.parsers.FactoryConfigurationError;
import java.util.*;

@Component
@Provides
public class KNXDeviceLightImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(KNXDeviceLightImporter.class);

    @ServiceProperty(name = "target", value = "(discovery.knx.device.addr=*)")
    private String filter;

    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        setServiceReference(serviceReference);
    }

    @Requires(filter = "(factory.name=org.ow2.chameleon.fuchsia.importer.knx.device.impl.SwitchImpl)", optional = false)
    Factory factorySwitch;

    @Requires(filter = "(factory.name=org.ow2.chameleon.fuchsia.importer.knx.device.impl.StepImpl)", optional = false)
    Factory factoryStep;

    private BundleContext context;

    private Map<String,ComponentInstance> instances=new HashMap<String,ComponentInstance>();

    public KNXDeviceLightImporter(BundleContext context){
        this.context=context;
    }

    @Requires
    private KNXLinkManager linkManager;

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        KNXDeclaration knxInput = KNXDeclaration.create(importDeclaration);
        Hashtable<String, Object> hs = new Hashtable<String, Object>();

        try {

            hs.put("groupaddr", knxInput.getKnxAddress());
            hs.put(Factory.INSTANCE_NAME_PROPERTY, knxInput.getId());

            LOG.info("Connecting from terminal {} to gateway {}",knxInput.getLocalhost(),knxInput.getGateway());

            KNXNetworkLink lnk = linkManager.getLink(knxInput.getLocalhost(), knxInput.getGateway());

            ProcessCommunicator pc = new ProcessCommunicatorImpl(lnk);

            hs.put("pc", pc);

        } catch (Exception e) {
            LOG.error("Failed to connect to the KNX Bus, ignoring connection and creating object",e);
        }

        try{
            ComponentInstance ci = null;

            String filter=String.format("(&(protocol=knx)(type=%s))", knxInput.getDpt());

            LOG.info("Looking for factories that match the filter '{}'",filter);

            Collection<ServiceReference<Factory>> srs=context.getServiceReferences(Factory.class,filter);

            if(srs!=null && srs.size()>0){

                if(srs.size()>1){
                    LOG.warn("More than one provider was found ({} to be exact), one will be picked up randomly",srs.size());
                }

                /*
                if(knxInput.getDpt().equals("switch")){
                    ci = factorySwitch.createComponentInstance(hs);
                }else if(knxInput.getDpt().equals("step")) {
                    ci = factoryStep.createComponentInstance(hs);
                }
                */

                LOG.debug("{} services found as factory",srs.size());
                ServiceReference<Factory> sr=srs.iterator().next();

                Factory fact=context.getService(sr);

                ci=fact.createComponentInstance(hs);

                Map<String, Object> metadata = new HashMap<String, Object>();

                metadata.putAll(importDeclaration.getMetadata());
                metadata.put("id", knxInput.getId());
                metadata.put("discovery.knx.device.dpt", knxInput.getDpt());
                metadata.put("discovery.knx.device.object", ((InstanceManager)ci).getPojoObject());

                ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

                Dictionary<String, Object> props = new Hashtable<String, Object>();

                context.registerService(ImportDeclaration.class,declaration,props);

                instances.put(knxInput.getId(),ci);

                super.handleImportDeclaration(importDeclaration);


            }else {
                LOG.debug("No services found as factory the device ID", knxInput.getId());
            }

        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            unacceptableConfiguration.printStackTrace();
        } catch (MissingHandlerException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        KNXDeclaration knxInput = KNXDeclaration.create(importDeclaration);

        ComponentInstance ci=instances.get(knxInput.getId());

        if(ci!=null){
            LOG.info("Removing instance related to {}",knxInput.getId());
            ci.dispose();
        }

        if(instances.size()==0){
            linkManager.disconnect();
        }

    }

    public String getName() {
        return name;
    }
}
