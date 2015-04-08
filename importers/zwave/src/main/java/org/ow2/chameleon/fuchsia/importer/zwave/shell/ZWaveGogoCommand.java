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
package org.ow2.chameleon.fuchsia.importer.zwave.shell;

import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.ow2.chameleon.fuchsia.importer.zwave.ZWaveImporter;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.SerialInterfaceException;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveController;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveEndpoint;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Collection;

import static org.ow2.chameleon.fuchsia.tools.shell.util.FuchsiaGogoUtil.getArgumentValue;

/**
 * Provides an abstraction for accessing ZWave Switch device
 * @Author Jander Botelho do Nascimento (botelho@imag.fr)
 */
@Component(immediate = true)
@Instantiate
@Provides
public class ZWaveGogoCommand {

    //@Requires(optional = true)
    //ZWaveImporterIface importer;

    @ServiceProperty(name = "osgi.command.scope", value = "zwave")
    private String scope;

    @Context
    BundleContext context;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    private String[] function = new String[]{"set"};

    @Descriptor(value = "Set value for a ZWave Device")
    public void set(@Descriptor("set value for a Z-Wave device") String... parameters) throws SerialInterfaceException {

        String port=null;
        String endpointStr=null;
        String nodeStr=null;
        String valueStr = getArgumentValue("-value", parameters);

        try {


            String id = getArgumentValue("-id", parameters);

            String filter=String.format("(importer.id=%s)", id);

            System.out.println("Looking for factories that match the filter "+filter);

            Collection<ServiceReference<ImportDeclaration>> srs=context.getServiceReferences(ImportDeclaration.class,filter);

            if(srs!=null && srs.size()>0){

                if(srs.size()>1){
                    System.out.println(String.format("More than one provider was found (%i to be exact), one will be picked up randomly ",srs.size()));
                }

                System.out.println(String.format("%s service(s) found  for id %s", srs.size(),id));
                ServiceReference<ImportDeclaration> sr=srs.iterator().next();

                ImportDeclaration fact=context.getService(sr);
                endpointStr=fact.getMetadata().get("endpoint").toString();
                port=fact.getMetadata().get("port").toString();
                nodeStr=fact.getMetadata().get("node").toString();

                System.out.println(String.format("Sending port %s, endpoint %s, node %s, value %s",port,endpointStr,nodeStr,valueStr));

                ZWaveController controller= ZWaveImporter.getControllers().get(port);

                ZWaveEndpoint endpoint=new ZWaveEndpoint(Integer.parseInt(endpointStr));
                ZWaveNode node=controller.getNode(Integer.parseInt(nodeStr));

                //ZWaveMultiLevelSwitchCommandClass c=new ZWaveMultiLevelSwitchCommandClass(node,controller,endpoint);
                controller.sendValue(node.getNodeId(), endpoint.getEndpointId(), Integer.parseInt(valueStr));

            }else {
                System.out.println("No services found as factory the device ID");
            }

            /**
             port = getArgumentValue("-port", parameters);
             endpointStr = getArgumentValue("-endpoint", parameters);
             nodeStr = getArgumentValue("-node", parameters);
            **/

        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
