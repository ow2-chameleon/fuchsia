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
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Step;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Switch;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.process.ProcessCommunicator;

@Component
@Provides(specifications = {Step.class,KNXDevice.class})
public class StepImpl extends KNXDeviceAbstract implements Step {

    @Property(mandatory = false)
    private ProcessCommunicator pc;

    @Property(mandatory = false)
    private String groupaddr;

    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    @Validate
    public void validate(){
        setPc(pc);
        setGroupaddr(groupaddr);
    }

    public void increase() {
        try {
            LOG.debug("Invoking Step increase");
            getPc().write(getDataPoint(),"increase");
        } catch (KNXException e) {
            e.printStackTrace();
        }
    }

    public void decrease() {
        try {
            LOG.debug("Invoking Step decrease");
            getPc().write(getDataPoint(),"decrease");
        } catch (KNXException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return name;
    }

    public DPT getDPT() {
        return DPT.STEP;
    }
}
