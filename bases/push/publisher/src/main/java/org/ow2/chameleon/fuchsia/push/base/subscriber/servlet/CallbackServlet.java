package org.ow2.chameleon.fuchsia.push.base.subscriber.servlet;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLinkImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
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
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class CallbackServlet extends HttpServlet implements SubscriberInput
{

    enum MessageStatus { ERROR, OK_Challenge, OK };

    private static final Logger LOG = LoggerFactory.getLogger(CallbackServlet.class);

    private ImportDeclaration importDeclaration;

    private SubscriberOutput subscriberOutput;

    public CallbackServlet(EventAdmin eventadmin,ImportDeclaration declaration,SubscriberOutput subscriberOutput){
        this.eventAdmin=eventadmin;
        this.importDeclaration=declaration;
        this.subscriberOutput=subscriberOutput;
    }

    private EventAdmin eventAdmin;

    public void TopicUpdated(String hubtopic, SyndFeed feed) {

        String queue=importDeclaration.getMetadata().get("push.eventAdmin.queue").toString();

        LOG.info("(subscriber), received updated content for {}, sending through eventAdmin in the queue {}",hubtopic,queue);

        Dictionary properties = new Hashtable();
        properties.put("topic",hubtopic);
        properties.put("content",feed.toString());

        Event eventAdminMessage = new Event(queue, properties);

        eventAdmin.sendEvent(eventAdminMessage);

    }

    public void ConfirmSubscriberRequestedSubscription(SubscriptionConfirmationRequest cr)  throws ActionNotRequestedByTheSubscriberException {


        if (((cr.getMode().equals("subscribe")) || (cr.getMode().equals("unsubscribe")))
                && (cr.getMode().length() > 0) && (cr.getTopic().length() > 0) && (cr.getChallenge().length() > 0)) {

            String action=cr.getMode() + ":" + cr.getTopic() + ":"
                    + null; //The last parameter should be checked

            if(!subscriberOutput.getApprovedActions().contains(action)){
                LOG.info("{} not requested by this subscriber, at least not the action {} (the only approved actions are {})",new Object[]{cr.getMode(),action,subscriberOutput.getApprovedActions()});

                throw new ActionNotRequestedByTheSubscriberException("action not approved");
            }else {
                subscriberOutput.getApprovedActions().remove(action);
            }

        }

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/x-www-form-urlencoded");

        try {
            SubscriptionConfirmationRequest scr=SubscriptionConfirmationRequest.from(request);
            ConfirmSubscriberRequestedSubscription(scr);
            response.getWriter().print(scr.getChallenge());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception invalidContentNotification) {
            LOG.error("Invalid content notification.", invalidContentNotification);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String hubmode = null, hubtopic = null, hubchallenge = null, hubverify = null, hublease = null;
        ArrayList<String> approvedActions;
        MessageStatus stsMessage = MessageStatus.ERROR;

        ClassLoader cl=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(SyndFeedInput.class.getClassLoader());

        if (request.getContentType().contains("application/atom+xml") || request.getContentType().contains("application/rss+xml")){

            InputStream in= request.getInputStream();

            try {
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(in));

                List<SyndLinkImpl> linkList = feed.getLinks();

                for (SyndLinkImpl link : linkList) {

                    if (link.getRel().equals("self")){
                        hubtopic = link.getHref().toString();
                    }
                }

                if (hubtopic == null){
                    hubtopic= feed.getUri();
                }

                TopicUpdated(hubtopic, feed);
            } catch (FeedException e) {
                LOG.error("Failed in creating feed response.", e);
            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }


            stsMessage = MessageStatus.OK;
        }

        response.setContentType("application/x-www-form-urlencoded");

        switch(stsMessage) {
            case OK:
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            case OK_Challenge:
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print(hubchallenge);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                break;
        }

    }

}	