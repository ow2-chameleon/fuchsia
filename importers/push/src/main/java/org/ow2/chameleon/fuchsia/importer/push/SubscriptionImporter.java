package org.ow2.chameleon.fuchsia.importer.push;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer for PuSH (PUbSbHubbub)
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

import org.apache.felix.ipojo.annotations.*;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.constant.HttpHeaders;
import org.ow2.chameleon.fuchsia.core.constant.HttpStatus;
import org.ow2.chameleon.fuchsia.core.constant.MediaType;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionException;
import org.ow2.chameleon.fuchsia.push.base.subscriber.SubscriberOutput;
import org.ow2.chameleon.fuchsia.push.base.subscriber.servlet.CallbackServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static org.ow2.chameleon.fuchsia.importer.push.Constants.*;

@Component
@Provides(specifications = {ImporterService.class, ImporterIntrospection.class, SubscriberOutput.class})
public class SubscriptionImporter extends AbstractImporterComponent implements SubscriberOutput {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionImporter.class);

    public static final String PUBSUBHUBBUB_USER_AGENT = "RSS pubsubhubbub 0.3";

    private static List<String> APPROVED_ACTIONS = new Vector<String>();

    private List<String> callbacksRegistered = new ArrayList<String>();


    @ServiceProperty(name = "target", value = "(&(push.hub.url=*)(scope=generic))")
    private String filter;

    @Requires
    HttpService httpService;

    @Requires
    EventAdmin eventAdmin;

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Validate
    public void start() {
        LOG.info("PuSH importer started.");
        super.start();
    }

    @Invalidate
    public void stop() {
        LOG.info("PuSH importer stopped.");
        super.stop();
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int subscribe(String hub, String topicUrl, String hostname, String verifyToken, String leaseSeconds) throws SubscriptionException {
        if (topicUrl != null) {
            UrlEncodedFormEntity entity = buildUrlEncodedFormEntity(hostname, topicUrl, verifyToken, "subscribe");
            HttpPost httppost = new HttpPost(hub);

            addAction("subscribe", topicUrl, verifyToken);

            httppost.setEntity(entity);
            httppost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
            httppost.setHeader(HttpHeaders.USER_AGENT, PUBSUBHUBBUB_USER_AGENT);

            return executeRequest(httppost, "subscription");
        }
        return HttpStatus.SC_BAD_REQUEST;
    }

    public int unsubscribe(String hub, String topicUrl, String hostname, String verifyToken) throws SubscriptionException {
        if (topicUrl != null) {
            UrlEncodedFormEntity entity = buildUrlEncodedFormEntity(hostname, topicUrl, verifyToken, "unsubscribe");
            HttpPost httppost = new HttpPost(hub);

            addAction("unsubscribe", topicUrl, verifyToken);

            httppost.setEntity(entity);
            httppost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
            httppost.setHeader(HttpHeaders.USER_AGENT, PUBSUBHUBBUB_USER_AGENT);

            return executeRequest(httppost, "unsubscription");
        }
        return HttpStatus.SC_BAD_REQUEST;
    }

    private int executeRequest(HttpPost httppost, String action) throws SubscriptionException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpResponse response;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            throw new SubscriptionException("IOException during stop of " + action, e);
        }

        if (response != null) {
            return response.getStatusLine().getStatusCode();
        } else {
            return HttpStatus.SC_BAD_REQUEST;
        }
    }

    private UrlEncodedFormEntity buildUrlEncodedFormEntity(String callbackserverurl, String topicUrl, String verifyToken, String mode) throws SubscriptionException {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(HUB_CALLBACK, callbackserverurl));
        nvps.add(new BasicNameValuePair(HUB_MODE, mode));
        nvps.add(new BasicNameValuePair(HUB_TOPIC, topicUrl));
        nvps.add(new BasicNameValuePair(HUB_VERIFY, "sync"));
        //For future https implementation
        //if ((secret !=null) && (secret.getBytes("utf8").length < 200))
        //nvps.add(new BasicNameValuePair("hub.hub.secret", secret));
        if (verifyToken != null) {
            nvps.add(new BasicNameValuePair(HUB_VERIFY_TOKEN, verifyToken));
        }

        try {
            return new UrlEncodedFormEntity(nvps);
        } catch (UnsupportedEncodingException e) {
            throw new SubscriptionException("UnsupportedEncodingException thrown while stopping subscription", e);
        }
    }

    private void addAction(String hubmode, String hubtopic, String hubverify) {
        String action = hubmode + ":" + hubtopic + ":" + hubverify;
        APPROVED_ACTIONS.add(action);
    }

    public List<String> getApprovedActions() {
        return APPROVED_ACTIONS;
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("adding import declaration {}", importDeclaration);

        try {

            Map<String, Object> data = importDeclaration.getMetadata();

            String hub = data.get("push.hub.url").toString();
            String hubTopic = data.get("push.hub.topic").toString();
            String callback = data.get("push.subscriber.callback").toString();

            URI callbackURI = new URI(callback);

            httpService.registerServlet(callbackURI.getPath(), new CallbackServlet(eventAdmin, importDeclaration, this), null, null);

            int statusCode = subscribe(hub, hubTopic, callback, null, null);

            if (statusCode == HttpStatus.SC_NO_CONTENT) {
                LOG.info("the status code of the subscription is 204: the request was verified and that the subscription is active");
            } else if (statusCode == HttpStatus.SC_ACCEPTED) {
                LOG.info("the status code of the subscription is 202: the subscription has yet to be verified (asynchronous verification)");
            } else {
                LOG.info("the status code of the subscription is {}", statusCode);
            }

            callbacksRegistered.add(callback);

            super.handleImportDeclaration(importDeclaration);
        } catch (Exception e) {
            LOG.error("failed to import declaration, with the message: " + e.getMessage(), e);
        }

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        LOG.info("removing import declaration {}", importDeclaration);

        Map<String, Object> data = importDeclaration.getMetadata();
        String hub = data.get("push.hub.url").toString();//"http://localhost:8080/subscribe";
        String hubTopic = data.get("push.hub.topic").toString();//"http://blogname.blogspot.com/feeds/posts/default";
        String targetCallback = data.get("push.subscriber.callback").toString();

        super.unhandleImportDeclaration(importDeclaration);

        for (String callback : callbacksRegistered) {

            if (callback.equals(targetCallback)) {

                LOG.info("Removing callback {}", callback);

                httpService.unregister(callback);

                try {

                    unsubscribe(hub, hubTopic, targetCallback, null);

                    LOG.info("Callback {} removed from the subscriber", callback);

                } catch (SubscriptionException e) {
                    LOG.error("Callback " + callback + " failed to be removed from the subscriber with the message", e);
                }

                break;
            }

        }

    }

}
