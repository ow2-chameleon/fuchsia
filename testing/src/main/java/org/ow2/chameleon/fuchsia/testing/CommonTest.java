package org.ow2.chameleon.fuchsia.testing;

import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ow2.chameleon.testing.helpers.BaseTest;

import java.io.IOException;

import static org.ops4j.pax.exam.CoreOptions.*;

public class CommonTest extends BaseTest {

    // The Fuchsia Helper
    protected FuchsiaHelper fuchsiaHelper;

    protected boolean assertj = true;

    /**
     * Done some initializations.
     */
    @Before
    public void fuchsiaSetUp() {
        fuchsiaHelper = new FuchsiaHelper(context);
    }

    /**
     * Closing the test.
     */
    @After
    public void fuchsiaTearDown() {
        fuchsiaHelper.dispose();
    }

    public Option[] defaultConfiguration() throws IOException {
        Option[] options = super.defaultConfiguration();

        options = OptionUtils.combine(options, fuchsiaBundles());

        if (deployAssertj()) {
            options = OptionUtils.combine(options, assertjBundles());
        }

        return options;
    }

    protected Option fuchsiaBundles() {
        return composite(
                mavenBundle().groupId("org.ow2.chameleon.fuchsia").artifactId("org.ow2.chameleon.fuchsia.core").versionAsInProject(),
                mavenBundle().groupId("org.ow2.chameleon.fuchsia.testing").artifactId("fuchsia-helpers").versionAsInProject()
        );
    }

    protected Option assertjBundles() {
        return composite(
                wrappedBundle(mavenBundle("org.assertj", "assertj-core").versionAsInProject())
        );
    }


    /**
     * Method to override to instruct pax exam to not deploy assertj.
     *
     * @return true to deploy assertj
     */
    public boolean deployAssertj() {
        return assertj;
    }

    /**
     * Ensure the deployment of iPOJO by pax exam, because Fuchsia always need iPOJO.
     *
     * @return true to deploy iPOJO
     */
    @Override
    public final boolean deployiPOJO() {
        return true;
    }

    /**
     * Ensure the deployment of Mockito by pax exam, because this bundle (fuchsia-helpers) contains classes which need
     * Mockito.
     *
     * @return true to deploy Mockito
     */
    @Override
    public final boolean deployMockito() {
        return true;
    }

}
