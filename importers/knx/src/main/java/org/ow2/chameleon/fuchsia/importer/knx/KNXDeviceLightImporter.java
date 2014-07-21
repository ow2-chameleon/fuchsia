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
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.knx.dao.KNXDeclaration;
import org.ow2.chameleon.fuchsia.importer.knx.util.KNXLinkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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

    private Map<String,ComponentInstance> instances=new HashMap<String,ComponentInstance>();

    @Requires
    private KNXLinkManager linkManager;

    private String getDPT(String dpt) {
        if (dpt.equals("switch"))
            return "1.001";
        if (dpt.equals("bool"))
            return "1.002";
        if (dpt.equals("string"))
            return "16.001";
        if (dpt.equals("float"))
            return "9.002";
        if (dpt.equals("ucount"))
            return "5.010";
        if (dpt.equals("angle"))
            return "5.003";
        return dpt;
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        KNXDeclaration knxInput = KNXDeclaration.create(importDeclaration);
        Hashtable<String, Object> hs = new Hashtable<String, Object>();

        try {

            hs.put("groupaddr", knxInput.getKnxAddress());
            hs.put(Factory.INSTANCE_NAME_PROPERTY, knxInput.getId());

            //final KNXNetworkLink lnk = createLink(knxInput.getLocalhost(),knxInput.getGateway());

            LOG.info("Connecting from terminal {} to gateway {}",knxInput.getLocalhost(),knxInput.getGateway());

            KNXNetworkLink lnk = linkManager.getLink(knxInput.getLocalhost(), knxInput.getGateway());

            ProcessCommunicator pc = new ProcessCommunicatorImpl(lnk);

            hs.put("pc", pc);

            //GroupAddress main = new GroupAddress(knxInput.getKnxAddress());

            //Datapoint dp = new StateDP(main, knxInput.getId(), 0, getDPT(knxInput.getDpt()));

            //Switch mySwitch=new SwitchImpl(pc,dp);

            /**
             System.out.println("write successful");
             pc.write(dp, "on");
             Thread.sleep(2000);
             pc.write(dp, "off");
             **/

        } catch (Exception e) {
            LOG.error("Failed to connect to the KNX Bus, ignoring connection and creating object",e);
        }

        try{
            ComponentInstance ci = null;

            if(knxInput.getDpt().equals("switch")){
                ci=factorySwitch.createComponentInstance(hs);
            }else if(knxInput.getDpt().equals("step")){
                ci=factoryStep.createComponentInstance(hs);
            }


            instances.put(knxInput.getId(),ci);
        }catch (UnacceptableConfiguration unacceptableConfiguration) {
            unacceptableConfiguration.printStackTrace();
        } catch (MissingHandlerException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
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
