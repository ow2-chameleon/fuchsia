package org.ow2.chameleon.fuchsia.push.base.hub.servlet;

import org.ow2.chameleon.fuchsia.push.base.hub.HubInput;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.ContentNotification;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.InvalidContentNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContentUpdatedNotificationServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private HubInput hub;

    public ContentUpdatedNotificationServlet(HubInput hub){
        this.hub=hub;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("This is a HUB URL");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {

            //Receiving notification from the publisher
            ContentNotification cn = ContentNotification.from(req);

            log.info("Publisher -> (Hub), new content notification received for the topic {}", cn.getUrl());

            hub.ContentNotificationReceived(cn);

        } catch (InvalidContentNotification invalidContentNotification) {
            log.error("Failed to parse notification",invalidContentNotification);
            resp.setStatus(400); //bad request
        } finally {
            resp.setStatus(204); //no content (notification accepted)
        }

    }

}
