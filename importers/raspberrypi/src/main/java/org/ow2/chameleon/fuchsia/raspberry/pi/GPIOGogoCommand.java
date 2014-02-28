package org.ow2.chameleon.fuchsia.raspberry.pi;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.impl.PinImpl;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.service.command.Descriptor;

import java.util.EnumSet;

@Component(immediate = true)
@Instantiate
@Provides
public class GPIOGogoCommand {

    @ServiceProperty(name = "osgi.command.scope", value = "pi")
    String scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] function = new String[]{"gpio"};


    public GPIOGogoCommand() {

    }

    @Descriptor(value = "Access raspberry pi gpio")
    public void gpio(@Descriptor("[-w WIRING_PI -v VALUE | -r WIRING_PI]") String... parameters) {

        final GpioController io = GpioFactory.getInstance();

        String read = getArgumentValue("-r", parameters);
        String write = getArgumentValue("-w", parameters);

        if (read != null) {

            System.out.println("reading from Wiring PI " + read);

            Pin pin = new PinImpl(RaspiGpioProvider.NAME, Integer.valueOf(read), "GPIO " + read,
                    EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                    PinPullResistance.all());

            GpioPinDigitalInput bottom = io.provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN);

            System.out.println("value:" + bottom.getState().getValue());

        } else {

            System.out.println("writing in Wiring PI " + write);

            Integer value = Integer.valueOf(getArgumentValue("-v", parameters));

            Pin pin = new PinImpl(RaspiGpioProvider.NAME, Integer.valueOf(write), "GPIO " + write,
                    EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                    PinPullResistance.all());

            if (value.equals(0)) {
                io.provisionDigitalOutputPin(pin).low();
            } else {
                io.provisionDigitalOutputPin(pin).high();
            }

        }
    }

    private String getArgumentValue(String option, String... params) {
        for (int i = 0; i < params.length; i++) {
            if (i < (params.length - 1) && params[i].equals(option)) {
                return params[i + 1];
            }
        }
        return null;
    }

}

