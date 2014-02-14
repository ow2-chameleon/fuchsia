package org.ow2.chameleon.fuchsia.raspberry.pi;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.impl.PinImpl;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.BundleContext;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

@Component(immediate = true)
@Instantiate
@Provides
public class GPIOGogoCommand {

    @ServiceProperty(name = "osgi.command.scope", value = "pi")
    String m_scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[]{"gpio"};


    private BundleContext m_context = null;

    public GPIOGogoCommand(BundleContext context) {
        this.m_context = context;
    }

    @Descriptor  (value = "Access raspberry pi gpio")
    public void gpio(@Descriptor("[-w WIRING_PI -v VALUE | -r WIRING_PI]") String... parameters){

        final GpioController io = GpioFactory.getInstance();

        String read=getArgumentValue("-r",parameters);
        String write=getArgumentValue("-w", parameters);

        if(read!=null){

            System.out.println("reading from Wiring PI "+read);

            Pin pin = new PinImpl(RaspiGpioProvider.NAME, Integer.valueOf(read), "GPIO "+read,
                    EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                    PinPullResistance.all());

            GpioPinDigitalInput bottom = io.provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN);

            System.out.println("value:"+bottom.getState().getValue());

        } else {

            System.out.println("writing in Wiring PI "+write);

            Integer value=Integer.valueOf(getArgumentValue("-v",parameters));

            Pin pin = new PinImpl(RaspiGpioProvider.NAME, Integer.valueOf(write), "GPIO "+write,
                    EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                    PinPullResistance.all());

            if(value.equals(0)) io.provisionDigitalOutputPin(pin).low();
            else io.provisionDigitalOutputPin(pin).high();

        }


    }

    private String getArgumentValue(String option, String... params) {

        boolean found = false;
        String value = null;

        for (int i = 0; i < params.length; i++) {
            if (i < (params.length - 1) && params[i].equals(option)) {
                found = true;
                value = params[i + 1];
                break;
            }
        }

        if (found)
            return value;

        return null;
    }

}

