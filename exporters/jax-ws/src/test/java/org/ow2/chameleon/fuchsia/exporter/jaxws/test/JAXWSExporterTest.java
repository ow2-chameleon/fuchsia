package org.ow2.chameleon.fuchsia.exporter.jaxws.test;

import junit.framework.Assert;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
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
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.exporter.jaxws.JAXWSExporter;
import org.ow2.chameleon.fuchsia.exporter.jaxws.test.ctd.ServiceForExportation;
import org.ow2.chameleon.fuchsia.exporter.jaxws.test.ctd.ServiceForExportationImpl;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class JAXWSExporterTest {

    @Mock
    org.osgi.framework.BundleContext context;

    private ServiceRegistration registrationFromClassToBeExported = mock(ServiceRegistration.class);

    private ServiceReference serviceReferenceFromExporter=  mock(ServiceReference.class);

    private ExportedPackage exportPackageForClass = mock(ExportedPackage.class);

    private Bundle bundeToLoadClassFrom= mock(Bundle.class);

    private PackageAdmin packageAdminMock= mock(PackageAdmin.class);

    private ServiceForExportation id= spy(new ServiceForExportationImpl());

    private ServiceReference idServiceReference[]=new ServiceReference[]{id};

    private JAXWSExporter exporter;

    /**
     * Instantiate all mocks necessary for the exportation, and invokes @Validate method from the exporter
     */
    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        registrationFromClassToBeExported = mock(ServiceRegistration.class);

        serviceReferenceFromExporter=  mock(ServiceReference.class);

        exportPackageForClass = mock(ExportedPackage.class);

        bundeToLoadClassFrom= mock(Bundle.class);

        packageAdminMock= mock(PackageAdmin.class);

        id= spy(new ServiceForExportationImpl());

        idServiceReference=new ServiceReference[]{id};

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

        exporter=new JAXWSExporter(context);

        exporter.start();

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

        String endPointURL = "http://localhost:8080/cxf/"+ServiceForExportation.class.getSimpleName();
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

}