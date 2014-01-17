package org.ow2.chameleon.fuchsia.upnp.test;

import org.apache.felix.ipojo.ComponentInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;
import org.ow2.chameleon.testing.helpers.BaseTest;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.fest.assertions.Assertions.assertThat;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Test class to test UPnP functionalities.
 *
 * @author jeremy.savonet@gmail.com
 * @author jander nascimento (botelho at imag.fr)
 */
public class UPnPFunctionnalitiesTest extends BaseTest {

    private static final String LINKER_NAME = "FuchsiaUPnPLinker";

    private static final String IMPORTER_NAME = "FuchsiaUPnPImporter";

    private static final String DISCOVERY_NAME = "Fuchsia-UPnPDiscovery";

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
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia", "org.ow2.chameleon.fuchsia.core").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia.discovery", "org.ow2.chameleon.fuchsia.discovery.upnp").versionAsInProject()),

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
        Dictionary<String, String> confLinker = new Hashtable<String, String>();
        confLinker.put(INSTANCE_NAME_PROPERTY, LINKER_NAME);
        confLinker.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "(id=*)");
        confLinker.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(instance.name=" + IMPORTER_NAME + ")");
        ipojoHelper.createComponentInstance(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME, confLinker);

        Dictionary<String, String> confDisc = new Hashtable<String, String>();
        confDisc.put(INSTANCE_NAME_PROPERTY, DISCOVERY_NAME);
        ipojoHelper.createComponentInstance("Fuchsia-UPnPDiscovery-Factory", confDisc);
    }

    @After
    public void tearDownFinal() {
        ipojoHelper.getInstanceByName(LINKER_NAME).dispose();
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
    public void testUPnPLinkerCreation() {
        ComponentInstance component = ipojoHelper.getInstanceByName(LINKER_NAME);
        assertThat(component).isNotNull();
    }

    @Test
    public void testUPnPDiscoveryCreation() {
        ComponentInstance component = ipojoHelper.getInstanceByName(DISCOVERY_NAME);
        assertThat(component).isNotNull();
    }
}
