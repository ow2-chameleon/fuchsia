package org.ow2.chameleon.fuchsia.mqtt.test.dao;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public  class MessageHandler implements EventHandler {
    public void handleEvent(Event event) {

        if(event!=null){

            getLogger().info("<eventadmin type='inbound'>");
            getLogger().info("\tTOPIC: {}",event.getTopic());
            getLogger().info("\tProperty names: {}", Arrays.asList(event.getPropertyNames()));
            getLogger().info("</eventadmin>\n");

        }

    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }
}