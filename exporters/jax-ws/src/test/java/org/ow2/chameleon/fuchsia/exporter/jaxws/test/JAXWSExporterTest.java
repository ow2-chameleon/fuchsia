package org.ow2.chameleon.fuchsia.exporter.jaxws.test;

import junit.framework.Assert;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.*;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.exporter.jaxws.JAXWSExporter;
import org.ow2.chameleon.fuchsia.exporter.jaxws.test.ctd.ServiceForExportation;
import org.ow2.chameleon.fuchsia.exporter.jaxws.test.ctd.ServiceForExportationImpl;

import javax.servlet.ServletException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class JAXWSExporterTest {

    private static final Integer HTTP_PORT=8043;

    @Mock
    org.osgi.framework.BundleContext context;

    @Mock
    private ServiceRegistration registrationFromClassToBeExported;

    @Mock
    private ServiceReference serviceReferenceFromExporter;

    @Mock
    private ExportedPackage exportPackageForClass;

    @Mock
    private Bundle bundeToLoadClassFrom;

    @Mock
    private PackageAdmin packageAdminMock;

    private ServiceForExportation id= spy(new ServiceForExportationImpl());

    private ServiceReference idServiceReference[]=new ServiceReference[]{id};

    private JAXWSExporter exporter;

    @Mock
    private HttpService httpServiceMock;

    /**
     * Instantiate all mocks necessary for the exportation, and invokes @Validate method from the exporter
     */
    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        Dictionary<String, Object> props1 = new Hashtable<String, Object>();

        when(context.registerService(new String[]{ExportDeclaration.class.getName()}, id, props1)).thenReturn(registrationFromClassToBeExported);
        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(serviceReferenceFromExporter);
        when(serviceReferenceFromExporter.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
        when(context.getService(serviceReferenceFromExporter)).thenReturn(packageAdminMock);
        when(context.getBundle()).thenReturn(bundeToLoadClassFrom);
        when(context.getService(idServiceReference[0])).thenReturn(id);
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

        when(packageAdminMock.getExportedPackage(ServiceForExportation.class.getName())).thenReturn(exportPackageForClass);

        exporter=constructor().withParameterTypes(BundleContext.class).in(JAXWSExporter.class).newInstance(context);

        field("HTTP_PORT").ofType(Integer.class).in(exporter).set(HTTP_PORT);

    }

    /**
     * Remove all instantiation (avoid leak) and invoke @Invalidate method
     */
    @After
    public void setupClean() {

        exporter.stop();

        registrationFromClassToBeExported = null;

        serviceReferenceFromExporter = null;

        exportPackageForClass = null;

        bundeToLoadClassFrom = null;

        packageAdminMock = null;

        id=null;

        idServiceReference=null;

    }

    private ExportDeclaration buildExportDeclaration(String id,Class type,String context){

        return null;

    }

    @Test
    public void allParametersProvidedDontThrowException()  {

        exporter.start();

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("fuchsia.export.cxf.class.name",ServiceForExportation.class.getName());
        metadata.put("fuchsia.export.cxf.url.context","/"+ServiceForExportation.class.getSimpleName());

        ExportDeclaration declaration = spy(ExportDeclarationBuilder.fromMetadata(metadata).build());
        declaration.bind(serviceReferenceFromExporter);

        exporter.registration(serviceReferenceFromExporter);

        try {
            exporter.addDeclaration(declaration);
        } catch (BinderException e) {
            Assert.fail("All parameters were provided, not exception should be raised");
        }

    }

    @Test
    public void absentParameterThrowsProperException()  {

        exporter.start();

        Map<String, Object> noClassNameParameter=new HashMap<String, Object>();
        noClassNameParameter.put("fuchsia.export.cxf.url.context", "/"+ServiceForExportation.class.getSimpleName());

        Map<String, Object> noClassNameContext=new HashMap<String, Object>();
        noClassNameContext.put("fuchsia.export.cxf.class.name", ServiceForExportation.class.getName());

        ExportDeclaration declarationNoClassName = spy(ExportDeclarationBuilder.fromMetadata(noClassNameParameter).build());
        ExportDeclaration declarationNoNoContext = spy(ExportDeclarationBuilder.fromMetadata(noClassNameContext).build());

        try {
            declarationNoClassName.bind(serviceReferenceFromExporter);
            exporter.registration(serviceReferenceFromExporter);
            exporter.addDeclaration(declarationNoClassName);
            Assert.fail("An exception should be thrown duo to absence of class name");
        } catch (BinderException e) {
        }

        try {
            declarationNoNoContext.bind(serviceReferenceFromExporter);
            exporter.registration(serviceReferenceFromExporter);
            exporter.addDeclaration(declarationNoNoContext);
            Assert.fail("An exception should be thrown to the absence of Web context");
        } catch (BinderException e) {

        }

    }

    @Test
    public void registerDeclarationShouldCallHandle()  {

        exporter.start();

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("fuchsia.export.cxf.class.name",ServiceForExportation.class.getName());
        metadata.put("fuchsia.export.cxf.url.context","/"+ServiceForExportation.class.getSimpleName());

        ExportDeclaration declaration = spy(ExportDeclarationBuilder.fromMetadata(metadata).build());
        declaration.bind(serviceReferenceFromExporter);

        exporter.registration(serviceReferenceFromExporter);

        try {
            exporter.addDeclaration(declaration);
        } catch (BinderException e) {
            Assert.fail("All parameters were provided, not exception should be raised");
        }

        verify(declaration, times(1)).handle(serviceReferenceFromExporter);

    }

    @Test
    public void remoteWSInvokationShouldSucceed() throws BinderException {

        exporter.start();

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("fuchsia.export.cxf.class.name",ServiceForExportation.class.getName());
        metadata.put("fuchsia.export.cxf.url.context","/"+ServiceForExportation.class.getSimpleName());

        ExportDeclaration declaration = spy(ExportDeclarationBuilder.fromMetadata(metadata).build());
        declaration.bind(serviceReferenceFromExporter);

        exporter.registration(serviceReferenceFromExporter);

        exporter.addDeclaration(declaration);

        /**
         * Configure CXF Client to connect to the endpoint
         */

        ClientProxyFactoryBean factory = new ClientProxyFactoryBean();

        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());

        factory.setServiceClass(ServiceForExportation.class);

        String endPointURL = "http://localhost:"+HTTP_PORT+"/cxf/"+ServiceForExportation.class.getSimpleName();
        factory.setAddress(endPointURL);

        ServiceForExportation objectProxy = (ServiceForExportation)factory.create();

        final String stringValue="coucou";
        final Integer intValue=1789;

        objectProxy.ping();
        verify(id, times(1)).ping();

        objectProxy.ping(intValue);
        verify(id, times(1)).ping(intValue);

        objectProxy.ping(stringValue);
        verify(id, times(1)).ping(stringValue);

        String returnPongString=objectProxy.pongString(stringValue);
        verify(id, times(1)).pongString(stringValue);
        Assert.assertEquals(returnPongString,stringValue);

        Integer returnPongInteger=objectProxy.pongInteger(intValue);
        verify(id, times(1)).pongInteger(intValue);
        Assert.assertEquals(returnPongInteger,intValue);


    }

    @Test
    public void inCaseNoServiceAvailableDontBind() throws BinderException {

        exporter.start();

        try {
            when(context.getAllServiceReferences(ServiceForExportation.class.getName(), null)).thenReturn(null);
        } catch (InvalidSyntaxException e) {

        }

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("fuchsia.export.cxf.class.name",ServiceForExportation.class.getName());
        metadata.put("fuchsia.export.cxf.url.context","/"+ServiceForExportation.class.getSimpleName());

        ExportDeclaration declaration = spy(ExportDeclarationBuilder.fromMetadata(metadata).build());
        declaration.bind(serviceReferenceFromExporter);

        exporter.registration(serviceReferenceFromExporter);

        exporter.addDeclaration(declaration);

        verify(declaration, never()).handle(serviceReferenceFromExporter);

    }

    @Test
    public void importerNameCannotBeNull(){

        Assert.assertNotNull(exporter.getName());

    }

    @Test
    public void worksInCaseHttpServiceWasInjected() throws BinderException, ServletException, NamespaceException {

        field("http").ofType(HttpService.class).in(exporter).set(httpServiceMock);

        exporter.start();

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("fuchsia.export.cxf.class.name",ServiceForExportation.class.getName());
        metadata.put("fuchsia.export.cxf.url.context","/"+ServiceForExportation.class.getSimpleName());

        ExportDeclaration declaration = spy(ExportDeclarationBuilder.fromMetadata(metadata).build());
        declaration.bind(serviceReferenceFromExporter);

        exporter.registration(serviceReferenceFromExporter);

        exporter.addDeclaration(declaration);

        verify(httpServiceMock, times(1)).registerServlet(eq(org.ow2.chameleon.fuchsia.exporter.jaxws.internal.Constants.CXF_SERVLET), any(CXFNonSpringServlet.class), any(Dictionary.class),  any(org.osgi.service.http.HttpContext.class));

    }




}