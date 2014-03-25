package org.ow2.chameleon.fuchsia.exporter.jaxws;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Exporter JAX-WS
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

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.felix.ipojo.annotations.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractExporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.exporter.jaxws.internal.JAXWSExportDeclarationWrapper;
import org.ow2.chameleon.fuchsia.exporter.jaxws.internal.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;

@Component
@Provides(specifications = {ExporterService.class})
public class JAXWSExporter extends AbstractExporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(JAXWSExporter.class);

    private final BundleContext context;

    private Bus cxfbus;

    private ServiceReference serviceReference;

    private Map<String, org.apache.cxf.endpoint.Server> exportedDeclaration = new HashMap<String, org.apache.cxf.endpoint.Server>();

    @Requires
    private HttpService http;

    private Server httpServer;

    @ServiceProperty(name = "org.osgi.service.http.port")
    private Integer httpPort;

    @ServiceProperty(name = "target")
    private String filter;

    public JAXWSExporter(BundleContext context) {
        this.context = context;
    }

    @Override
    public void useExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {

        LOG.info("exporting {}", exportDeclaration.getMetadata());

        JAXWSExportDeclarationWrapper pojo = JAXWSExportDeclarationWrapper.create(exportDeclaration);

        try {

            ServerFactoryBean srvFactory = new ServerFactoryBean();

            Class ref = FuchsiaUtils.loadClass(this.context, pojo.getClazz());

            srvFactory.setServiceClass(ref);

            srvFactory.setBus(cxfbus);

            Object instance = null;

            ServiceReference[] jaxWsReferences = context.getAllServiceReferences(pojo.getClazz(), pojo.getFilter());

            if (jaxWsReferences == null) {
                LOG.warn("instance not found to be exported, ignoring exportation request, filter:" + pojo.getFilter());
                return;
            }

            Object object = context.getService(jaxWsReferences[0]);

            srvFactory.setServiceBean(object);

            srvFactory.setAddress(pojo.getWebcontext());

            org.apache.cxf.endpoint.Server endpoint = srvFactory.create();

            exportDeclaration.handle(serviceReference);

            exportedDeclaration.put(pojo.getWebcontext(), endpoint);

            srvFactory.getServer().start();

            LOG.info("Pushing CXF endpoint: {}", endpoint.getEndpoint().getEndpointInfo().getAddress());

        } catch (Exception e) {
            LOG.error("Failed exporting in CXF", e);
            throw new BinderException(e);
        }
    }

    @Override
    protected void denyExportDeclaration(ExportDeclaration exportDeclaration) {

        LOG.info("destroying exportation {}", exportDeclaration.getMetadata());

        final String webcontext = (String) exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_WEB_CONTEXT);

        exportDeclaration.unhandle(serviceReference);

        org.apache.cxf.endpoint.Server exported = exportedDeclaration.get(webcontext);

        if (exported != null) {
            exported.destroy();
            LOG.info("Endpoint destroyed: {}", webcontext);
        } else {
            LOG.warn("Error destroying endpoint {}, is was not registered before.", webcontext);
        }

    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }

    @Validate
    public void start() {

        super.start();

        System.setProperty("org.apache.cxf.nofastinfoset", "true");

        CXFNonSpringServlet cxfServlet = new CXFNonSpringServlet();

        if (http != null) {
            try {
                http.registerServlet(Constants.CXF_SERVLET, cxfServlet, null, null);
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

    @Invalidate
    public void stop() {

        super.stop();

        if (http != null) {
            http.unregister(Constants.CXF_SERVLET);
        } else {
            try {
                httpServer.stop();
            } catch (Exception e) {
                LOG.error("Failed to stop standalone CXF Jetty server.", e);
            }
        }

        for (Map.Entry<String, org.apache.cxf.endpoint.Server> item : exportedDeclaration.entrySet()) {
            item.getValue().destroy();
        }

    }

    private CXFServlet configStandaloneServer() {

        httpServer = new Server(httpPort);

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

}

