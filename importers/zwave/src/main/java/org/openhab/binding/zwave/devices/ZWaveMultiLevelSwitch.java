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
package org.openhab.binding.zwave.devices;

/**
 * Provides an abstraction for accessing ZWave Switch device
 * @Author Jander Botelho do Nascimento (botelho@imag.fr)
 */
public interface ZWaveMultiLevelSwitch {

    /**
     * Set to the minimum value acceptable by the device
     */
    public void minimum();

    /**
     * Set for the next higher value
     */
    public void stepUp();

    /**
     * Retrieves the minimum value
     * @return
     */
    public Integer getMinimumValue();

    /**
     * Retrieves the Maximum value
     * @return
     */
    public Integer getMaximumValue();

    /**
     * Set a custom value
     * @param value
     */
    public void setValue(Integer value);

    /**
     * Get the current value
     * @return
     */
    public Integer getValue();

    /**
     * Set for the next lower value
     */
    public void stepDown();

    /**
     * set the maximum value
     */
    public void maximum();

}
