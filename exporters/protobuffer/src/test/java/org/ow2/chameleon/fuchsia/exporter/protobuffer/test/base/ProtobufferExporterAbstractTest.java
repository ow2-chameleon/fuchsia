package org.ow2.chameleon.fuchsia.exporter.protobuffer.test.base;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.*;
import org.osgi.service.http.HttpService;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.exporter.protobuffer.ProtobufferExporter;
import org.ow2.chameleon.fuchsia.exporter.protobuffer.test.ctd.AddressBookProtos;
import org.ow2.chameleon.fuchsia.exporter.protobuffer.test.ctd.AddressBookServiceImpl;

import java.util.*;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ProtobufferExporterAbstractTest {

    protected static final Integer HTTP_PORT=8043;

    @Mock
    protected org.osgi.framework.BundleContext context;

    @Mock
    protected ServiceRegistration registrationFromClassToBeExported;

    @Mock
    protected ServiceReference serviceReferenceFromExporter;

    @Mock
    protected ExportedPackage exportPackageForClass;

    @Mock
    protected Bundle bundeToLoadClassFrom;

    @Mock
    protected PackageAdmin packageAdminMock;

    @Mock
    protected AddressBookProtos.AddressBookService id;

    //protected ServiceReference idServiceReference[]=new ServiceReference[]{id};

    protected ProtobufferExporter exporter;

    @Mock
    protected HttpService httpServiceMock;

    final AddressBookServiceImpl asi=new AddressBookServiceImpl();

    final ServiceReference serviceReference=mock(ServiceReference.class);

    Class<? extends Class> classService=AddressBookProtos.AddressBookService.class.getClass();

    /**
     * Instantiate all mocks necessary for the exportation, and invokes @Validate method from the exporter
     */
    @Before
    public void setup() throws InvalidSyntaxException, ClassNotFoundException {

        MockitoAnnotations.initMocks(this);

        prepareMockInterceptors();

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

        //idServiceReference=null;

    }

    protected void prepareMockInterceptors() throws InvalidSyntaxException, ClassNotFoundException {

        Dictionary<String, Object> props1 = new Hashtable<String, Object>();

        when(context.registerService(new String[]{ExportDeclaration.class.getName()}, id, props1)).thenReturn(registrationFromClassToBeExported);
        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(serviceReferenceFromExporter);
        when(serviceReferenceFromExporter.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
        when(context.getService(serviceReferenceFromExporter)).thenReturn(packageAdminMock);
        when(context.getBundle()).thenReturn(bundeToLoadClassFrom);

        when(context.getServiceReferences(any(Class.class), any(String.class))).thenAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                Class load=(Class)invocationOnMock.getArguments()[0];

                if(load.toString().contains("AddressBookProtos$AddressBookService") ){
                    Collection<ServiceReference> references=new HashSet<ServiceReference>(){{add(asi);}};
                    return  references;
                }else {
                    Collection<ServiceReference> references=new HashSet<ServiceReference>(){{add(serviceReference);}};
                    return references;
                }

            }
        });
        when(context.getService(serviceReference)).thenReturn(id);
        when(context.getService(asi)).thenReturn(asi);
        when(bundeToLoadClassFrom.loadClass(anyString())).thenAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                Class clazz=getClass().getClassLoader().loadClass((String) invocationOnMock.getArguments()[0]);

                return clazz;
            }
        });

        exporter=constructor().withParameterTypes(BundleContext.class).in(ProtobufferExporter.class).newInstance(context);

        field("httpPort").ofType(Integer.class).in(exporter).set(HTTP_PORT);
    }

    protected ExportDeclaration getValidDeclaration(){
        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("id","protobuffer-exporter");
        metadata.put("rpc.export.address","http://localhost:8085/cxf/AddressBookService");
        metadata.put("rpc.export.class",AddressBookProtos.class.getName());
        metadata.put("rpc.export.message","AddressBookServiceMessage");
        metadata.put("rpc.export.service","AddressBookService");

        ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();

        return declaration;
    }

    protected ExportDeclaration getInvalidDeclaration(){
        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("id","protobuffer-exporter");
        //metadata.put("rpc.export.address","http://localhost:8085/cxf/AddressBookService");
        metadata.put("rpc.export.class","org.jander.ctd.AddressBookProtos");
        metadata.put("rpc.export.message","AddressBookServiceMessage");
        metadata.put("rpc.export.service","AddressBookService");

        ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();

        return declaration;
    }

}
