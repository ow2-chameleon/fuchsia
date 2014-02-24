package org.ow2.chameleon.fuchsia.push.base.hub.servlet;

import org.ow2.chameleon.fuchsia.push.base.hub.HubInput;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SubscriptionServlet extends HttpServlet {

    private HubInput hub;

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionServlet.class);

    public SubscriptionServlet(HubInput hub) {
        this.hub = hub;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        SubscriptionRequest subscriptionRequest = null;


        try {
            subscriptionRequest = SubscriptionRequest.from(req);

            hub.subscriptionRequestReceived(subscriptionRequest);

            resp.setStatus(204);

        } catch (Exception exception) {

            resp.setStatus(400);

            LOG.error("Failed to created response to the request", exception);

        }


    }

}
