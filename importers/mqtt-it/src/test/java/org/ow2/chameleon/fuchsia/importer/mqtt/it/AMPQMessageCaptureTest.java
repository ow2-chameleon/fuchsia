package org.ow2.chameleon.fuchsia.importer.mqtt.it;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import java.io.IOException;
import java.util.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

/**
 * Test class AMQP (low level) message receiving
 *
 * @author botelho@imag.fr
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class AMPQMessageCaptureTest extends RabbitMQTestSuite {

    @Inject
    EventAdmin eventAdmin;

    @Inject
    FuchsiaGogoCommand command;

    ComponentInstance linkerComponentInstance;
    ComponentInstance importerComponentInstance;

    protected IPOJOHelper ipojoHelper;

    @Before
    public void instantiateAMPQPlaform(){

        assertRabbitMQisRunning();

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
    public void LinkerImporterCreated()  {

        ComponentInstance linkerInstance = ipojoHelper.getInstanceByName("MQTTLinker");

        assertThat(linkerInstance).isNotNull();
        assertThat(linkerInstance.getState()).isEqualTo(ComponentInstance.VALID);


    }

    @Test
    public void ImporterCreated()  {

        ComponentInstance importerInstance = ipojoHelper.getInstanceByName("MQTTImporter");

        assertThat(importerInstance).isNotNull();
        assertThat(importerInstance.getState()).isEqualTo(ComponentInstance.VALID);

    }

    @Test
    public void ConsumeSingleMessageEventAdmin() throws IOException {

        final String queue="public";

        Dictionary handlerProperties = new Hashtable();
        handlerProperties.put(EventConstants.EVENT_TOPIC, queue);

        MessageHandler ht=new MessageHandler();

        MessageHandler htmock=spy(ht);

        bundleContext.registerService(EventHandler.class.getName(),htmock,handlerProperties);

        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.DEVICE_ID, "00000000-54b3-e7c7-0000-000046bffd97");
        metadata.put("mqtt.queue",queue);

        ImportDeclaration declaration=createImportationDeclaration("importDeclaration",metadata);
        assertThat(declaration).isNotNull();

        sendSampleAMPQMessage();

        verify(htmock,times(1)).handleEvent(any(Event.class));

    }

    @Test
    public void ConsumeMultipleMessageEventAdmin() throws IOException {

        final String queue="public";

        Dictionary handlerProperties = new Hashtable();
        handlerProperties.put(EventConstants.EVENT_TOPIC, queue);

        MessageHandler ht=new MessageHandler();

        MessageHandler htmock=spy(ht);

        bundleContext.registerService(EventHandler.class.getName(),htmock,handlerProperties);

        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.DEVICE_ID, "00000000-54b3-e7c7-0000-000046bffd97");
        metadata.put("mqtt.queue",queue);

        ImportDeclaration declaration=createImportationDeclaration("importDeclaration",metadata);

        assertThat(declaration).isNotNull();

        final int TOTAL=10;

        for(int counter=0;counter<TOTAL;counter++){
            sendSampleAMPQMessage();
        }

        verify(htmock,times(TOTAL)).handleEvent(any(Event.class));

    }

    @Test
    public void MessageSentWithRightArgument() throws IOException {

        final String queue="public";

        Dictionary handlerProperties = new Hashtable();
        handlerProperties.put(EventConstants.EVENT_TOPIC,queue);

        MessageHandler ht=new MessageHandler();

        MessageHandler htmock=spy(ht);

        bundleContext.registerService(EventHandler.class.getName(),htmock,handlerProperties);

        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.DEVICE_ID, "00000000-54b3-e7c7-0000-000046bffd99");
        metadata.put("mqtt.queue",queue);

        ImportDeclaration declaration=createImportationDeclaration("importDeclaration",metadata);

        assertThat(declaration).isNotNull();

        sendSampleAMPQMessage();

        ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

        verify(htmock,times(1)).handleEvent(argument.capture());

        assertThat(argument.getValue().getTopic()).isEqualTo(queue);

    }

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

}
