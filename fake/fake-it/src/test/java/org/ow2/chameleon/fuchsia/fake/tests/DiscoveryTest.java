package org.ow2.chameleon.fuchsia.fake.tests;

import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.fake.device.GenericDevice;
import org.ow2.chameleon.fuchsia.fake.device.GenericFakeDevice;
import org.ow2.chameleon.testing.helpers.BaseTest;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Test class to test the weel functionning of discovery->fake device->proxy
 *
 * @author jeremy.savonet@gmail.com
 */
public class DiscoveryTest extends BaseTest {

    @Inject
    BundleContext m_bundleContext;

    /**
     * Common test options.
     */
    @Override
    protected Option[] getCustomOptions() {

        return options(
                // iCasa bundles to test
                mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-core").versionAsInProject(),
                mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-fake-device").versionAsInProject(),
                mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-fake-discovery").versionAsInProject(),
                mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-fake-importer").versionAsInProject(),


                //Fest assert wrapper bundles
                wrappedBundle(mavenBundle("org.assertj", "assertj-core").versionAsInProject())

        );
    }

    /**
     * Common test setup.
     */
    @Before
    public void commonSetUp() {
        super.commonSetUp();
    }

    /**
     * Common test tear down.
     */
    @Override
    public void commonTearDown() {
        super.commonTearDown();
    }

    @Override
    public boolean deployTestBundle() {
        return false;
    }


    @Test
    public void testCreateFakeBL() throws InvalidSyntaxException {

        ServiceReference[] deviceRef = m_bundleContext.getServiceReferences(GenericDevice.class.getName(),"(device.serialNumber=BL-1234)");
        Object device =  m_bundleContext.getService(deviceRef[0]);
        GenericFakeDevice genericDevice = (GenericFakeDevice) device;

        assertThat(genericDevice.getSerialNumber()).isEqualTo("BL-1234");
    }

    @Test
    public void testCreateFakeDL() throws InvalidSyntaxException {

        ServiceReference[] deviceRef = m_bundleContext.getServiceReferences(GenericDevice.class.getName(),"(device.serialNumber=DL-5678)");
        Object device =  m_bundleContext.getService(deviceRef[0]);
        GenericFakeDevice genericDevice = (GenericFakeDevice) device;

        assertThat(genericDevice.getSerialNumber()).isEqualTo("DL-5678");
    }

    @Test
    public void testCreateFakePT() throws InvalidSyntaxException {

        ServiceReference[] deviceRef = m_bundleContext.getServiceReferences(GenericDevice.class.getName(),"(device.serialNumber=PT-1234)");
        Object device =  m_bundleContext.getService(deviceRef[0]);
        GenericFakeDevice genericDevice = (GenericFakeDevice) device;

        assertThat(genericDevice.getSerialNumber()).isEqualTo("PT-1234");
    }


//    @Test
//    public void testCreateProxyBL() throws InvalidSyntaxException {
//
//        ServiceReference[] deviceRef = m_bundleContext.getServiceReferences(GenericDevice.class.getName(),"(device.serialNumber=BL-1234)");
//        Object device =  m_bundleContext.getService(deviceRef[0]);
//        GenericFakeDevice genericDevice = (GenericFakeDevice) device;
//
//        GenericDevice proxy = (GenericDevice) Proxy.newProxyInstance(DelegationProxy.class.getClassLoader(), new Class[]{GenericDevice.class}, new DelegationProxy(genericDevice));
//        assertThat(proxy.getSerialNumber()).isEqualTo("BL-1234");
//
//    }
}
