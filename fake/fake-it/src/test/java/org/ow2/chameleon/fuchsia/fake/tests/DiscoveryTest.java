package org.ow2.chameleon.fuchsia.fake.tests;

import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.fuchsia.fake.device.GenericDevice;
import org.ow2.chameleon.fuchsia.testing.CommonTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

/**
 * Test class to test the well functioning of discovery->fake device->proxy
 *
 * @author jeremy.savonet@gmail.com
 */
public class DiscoveryTest extends CommonTest {

    /**
     * Common test options.
     */
    @Override
    protected Option[] getCustomOptions() {

        return options(
                // iCasa bundles to test
                mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-fake-device").versionAsInProject(),
                mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-fake-discovery").versionAsInProject(),
                mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-fake-importer").versionAsInProject()
        );
    }

    @Override
    public boolean deployTestBundle() {
        return false;
    }


    @Test
    public void testCreateFakeBL() throws InvalidSyntaxException {
        GenericDevice genericDevice = osgiHelper.getServiceObject(GenericDevice.class, "(device.serialNumber=BL-1234)");
        assertThat(genericDevice.getSerialNumber()).isEqualTo("BL-1234");
    }

    @Test
    public void testCreateFakeDL() throws InvalidSyntaxException {
        GenericDevice genericDevice = osgiHelper.getServiceObject(GenericDevice.class, "(device.serialNumber=DL-5678)");
        assertThat(genericDevice.getSerialNumber()).isEqualTo("DL-5678");
    }

    @Test
    public void testCreateFakePT() throws InvalidSyntaxException {
        GenericDevice genericDevice = osgiHelper.getServiceObject(GenericDevice.class, "(device.serialNumber=PT-1234)");
        assertThat(genericDevice.getSerialNumber()).isEqualTo("PT-1234");
    }


//    @Test
//    public void testCreateProxyBL() throws InvalidSyntaxException {
//        GenericDevice genericDevice = osgiHelper.getServiceObject(GenericDevice.class, "(device.serialNumber=BL-1234)");
//
//        GenericDevice proxy = (GenericDevice) Proxy.newProxyInstance(DelegationProxy.class.getClassLoader(), new Class[]{GenericDevice.class}, new DelegationProxy(genericDevice));
//        assertThat(proxy.getSerialNumber()).isEqualTo("BL-1234");
//
//    }
}
