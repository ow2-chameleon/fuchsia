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

import org.ow2.chameleon.fuchsia.importer.knx.device.exception.RequestFailedException;
import org.ow2.chameleon.fuchsia.importer.knx.device.exception.ValueOutOfTheRangeException;

/**
 * Corresponds to the DPT 5.004
 * @author Jander Nascimento
 */
public interface Percent extends KNXDevice {

    /**
     *
     * @param value, value is a 8bits value, thus it MUST be in the range [0..255]
     * @throws org.ow2.chameleon.fuchsia.importer.knx.device.exception.ValueOutOfTheRangeException in case if the value passed is out of the range
     */
    public void set(Integer value) throws ValueOutOfTheRangeException;

    /**
     * Query the device the actual value set
     * @return the current value set
     */
    public Integer get() throws RequestFailedException;

}
