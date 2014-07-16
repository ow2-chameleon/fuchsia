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
package org.ow2.chameleon.fuchsia.importer.knx.shell;

import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.KNXDevice;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Step;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Switch;
import org.ow2.chameleon.fuchsia.tools.shell.util.FuchsiaGogoUtil;
import org.ow2.chameleon.fuchsia.tools.shell.util.exception.MandatoryArgumentException;

import java.lang.reflect.Method;
import java.util.Set;

@Component(immediate = true)
@Instantiate
@Provides
public class KNXGogoCommand {

    private final BundleContext context;

    @Requires(optional = true, specification = KNXDevice.class)
    private Set<KNXDevice> devices;

    @ServiceProperty(name = "osgi.command.scope", value = "knx")
    private String scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    private String[] function = new String[]{"write","list"};

    public KNXGogoCommand(BundleContext context) {
        this.context = context;
    }

    @Descriptor(value = "List all KNX devices found")
    public void list(@Descriptor("list") String... parameters) {

        StringBuilder sb=new StringBuilder();

        if(devices==null || devices.isEmpty()){

            sb.append(FuchsiaGogoUtil.createASCIIBox("", new StringBuilder("No KNX devices detected")));

        } else {
            for(KNXDevice device: devices){

                StringBuilder deviceStr=new StringBuilder();

                deviceStr.append(String.format("id = %s\n",device.getId()));
                deviceStr.append(String.format("datapoint type (DPT) = %s\n",device.getDPT()));
                deviceStr.append(String.format("group addr = %s\n",device.getGroupaddr()));

                sb.append(FuchsiaGogoUtil.createASCIIBox(device.getId(), deviceStr));

            }
        }

        System.out.println(sb.toString());

    }

    @Descriptor  (value = "Writes commands in a KNX group address")
    public void write(@Descriptor("-id DEVICE_ID -method KNX_METHOD") String... parameters) {

        boolean found=false;

        try {

            String command=FuchsiaGogoUtil.getArgumentValue("-method",Boolean.TRUE,parameters);
            String id=FuchsiaGogoUtil.getArgumentValue("-id",true,parameters);

            if(devices==null || devices.isEmpty()){
                System.out.println("No devices available to command");
                return;
            }

            for(KNXDevice device: devices){

                if(!device.getId().equals(id)){
                    continue;
                }
                found=true;

                System.out.println(String.format("invoking method %s from device %s", command, device.getId()));

                try{

                    if(device instanceof Switch){
                        Switch swi=(Switch)device;
                        invokeMethod(Switch.class,swi,command);
                    }else if(device instanceof Step){
                        Step step=(Step)device;
                        invokeMethod(Step.class,step,command);
                    }

                    System.out.println("Invokation finished.");

                }catch (Exception e){

                    System.out.println(String.format("Invokation failed, reason %s",e.getMessage()));

                }


            }

            if(!found){
                System.out.println(String.format("Device id %s not found",id));
            }

        }catch (MandatoryArgumentException e){

            System.out.println(e.getMessage());

        }

    }

    private void invokeMethod(Class c,Object instance,String command){

        try {
            Method method = c.getMethod(command);
            method.invoke(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
