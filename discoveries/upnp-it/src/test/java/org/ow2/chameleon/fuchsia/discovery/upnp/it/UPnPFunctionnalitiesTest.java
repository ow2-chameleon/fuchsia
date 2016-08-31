package org.ow2.chameleon.fuchsia.discovery.upnp.it;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Discovery UPnP [IntegrationTests]
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.felix.ipojo.ComponentInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;
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
                //wrappedBundle(mavenBundle("org.osgi", "org.osgi.compendium").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia", "org.ow2.chameleon.fuchsia.core").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia.discovery", "org.ow2.chameleon.fuchsia.discovery.upnp").versionAsInProject()),

                //Fest assert wrapper bundles
                wrappedBundle(mavenBundle("org.easytesting", "fest-util").versionAsInProject()),
                wrappedBundle(mavenBundle("org.easytesting", "fest-assert").versionAsInProject()),

                bootDelegationPackage("com.intellij.rt.coverage.data"),
                mavenBundle("org.apache.felix", "org.apache.felix.log").versionAsInProject(),
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject()
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
        ipojoHelper.createComponentInstance("org.ow2.chameleon.fuchsia.discovery.upnp.UPnPDiscovery", confDisc);
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
