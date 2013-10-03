package org.ow2.chameleon.fuchsia.mqtt.importer.amqp.adaptors;

import com.rabbitmq.client.QueueingConsumer;
import org.osgi.service.event.Event;

import java.util.Dictionary;
import java.util.Hashtable;

public class AMQPMessageCoat {


    private QueueingConsumer.Delivery delivery;

    public AMQPMessageCoat(QueueingConsumer.Delivery delivery){
        this.delivery=delivery;
    }

    public Event asEventAdminMessage(){

        Dictionary properties = new Hashtable();

        Event eventAdminMessage = new Event("com/acme/reportgenerator/GENERATED", properties);

        return eventAdminMessage;

    }

}


