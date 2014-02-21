package org.ow2.chameleon.fuchsia.importer.mqtt.it.dao;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public  class MessageHandler implements EventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);

    public void handleEvent(Event event) {

        if(event!=null){
            LOG.info("<eventadmin type='inbound'>");
            LOG.info("\tTOPIC: {}",event.getTopic());
            LOG.info("\tProperty names: {}", Arrays.asList(event.getPropertyNames()));
            LOG.info("</eventadmin>\n");
        }

    }

}
