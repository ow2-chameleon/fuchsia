package org.ow2.chameleon.fuchsia.exporter.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractExporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;

@Component(name = "CxfExporterFactory")
@Provides(specifications = {ExporterService.class})
public class CxfExporterFactory extends AbstractExporterComponent {

    private Bus cxfbus;

    private BundleContext context;

    private Logger logger=LoggerFactory.getLogger(this.getClass());

    private Map<String,Server> exportedDeclaration=new HashMap<String,Server>();

    @Requires
    HttpService http;

    @ServiceProperty(name = "target")
    private String filter;


    public CxfExporterFactory(BundleContext context){
        this.context=context;
    }

    @Override
    public void useExportDeclaration(ExportDeclaration exportDeclaration) {

        logger.info("exporting {}",exportDeclaration.getMetadata());

        final String classname = (String) exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_TYPE);
        final String webcontext = (String) exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_WEB_CONTEXT);
        final Object instance = exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_INSTANCE);

        //ClassLoader loader = Thread.currentThread().getContextClassLoader();

        //Thread.currentThread().setContextClassLoader(CXFNonSpringServlet.class.getClassLoader());

        try {

            ServerFactoryBean srvFactory = new ServerFactoryBean();

            Class ref = FuchsiaUtils.loadClass(this.context,classname);

            srvFactory.setServiceClass(ref);

            srvFactory.setBus(cxfbus);

            srvFactory.setServiceBean(instance);

            srvFactory.setAddress(webcontext);

            Server endpoint = srvFactory.create();

            exportedDeclaration.put(webcontext,endpoint);

            logger.info("Pushing CXF endpoint: {}",endpoint.getEndpoint().getEndpointInfo().getAddress());

        } catch (Exception e) {

            logger.error("Failed exporting in CXF, with the message {}",e.getMessage());
            throw new RuntimeException(e);

        }

        //Thread.currentThread().setContextClassLoader(loader);

    }

    @Override
    protected void denyExportDeclaration(ExportDeclaration exportDeclaration) {

        logger.info("destroying exportation {}",exportDeclaration.getMetadata());

        final String webcontext = (String) exportDeclaration.getMetadata().get(Constants.CXF_EXPORT_WEB_CONTEXT);

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

    @Validate
    public void start() {

        System.setProperty("org.apache.cxf.nofastinfoset", "true");

        CXFNonSpringServlet cxfServlet = new CXFNonSpringServlet();

        try {
            http.registerServlet(Constants.CXF_SERVLET, cxfServlet, null, null);

        } catch (ServletException e) {
            logger.error("Failed registering CXF servlet, with the message {}",e.getMessage());
            e.printStackTrace();
        } catch (NamespaceException e) {
            logger.error("Failed registering CXF servlet, with the message {}",e.getMessage());
            e.printStackTrace();
        }

        cxfbus = cxfServlet.getBus();

    }

    @Invalidate
    public void stop(){

        http.unregister(Constants.CXF_SERVLET);

        for(Map.Entry<String,Server> item:exportedDeclaration.entrySet()){
            item.getValue().destroy();
        }

    }

}

