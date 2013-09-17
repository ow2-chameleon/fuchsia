package org.ow2.chameleon.fuchsia.fake.device;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Constants;

/**
 * This class represent the factory of a generic fake device
 *
 * @author jeremy.savonet@gmail.com
 */
@Component(name = "fuchsia.GenericFakeDevice")
@Provides(specifications = GenericDevice.class, properties = {@StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION)})
public class GenericFakeDevice implements GenericDevice {

    @ServiceProperty(name = DEVICE_SERIAL_NUMBER, mandatory = true)
    private String serialNumber;

    @ServiceProperty
    private String deviceType;

    @ServiceProperty
    private String deviceSubTYpe;


    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceSubType() {
        return deviceSubTYpe;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}
