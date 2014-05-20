package org.ow2.chameleon.fuchsia.testing.common.services;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Base Test: Utilities for test
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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.Dictionary;


/**
 * Basic implementation of HttpService from OSGi which can be injected in a mock instance.
 */
public class HttpServiceImpl implements HttpService {

    Server server;

    public HttpServiceImpl(int port) throws Exception {
        server = new Server(port);
    }

    public void registerServlet(String context, Servlet servlet, Dictionary dictionary, HttpContext httpContext) throws ServletException, NamespaceException {

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        server.setHandler(contexts);
        ServletContextHandler root = new ServletContextHandler(contexts, "/",
                ServletContextHandler.SESSIONS);
        ServletHolder servletHolder = new ServletHolder(servlet);
        root.addServlet(servletHolder, context);

        if (!server.getServer().getState().equals(server.STARTED)) {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void registerResources(String s, String s2, HttpContext httpContext) throws NamespaceException {
        throw new UnsupportedOperationException("Resource registering is not allowed in the mock implementation of httpservice");
    }

    public void unregister(String s) {

    }

    public HttpContext createDefaultHttpContext() {
        return null;
    }

    public Server getServer() {
        return server;
    }
}

