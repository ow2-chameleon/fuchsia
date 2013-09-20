package org.ow2.chameleon.fuchsia.upnp.test;

import org.apache.felix.ipojo.ComponentInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.fake.device.GenericDevice;
import org.ow2.chameleon.fuchsia.upnp.clock.ClockDevice;
import org.ow2.chameleon.fuchsia.upnp.discovery.UPnPFuchsiaDiscoveryImpl;
import org.ow2.chameleon.fuchsia.upnp.importer.UPnPFuchsiaImporterImpl;
import org.ow2.chameleon.testing.helpers.BaseTest;

import javax.inject.Inject;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.fest.assertions.Assertions.assertThat;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Test class to test UPnP functionnalities.
 *
 * @author jeremy.savonet@gmail.com
 * @author jander nascimento (botelho at imag.fr)
 */
public class UPnPFunctionnalitiesTest extends BaseTest {

    @Inject
    ClockDevice m_service;

    private static final String IMPORTER_NAME = "FuchsiaUPnPImporter";
    private static final String DISCOVERY_NAME ="Fuchsia-UPnPDiscovery";

    /**
     * Common test options.
     */
    @Override
    protected Option[] getCustomOptions() {

        return options(
                // fuchsia bundles to test
                wrappedBundle(mavenBundle("org.apache.felix", "org.apache.felix.upnp.basedriver").versionAsInProject()),
                wrappedBundle(mavenBundle("org.apache.felix", "org.apache.felix.upnp.extra").versionAsInProject()),
                wrappedBundle(mavenBundle("org.osgi", "org.osgi.compendium").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-core").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia.fake", "fuchsia-fake-device").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia.examples", "fuchsia-upnp-components").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia.upnp", "fuchsia-upnp-config").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia.upnp", "fuchsia-upnp-importer").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia.upnp", "fuchsia-upnp-discovery").versionAsInProject()),

                //Fest assert wrapper bundles
                wrappedBundle(mavenBundle("org.easytesting", "fest-util").versionAsInProject()),
                wrappedBundle(mavenBundle("org.easytesting", "fest-assert").versionAsInProject()),

                bootDelegationPackage("com.intellij.rt.coverage.data")

        );
    }

    /**
     * Common test setup.
     */
    @Before
    public void setupFinal() {
        Dictionary<String, String> confImp = new Hashtable<String, String>();
        confImp.put(INSTANCE_NAME_PROPERTY, IMPORTER_NAME);
        confImp.put("target", "(id=*)");
        ipojoHelper.createComponentInstance("Fuchsia-UPnPImporter-Factory", confImp);

        Dictionary<String, String> confDisc = new Hashtable<String, String>();
        confDisc.put(INSTANCE_NAME_PROPERTY, DISCOVERY_NAME);
        ipojoHelper.createComponentInstance("Fuchsia-UPnPDiscovery-Factory", confDisc);
    }

    @After
    public void tearDownFinal() {
        ipojoHelper.getInstanceByName(IMPORTER_NAME).dispose();
        ipojoHelper.getInstanceByName(DISCOVERY_NAME).dispose();
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
    public void testUPnPImporterCreation() {
        ComponentInstance component = ipojoHelper.getInstanceByName(IMPORTER_NAME);
        assertThat(component).isNotNull();
    }

    @Test
    public void testUPnPDiscoveryCreation() {
        ComponentInstance component = ipojoHelper.getInstanceByName(DISCOVERY_NAME);
        assertThat(component).isNotNull();
    }

    @Test
    public void testUPnPServiceCreation() {
        System.out.println("Device ID : " + m_service.getDescriptions(null));
        assertThat(m_service.getDescriptions(null)).isNotEqualTo(null);
    }

    @Test
    @Ignore
    public void testUPnPServiceImportation() {
        System.out.println("Device ID : " + m_service.getDescriptions(null));
        GenericDevice device = (GenericDevice) osgiHelper.getServiceObject(osgiHelper.getServiceReference(GenericDevice.class));
        assertThat(device.getSerialNumber()).isNotNull();
    }

    @Test
    public void testUPnPProxyCreation() {
        DiscoveryService uPnPFuchsiaDiscovery = (DiscoveryService) osgiHelper.getServiceObject(osgiHelper.getServiceReference(DiscoveryService.class));
        UPnPFuchsiaImporterImpl uPnPFuchsiaImporter =  (UPnPFuchsiaImporterImpl) osgiHelper.getServiceObject(osgiHelper.getServiceReference(UPnPFuchsiaImporterImpl.class));
        System.out.println("osgiHelper.getServiceReference(UPnPFuchsiaImporterImpl.class) : " +osgiHelper.getServiceReference(UPnPFuchsiaImporterImpl.class));
        assertThat(uPnPFuchsiaDiscovery).isNotNull();
        assertThat(uPnPFuchsiaImporter).isNotNull();

        /*
        Check architecture wise its a problem to add this at main discovery interface

        Set<ImportDeclaration> iDecs = uPnPFuchsiaDiscovery.getImportDeclarations();

        synchronized (this) {
            for(ImportDeclaration iDec : iDecs) {

                GenericDevice deviceProxy = uPnPFuchsiaImporter.getObjectProxy(iDec);
                assertThat(deviceProxy.getSerialNumber()).isNotNull();
            }
        }
        */
    }
}
