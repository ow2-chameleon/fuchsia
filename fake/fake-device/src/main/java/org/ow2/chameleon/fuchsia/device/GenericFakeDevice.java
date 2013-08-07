package org.ow2.chameleon.fuchsia.device;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Constants;

/**
 * This class represent a generic fake device
 */
@Component (name = "iCasa.GenericFakeDevice")
@Provides(specifications = GenericDevice.class, properties = { @StaticServiceProperty(type = "java.lang.String", name = Constants.SERVICE_DESCRIPTION) })
//@Instantiate
public class GenericFakeDevice implements GenericDevice {

    @ServiceProperty(name = GenericFakeDevice.DEVICE_SERIAL_NUMBER, mandatory = true)
    String serialNumber;

    @ServiceProperty
    String deviceType;

    @ServiceProperty
    String deviceSubTYpe;


    public String getDeviceType() {
        return deviceType;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDeviceSubType() {
        return deviceSubTYpe;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getSerialNumber() {
        return serialNumber;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
