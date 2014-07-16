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

import org.ow2.chameleon.fuchsia.importer.knx.device.iface.KNXDevice;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.process.ProcessCommunicator;

/**
 * Created by adele on 11/07/14.
 */
public abstract class KNXDeviceAbstract implements KNXDevice {

    private Datapoint dp;
    private ProcessCommunicator pc;
    private String groupaddr;

    public final Datapoint getDataPoint() {
        return dp;
    }

    public final void setDataPoint(Datapoint dp){
       this.dp=dp;
    }

    public final ProcessCommunicator getPc() {
        return pc;
    }

    public final void setPc(ProcessCommunicator pc) {
        this.pc = pc;
    }

    private final void createDataPoint(){

        if(getDataPoint()==null) {

            try {
                GroupAddress main = new GroupAddress(getGroupaddr());
                Datapoint dp = new StateDP(main, "", 0, getDPT().getDPTID());
                setDataPoint(dp);
            } catch (KNXFormatException e) {
                e.printStackTrace();
            }

        }

    }

    public final void setGroupaddr(String groupaddr) {
        this.groupaddr = groupaddr;
        createDataPoint();
    }

    public final String getGroupaddr() {
        return groupaddr;
    }
}
