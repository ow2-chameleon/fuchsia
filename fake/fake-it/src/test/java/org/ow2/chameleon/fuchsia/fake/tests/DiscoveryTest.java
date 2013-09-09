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
 * Note :
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
                mavenBundle("org.ow2.chameleon.fuchsia.fake", "fuchsia-fake-device").versionAsInProject(),
                mavenBundle("org.ow2.chameleon.fuchsia.fake", "fuchsia-fake-discovery").versionAsInProject(),
                mavenBundle("org.ow2.chameleon.fuchsia.fake", "fuchsia-fake-importer").versionAsInProject()
        );
    }

    @Override
    public boolean deployTestBundle() {
        return false;
    }
}
