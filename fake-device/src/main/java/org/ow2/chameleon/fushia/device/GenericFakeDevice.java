package org.ow2.chameleon.fushia.device;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;

/**
 * This class represent a generic fake device
 */
@Component (name = "iCasa.GenericFakeDevice")
@Provides (specifications = GenericDevice.class)
public class GenericFakeDevice implements GenericDevice {

    @Property
    String serialNumber;

    @Property
    String deviceType;

    @Property
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
