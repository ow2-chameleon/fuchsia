package org.ow2.chameleon.fuchsia.exporter.jaxws.test;

import junit.framework.Assert;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.junit.Test;
import org.osgi.framework.*;
import org.ow2.chameleon.fuchsia.core.declaration.*;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.exporter.jaxws.test.base.JAXExporterAbstractTest;
import org.ow2.chameleon.fuchsia.exporter.jaxws.test.ctd.ServiceForExportation;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.*;

public class JAXWSExporterWithoutHttpServiceTest extends JAXExporterAbstractTest {

    @Test
    public void allParametersProvidedDontThrowException()  {

        Map<String, Object> metadata=new HashMap<String, Object>();
        metadata.put(ID,"TestJAXWSDeclaration");
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
        noClassNameParameter.put(Constants.ID,"TestJAXWSDeclaration");
        noClassNameParameter.put("fuchsia.export.cxf.url.context", "/"+ServiceForExportation.class.getSimpleName());

        Map<String, Object> noClassNameContext=new HashMap<String, Object>();
        noClassNameContext.put(Constants.ID,"TestJAXWSDeclaration");
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
        metadata.put(ID,"TestJAXWSDeclaration");
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
        metadata.put(ID,"TestJAXWSDeclaration");
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

        try {
            when(context.getAllServiceReferences(ServiceForExportation.class.getName(), null)).thenReturn(null);
        } catch (InvalidSyntaxException e) {

        }

        Map<String, Object> metadata=new HashMap<String, Object>();
        metadata.put(ID,"TestJAXWSDeclaration");
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

}
