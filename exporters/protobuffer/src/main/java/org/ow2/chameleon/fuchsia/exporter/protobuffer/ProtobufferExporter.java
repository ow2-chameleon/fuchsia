package org.ow2.chameleon.fuchsia.exporter.protobuffer;

import com.google.code.cxf.protobuf.ProtobufServerFactoryBean;
import com.google.code.cxf.protobuf.binding.ProtobufBindingFactory;
import com.google.protobuf.Service;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
//import org.apache.cxf.endpoint.Server;
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
import org.ow2.chameleon.fuchsia.exporter.protobuffer.internal.ProtobufferExporterPojo;
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

    private Map<String,Server> serverPublished=new HashMap<String,Server>();

    org.eclipse.jetty.server.Server httpServer;

    @ServiceProperty(name = "org.osgi.service.http.port")
    private Integer HTTP_PORT;

    private Bus cxfbus;

    @Requires
    HttpService http;

    @ServiceProperty(name = "target")
    private String filter;

    BundleContext context;

    public ProtobufferExporter(BundleContext context) {
        this.context = context;
    }

    @Validate
    public void start() {
        System.setProperty("org.apache.cxf.nofastinfoset", "true");
        super.start();

        CXFNonSpringServlet cxfServlet = new CXFNonSpringServlet();

        try {

            if(http!=null){
                http.registerServlet("/cxf", cxfServlet, null, null);
            }else {

                try {

                    cxfServlet=configStandaloneServer();

                    httpServer.start();

                } catch (Exception e1) {
                    LOG.error("Impossible to start standalone CXF Jetty server.",e1);
                }



            }

        } catch (ServletException e) {
            LOG.error("Failed registering CXF servlet", e);
        } catch (NamespaceException e) {
            LOG.error("Failed registering CXF servlet", e);
        }

        cxfbus = cxfServlet.getBus();

    }

    private CXFServlet configStandaloneServer(){
        httpServer = new org.eclipse.jetty.server.Server(HTTP_PORT);
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
    public void stop(){
        super.stop();
        if(httpServer!=null){
            try {
                httpServer.stop();
            } catch (Exception e) {
                LOG.warn("Failed to stop standalone server.",e);
            }
        }
        for(Map.Entry<String,Server> entry:serverPublished.entrySet()){
            serverPublished.remove(entry.getKey());
            entry.getValue().stop();
        }
    }

    @Override
    protected void useExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {

        LOG.info("initiating exportation...");

        ProtobufferExporterPojo pojo=ProtobufferExporterPojo.create(exportDeclaration);

        try {

            Class inter = FuchsiaUtils.loadClass(context, String.format("%s$%s", pojo.getClazz(), pojo.getService()));
            Class messageClass = FuchsiaUtils.loadClass(context, String.format("%s$%s", pojo.getClazz(), pojo.getMessage()));
            LOG.info("Looking for service that provides class {}",pojo.getClazz()+"$"+pojo.getService());
            Collection<ServiceReference<Service>> protobuffReferences = context.getServiceReferences(inter, pojo.getFilter());
            LOG.info("using filter " + pojo.getFilter() + " to find instance");
            if (protobuffReferences.size() == 0) {

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
                    Server server=serverFactoryBean.create();
                    serverPublished.put(pojo.getId(),server);
                    Thread.currentThread().setContextClassLoader(loader);
                    LOG.info("exporting the service with the id:" + pojo.getId());
                }

            } else if (protobuffReferences.size() > 1) {
                LOG.info("more than one were found to be exported");
            }

        } catch (InvalidSyntaxException e) {
            LOG.error("Invalid filter exception", e);
        } catch (ClassNotFoundException e) {
            LOG.error("Class not found", e);
        }

    }

    @Override
    protected void denyExportDeclaration(ExportDeclaration exportDeclaration) throws BinderException {

        ProtobufferExporterPojo pojo=ProtobufferExporterPojo.create(exportDeclaration);

        Server server=serverPublished.get(pojo.getId());

        if(server!=null){
            LOG.info("Destroying endpoint:" + server.getEndpoint().getEndpointInfo().getAddress());
            server.destroy();
        }else {
            LOG.warn("nothing to destroy");
        }

    }

    public String getName() {
        return this.getClass().getName();
    }
}
