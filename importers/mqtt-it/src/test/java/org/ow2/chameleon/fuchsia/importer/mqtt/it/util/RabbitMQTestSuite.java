package org.ow2.chameleon.fuchsia.importer.mqtt.it.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.felix.ipojo.Factory;
import org.junit.internal.AssumptionViolatedException;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.testing.helpers.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import static org.ops4j.pax.exam.CoreOptions.*;

public class RabbitMQTestSuite extends BaseTest {

    protected static final Logger LOG = LoggerFactory.getLogger(RabbitMQTestSuite.class);

    @Inject
    protected BundleContext bundleContext;

    @Override
    protected Option[] getCustomOptions() {

        return options(
                packLog(),
                packMockito(),
                packMQTTClient(),
                packConsole(),
                // fuchsia bundles to test
                mavenBundle("com.rabbitmq", "amqp-client").versionAsInProject(),
                wrappedBundle(mavenBundle("org.easytesting", "fest-util").versionAsInProject()),
                wrappedBundle(mavenBundle("org.easytesting", "fest-assert").versionAsInProject()),
                mavenBundle("org.ow2.chameleon.fuchsia", "org.ow2.chameleon.fuchsia.core").versionAsInProject(),
                mavenBundle("org.ow2.chameleon.fuchsia.importer", "org.ow2.chameleon.fuchsia.importer.mqtt").versionAsInProject(),
                mavenBundle("org.ow2.chameleon.fuchsia.tools", "fuchsia-gogo-shell").versionAsInProject()
        );
    }

    @Override
    public boolean deployTestBundle() {
        return false;
    }

    /**
     * This pack enable the access to the console while debugging in Eclipse (doesnt work in IntelliJ).
     *
     * @return
     */
    protected CompositeOption packConsole() {
        CompositeOption logConfig = new DefaultCompositeOption(
                mavenBundle("org.apache.felix",
                        "org.apache.felix.gogo.command").versionAsInProject(),
                mavenBundle("org.apache.felix",
                        "org.apache.felix.gogo.runtime").versionAsInProject(),
                mavenBundle("org.apache.felix",
                        "org.apache.felix.gogo.shell").versionAsInProject(),
                mavenBundle("org.apache.felix",
                        "org.apache.felix.ipojo.arch.gogo").versionAsInProject()
        );

        return logConfig;
    }

    protected CompositeOption packMockito() {

        return new DefaultCompositeOption(
                mavenBundle("org.mockito", "mockito-core").versionAsInProject(),
                mavenBundle("org.objenesis", "objenesis").versionAsInProject(),
                mavenBundle("org.hamcrest", "hamcrest-core").versionAsInProject(),
                frameworkProperty("felix.bootdelegation.implicit").value("false") //this is a bug fix due to a mockito miss usage of java classloader
        );

    }

    protected CompositeOption packLog() {

        return new DefaultCompositeOption(
                mavenBundle("org.apache.felix", "org.apache.felix.eventadmin").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.log").versionAsInProject(),
                mavenBundle("org.ow2.bundles", "ow2-util-log").versionAsInProject(),
                mavenBundle("org.ow2.util", "util-log").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject(),
                wrappedBundle(mavenBundle("org.ow2.util", "util-i18n").versionAsInProject())

        );

    }

    protected CompositeOption packMQTTClient() {

        return new DefaultCompositeOption(
                wrappedBundle(mavenBundle("org.fusesource.mqtt-client", "mqtt-client").versionAsInProject()),
                wrappedBundle(mavenBundle("org.fusesource.hawtbuf", "hawtbuf").versionAsInProject()),
                wrappedBundle(mavenBundle("org.fusesource.hawtdispatch", "hawtdispatch-transport").versionAsInProject()),
                wrappedBundle(mavenBundle("org.fusesource.hawtdispatch", "hawtdispatch").versionAsInProject())
        );

    }

    /**
     * Verify is Rabbit MQ service is available in the current platform.
     *
     * @return the availability of the rabbitmq daemon, true in case its available, or false otherwise
     */
    protected final void assertRabbitMQisRunning() throws AssumptionViolatedException {

        ConnectionFactory factory = new ConnectionFactory();

        try {

            Connection connection = factory.newConnection();

        } catch (IOException e) {

            final String message = "RabbitMQ service not available, ignoring test";

            LOG.warn(message);

            throw new AssumptionViolatedException(message);
        }

    }

    protected ImportDeclaration createImportationDeclaration(String instanceName, HashMap<String, Object> metadata) {

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Factory.INSTANCE_NAME_PROPERTY, instanceName);

        bundleContext.registerService(new String[]{ImportDeclaration.class.getName()}, declaration, props);

        return declaration;

    }

}
