package org.ow2.chameleon.fuchsia.exporter.jsonrpc;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Exporter JSON-RPC
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

import com.googlecode.jsonrpc4j.JsonRpcServer;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractExporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.exporter.jsonrpc.model.JSONRPCExportDeclarationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

/**
 * Provides an {@link ImporterService} allowing to access a.
 * remote endpoint through jsonrpc thanks to the jsonrpc4j implementation.
 * <p/>
 * A valid {@link ImportDeclaration} for this ImporterService contains has metadata :
 * - ID : a unique String which is the id of the JSON-RPC service
 * - URL : a String containing the URL of the JSON-RPC service to import into OSGi.
 * - SERVICE_CLASS : a String containing the name of the class to use to build the proxy
 * <p/>
 * TODO : Improves the client management, only one client should be created for a given uri.
 */
@Component
@Provides(specifications = {ExporterService.class})
public class JSONRPCExporter extends AbstractExporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(JSONRPCExporter.class);

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @ServiceProperty(name = "target", value = "(&(fuchsia.export.jsonrpc.instance=*)(scope=generic))")
    private String filter;

    @Requires
    HttpService web;

    private Set<String> registeredServlets = new HashSet<String>();

    private final BundleContext context;

    private ServiceReference serviceReference;

    public JSONRPCExporter(BundleContext pContext) {
        context = pContext;
    }

    @Override
    protected void useExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {
        Class<?> klass = null;
        JSONRPCExportDeclarationWrapper jp = JSONRPCExportDeclarationWrapper.create(exportDeclaration);

        try {
            klass = FuchsiaUtils.loadClass(context, jp.getInstanceClass());
        } catch (ClassNotFoundException e) {
            LOG.warn("Failed to load from the own bundle, loading externally", e);
        }

        String osgiFilter = String.format("(instance.name=%s)", jp.getInstanceName());
        List<ServiceReference> references = null;
        try {
            references = new ArrayList<ServiceReference>(context.getServiceReferences(klass, osgiFilter));
        } catch (InvalidSyntaxException e) {
            LOG.error("LDAP filter specified on the linker is not valid, recheck your LDAP filters for the linker and exporter. ", e);
            return;
        }

        Object serviceToBePublished = context.getService(references.iterator().next());
        final JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBePublished, klass);
        final String endpointURL = String.format("%s/%s", jp.getUrlContext(), jp.getInstanceName());
        Servlet gs = new RPCServlet(jsonRpcServer);

        try {
            web.registerServlet(endpointURL, gs, new Hashtable(), null);
            exportDeclaration.handle(serviceReference);
            registeredServlets.add(endpointURL);
            LOG.info("JSONRPC-exporter, publishing object exporter at: {}", endpointURL);
        } catch (NamespaceException e) {
            LOG.error("Namespace failure", e);
        } catch (ServletException e) {
            LOG.error("Failed registering the servlet to respond the RPC request.", e);
        }
    }

    @Override
    protected void denyExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {

        JSONRPCExportDeclarationWrapper jp = JSONRPCExportDeclarationWrapper.create(exportDeclaration);

        final String endpointURL = String.format("%s/%s", jp.getUrlContext(), jp.getInstanceName());

        exportDeclaration.unhandle(serviceReference);

        registeredServlets.remove(endpointURL);

        web.unregister(endpointURL);

    }

    public String getName() {
        return name;
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }

    /*
     * (non-Javadoc)
     * @see org.ow2.chameleon.rose.AbstractImporterComponent#stop()
     */
    @Override
    @Invalidate
    public void stop() {
        unregisterAllServlets();
    }

    /**
     * Unregister all servlets registered by this exporter.
     */
    private void unregisterAllServlets() {

        for (String endpoint : registeredServlets) {
            registeredServlets.remove(endpoint);
            web.unregister(endpoint);
            LOG.info("endpoint {} unregistered", endpoint);
        }

    }

    static class RPCServlet extends HttpServlet {

        private final JsonRpcServer jsonRpcServer;

        public RPCServlet(JsonRpcServer jsonRpcServer) {
            this.jsonRpcServer = jsonRpcServer;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            jsonRpcServer.handle(req, resp);
        }

    }

}


