package org.ow2.chameleon.fuchsia.mqtt.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.tools.shell.FuchsiaGogoCommand;
import org.ow2.chameleon.testing.helpers.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

/**
 * Test class MQTT
 *
 * @author botelho@imag.fr
 */
@ExamReactorStrategy(PerMethod.class)
public class MQTTTest extends BaseTest {

    @Inject
    private BundleContext bundleContext;

    @Inject
    EventAdmin eventAdmin;

    @Inject
    FuchsiaGogoCommand command;

    /**
     * Common test options.
    */
    @Override
    protected Option[] getCustomOptions() {

        return options(
                wrappedBundle(mavenBundle("org.apache.felix", "org.apache.felix.eventadmin").versionAsInProject()),
                wrappedBundle(mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime").versionAsInProject()),
                wrappedBundle(mavenBundle("org.apache.felix", "org.apache.felix.log").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.bundles", "ow2-util-log").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.util", "util-log").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.util", "util-i18n").versionAsInProject()),
                // fuchsia bundles to test
                wrappedBundle(mavenBundle("com.rabbitmq", "amqp-client").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-core").versionAsInProject()),
                wrappedBundle(mavenBundle("org.easytesting", "fest-util").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia.mqtt", "fuchsia-mqtt-importer").versionAsInProject()),
                wrappedBundle(mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject()),
                wrappedBundle(mavenBundle("org.ow2.chameleon.fuchsia.tools", "fuchsia-gogo-shell").versionAsInProject()),
                wrappedBundle(mavenBundle("org.mockito", "mockito-core").versionAsInProject()),
                wrappedBundle(mavenBundle("org.objenesis", "objenesis").versionAsInProject()),
                //wrappedBundle(mavenBundle("org.slf4j", "slf4j-api").versionAsInProject()),
                wrappedBundle(mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject()),
                frameworkProperty("felix.bootdelegation.implicit").value("false"),
                wrappedBundle(mavenBundle("org.easytesting", "fest-assert").versionAsInProject())
        );
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

    @Before
    public void instantiateAMPQPlaform(){

        Properties linker=new Properties();
        linker.put(FILTER_IMPORTDECLARATION_PROPERTY,"(id=*)");
        linker.put(FILTER_IMPORTERSERVICE_PROPERTY,"(instance.name=AMQPImporter)");
        linker.put(Factory.INSTANCE_NAME_PROPERTY,"MQTTLinker");

        ipojoHelper.createComponentInstance(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME,linker);

        Properties importer=new Properties();
        importer.put(FILTER_IMPORTDECLARATION_PROPERTY,"(id=*)");
        importer.put("target","(id=*)");
        importer.put(Factory.INSTANCE_NAME_PROPERTY,"AMQPImporter");

        ipojoHelper.createComponentInstance("AMQPImporterFactory",importer);

    }

    @Test
    public void testLinkerImporterCreated()  {

        ComponentInstance linkerInstance = ipojoHelper.getInstanceByName("MQTTLinker");

        assertThat(linkerInstance).isNotNull();

    }

    @Test
    public void testImporterCreated()  {

        ComponentInstance importerInstance = ipojoHelper.getInstanceByName("AMQPImporter");

        assertThat(importerInstance).isNotNull();

    }

    @Test
    public void testProduceAMPQConsumeEventAdmin() throws IOException {

        assertRabbitMQisRunning();

        Dictionary handlerProperties = new Hashtable();
        handlerProperties.put(EventConstants.EVENT_TOPIC, "public");

        HandlerMQTT ht=new HandlerMQTT();

        HandlerMQTT htmock=spy(ht);

        bundleContext.registerService(EventHandler.class.getName(),htmock,handlerProperties);

        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.DEVICE_ID, "00000000-54b3-e7c7-0000-000046bffd97");
        metadata.put("mqtt.queue","public");

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Factory.INSTANCE_NAME_PROPERTY,"importDeclaration");
        bundleContext.registerService(new String[]{ImportDeclaration.class.getName()}, declaration, props);

        assertThat(declaration).isNotNull();

        sendSampleAMPQMessage();

        verify(htmock,times(1)).handleEvent(any(Event.class));

    }

    /**
     * Sends a sample message to the RabbitMQ bus (as AMPQ message)
     * @throws IOException
     */
    private void sendSampleAMPQMessage() throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        final String topic="public";
        final String quote = "The force of mind is only as great as its expression; its depth only as deep as its power to expand and lose itself";

        getLogger().info("<eventadmin type='outbound'>");
        getLogger().info("\tTOPIC: {}",topic);
        getLogger().info("\tQuote: {}",quote);
        getLogger().info("</eventadmin>\n");

        channel.basicPublish("", topic, null, quote.getBytes());

    }

    /**
     * Verify is Rabbit MQ service is available in the current platform
     * @return the availability of the rabbitmq daemon, true in case its available, or false otherwise
     */
    private void assertRabbitMQisRunning() throws AssumptionViolatedException {

        ConnectionFactory factory = new ConnectionFactory();

        try {

            Connection connection = factory.newConnection();

        } catch (IOException e) {

            final String message="RabbitMQ service not available, ignoring test";

            getLogger().warn(message);

            throw new AssumptionViolatedException(message);
        }

    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

}
