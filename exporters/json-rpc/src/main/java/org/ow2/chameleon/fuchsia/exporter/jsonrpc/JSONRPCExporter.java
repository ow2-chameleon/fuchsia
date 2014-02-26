package org.ow2.chameleon.fuchsia.exporter.jsonrpc;

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
import org.ow2.chameleon.fuchsia.exporter.jsonrpc.model.JSONRPCExporterPojo;
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
 * Provides an {@link ImporterService} allowing to access a
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

    @ServiceProperty(name = "target")
    private String filter;

    @Requires
    HttpService web;

    private Set<String> registeredServlets=new HashSet<String>();

    private final BundleContext context;

    private ServiceReference serviceReference;

    public JSONRPCExporter(BundleContext pContext) {
        context = pContext;
    }

    @Override
    protected void useExportDeclaration(ExportDeclaration exportDeclaration) {

        Class<?> klass;

        JSONRPCExporterPojo jp=JSONRPCExporterPojo.create(exportDeclaration);

        try {

            try{
                klass = FuchsiaUtils.loadClass(context, jp.getInstanceClass());
            }catch (ClassNotFoundException e){
                LOG.warn("Failed to load from the own bundle, loading externally", e);
                klass=context.getBundle().loadClass(jp.getInstanceClass());
            }

            String osgiFilter= String.format("(instance.name=%s)", jp.getInstanceName());

            Collection<ServiceReference> references = new ArrayList(context.getServiceReferences(klass,osgiFilter));

            Object serviceToBePublished=context.getService(references.iterator().next());

            final JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBePublished, klass);

            final String endpointURL=String.format("%s/%s",jp.getUrlContext(),jp.getInstanceName());

            Servlet gs=new RPCServlet(jsonRpcServer);

            web.registerServlet(endpointURL, gs, new Hashtable(), null);

            exportDeclaration.handle(serviceReference);

            registeredServlets.add(endpointURL);

            LOG.info("JSONRPC-exporter, publishing object exporter at: {}", endpointURL);

        } catch (InvalidSyntaxException e) {
            LOG.error("LDAP filter specified on the linker is not valid, recheck your LDAP filters for the linker and exporter. ", e);
        } catch (ClassNotFoundException e) {
            LOG.error("The class to be exporter could not be loaded.", e);
        } catch (NamespaceException e) {
            LOG.error("Namespace failure", e);
        } catch (ServletException e) {
            LOG.error("Failed registering the servlet to respond the RPC request.", e);
        } finally {
            LOG.info("JSONRPC exporter finished to process exportation request.");
        }

    }

    @Override
    protected void denyExportDeclaration(ExportDeclaration exportDeclaration) {

        JSONRPCExporterPojo jp=JSONRPCExporterPojo.create(exportDeclaration);

        final String endpointURL=String.format("%s/%s",jp.getUrlContext(),jp.getInstanceName());

        exportDeclaration.unhandle(serviceReference);

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
    protected void stop() {
        unregisterAllServlets();
    }

    /**
     * Unregister all servlets registered by this exporter
     */
    private void unregisterAllServlets(){

        for(String endpoint:registeredServlets){
            web.unregister(endpoint);
            LOG.info("endpoint {} unregistered", endpoint);
        }

    }

    class RPCServlet extends HttpServlet {

        private final JsonRpcServer jsonRpcServer;

        public RPCServlet(JsonRpcServer jsonRpcServer) {
            this.jsonRpcServer=jsonRpcServer;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            jsonRpcServer.handle(req,resp);
        }

    }

}


