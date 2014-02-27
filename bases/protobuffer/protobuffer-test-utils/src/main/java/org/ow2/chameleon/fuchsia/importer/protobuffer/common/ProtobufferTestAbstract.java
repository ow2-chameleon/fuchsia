package org.ow2.chameleon.fuchsia.importer.protobuffer.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.protobuffer.common.ctd.AddressBookProtos;
import org.ow2.chameleon.fuchsia.importer.protobuffer.common.ctd.AddressBookServiceImpl;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public abstract class ProtobufferTestAbstract<T extends Declaration,S extends DeclarationBinder> {

    public static final Integer HTTP_PORT=8046;

    public static final String ENDPOINT_URL="http://localhost:"+HTTP_PORT+"/cxf/" + AddressBookProtos.AddressBookService.class.getSimpleName();

    protected S fuchsiaDeclarationBinder;

    @Mock
    protected ServiceReference fuchsiaDeclarationBinderServiceReference;

    protected final AddressBookServiceImpl protobufferRemoteService =new AddressBookServiceImpl();

    @Mock
    protected PackageAdmin packageAdminMock;

    @Mock
    protected ExportedPackage importPackageForClass;

    @Mock
    protected Bundle bundeToLoadClassFrom;

    @Mock
    protected ServiceRegistration proxyServiceRegistration;

    @Mock
    protected org.osgi.framework.BundleContext context;

    public void initInterceptors() throws Exception{
        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(fuchsiaDeclarationBinderServiceReference);
        when(packageAdminMock.getExportedPackage(protobufferRemoteService.getClass().getName())).thenReturn(importPackageForClass);
        when(context.getService(fuchsiaDeclarationBinderServiceReference)).thenReturn(packageAdminMock);
        when(context.getBundle()).thenReturn(bundeToLoadClassFrom);
        when(fuchsiaDeclarationBinderServiceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
        //when(context.registerService(any(Class.class), any(protobufferRemoteService.getClass()), any(Dictionary.class))).thenReturn(proxyServiceRegistration);
        when(bundeToLoadClassFrom.loadClass(anyString())).thenAnswer(new Answer<Class>() {
            public Class answer(InvocationOnMock invocation) throws Throwable {
                Class clazz=getClass().getClassLoader().loadClass((String) invocation.getArguments()[0]);
                return clazz;
            }
        });
    }

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        initInterceptors();

    }

    @Test
    public void testValidDeclarations() throws Exception {
        try {
            for(T declaration:getValidDeclarations()){
                fuchsiaDeclarationBinder.useDeclaration(declaration);
            }
        }catch(BinderException be){
            Assert.fail("A BinderException should NOT have been thrown since not all information were provided");
        }
    }

    @Test
    public void testInvalidDeclarationExceptionThrown() throws Exception {
        try {
            for(T declaration:getInvalidDeclarations()){
                fuchsiaDeclarationBinder.useDeclaration(declaration);
            }
            Assert.fail("A BinderException should have been thrown since not all information required were provided");
        }catch(BinderException be){
            //An exception for this case is normal, since not all information were provided
        }
    }

    public abstract List<T> getInvalidDeclarations();

    public abstract List<T> getValidDeclarations();

}
