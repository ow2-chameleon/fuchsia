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
import org.ow2.chameleon.fuchsia.core.constant.MediaType;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.InvalidContentNotification;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.ow2.chameleon.fuchsia.push.base.hub.HubConstants.HUB_MODE;
import static org.ow2.chameleon.fuchsia.push.base.hub.HubConstants.HUB_URL;

public class ContentNotification {

    private String mode;
    private String url;

    public ContentNotification(String mode, String url) {
        this.mode = mode;
        this.url = url;
    }

    public static ContentNotification from(HttpServletRequest request) throws InvalidContentNotification {

        validateRequest(request);

        ContentNotification cn = new ContentNotification(
                request.getParameter(HUB_MODE),
                request.getParameter(HUB_URL)
        );


        return cn;
    }

    public List<NameValuePair> toRequesParameters() {

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(HUB_MODE, getMode()));
        nvps.add(new BasicNameValuePair(HUB_URL, getUrl()));

        return nvps;

    }

    public String getMode() {
        return mode;
    }

    public String getUrl() {
        return url;
    }

    private static void validateRequest(HttpServletRequest request) throws InvalidContentNotification {
        if (!MediaType.APPLICATION_FORM_URLENCODED.equals(request.getContentType())) {
            throw new InvalidContentNotification("Invalid content type");
        }

        if (request.getParameter(HUB_MODE) == null) {
            throw new InvalidContentNotification("No " + HUB_MODE + " provided");
        }

        if (request.getParameter(HUB_URL) == null) {
            throw new InvalidContentNotification("No " + HUB_URL + " provided");
        }
    }
}

