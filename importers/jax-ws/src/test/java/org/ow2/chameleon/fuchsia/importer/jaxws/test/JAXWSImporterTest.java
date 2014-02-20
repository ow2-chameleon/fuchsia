package org.ow2.chameleon.fuchsia.importer.jaxws.test;

import junit.framework.Assert;
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
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.jaxws.JAXWSImporter;
import org.ow2.chameleon.fuchsia.importer.jaxws.test.ctd.ServiceForExportation;
import org.ow2.chameleon.fuchsia.importer.jaxws.test.ctd.ServiceForExportationimpl;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class JAXWSImporterTest {

    private static final Integer HTTP_PORT=8043;

    private static final String ENDPOINT_URL="http://localhost:"+HTTP_PORT+"/cxf/" + ServiceForExportation.class.getSimpleName();

    Server httpServer;

    @Mock
    org.osgi.framework.BundleContext context;

    @Mock
    private ServiceReference importerServiceRegistration;

    @Mock
    private PackageAdmin packageAdminMock;

    @Mock
    private ExportedPackage importPackageForClass;

    @Mock
    private Bundle bundeToLoadClassFrom;

    @Mock
    private ServiceRegistration proxyServiceRegistration;

    private ServiceForExportation id= spy(new ServiceForExportationimpl());

    private ServiceReference idServiceReference[]=new ServiceReference[]{id};

    ServiceForExportation proxyRegisteredOSGi;

    JAXWSImporter importer;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        registerMockitoMockInterception();

        startServer();

        publishBean();

        initializeImporter();

    }

    @After
    public void setupClean() throws Exception {

        if(httpServer!=null){
            httpServer.stop();
        }

        importer.stop();
        httpServer.stop();

    }

    @Test
    public void absentParameterThrowsProperException() throws Exception {

        JAXWSImporter importer=new JAXWSImporter(context);

        importer.start();

        Map<String, Object> metadataNoClassName=new HashMap<String, Object>();

        metadataNoClassName.put("id", "b");
        metadataNoClassName.put("endpoint.url",ENDPOINT_URL);

        Map<String, Object> metadataNoEndpoint=new HashMap<String, Object>();

        metadataNoEndpoint.put("id", "b");
        metadataNoEndpoint.put("className", ServiceForExportation.class.getName());

        ImportDeclaration declarationNoClassName = ImportDeclarationBuilder.fromMetadata(metadataNoClassName).build();
        ImportDeclaration declarationNoEndpoint = ImportDeclarationBuilder.fromMetadata(metadataNoEndpoint).build();

        importer.registration(importerServiceRegistration);

        try {
            declarationNoClassName.bind(importerServiceRegistration);
            declarationNoClassName.handle(importerServiceRegistration);
            importer.addDeclaration(declarationNoClassName);
            Assert.fail("An exception should be thrown duo to absence of class name");
        } catch (Exception e) {

           if(! (e instanceof BinderException)){
               Assert.fail("An exception type BinderException should be thrown duo to absence of class name");
           }

        }

        try {
            declarationNoEndpoint.bind(importerServiceRegistration);
            declarationNoEndpoint.handle(importerServiceRegistration);
            importer.addDeclaration(declarationNoEndpoint);
            Assert.fail("An exception should be thrown duo to absence of endpoint URL");
        } catch (Exception e) {

            if(! (e instanceof BinderException)){
                Assert.fail("An exception should be thrown duo to absence of class name");
            }

        }

        importer.stop();

    }

    @Test
    public void allParametersProvidedDontThrowException() throws Exception {

        JAXWSImporter importer=new JAXWSImporter(context);

        importer.start();

        Map<String, Object> metadataNoClassName=new HashMap<String, Object>();

        metadataNoClassName.put("id", "b");
        metadataNoClassName.put("className", ServiceForExportation.class.getName());
        metadataNoClassName.put("endpoint.url", ENDPOINT_URL);

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadataNoClassName).build();

        importer.registration(importerServiceRegistration);

        declaration.bind(importerServiceRegistration);

        declaration.handle(importerServiceRegistration);

        importer.addDeclaration(declaration);

        importer.stop();

    }

    @Test
    public void localProxyInvokationShouldSucceed() throws Exception {

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("id","b");
        metadata.put("className",ServiceForExportation.class.getName());
        metadata.put("endpoint.url",ENDPOINT_URL);

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        importer.registration(importerServiceRegistration);

        declaration.bind(importerServiceRegistration);

        declaration.handle(importerServiceRegistration);

        importer.addDeclaration(declaration);

        final String stringValue="coucou";
        final Integer intValue=1789;

        proxyRegisteredOSGi.ping();
        verify(id, times(1)).ping();

        proxyRegisteredOSGi.ping(intValue);
        verify(id, times(1)).ping(intValue);

        proxyRegisteredOSGi.ping(stringValue);
        verify(id, times(1)).ping(stringValue);

        String returnPongString=proxyRegisteredOSGi.pongString(stringValue);
        verify(id, times(1)).pongString(stringValue);
        Assert.assertEquals(returnPongString,stringValue);

        Integer returnPongInteger=proxyRegisteredOSGi.pongInteger(intValue);
        verify(id, times(1)).pongInteger(intValue);
        Assert.assertEquals(returnPongInteger,intValue);

        importer.stop();

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


    }

    private void startServer() throws Exception {

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

    private void publishBean(){

        ServerFactoryBean srvFactory = new ServerFactoryBean();

        srvFactory.setServiceClass(ServiceForExportation.class);

        srvFactory.setServiceBean(id);

        srvFactory.setAddress("/"+srvFactory.getServiceClass().getSimpleName());

        org.apache.cxf.endpoint.Server endpoint = srvFactory.create();

        srvFactory.getServer().start();

    }

    private void initializeImporter(){

        importer=new JAXWSImporter(context);

        importer.start();

    }

}
