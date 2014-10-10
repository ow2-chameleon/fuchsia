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
import org.ow2.chameleon.fuchsia.importer.knx.device.exception.RequestFailedException;
import org.ow2.chameleon.fuchsia.importer.knx.device.exception.ValueOutOfTheRangeException;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.KNXDevice;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Percent;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.UCount;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.process.ProcessCommunicator;

@Component
@Provides(specifications = {Percent.class,KNXDevice.class},
        properties = { @StaticServiceProperty(name = "type", type="java.lang.String", value = "percent", immutable = true),
                @StaticServiceProperty(name = "protocol", type="java.lang.String", value = "knx", immutable = true)})
public class PercentImpl extends KNXDeviceAbstract implements UCount{

    @ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
    private String name;

    public String getId() {
        return name;
    }

    public DPT getDPT() {
        return DPT.PERCENT;
    }

    @Property
    public void setPc(ProcessCommunicator pc) {
        super.setPc(pc);
    }

    @Property
    public void setGroupaddr(String groupaddr) {
        super.setGroupaddr(groupaddr);
    }

    public void set(Integer value) throws ValueOutOfTheRangeException{
        if(value<0 || value>255){
            throw new ValueOutOfTheRangeException(String.format("For the datatype %s, the range is [0,255]",getDPT()));
        }
        try {
            getPc().write(getDataPoint(),value.toString());
        } catch (KNXException e) {
            LOG.warn("Failed to send value {} to the device {}",value,getGroupaddr());
        }
    }

    public Integer get() throws RequestFailedException {

        try {
            String stateReturned=getPc().read(getDataPoint());
            return Integer.valueOf(stateReturned);
        } catch (KNXException e) {
            LOG.warn("Failed to get value from the device {}", getGroupaddr());
            throw new RequestFailedException(e);
        }

    }
}
