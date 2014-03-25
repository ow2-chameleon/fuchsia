package org.ow2.chameleon.fuchsia.push.base.hub.servlet;

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

import org.ow2.chameleon.fuchsia.core.constant.HttpStatus;
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

    private static final Logger LOG = LoggerFactory.getLogger(ContentUpdatedNotificationServlet.class);

    private HubInput hub;

    public ContentUpdatedNotificationServlet(HubInput hub) {
        this.hub = hub;
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

            LOG.info("Publisher -> (Hub), new content notification received for the topic {}", cn.getUrl());

            hub.contentNotificationReceived(cn);

        } catch (InvalidContentNotification invalidContentNotification) {
            LOG.error("Failed to parse notification", invalidContentNotification);
            //bad request
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
        } finally {
            //no content (notification accepted)
            resp.setStatus(HttpStatus.SC_NO_CONTENT);
        }

    }

}
