package org.ow2.chameleon.fuchsia.mqtt.importer.amqp;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.mqtt.importer.amqp.adaptors.AMQPMessageCoat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component(name = "AMQPJointFactory")
@Provides
public class AMQPAndEventAdminJoint implements Joint, Runnable {

    @Requires
    EventAdmin eventAdmin;

    private Connection connection;

    private Channel channel;

    private boolean isMonitoringArrivals;

    private Thread eventArrivalMonitor;

    @Property
    String queue;

    @Validate
    public void start() throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(queue, false, false, false, null);

        eventArrivalMonitor =new Thread(this);
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

                eventAdmin.sendEvent(new AMQPMessageCoat(delivery).asEventAdminMessage());

                getLogger().info("EventAdmin: message '{}' sent,", message);

            }

        } catch (Exception e) {

            getLogger().error("Failed to monitor AMPQ Queue named '{}' duo to {}", queue,e.getMessage());

        }

    }

    public boolean validate(ImportDeclaration declaration) {
        return false;
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

}
