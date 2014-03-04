package org.ow2.chameleon.fuchsia.push.base.hub.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.osgi.service.http.HttpService;
import org.ow2.chameleon.fuchsia.core.constant.HttpHeaders;
import org.ow2.chameleon.fuchsia.core.constant.MediaType;
import org.ow2.chameleon.fuchsia.push.base.hub.Hub;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.ContentNotification;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionConfirmationRequest;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionRequest;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionException;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionOriginVerificationException;
import org.ow2.chameleon.fuchsia.push.base.hub.servlet.ContentUpdatedNotificationServlet;
import org.ow2.chameleon.fuchsia.push.base.hub.servlet.SubscriptionServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(name = "PuSHHubFactory")
public class HubImpl implements Hub {

    private static final Logger LOG = LoggerFactory.getLogger(HubImpl.class);

    @Requires
    private HttpService http;

    private Map<String, List<String>> topicCallbackSubscriptionMap = new HashMap<String, List<String>>();

    public HubImpl() {

    }

    @Validate
    public void start() {
        try {

            http.registerServlet("/hub/subscribe", new SubscriptionServlet(this), null, null);

            http.registerServlet("/hub/main", new ContentUpdatedNotificationServlet(this), null, null);

            LOG.info("Hub started.");

        } catch (Exception e) {

            LOG.error("Failed to startup hub urls", e);

        }
    }

    /**
     * Input method, called by a Publisher indicating that a new content is available. This method should either analyse
     * the http header info to verify that changes were really made or download the content and verify for himself if the content has changed
     *
     * @param cn DTO with the notification info given by the protocol
     */
    public void contentNotificationReceived(ContentNotification cn) {

        LOG.info("Publisher -> (Hub), notification of new content available for the topic {}.", cn.getUrl());

        notifySubscriberCallback(cn);

    }

    /**
     * Input method, called by a Subscriber indicating its intent into receive notification about a given topic
     *
     * @param sr DTO containing the info given by the protocol
     */
    public void subscriptionRequestReceived(SubscriptionRequest sr) throws SubscriptionException {

        LOG.info("Subscriber -> (Hub), new subscription request received.", sr.getCallback());

        try {

            verifySubscriberRequestedSubscription(sr);

            if ("subscribe".equals(sr.getMode())) {
                LOG.info("Adding callback {} to the topic {}", sr.getCallback(), sr.getTopic());
                addCallbackToTopic(sr.getCallback(), sr.getTopic());
            } else if ("unsubscribe".equals(sr.getMode())) {
                LOG.info("Removing callback {} from the topic {}", sr.getCallback(), sr.getTopic());
                removeCallbackToTopic(sr.getCallback(), sr.getTopic());
            }

        } catch (Exception e) {
            throw new SubscriptionException(e);
        }

    }

    /**
     * Output method that sends a subscription confirmation for the subscriber to avoid DoS attacks, or false subscription
     *
     * @param sr
     * @return True case the subscription was confirmed, or False otherwise
     * @throws org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionOriginVerificationException
     */
    public Boolean verifySubscriberRequestedSubscription(SubscriptionRequest sr) throws SubscriptionOriginVerificationException {

        LOG.info("(Hub) -> Subscriber, sending notification to verify the origin of the subscription {}.", sr.getCallback());

        SubscriptionConfirmationRequest sc = new SubscriptionConfirmationRequest(sr.getMode(),
                sr.getTopic(), "challenge", "0");

        URI uri;
        try {
            uri = new URIBuilder(sr.getCallback()).setParameters(sc.toRequestParameters()).build();
        } catch (URISyntaxException e) {
            throw new SubscriptionOriginVerificationException("URISyntaxException while sending a confirmation of subscription", e);
        }

        HttpGet httpGet = new HttpGet(uri);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        CloseableHttpResponse response;
        try {
            response = httpclient.execute(httpGet);
        } catch (IOException e) {
            throw new SubscriptionOriginVerificationException("IOException while sending a confirmation of subscription", e);
        }

        LOG.info("Subscriber replied with the http code {}.", response.getStatusLine().getStatusCode());

        Integer returnedCode = response.getStatusLine().getStatusCode();

        // if code is a success code return true, else false
        return (199 < returnedCode) && (returnedCode < 300);
    }

    /**
     * Output method responsible for sending the updated content to the Subscribers
     *
     * @param cn
     */
    public void notifySubscriberCallback(ContentNotification cn) {
        String content = fetchContentFromPublisher(cn);

        distributeContentToSubscribers(content, cn.getUrl());
    }

    private void addCallbackToTopic(String callback, String topic) {
        List<String> callbacks = topicCallbackSubscriptionMap.get(topic);

        if (callbacks == null) {
            callbacks = new ArrayList<String>();
            topicCallbackSubscriptionMap.put(topic, callbacks);
        }

        callbacks.add(callback);
    }

    private void removeCallbackToTopic(String callback, String topic) {

        List<String> callbacks = topicCallbackSubscriptionMap.get(topic);

        if (callbacks == null) {
            callbacks = new ArrayList<String>();
            topicCallbackSubscriptionMap.put(topic, callbacks);
        }

        Boolean removedFromList = callbacks.remove(callback);

        if (removedFromList) {
            LOG.info("Hub, callback {} removed from the topic {}", callback, topic);
        } else {
            LOG.info("Hub, callback {} was not present for the topic {}", callback, topic);
        }

    }

    private String fetchContentFromPublisher(ContentNotification cn) {

        LOG.info("(Hub) -> Publisher, fetching content from the publisher on the url {}", cn.getUrl());

        HttpGet httpGet = new HttpGet(cn.getUrl());

        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {

            CloseableHttpResponse response = httpclient.execute(httpGet);

            String contentReceived = inputStreamToString(response.getEntity().getContent());

            LOG.info("(Hub) -> Publisher, Content fetched from publisher \n{}\n", contentReceived);

            return contentReceived;

        } catch (IOException e) {

            LOG.info("(Hub) -> Publisher, Failed to fetched from publisher", e);
        }

        return null;

    }

    private void distributeContentToSubscribers(String content, String topic) {

        List<String> callbacks = topicCallbackSubscriptionMap.get(topic);

        LOG.info(String.format("The topic [%s] has the callbacks %s", topic, callbacks));


        if (callbacks != null) {
            for (String callback : callbacks) {

                HttpPost httpPost = new HttpPost(callback);

                try {

                    httpPost.setEntity(new StringEntity(content));
                    httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_ATOM_XML);
                    LOG.info("publisher --> Distributed content to callback: " + callback);

                    CloseableHttpClient httpclient = HttpClients.createDefault();

                    LOG.info("(Hub) -> Subscriber, send content to subscriber {}", callback);

                    CloseableHttpResponse response = httpclient.execute(httpPost);

                    LOG.info("(Hub) -> Subscriber, subcribers response {}", response.getStatusLine().getStatusCode());

                } catch (Exception e) {

                    LOG.info("Failed distributing content to subscribers", e);

                }
            }
        }
    }

    private static String inputStreamToString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            sb.append(line).append("\n");
            line = reader.readLine();
        }
        in.close();
        return sb.toString();
    }


}
