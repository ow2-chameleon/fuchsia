package org.ow2.chameleon.fuchsia.ws.internal;

import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import java.util.*;

import static org.ow2.chameleon.fuchsia.core.FuchsiaUtils.loadClass;

/**
 * This component provides a JAX-WS, Apache CXF based implementation of an
 *
 * @author Jonathan Bardin <jonathan.bardin@imag.fr>
 * @Edited Jeremy.Savonet@gmail.com
 */
@Component(name="Fuchsia_importer.cxf")
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class, CXFImporterComp.class})
//@Instantiate (name="Fuchsia_importer_cxf")
public class CXFImporterComp extends AbstractImporterComponent {

    public static final String ENDPOINT_URL = "endpoint.url"; //TODO FIXME

    private static final String CLASSNAME = "className";

    private final Map<ImportDeclaration, ServiceRegistration> map;

    @ServiceProperty(name = "target", value = "(jax-ws.importer.interfaces=*)")
    private String filter;

    @ServiceProperty(name = "instance.name")
    private String name;

    private final BundleContext context;

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CXFImporterComp(BundleContext pContext) {
        context=pContext;
        map = new HashMap<ImportDeclaration, ServiceRegistration>();
    }

    @Override
    @Invalidate
    protected void stop() {
        logger.info("STOP CXF IMPORTER SERVICE");
        super.stop();
    }

    @Override
    @Validate
    protected void start() {
        logger.info("START CXF IMPORTER SERVICE");
        super.start();
    }


    /**
     * Return the name of the instance
     * @return name of this instance
     */
    public String getName() {
        return name;
    }

	/*--------------------------*
	 * ImporterService methods  *
	 *--------------------------*/

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) {
        logger.debug("Create proxy" + importDeclaration.getMetadata());
        ClientProxyFactoryBean factory;
        String endPointURL;
        Object objectProxy;

        //Try to load the class
        final Class<?> klass;
        try {
            klass = loadClass(context, (String) importDeclaration.getMetadata().get(CLASSNAME));
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load class",e);
            return;
        }

        final ClassLoader origin = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            logger.debug(String.valueOf(klass));
            //use annotations if present
            if (klass.isAnnotationPresent(WebService.class)){
                factory = new JaxWsProxyFactoryBean();
            } else {
                factory = new ClientProxyFactoryBean();
            }

            factory.getInInterceptors().add(new LoggingInInterceptor());
            factory.getOutInterceptors().add(new LoggingOutInterceptor());

            //set the class XXX only one class is supported
            factory.setServiceClass(klass);

            //set the URL
            if(!(importDeclaration.getMetadata().get(ENDPOINT_URL) instanceof String)) {
                return;  //TODO FIXME
            }

            endPointURL = (String) importDeclaration.getMetadata().get(ENDPOINT_URL);
            factory.setAddress(endPointURL);

            logger.debug(String.valueOf(factory.getAddress()));
            logger.debug(String.valueOf(factory.getServiceFactory()));

            //create the proxy
            objectProxy = factory.create();

            //Publish object
            map.put(importDeclaration,registerProxy(objectProxy,klass));
        } finally {
            Thread.currentThread().setContextClassLoader(origin);
        }
    }

    /**
     * Utility method to register a proxy has a Service in OSGi.
     *
     */
    protected ServiceRegistration registerProxy(Object objectProxy, Class clazz) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        ServiceRegistration registration;
        registration = context.registerService(clazz, objectProxy, props);

        return registration;
    }

    /**
     *  Destroy the proxy & update the map containing the registration ref
     * @param importDeclaration
     */
    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) {
        logger.debug("CXFImporter destroy a proxy for " + importDeclaration);
        ServiceRegistration serviceRegistration = map.get(importDeclaration);
        serviceRegistration.unregister();
        map.remove(importDeclaration);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
