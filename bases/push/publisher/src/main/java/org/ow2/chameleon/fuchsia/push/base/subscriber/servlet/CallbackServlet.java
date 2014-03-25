package org.ow2.chameleon.fuchsia.push.base.subscriber.servlet;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Base PUbSubHubbub Publisher
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLinkImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.ow2.chameleon.fuchsia.core.constant.HttpStatus;
import org.ow2.chameleon.fuchsia.core.constant.MediaType;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionConfirmationRequest;
import org.ow2.chameleon.fuchsia.push.base.subscriber.SubscriberInput;
import org.ow2.chameleon.fuchsia.push.base.subscriber.SubscriberOutput;
import org.ow2.chameleon.fuchsia.push.base.subscriber.exception.ActionNotRequestedByTheSubscriberException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class CallbackServlet extends HttpServlet implements SubscriberInput {

    enum MessageStatus {
        ERROR,
        OK_CHALLENGE,
        OK
    }

    private static final Logger LOG = LoggerFactory.getLogger(CallbackServlet.class);

    private ImportDeclaration importDeclaration;

    private SubscriberOutput subscriberOutput;

    private EventAdmin eventAdmin;

    public CallbackServlet(EventAdmin eventadmin, ImportDeclaration declaration, SubscriberOutput subscriberOutput) {
        this.eventAdmin = eventadmin;
        this.importDeclaration = declaration;
        this.subscriberOutput = subscriberOutput;
    }

    public void topicUpdated(String hubtopic, SyndFeed feed) {

        String queue = importDeclaration.getMetadata().get("push.eventAdmin.queue").toString();

        LOG.info("(subscriber), received updated content for {}, sending through eventAdmin in the queue {}", hubtopic, queue);

        Dictionary properties = new Hashtable();
        properties.put("topic", hubtopic);
        properties.put("content", feed.toString());

        Event eventAdminMessage = new Event(queue, properties);

        eventAdmin.sendEvent(eventAdminMessage);

    }

    public void confirmSubscriberRequestedSubscription(SubscriptionConfirmationRequest cr) throws ActionNotRequestedByTheSubscriberException {


        if (("subscribe".equals(cr.getMode()) || ("unsubscribe".equals(cr.getMode())))
                && (cr.getTopic().length() > 0) && (cr.getChallenge().length() > 0)) {

            //The last parameter should be checked
            String action = cr.getMode() + ":" + cr.getTopic() + ":" + null;

            if (!subscriberOutput.getApprovedActions().contains(action)) {
                LOG.info("{} not requested by this subscriber, at least not the action {} (the only approved actions are {})", new Object[]{cr.getMode(), action, subscriberOutput.getApprovedActions()});

                throw new ActionNotRequestedByTheSubscriberException("action not approved");
            } else {
                subscriberOutput.getApprovedActions().remove(action);
            }

        }

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            SubscriptionConfirmationRequest scr = SubscriptionConfirmationRequest.from(request);
            confirmSubscriberRequestedSubscription(scr);
            response.getWriter().print(scr.getChallenge());
            response.setStatus(HttpStatus.SC_OK);
        } catch (Exception invalidContentNotification) {
            LOG.error("Invalid content notification.", invalidContentNotification);
            response.setStatus(HttpStatus.SC_NOT_FOUND);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String hubtopic = null, hubchallenge = null;
        MessageStatus stsMessage = MessageStatus.ERROR;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(SyndFeedInput.class.getClassLoader());

        if (request.getContentType().contains(MediaType.APPLICATION_ATOM_XML)
                || request.getContentType().contains(MediaType.APPLICATION_RSS_XML)) {

            InputStream in = request.getInputStream();

            try {
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(in));

                List<SyndLinkImpl> linkList = feed.getLinks();

                for (SyndLinkImpl link : linkList) {

                    if ("self".equals(link.getRel())) {
                        hubtopic = link.getHref();
                    }
                }

                if (hubtopic == null) {
                    hubtopic = feed.getUri();
                }

                topicUpdated(hubtopic, feed);
            } catch (FeedException e) {
                LOG.error("Failed in creating feed response.", e);
            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }


            stsMessage = MessageStatus.OK;
        }

        response.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        switch (stsMessage) {
            case OK:
                response.setStatus(HttpStatus.SC_OK);
                break;
            case OK_CHALLENGE:
                response.setStatus(HttpStatus.SC_OK);
                response.getWriter().print(hubchallenge);
                break;
            default:
                response.setStatus(HttpStatus.SC_NOT_FOUND);
                break;
        }

    }

}
