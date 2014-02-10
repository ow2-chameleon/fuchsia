package org.ow2.chameleon.fuchsia.pubsubhub.hub.servlet;

import org.ow2.chameleon.fuchsia.pubsubhub.hub.HubInput;
import org.ow2.chameleon.fuchsia.pubsubhub.hub.dto.SubscriptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public  class SubscriptionServlet extends HttpServlet {

    private HubInput hub;

    private static final Logger log= LoggerFactory.getLogger(SubscriptionServlet.class);

    public SubscriptionServlet(HubInput hub){
        this.hub=hub;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        SubscriptionRequest subscriptionRequest = null;


        try {
            subscriptionRequest = SubscriptionRequest.from(req);

            hub.SubscriptionRequestReceived(subscriptionRequest);

            resp.setStatus(204);

        } catch (Exception exception) {

            resp.setStatus(400);

            log.error("Failed to created response to the request",exception);

        }


    }

}
