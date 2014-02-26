package org.ow2.chameleon.fuchsia.importer.protobuffer.test.base;

import com.google.code.cxf.protobuf.ProtobufServerFactoryBean;
import com.google.code.cxf.protobuf.binding.ProtobufBindingFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.Server;
import org.eclipse.jetty.util.component.Container;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.importer.protobuffer.ProtobufferImporter;
import org.ow2.chameleon.fuchsia.importer.protobuffer.internal.ProtobufferImporterPojo;
import org.ow2.chameleon.fuchsia.importer.protobuffer.test.ctd.AddressBookProtos;
import org.ow2.chameleon.fuchsia.importer.protobuffer.test.ctd.AddressBookServiceImpl;

import java.util.Hashtable;
import java.util.Map;

import static org.mockito.Mockito.spy;

public abstract class ProtobufferImporterAbstractTest {

    public static final Integer HTTP_PORT=8046;

    public static final String ENDPOINT_URL="http://localhost:"+HTTP_PORT+"/cxf/" + AddressBookProtos.AddressBookService.class.getSimpleName();

    org.eclipse.jetty.server.Server httpServer;

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

    protected AddressBookProtos.AddressBookService proxyService;

    protected final AddressBookServiceImpl id=new AddressBookServiceImpl();

    protected Bus cxfbus;

    protected ProtobufferImporter importer;

    protected Server server;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        registerMockitoMockInterception();

    }

    public ImportDeclaration getValidImportDeclaration(){
        Map<String, Object> metadata=new Hashtable<String, Object>();
        metadata.put("id","protobuffer-importer");
        metadata.put("rpc.server.address",ENDPOINT_URL);
        metadata.put("rpc.proto.class",AddressBookProtos.class.getName());
        metadata.put("rpc.proto.message","AddressBookServiceMessage");
        metadata.put("rpc.proto.service","AddressBookService");

        return ImportDeclarationBuilder.fromMetadata(metadata).build();
    }

    public ImportDeclaration getInvalidImportDeclaration(){
        Map<String, Object> metadata=new Hashtable<String, Object>();
        //metadata.put("id","protobuffer-importer");
        metadata.put("rpc.server.address",ENDPOINT_URL);
        metadata.put("rpc.proto.class",AddressBookProtos.class.getName());
        metadata.put("rpc.proto.message","AddressBookServiceMessage");
        metadata.put("rpc.proto.service","AddressBookService");

        return ImportDeclarationBuilder.fromMetadata(metadata).build();
    }

    public abstract void registerMockitoMockInterception();

    protected void startServer(ProtobufferImporterPojo pojo){

        cxfbus = BusFactory.getDefaultBus(true);
        BindingFactoryManager mgr = cxfbus.getExtension(BindingFactoryManager.class);
        mgr.registerBindingFactory(ProtobufBindingFactory.PROTOBUF_BINDING_ID, new ProtobufBindingFactory(cxfbus));
        ProtobufServerFactoryBean serverFactoryBean = new ProtobufServerFactoryBean();
        serverFactoryBean.setAddress(pojo.getAddress());
        serverFactoryBean.setBus(cxfbus);
        serverFactoryBean.setServiceBean(id);
        serverFactoryBean.setMessageClass(AddressBookProtos.AddressBookServiceMessage.class);
        Thread.currentThread().setContextClassLoader(Container.class.getClassLoader());
        server=serverFactoryBean.create();

    }

    protected void stopServer(){
        server.stop();
    }
}
