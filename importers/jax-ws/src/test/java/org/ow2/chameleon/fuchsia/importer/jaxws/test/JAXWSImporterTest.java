package org.ow2.chameleon.fuchsia.importer.jaxws.test;

import junit.framework.Assert;
import org.junit.Test;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.jaxws.JAXWSImporter;
import org.ow2.chameleon.fuchsia.importer.jaxws.test.base.JAXWSImporterAbstractTest;
import org.ow2.chameleon.fuchsia.importer.jaxws.test.ctd.ServiceForExportation;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class JAXWSImporterTest extends JAXWSImporterAbstractTest {

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
            importer.addDeclaration(declarationNoClassName);
            Assert.fail("An exception should be thrown duo to absence of class name");
        } catch (Exception e) {

            if(! (e instanceof BinderException)){
                Assert.fail("An exception type BinderException should be thrown duo to absence of class name");
            }

        }

        try {
            declarationNoEndpoint.bind(importerServiceRegistration);
            importer.addDeclaration(declarationNoEndpoint);
            Assert.fail("An exception should be thrown duo to absence of endpoint URL");
        } catch (Exception e) {

            if(! (e instanceof BinderException)){
                Assert.fail("An exception should be thrown duo to absence of class name");
            }

        }

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

        importer.addDeclaration(declaration);

    }

    @Test
    public void localProxyInvocationShouldSucceed() throws Exception {

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("id","b");
        metadata.put("className",ServiceForExportation.class.getName());
        metadata.put("endpoint.url",ENDPOINT_URL);

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        importer.registration(importerServiceRegistration);

        declaration.bind(importerServiceRegistration);

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
        Assert.assertEquals(returnPongInteger, intValue);

    }

    @Test
    public void loadingClassDoesntExist() throws BinderException {

        JAXWSImporter importer=new JAXWSImporter(context);

        importer.start();

        Map<String, Object> metadataNoClassName=new HashMap<String, Object>();

        metadataNoClassName.put("id", "b");
        metadataNoClassName.put("className", ServiceForExportation.class.getName()+"DontExist");
        metadataNoClassName.put("endpoint.url", ENDPOINT_URL);

        ImportDeclarationBuilder builder=mock(ImportDeclarationBuilder.class);

        ImportDeclaration declaration = spy(builder.fromMetadata(metadataNoClassName).build());

        importer.registration(importerServiceRegistration);

        declaration.bind(importerServiceRegistration);

        importer.addDeclaration(declaration);

        verify(declaration, never()).handle(importerServiceRegistration);

    }

    @Test
    public void importerNameCannotBeNull(){

        Assert.assertNotNull(importer.getName());

    }

}
