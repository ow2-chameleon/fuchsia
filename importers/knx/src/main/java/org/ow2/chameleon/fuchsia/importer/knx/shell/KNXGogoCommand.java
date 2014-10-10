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
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.*;
import org.ow2.chameleon.fuchsia.tools.shell.util.FuchsiaGogoUtil;
import org.ow2.chameleon.fuchsia.tools.shell.util.exception.MandatoryArgumentException;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Jander Nascimento
 */
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
    private String[] function = new String[]{"invoke","list"};

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

    @Descriptor  (value = "Invoke KNX command KNX into a group address")
    public void invoke(@Descriptor("-id DEVICE_ID -method KNX_METHOD [-value VALUE]") String... parameters) {

        boolean found=false;

        try {

            String id=FuchsiaGogoUtil.getArgumentValue("-id",Boolean.TRUE,parameters);
            String command=FuchsiaGogoUtil.getArgumentValue("-method",Boolean.TRUE,parameters);
            String value=FuchsiaGogoUtil.getArgumentValue("-value",Boolean.FALSE,parameters);

            if(devices==null || devices.isEmpty()){
                System.out.println("No devices available to command");
                return;
            }

            for(KNXDevice device: devices){

                if(!device.getId().equals(id)){
                    continue;
                }
                found=true;

                System.out.println(String.format("Invoking method %s from device %s", command, device.getId()));

                try{

                    Object retval=null;

                    if(device instanceof Switch){
                        Switch swi=(Switch)device;
                        retval=invokeMethod(Switch.class,swi,command);
                    }else if(device instanceof Step){
                        Step step=(Step)device;
                        invokeMethod(Step.class,step,command);
                    }else if(device instanceof UCount){
                        UCount ucount=(UCount)device;
                        retval=invokeMethod(UCount.class,ucount,command,value);
                    }else if(device instanceof Percent){
                        Percent percent=(Percent)device;
                        retval=invokeMethod(Percent.class,percent,command,value);
                    }

                    if(retval!=null){
                        System.out.println(String.format("Returned value: %s",retval.toString()));
                    }

                    System.out.println("Invocation finished.");

                }catch (Exception e){

                    System.out.println(String.format("Invocation failed, reason %s",e.getMessage()));

                }


            }

            if(!found){
                System.out.println(String.format("Device id %s not found",id));
            }

        }catch (MandatoryArgumentException e){

            System.out.println(e.getMessage());

        }

    }

    private Object invokeMethod(Class c,Object instance,String command,Object ... args){

        try {
            Method method = c.getMethod(command);
            Object returnedValue=null;

            if(args.length>0){
                returnedValue=method.invoke(instance,args);
            }else {
                returnedValue=method.invoke(instance);
            }

            return returnedValue;

        } catch (Exception e) {
            System.out.println(String.format("Failed invoking command %s, with the message %s", command, e.getMessage()));
        }

        return null;
    }

}
