package org.ow2.chameleon.fuchsia.push.base.hub.dto;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Base PUbSubHubbub Hub
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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.InvalidContentNotification;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.ow2.chameleon.fuchsia.push.base.hub.HubConstants.*;

public class SubscriptionRequest {

    String callback;
    String mode;
    String topic;
    String verify;

    public SubscriptionRequest(String callback, String mode, String topic, String verify) {

        this.callback = callback;
        this.mode = mode;
        this.topic = topic;
        this.verify = verify;

    }

    public static SubscriptionRequest from(HttpServletRequest request) throws InvalidContentNotification {

        validateRequest(request);

        SubscriptionRequest sr = new SubscriptionRequest(
                request.getParameter(HUB_CALLBACK),
                request.getParameter(HUB_MODE),
                request.getParameter(HUB_TOPIC),
                request.getParameter(HUB_VERIFY)
        );

        return sr;

    }

    public List<NameValuePair> toRequestParameters() {

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(HUB_CALLBACK, getCallback()));
        nvps.add(new BasicNameValuePair(HUB_MODE, getMode()));
        nvps.add(new BasicNameValuePair(HUB_TOPIC, getTopic()));
        nvps.add(new BasicNameValuePair(HUB_VERIFY, getVerify()));

        return nvps;

    }

    private static void validateRequest(HttpServletRequest request) throws InvalidContentNotification {

        if (
                request.getParameter(HUB_CALLBACK) == null ||
                        request.getParameter(HUB_MODE) == null ||
                        request.getParameter(HUB_TOPIC) == null ||
                        request.getParameter(HUB_VERIFY) == null
                ) {
            throw new InvalidContentNotification("Parameters requested not defined in the request");
        }
    }

    public String getCallback() {
        return callback;
    }

    public String getMode() {
        return mode;
    }

    public String getTopic() {
        return topic;
    }

    public String getVerify() {
        return verify;
    }
}
