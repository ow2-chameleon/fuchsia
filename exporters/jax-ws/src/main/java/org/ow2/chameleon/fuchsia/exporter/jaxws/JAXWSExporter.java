package org.ow2.chameleon.fuchsia.exporter.jaxws;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractExporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.exporter.jaxws.internal.Constants;
import org.ow2.chameleon.fuchsia.exporter.jaxws.internal.CxfExporterPojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;

@Component
@Provides(specifications = {ExporterService.class})
public class JAXWSExporter extends AbstractExporterComponent {

    private Bus cxfbus;

    private final BundleContext context;

    private ServiceReference serviceReference;

    private Logger logger=LoggerFactory.getLogger(this.getClass());

    private Map<String,Server> exportedDeclaration=new HashMap<String,Server>();

    @Requires
    HttpService http;

    @ServiceProperty(name = "target")
    private String filter;

    public JAXWSExporter(BundleContext context){
        this.context=context;
    }

    @Override
    public void useExportDeclaration(ExportDeclaration exportDeclaration) {

        logger.info("exporting {}",exportDeclaration.getMetadata());

        CxfExporterPojo pojo=CxfExporterPojo.create(exportDeclaration);

        try {

            ServerFactoryBean srvFactory = new ServerFactoryBean();

            Class ref = FuchsiaUtils.loadClass(this.context,pojo.getClazz());

            srvFactory.setServiceClass(ref);

            srvFactory.setBus(cxfbus);

            Object instance=null;
            ServiceReference[] protobuffReferences = context.getAllServiceReferences(pojo.getClazz(),pojo.getFilter());

            if(protobuffReferences==null){
                logger.warn("instance not found to be exported, ignoring exportation request, filter:"+pojo.getFilter());
            }

            srvFactory.setServiceBean(instance);

            srvFactory.setAddress(pojo.getWebcontext());

            Server endpoint = srvFactory.create();

            exportDeclaration.handle(serviceReference);

            exportedDeclaration.put(pojo.getWebcontext(),endpoint);

            logger.info("Pushing CXF endpoint: {}",endpoint.getEndpoint().getEndpointInfo().getAddress());

        } catch (Exception e) {

            logger.error("Failed exporting in CXF, with the message {}",e.getMessage());
            throw new RuntimeException(e);

        }

    }

    @Override
    protected void denyExportDeclaration(ExportDeclaration exportDeclaration) {

        logger.info("destroying exportation {}",exportDeclaration.getMetadata());

        final String webcontext = (String) exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_WEB_CONTEXT);

        exportDeclaration.unhandle(serviceReference);

        Server exported=exportedDeclaration.get(webcontext);

        if(exported!=null){
            exported.destroy();
            logger.info("Endpoint destroyed: {}",webcontext);
        }else {
            logger.warn("Error destroying endpoint {}, is was not registered before.",webcontext);
        }

    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    @PostRegistration
    protected void registration(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }

    @Validate
    public void start() {

        super.start();

        System.setProperty("org.apache.cxf.nofastinfoset", "true");

        CXFNonSpringServlet cxfServlet = new CXFNonSpringServlet();

        try {
            http.registerServlet(Constants.CXF_SERVLET, cxfServlet, null, null);
        } catch (ServletException e) {
            logger.error("Failed registering CXF servlet",e);
        } catch (NamespaceException e) {
            logger.error("Failed registering CXF servlet",e);
        }

        cxfbus = cxfServlet.getBus();

    }

    @Invalidate
    public void stop(){

        super.stop();

        http.unregister(Constants.CXF_SERVLET);

        for(Map.Entry<String,Server> item:exportedDeclaration.entrySet()){
            item.getValue().destroy();
        }

    }

}

