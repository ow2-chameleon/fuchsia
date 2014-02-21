package org.ow2.chameleon.fuchsia.importer.mqtt.it;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.importer.mqtt.it.dao.MessageHandler;
import org.ow2.chameleon.fuchsia.importer.mqtt.it.util.RabbitMQTestSuite;
import org.ow2.chameleon.fuchsia.tools.shell.FuchsiaGogoCommand;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;

import javax.inject.Inject;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.ow2.chameleon.fuchsia.core.component.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.component.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

/**
 * Test class MQTT
 *
 * @author botelho@imag.fr
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class MQTTMessageCaptureTest extends RabbitMQTestSuite {

    @Inject
    EventAdmin eventAdmin;

    @Inject
    FuchsiaGogoCommand command;

    ComponentInstance linkerComponentInstance;
    ComponentInstance importerComponentInstance;

    protected IPOJOHelper ipojoHelper;


    @Before
    public void instantiateAMPQPlaform(){

        ipojoHelper=new IPOJOHelper(bundleContext);

        Properties linker=new Properties();
        linker.put(FILTER_IMPORTDECLARATION_PROPERTY,"(id=*)");
        linker.put(FILTER_IMPORTERSERVICE_PROPERTY,"(instance.name=MQTTImporter)");
        linker.put(Factory.INSTANCE_NAME_PROPERTY,"MQTTLinker");

        linkerComponentInstance=ipojoHelper.createComponentInstance(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME,linker);

        Properties importer=new Properties();
        importer.put(FILTER_IMPORTDECLARATION_PROPERTY,"(id=*)");
        importer.put("target","(id=*)");
        importer.put(Factory.INSTANCE_NAME_PROPERTY,"MQTTImporter");

        importerComponentInstance=ipojoHelper.createComponentInstance("org.ow2.chameleon.fuchsia.importer.mqtt.MQTTImporter",importer);

    }

    @After
    public void uninstantiateAMPQPlatform(){
        linkerComponentInstance.dispose();
        importerComponentInstance.dispose();

    }

    @Test
    public void ConsumeSingleMessageEventAdmin() throws Exception {

        assertRabbitMQisRunning();

        final String queue="public";

        Dictionary handlerProperties = new Hashtable();
        handlerProperties.put(EventConstants.EVENT_TOPIC, queue);

        MessageHandler ht=new MessageHandler();

        MessageHandler htmock=spy(ht);

        bundleContext.registerService(EventHandler.class.getName(),htmock,handlerProperties);

        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.DEVICE_ID, "00000000-54b3-e7c7-0000-000046bffd98");
        metadata.put("mqtt.queue",queue);

        ImportDeclaration declaration=createImportationDeclaration("importDeclaration", metadata);

        assertThat(declaration).isNotNull();

        sendSampleMQTTMessage();

        verify(htmock,times(1)).handleEvent(any(Event.class));
    }

    /**
     * Seinding message ot type MQTT do not work properly with version 3.1.5 of RabbitMQ, See https://github.com/rabbitmq/rabbitmq-mqtt/issues/5
     * @throws Exception
     */
    private void sendSampleMQTTMessage() throws Exception {

        final String topic="public";
        final String quote = "The force of mind is only as great as its expression; its depth only as deep as its power to expand and lose itself";

        // MQTT Client
        MQTT mqtt = new MQTT();
        mqtt.setHost("localhost", 1883);
        final BlockingConnection connection = mqtt.blockingConnection();

        mqtt.setCleanSession(false);

        connection.connect();

        Thread.sleep(1000);

        assertThat(connection.isConnected()).isTrue();

        LOG.info("<eventadmin type='outbound'>");
        LOG.info("\tTOPIC: {}",topic);
        LOG.info("\tQuote: {}",quote);
        LOG.info("</eventadmin>\n");

        connection.publish(topic, quote.getBytes(), QoS.AT_MOST_ONCE, false);

    }

}
