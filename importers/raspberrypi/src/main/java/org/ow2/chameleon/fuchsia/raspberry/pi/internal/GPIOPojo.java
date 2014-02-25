package org.ow2.chameleon.fuchsia.raspberry.pi.internal;


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

    public static GPIOPojo create(Map<String, Object> metadata) {
        GPIOPojo pojo = new GPIOPojo();
        pojo.id = metadata.get("id").toString();
        pojo.pin = metadata.get("importer.gpio.pin").toString();
        pojo.name = metadata.get("importer.gpio.name").toString();

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
