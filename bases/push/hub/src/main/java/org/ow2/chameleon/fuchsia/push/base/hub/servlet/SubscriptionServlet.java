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

            resp.setStatus(HttpStatus.SC_NO_CONTENT);

        } catch (Exception exception) {

            resp.setStatus(HttpStatus.SC_BAD_REQUEST);

            LOG.error("Failed to created response to the request", exception);

        }


    }

}
