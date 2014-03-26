package org.ow2.chameleon.fuchsia.raspberry.pi.controller;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer Raspberry Pi GPIO
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.impl.PinImpl;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


/**
 * Provides a singleton access to the device, as specified in the pi4j api, this object should be unique and shared.
 * among the application to access the device pin
 *
 * @author Jander Nascimento <botelho@imag.fr>
 */
@Component(publicFactory = false)
@Instantiate
@Provides
public class PiController {

    private final GpioController GPIO_CONTROLLER = GpioFactory.getInstance();

    private Map<Integer, Pin> pins = new HashMap<Integer, Pin>();
    private Map<Integer, GpioPinDigitalMultipurpose> pinMp = new HashMap<Integer, GpioPinDigitalMultipurpose>();

    public int readPin(int pinum) {
        return fetchMp(pinum).getState().getValue();
    }

    public void writePin(int pinInt, int value) {
        fetchMp(pinInt).setState(value != 0);
    }

    private Pin fetchPin(int pinInt) {
        if (!pins.containsKey(pinInt)) {
            Pin pin = new PinImpl(RaspiGpioProvider.NAME, pinInt, "GPIO " + pinInt,
                    EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                    PinPullResistance.all());
            pins.put(pinInt, pin);
        }
        return pins.get(pinInt);
    }

    private GpioPinDigitalMultipurpose fetchMp(int pinInt) {
        if (!pins.containsKey(pinInt)) {
            GpioPinDigitalMultipurpose pin = GPIO_CONTROLLER.provisionDigitalMultipurposePin(fetchPin(pinInt), PinMode.ANALOG_INPUT);
            pinMp.put(pinInt, pin);
        }

        return pinMp.get(pinInt);
    }

    @Invalidate
    public void release() {
        GPIO_CONTROLLER.shutdown();
        GPIO_CONTROLLER.getProvisionedPins().removeAll(pins.values());
    }

}
