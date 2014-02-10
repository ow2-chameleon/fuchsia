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
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.ContentNotification;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionConfirmationRequest;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionRequest;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionException;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionOriginVerificationException;
import org.ow2.chameleon.fuchsia.push.base.hub.servlet.ContentUpdatedNotificationServlet;
import org.ow2.chameleon.fuchsia.push.base.hub.servlet.SubscriptionServlet;
import org.ow2.chameleon.fuchsia.push.base.hub.Hub;
import org.ow2.chameleon.fuchsia.push.base.hub.Hub;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionConfirmationRequest;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionRequest;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionOriginVerificationException;
import org.ow2.chameleon.fuchsia.push.base.hub.servlet.ContentUpdatedNotificationServlet;
import org.ow2.chameleon.fuchsia.push.base.hub.servlet.SubscriptionServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(name="PuSHHubFactory")
public class HubImpl implements Hub {

    @Requires
    HttpService http;

    BundleContext context;

    Map<String, List<String>> topicCallbackSubscriptionMap = new HashMap<String, List<String>>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HubImpl() {}

    public HubImpl(BundleContext context) {
        this.context = context;
    }

    @Validate
    public void start() {
        try {

            http.registerServlet("/hub/subscribe", new SubscriptionServlet(this), null, null);

            http.registerServlet("/hub/main", new ContentUpdatedNotificationServlet(this), null, null);

            logger.info("Hub started.");

        } catch (Exception e) {

            logger.error("Failed to startup hub urls",e);

        }
    }

    /**
     * Input method, called by a Publisher indicating that a new content is available. This method should either analyse
     * the http header info to verify that changes were really made or download the content and verify for himself if the content has changed
     * @param cn DTO with the notification info given by the protocol
     */
    public void ContentNotificationReceived(ContentNotification cn) {

        logger.info("Publisher -> (Hub), notification of new content available for the topic {}.",cn.getUrl());

        NotifySubscriberCallback(cn);

    }

    /**
     * Input method, called by a Subscriber indicating its intent into receive notification about a given topic
     * @param sr DTO containing the info given by the protocol
     */
    public void SubscriptionRequestReceived(SubscriptionRequest sr) throws SubscriptionException {

        logger.info("Subscriber -> (Hub), new subscription request received.",sr.getCallback());

        try {

            VerifySubscriberRequestedSubscription(sr);

            if(sr.getMode().equals("subscribe")){
                logger.info("Adding callback {} to the topic {}",sr.getCallback(),sr.getTopic());
                addCallbackToTopic(sr.getCallback(),sr.getTopic());
            }else if(sr.getMode().equals("unsubscribe")){
                logger.info("Removing callback {} from the topic {}",sr.getCallback(),sr.getTopic());
                removeCallbackToTopic(sr.getCallback(),sr.getTopic());
            }

        } catch (Exception e) {
            throw new SubscriptionException(e);
        }

    }

    /**
     * Output method that sends a subscription confirmation for the subscriber to avoid DoS attacks, or false subscription
     * @param sr
     * @return True case the subscription was confirmed, or False otherwise
     * @throws org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionOriginVerificationException
     * @throws URISyntaxException
     * @throws IOException
     */
    public Boolean VerifySubscriberRequestedSubscription(SubscriptionRequest sr) throws SubscriptionOriginVerificationException, URISyntaxException, IOException {

        logger.info("(Hub) -> Subscriber, sending notification to verify the origin of the subscription {}.", sr.getCallback());

        SubscriptionConfirmationRequest sc = new SubscriptionConfirmationRequest(sr.getMode(),
                sr.getTopic(), "challenge", "0");

        URI uri = new URIBuilder(sr.getCallback()).setParameters(sc.toRequestParameters()).build();

        HttpGet httpGet = new HttpGet(uri);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        CloseableHttpResponse response1 = httpclient.execute(httpGet);

        logger.info("Subscriber replied with the http code {}.",response1.getStatusLine().getStatusCode());

        Integer returnedCode=response1.getStatusLine().getStatusCode();

        if(returnedCode>199 && returnedCode<300){
            return true;
        }

        return false;
    }

    /**
     * Output method responsible for sending the updated content to the Subscribers
     * @param cn
     */
    public void NotifySubscriberCallback(ContentNotification cn) {

        String content=fetchContentFromPublisher(cn);

        distributeContentToSubscribers(content, cn.getUrl());


    }

    private void addCallbackToTopic(String callback,String topic){

        List<String> callbacks = topicCallbackSubscriptionMap.get(topic);

        if (callbacks == null) {
            callbacks = new ArrayList<String>();
            topicCallbackSubscriptionMap.put(topic,callbacks);
        }

        callbacks.add(callback);

    }

    private void removeCallbackToTopic(String callback,String topic){

        List<String> callbacks = topicCallbackSubscriptionMap.get(topic);

        if (callbacks == null) {
            callbacks = new ArrayList<String>();
            topicCallbackSubscriptionMap.put(topic,callbacks);
        }

        Boolean removedFromList=callbacks.remove(callback);

        if(removedFromList){
            logger.info("Hub, callback {} removed from the topic {}",callback,topic);
        }else {
            logger.info("Hub, callback {} was not present for the topic {}",callback,topic);
        }

    }

    private String fetchContentFromPublisher(ContentNotification cn) {

        logger.info("(Hub) -> Publisher, fetching content from the publisher on the url {}", cn.getUrl());

        HttpGet httpGet = new HttpGet(cn.getUrl());

        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {

            CloseableHttpResponse response = httpclient.execute(httpGet);

            String contentReceived=inputStreamToString(response.getEntity().getContent());

            logger.info("(Hub) -> Publisher, Content fetched from publisher \n{}\n",contentReceived);

            return contentReceived;

        } catch (IOException e) {

            logger.info("(Hub) -> Publisher, Failed to fetched from publisher",e);
        }

        return null;

    }

    private void distributeContentToSubscribers(String content, String topic) {

        List<String> callbacks = topicCallbackSubscriptionMap.get(topic);

        System.out.println(String.format("The topic [%s] has the callbacks %s",topic,callbacks));


        if(callbacks!=null)
        for (String callback : callbacks) {

            HttpPost httpPost = new HttpPost(callback);

            try {

                httpPost.setEntity(new StringEntity(content));
                httpPost.setHeader("Content-Type","application/atom+xml");
                System.out.println("publisher --> Distributed content to callback: " + callback);

                CloseableHttpClient httpclient = HttpClients.createDefault();

                logger.info("(Hub) -> Subscriber, send content to subscriber {}",callback);

                CloseableHttpResponse response=httpclient.execute(httpPost);

                logger.info("(Hub) -> Subscriber, subcribers response {}",response.getStatusLine().getStatusCode());

            } catch (Exception e) {

                logger.info("Failed distributing content to subscribers",e);

            }
        }


    }

    private static String inputStreamToString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        in.close();
        return sb.toString();
    }



}
