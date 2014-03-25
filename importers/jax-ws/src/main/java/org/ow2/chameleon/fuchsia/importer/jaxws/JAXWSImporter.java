package org.ow2.chameleon.fuchsia.importer.jaxws;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer JAX-WS
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

import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.jaxws.internal.JAXWSImportDeclarationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.ow2.chameleon.fuchsia.core.FuchsiaUtils.loadClass;

/**
 * This component provides a JAX-WS, Apache CXF based implementation of an
 *
 * @author Jonathan Bardin <jonathan.bardin@imag.fr>
 * @Edited Jeremy.Savonet@gmail.com
 * @Edited Jander Nascimento <botelho@imag.fr>
 */
@Component
@Provides(specifications = {org.ow2.chameleon.fuchsia.core.component.ImporterService.class})
public class JAXWSImporter extends AbstractImporterComponent {

    public static final String ENDPOINT_URL = "endpoint.url";

    public static final String CLASSNAME = "className";

    /**
     * logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(JAXWSImporter.class);

    private final Map<ImportDeclaration, ServiceRegistration> map;

    @ServiceProperty(name = "target", value = "(jax-ws.importer.interfaces=*)")
    private String filter;

    @ServiceProperty(name = "instance.name")
    private String name;

    private final BundleContext context;

    private ServiceReference serviceReference;

    public JAXWSImporter(BundleContext pContext) {
        context = pContext;
        map = new HashMap<ImportDeclaration, ServiceRegistration>();
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        this.serviceReference = serviceReference;
    }

    @Override
    @Invalidate
    public void stop() {
        LOG.info("STOP CXF IMPORTER SERVICE");
        super.stop();
    }

    @Override
    @Validate
    public void start() {
        LOG.info("START CXF IMPORTER SERVICE");
        super.start();
    }


    /**
     * Return the name of the instance
     *
     * @return name of this instance
     */
    public String getName() {
        return name;
    }

    /*--------------------------*
     * ImporterService methods  *
     *--------------------------*/

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {
        LOG.debug("Create proxy" + importDeclaration.getMetadata());
        ClientProxyFactoryBean factory;

        Object objectProxy;

        JAXWSImportDeclarationWrapper pojo = JAXWSImportDeclarationWrapper.create(importDeclaration);

        //Try to load the class
        final Class<?> klass;
        try {
            klass = loadClass(context, pojo.getClazz());
        } catch (ClassNotFoundException e) {
            LOG.error("Failed to load class", e);
            return;
        }

        final ClassLoader origin = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            LOG.debug(String.valueOf(klass));
            //use annotations if present
            if (klass.isAnnotationPresent(WebService.class)) {
                factory = new JaxWsProxyFactoryBean();
            } else {
                factory = new ClientProxyFactoryBean();
            }

            factory.getInInterceptors().add(new LoggingInInterceptor());
            factory.getOutInterceptors().add(new LoggingOutInterceptor());

            //set the class XXX only one class is supported
            factory.setServiceClass(klass);

            factory.setAddress(pojo.getEndpoint());

            LOG.debug(String.valueOf(factory.getAddress()));
            LOG.debug(String.valueOf(factory.getServiceFactory()));

            //create the proxy
            objectProxy = factory.create();

            // set the importDeclaration has handled
            importDeclaration.handle(serviceReference);

            //Publish object
            map.put(importDeclaration, registerProxy(objectProxy, klass));
        } finally {
            Thread.currentThread().setContextClassLoader(origin);
        }
    }

    /**
     * Utility method to register a proxy has a Service in OSGi.
     */
    protected ServiceRegistration registerProxy(Object objectProxy, Class clazz) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        ServiceRegistration registration;
        registration = context.registerService(clazz, objectProxy, props);

        return registration;
    }

    /**
     * Destroy the proxy & update the map containing the registration ref
     *
     * @param importDeclaration
     */
    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) {
        LOG.debug("CXFImporter destroy a proxy for " + importDeclaration);
        ServiceRegistration serviceRegistration = map.get(importDeclaration);
        serviceRegistration.unregister();

        // set the importDeclaration has unhandled
        importDeclaration.unhandle(serviceReference);

        map.remove(importDeclaration);
    }

}
