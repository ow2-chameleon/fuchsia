package org.ow2.chameleon.fuchsia.mqtt.importer.amqp;


import com.rabbitmq.client.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

@Component(name = "AMQPJointFactory")
@Provides
public class AMQPOutputRouter implements Runnable {

    @Requires
    EventAdmin eventAdmin;

    private Connection connection;

    private Channel channel;

    private boolean isMonitoringArrivals;

    private Thread eventArrivalMonitor;

    @Property(name = "mqtt.server.host",value = ConnectionFactory.DEFAULT_HOST)
    private String serverHost;

    @Property(name = "mqtt.server.port",value = ConnectionFactory.DEFAULT_AMQP_PORT+"")
    private int serverPort;

    @Property(name = "mqtt.queue")
    String queue;

    @Validate
    public void start() throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        connection = factory.newConnection(new Address[]{new Address(serverHost,serverPort)});
        channel = connection.createChannel();
        channel.queueDeclare(queue, false, false, false, null);

        eventArrivalMonitor = new Thread(this);
        eventArrivalMonitor.start();

        isMonitoringArrivals =true;

    }

    @Invalidate
    public void stop(){
        isMonitoringArrivals =false;
    }

    public void run(){

        try {

            getLogger().info("Monitoring AMPQ Queue named '{}'", queue);

            QueueingConsumer consumer = new QueueingConsumer(channel);

            channel.basicConsume(queue, true, consumer);

            while (isMonitoringArrivals) {

                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());

                getLogger().info("AMQP: message '{}' received,", message);

                getLogger().info("Forwarding ..");

                Hashtable metatable=new Hashtable();
                metatable.put("content", message);

                eventAdmin.sendEvent(getEventAdminMessage(metatable));

                getLogger().info("EventAdmin: message '{}' queue '{}' sent", message,queue);

            }

        } catch (Exception e) {

            getLogger().error("Failed to monitor AMPQ Queue named '{}'", queue,e);

        }

    }

    private Event getEventAdminMessage(Dictionary properties) {

        Event eventAdminMessage = new Event(queue, properties);

        return eventAdminMessage;

    }

    public boolean validate(ImportDeclaration declaration) {
        return false;
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

}
