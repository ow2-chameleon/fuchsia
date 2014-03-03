package org.ow2.chameleon.fuchsia.raspberry.pi.internal;


import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

import java.util.Map;

/**
 * Makes easier to access the Declaration metadata
 */
public class GPIOPojo {

    private String id;
    private String pin;
    private String name;

    private GPIOPojo() {

    }

    public static GPIOPojo create(Map<String, Object> metadata) throws BinderException {

        Object id=metadata.get("id");
        Object pin=metadata.get("importer.gpio.pin");
        Object name=metadata.get("importer.gpio.name");

        if(id==null ||pin==null||name==null){
            throw new BinderException("Not enough information provided to create GPIO Importer");
        }

        GPIOPojo pojo = new GPIOPojo();

        pojo.id = id.toString();
        pojo.pin = pin.toString();
        pojo.name = name.toString();

        return pojo;
    }

    public String getId() {
        return id;
    }

    public String getPin() {
        return pin;
    }

    public String getName() {
        return name;
    }
}
