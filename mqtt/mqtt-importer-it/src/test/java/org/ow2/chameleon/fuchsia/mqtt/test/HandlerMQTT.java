package org.ow2.chameleon.fuchsia.mqtt.test;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: jnascimento
 * Date: 14/10/13
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */
public class HandlerMQTT  implements EventHandler {
    public void handleEvent(Event event) {

        if(event!=null){

            getLogger().info("<eventadmin type='inbound'>");
            getLogger().info("\tTOPIC: {}",event.getTopic());
            getLogger().info("\tProperty names: {}",Arrays.asList(event.getPropertyNames()));
            getLogger().info("</eventadmin>\n");

        }

    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
