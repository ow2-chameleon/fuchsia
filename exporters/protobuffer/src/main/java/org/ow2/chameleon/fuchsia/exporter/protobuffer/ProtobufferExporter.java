package org.ow2.chameleon.fuchsia.exporter.protobuffer;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Exporter Protobuffer
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

import com.google.code.cxf.protobuf.ProtobufServerFactoryBean;
import com.google.code.cxf.protobuf.binding.ProtobufBindingFactory;
import com.google.protobuf.Service;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.felix.ipojo.annotations.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.Container;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractExporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.exporter.protobuffer.internal.ProtobufferExportDeclarationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Provides(specifications = {ExporterService.class})
public class ProtobufferExporter extends AbstractExporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(ProtobufferExporter.class);

    private final BundleContext context;

    private Map<String, Server> serverPublished = new HashMap<String, Server>();

    private org.eclipse.jetty.server.Server httpServer;

    @ServiceProperty(name = "org.osgi.service.http.port")
    private Integer httpPort;

    private Bus cxfbus;

    @Requires
    private HttpService http;

    @ServiceProperty(name = "target", value = "(&(rpc.export.address=*)(scope=generic))")
    private String filter;

    public ProtobufferExporter(BundleContext context) {
        this.context = context;
    }

    @Validate
    public void start() {
        System.setProperty("org.apache.cxf.nofastinfoset", "true");
        super.start();

        CXFNonSpringServlet cxfServlet = new CXFNonSpringServlet();

        if (http != null) {
            try {
                http.registerServlet("/cxf", cxfServlet, null, null);
            } catch (ServletException e) {
                LOG.error("Failed registering CXF servlet", e);
            } catch (NamespaceException e) {
                LOG.error("Failed registering CXF servlet", e);
            }
        } else {
            try {
                cxfServlet = configStandaloneServer();
                httpServer.start();
            } catch (Exception e1) {
                LOG.error("Impossible to start standalone CXF Jetty server.", e1);
            }
        }
        cxfbus = cxfServlet.getBus();
    }

    private CXFServlet configStandaloneServer() {
        httpServer = new org.eclipse.jetty.server.Server(httpPort);
        Bus bus = BusFactory.getDefaultBus(true);
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        httpServer.setHandler(contexts);
        ServletContextHandler root = new ServletContextHandler(contexts, "/",
                ServletContextHandler.SESSIONS);
        CXFServlet cxf = new CXFServlet();
        cxf.setBus(bus);
        ServletHolder servlet = new ServletHolder(cxf);
        root.addServlet(servlet, "/cxf/*");
        return cxf;
    }

    @Invalidate
    public void stop() {
        super.stop();
        if (httpServer != null) {
            try {
                httpServer.stop();
            } catch (Exception e) {
                LOG.warn("Failed to stop standalone server.", e);
            }
        }
        for (Map.Entry<String, Server> entry : serverPublished.entrySet()) {
            serverPublished.remove(entry.getKey());
            entry.getValue().stop();
        }
    }

    @Override
    protected void useExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {

        LOG.info("initiating exportation...");

        ProtobufferExportDeclarationWrapper pojo = ProtobufferExportDeclarationWrapper.create(exportDeclaration);
        Class inter, messageClass;
        try {
            inter = FuchsiaUtils.loadClass(context, String.format("%s$%s", pojo.getClazz(), pojo.getService()));
            messageClass = FuchsiaUtils.loadClass(context, String.format("%s$%s", pojo.getClazz(), pojo.getMessage()));
        } catch (ClassNotFoundException e) {
            LOG.error("Class not found", e);
            return;
        }
        LOG.info("Looking for service that provides class {}", pojo.getClazz() + "$" + pojo.getService());
        Collection<ServiceReference<Service>> protobuffReferences = null;
        try {
            protobuffReferences = context.getServiceReferences(inter, pojo.getFilter());
        } catch (InvalidSyntaxException e) {
            LOG.error("Invalid filter exception", e);
            return;
        }

        LOG.info("using filter " + pojo.getFilter() + " to find instance");
        if (protobuffReferences.isEmpty()) {
            LOG.info("nothing to be exported was found");
        } else if (protobuffReferences.size() == 1) {
            for (ServiceReference<Service> sr : protobuffReferences) {
                Service protobufferService = context.getService(sr);
                BindingFactoryManager mgr = cxfbus.getExtension(BindingFactoryManager.class);
                mgr.registerBindingFactory(ProtobufBindingFactory.PROTOBUF_BINDING_ID, new ProtobufBindingFactory(cxfbus));
                ProtobufServerFactoryBean serverFactoryBean = new ProtobufServerFactoryBean();
                serverFactoryBean.setAddress(pojo.getAddress());
                serverFactoryBean.setBus(cxfbus);
                serverFactoryBean.setServiceBean(inter.cast(protobufferService));
                serverFactoryBean.setMessageClass(messageClass);
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(Container.class.getClassLoader());
                Server server = serverFactoryBean.create();
                serverPublished.put(pojo.getId(), server);
                Thread.currentThread().setContextClassLoader(loader);
                LOG.info("exporting the service with the id:" + pojo.getId());
            }
        } else {
            LOG.info("more than one were found to be exported");
        }
    }

    @Override
    protected void denyExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {

        ProtobufferExportDeclarationWrapper pojo = ProtobufferExportDeclarationWrapper.create(exportDeclaration);

        Server server = serverPublished.get(pojo.getId());

        serverPublished.remove(pojo.getId());

        if (server != null) {
            LOG.info("Destroying endpoint:" + server.getEndpoint().getEndpointInfo().getAddress());
            server.destroy();
        } else {
            LOG.warn("no endpoint to destroy");
        }

    }

    public String getName() {
        return this.getClass().getName();
    }
}
