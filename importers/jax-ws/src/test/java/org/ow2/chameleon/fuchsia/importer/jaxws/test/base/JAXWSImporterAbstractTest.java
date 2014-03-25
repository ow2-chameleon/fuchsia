package org.ow2.chameleon.fuchsia.importer.jaxws.test.base;

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

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.importer.jaxws.JAXWSImporter;
import org.ow2.chameleon.fuchsia.importer.jaxws.test.ctd.ServiceForExportation;
import org.ow2.chameleon.fuchsia.importer.jaxws.test.ctd.ServiceForExportationimpl;

import java.util.Dictionary;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public abstract class JAXWSImporterAbstractTest {

    protected static final Integer HTTP_PORT=8043;

    protected static final String ENDPOINT_URL="http://localhost:"+HTTP_PORT+"/cxf/" + ServiceForExportation.class.getSimpleName();

    protected Server httpServer;

    @Mock
    protected org.osgi.framework.BundleContext context;

    @Mock
    protected ServiceReference importerServiceRegistration;

    @Mock
    protected PackageAdmin packageAdminMock;

    @Mock
    protected ExportedPackage importPackageForClass;

    @Mock
    protected Bundle bundeToLoadClassFrom;

    @Mock
    protected ServiceRegistration proxyServiceRegistration;

    protected ServiceForExportation id= spy(new ServiceForExportationimpl());

    protected ServiceReference idServiceReference[]=new ServiceReference[]{id};

    protected ServiceForExportation proxyRegisteredOSGi;

    protected JAXWSImporter importer;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        initializeImporter();

        registerMockitoMockInterception();

        startServer();

        publishBean();

    }

    @After
    public void setupClean() throws Exception {

        if(httpServer!=null){
            httpServer.stop();
        }

        importer.stop();
        httpServer.stop();

    }

    private void registerMockitoMockInterception(){

        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(importerServiceRegistration);
        when(packageAdminMock.getExportedPackage(ServiceForExportation.class.getName())).thenReturn(importPackageForClass);
        when(context.getService(importerServiceRegistration)).thenReturn(packageAdminMock);
        when(context.getBundle()).thenReturn(bundeToLoadClassFrom);
        when(importerServiceRegistration.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);

        when(context.registerService(any(Class.class), any(ServiceForExportation.class), any(Dictionary.class))).thenReturn(proxyServiceRegistration);


        try {
            when(context.getAllServiceReferences(ServiceForExportation.class.getName(), null)).thenReturn(idServiceReference);
            when(bundeToLoadClassFrom.loadClass(anyString())).thenAnswer(new Answer<Class>() {
                public Class answer(InvocationOnMock invocation) throws Throwable {
                    return Thread.currentThread().getContextClassLoader().loadClass((String) invocation.getArguments()[0]);
                }
            });
        } catch (InvalidSyntaxException e) {

        } catch (ClassNotFoundException e) {

        }

        when(context.registerService(any(Class.class), any(ServiceForExportation.class), any(Dictionary.class))).thenAnswer(new Answer<ServiceRegistration>() {
            public ServiceRegistration answer(InvocationOnMock invocation) throws Throwable {

                ServiceRegistration proxyServiceRegistration=mock(ServiceRegistration.class);

                proxyRegisteredOSGi =(ServiceForExportation)invocation.getArguments()[1];

                return proxyServiceRegistration;
            }
        });
        field("name").ofType(String.class).in(importer).set(importer.getClass().getName());


    }

    protected void startServer() throws Exception {

        httpServer=new Server(HTTP_PORT);

        Bus bus = BusFactory.getDefaultBus(true);
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        httpServer.setHandler(contexts);

        ServletContextHandler root = new ServletContextHandler(contexts, "/",
                ServletContextHandler.SESSIONS);

        BusFactory.setDefaultBus(bus);

        CXFServlet cxf = new CXFServlet();

        cxf.setBus(bus);

        ServletHolder servlet = new ServletHolder(cxf);

        root.addServlet(servlet, "/cxf/*");

        httpServer.start();

    }

    protected void publishBean(){

        ServerFactoryBean srvFactory = new ServerFactoryBean();

        srvFactory.setServiceClass(ServiceForExportation.class);

        srvFactory.setServiceBean(id);

        srvFactory.setAddress("/"+srvFactory.getServiceClass().getSimpleName());

        org.apache.cxf.endpoint.Server endpoint = srvFactory.create();

        srvFactory.getServer().start();

    }

    protected void initializeImporter(){

        importer=new JAXWSImporter(context);

        importer.start();

    }

}
