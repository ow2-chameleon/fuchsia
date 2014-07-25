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
package org.ow2.chameleon.fuchsia.importer.knx.device.impl;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.fuchsia.importer.knx.device.DPT;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.KNXDevice;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Switch;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.process.ProcessCommunicator;

@Component
@Provides(specifications = {Switch.class,KNXDevice.class})
public class SwitchImpl extends KNXDeviceAbstract implements Switch {

    @Property //(name = "pc",mandatory = false)
    private ProcessCommunicator pc;

    @Property //(name = "groupaddr",mandatory = false)
    private String groupaddr;

    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    @Validate
    public void validate(){
        setPc(pc);
        setGroupaddr(groupaddr);
    }

    public SwitchImpl(ProcessCommunicator pc, String groupaddr){
        setPc(pc);
        setGroupaddr(groupaddr);
    }

    public void on() {
        try {
            System.out.println("invoking switch on");
            getPc().write(getDataPoint(), "on");
        } catch (KNXException e) {
            e.printStackTrace();
        }
    }

    public void off() {
        try {
            System.out.println("invoking switch off");
            getPc().write(getDataPoint(), "off");
        } catch (KNXException e) {
            e.printStackTrace();
        }
    }

    public boolean isOn() {

        String stateReturned;

        try {
            System.out.println("checking switch state");
            stateReturned=getPc().read(getDataPoint());
            System.out.println("returned "+stateReturned);
            return stateReturned.equals("on")?true:false;
        } catch (KNXException e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean isOff() {
        return !isOn();
    }

    public String getId() {
        return name;
    }

    public DPT getDPT() {
        return DPT.SWITCH;
    }

}
