package org.ow2.chameleon.fuchsia.raspberry.pi.device;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.fuchsia.raspberry.pi.controller.PiController;
import org.ow2.chameleon.fuchsia.tools.proxiesutils.ProxyFacetInvokable;

@Component
@Provides
public class GPIOOutputPinFactory implements ProxyFacetInvokable {

    @Requires
    private transient PiController controller;

    @Property(mandatory = true,immutable = true)
    private int pin;

    public void invoke(String method, Integer transactionID, Object callback, Object... args) {

        invoke(method,args);

    }

    public Object invoke(String method, Object... args) {

        if(method.equals("on")){
            on();
        }else if(method.equals("off")){
            off();
        }

        return null;
    }

    /**
     * Changes the value of the pin to 1 - enable voltage to run on it
     */
    public void on(){

        controller.WritePin(pin, 1);

    }

    /**
     * Changes the value of the pin to 0 - disable voltage to run on it
     */
    public void off(){

        controller.WritePin(pin, 0);

    }
}
