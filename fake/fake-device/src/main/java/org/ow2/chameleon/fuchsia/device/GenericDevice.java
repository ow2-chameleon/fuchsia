package org.ow2.chameleon.fuchsia.device;

/**
 * Define a simple interface for generic device !
 *
 * Really simple just for fake discovery !
 */
public interface GenericDevice {

    /**
     * Service property indicating the hardware serial number of the device.
     *
     * <ul>
     * <li>This property is <b>mandatory</b></li>
     * <li>Type of values : <b><code>java.lang.String</code></b></li>
     * <li>Description : the hardware serial number of the device. Must be unique
     * and immutable.</li>
     * </ul>
     *
     * @see #getSerialNumber()
     */
    String DEVICE_SERIAL_NUMBER = "device.serialNumber";

    String DEVICE_TYPE = "device.type";

    String DEVICE_SUBTYPE = "device.subtype";

    /**
     * Return the type of the device.
     *
     * @return the type of the device.
     * @see #DEVICE_TYPE
     */
    String getDeviceType();

    /**
     * Return the subtype of the device.
     *
     * @return the type of the device.
     * @see #DEVICE_SUBTYPE
     */
    String getDeviceSubType();

    /**
     * Return the serial number of the device.
     *
     * @return the serial number of the device.
     * @see #DEVICE_SERIAL_NUMBER
     */
    String getSerialNumber();
}
