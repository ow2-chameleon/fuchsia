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
import org.ow2.chameleon.fuchsia.examples.jaxws.helloWS.HelloWorldWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import java.util.*;

/**
 * This component provides a JAX-WS, Apache CXF based implementation of an
 *
 * @author Jonathan Bardin <jonathan.bardin@imag.fr>
 * @Edited Jeremy.Savonet@gmail.com
 */
@Component(name="Fuchsia_importer.cxf")
@Provides(specifications = org.ow2.chameleon.fuchsia.core.component.ImporterService.class)
@Instantiate (name="Fuchsia_importer_cxf")
public class CXFImporterComp extends AbstractImporterComponent {

    public static final String ENDPOINT_URL = "endpoint.url"; //TODO FIXME

    public static final String INTERFACES = "jax-ws.importer.interfaces";

    private final Map<ImportDeclaration, ServiceRegistration> map;

    @ServiceProperty(name = "target", value = "(jax-ws.importer.interfaces=*)")
    private String filter;

    @ServiceProperty(name = "instance.name")
    private String name;

    private BundleContext context;

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

    //TODO see with Morgan
    public List<String> getConfigPrefix() {
        return null;
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
    protected void createProxy(ImportDeclaration importDeclaration) {
        logger.debug("Create proxy" + importDeclaration.getMetadata());
        ClientProxyFactoryBean factory;
        String endPointURL;
        Object objectProxy;

        List<Class<?>> testClass = new ArrayList<Class<?>>();
        testClass.add(HelloWorldWS.class);
        //Try to load the class
        final List<Class<?>> klass = testClass;
//        final List<Class<?>> klass = loadClasses(context, importDeclaration);

        final ClassLoader origin = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            logger.debug(String.valueOf(klass.get(0)));
            //use annotations if present
            if (klass.get(0).isAnnotationPresent(WebService.class)){
                factory = new JaxWsProxyFactoryBean();
            } else {
                factory = new ClientProxyFactoryBean();
            }

            factory.getInInterceptors().add(new LoggingInInterceptor());
            factory.getOutInterceptors().add(new LoggingOutInterceptor());

            //set the class XXX only one class is supported
            factory.setServiceClass(klass.get(0));

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
            HelloWorldWS newObject = (HelloWorldWS)objectProxy;

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
    protected ServiceRegistration registerProxy(Object objectProxy, List<?> clazz) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        ServiceRegistration registration;
        registration = context.registerService((Class)clazz.get(0), objectProxy, props);

        return registration;
    }

    /**
     *  Destroy the proxy & update the map containing the registration ref
     * @param importDeclaration
     */
    @Override
    protected void destroyProxy(ImportDeclaration importDeclaration) {
        logger.debug("CXFImporter destroy a proxy for " + importDeclaration);

        ServiceRegistration serviceRegistration = map.get(importDeclaration);
        serviceRegistration.unregister();
        map.remove(importDeclaration);
    }
}
