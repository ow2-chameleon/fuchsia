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
package org.ow2.chameleon.fuchsia.importer.knx.device.iface;

import org.ow2.chameleon.fuchsia.importer.knx.device.DPT;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessEvent;

/**
 * Interface implemented by all KNX devices supported by Fuchsia
 */
public interface KNXDevice {

    /**
     * KNX device identifier
     * @return
     */
    public String getId();

    /**
     * Data Point Type supported by the device
     * @return
     */
    public DPT getDPT();

    /**
     * ProcessCommunicator a network link that allows to send and receive message from/to the bus
     * @return
     */
    public ProcessCommunicator getPc();

    /**
     * Return a String that represets the KNX Group Address of the device
     * @return
     */
    public String getGroupaddr();

    /**
     * This method is invoked when a new message targets the GroupAddress of this device
     * @param e Contains the source, destination and the payload of the message
     */
    public void messageReceived(ProcessEvent e);

    /**
     * This method is invoked when a new message is sent by the GroupAddress of this device
     * @param e Contains the source, destination and the payload of the message
     */
    public void messageSent(ProcessEvent e);

}
