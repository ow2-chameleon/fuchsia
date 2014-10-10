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

/**
 * Corresponds to the DPT 1.001
 * @author Jander Nascimento
 */
public interface Switch extends KNXDevice {

    /**
     * Switch ON the device
     */
    public void on();

    /**
     * Switch OFF the device
     */
    public void off();

    /**
     * Query if the device is ON
     * @return true case the device is ON, false otherwise
     */
    public boolean isOn();

    /**
     * Query if the device is OFF
     * @return true case the device is OFF, false otherwise
     */
    public boolean isOff();

}
