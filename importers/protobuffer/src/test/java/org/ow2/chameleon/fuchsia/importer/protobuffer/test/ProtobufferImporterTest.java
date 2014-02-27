package org.ow2.chameleon.fuchsia.importer.protobuffer.test;

import com.google.code.cxf.protobuf.ProtobufServerFactoryBean;
import com.google.code.cxf.protobuf.binding.ProtobufBindingFactory;
import com.google.code.cxf.protobuf.client.SimpleRpcController;
import com.google.protobuf.RpcCallback;
import junit.framework.Assert;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.Server;
import org.eclipse.jetty.util.component.Container;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.importer.protobuffer.ProtobufferImporter;
import org.ow2.chameleon.fuchsia.importer.protobuffer.common.ProtobufferTestAbstract;
import org.ow2.chameleon.fuchsia.importer.protobuffer.common.ctd.AddressBookProtos;
import org.ow2.chameleon.fuchsia.importer.protobuffer.internal.ProtobufferImporterPojo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.spy;

public class ProtobufferImporterTest extends ProtobufferTestAbstract<ImportDeclaration,ProtobufferImporter> {

    protected Server server;

    protected void startStandaloneServer(ProtobufferImporterPojo pojo){

        Bus cxfbus = BusFactory.getDefaultBus(true);
        BindingFactoryManager mgr = cxfbus.getExtension(BindingFactoryManager.class);
        mgr.registerBindingFactory(ProtobufBindingFactory.PROTOBUF_BINDING_ID, new ProtobufBindingFactory(cxfbus));
        ProtobufServerFactoryBean serverFactoryBean = new ProtobufServerFactoryBean();
        serverFactoryBean.setAddress(pojo.getAddress());
        serverFactoryBean.setBus(cxfbus);
        serverFactoryBean.setServiceBean(protobufferRemoteService);
        serverFactoryBean.setMessageClass(AddressBookProtos.AddressBookServiceMessage.class);
        Thread.currentThread().setContextClassLoader(Container.class.getClassLoader());
        server=serverFactoryBean.create();

    }

    protected void stopStandaloneServer(){
        server.stop();
    }

    public void initInterceptors() throws Exception {

         super.initInterceptors();

         fuchsiaDeclarationBinder = spy(new ProtobufferImporter(context));

    }

    @Test
    public void testImportProxyInvocation() throws Exception {

        ImportDeclaration declaration=getValidDeclarations().get(0);
        ProtobufferImporterPojo pojo=ProtobufferImporterPojo.create(declaration);

        fuchsiaDeclarationBinder.start();

        startStandaloneServer(pojo);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        SimpleRpcController controller = new SimpleRpcController();

        final AddressBookProtos.Person.Builder personAlice = AddressBookProtos.Person.newBuilder();
        AddressBookProtos.Person alice = personAlice.setId(1).setName("Alice").build();

        protobufferRemoteServiceProxy.addPerson(controller, alice, new RpcCallback<AddressBookProtos.AddressBookSize>() {
            public void run(AddressBookProtos.AddressBookSize size) {
                Assert.assertEquals(1, size.getSize());
            }
        });

        controller.reset();
        protobufferRemoteServiceProxy.listPeople(controller, AddressBookProtos.NamePattern.newBuilder().setPattern("A")
                .build(), new RpcCallback<AddressBookProtos.AddressBook>() {
            public void run(AddressBookProtos.AddressBook response) {
                Assert.assertEquals(1, response.getPersonList().size());
                for (AddressBookProtos.Person personIt : response.getPersonList()) {
                    Assert.assertEquals(personIt.getName(), personAlice.getName());
                }
            }
        });

        fuchsiaDeclarationBinder.stop();

        stopStandaloneServer();

    }

    @Test
    public void testCleanupAfterStop() throws Exception {

        ImportDeclaration declaration=getValidDeclarations().get(0);
        ProtobufferImporterPojo pojo=ProtobufferImporterPojo.create(declaration);
        fuchsiaDeclarationBinder.start();
        startStandaloneServer(pojo);
        //Add declaration
        fuchsiaDeclarationBinder.useDeclaration(declaration);
        Map<String,ServiceRegistration> registeredImporters=field("registeredImporter").ofType(Map.class).in(fuchsiaDeclarationBinder).get();
        //Check if the number of registered importers is one
        Assert.assertEquals(1,registeredImporters.size());
        //Stop the service
        fuchsiaDeclarationBinder.stop();
        //Check if the number of registered importers is zero
        Assert.assertEquals(0,registeredImporters.size());

        stopStandaloneServer();
    }

    @Override
    public List<ImportDeclaration> getInvalidDeclarations() {
        final Map<String, Object> metadata=new Hashtable<String, Object>();
        //metadata.put("id","protobuffer-importer");
        metadata.put("rpc.server.address",ENDPOINT_URL);
        metadata.put("rpc.proto.class",AddressBookProtos.class.getName());
        metadata.put("rpc.proto.message","AddressBookServiceMessage");
        metadata.put("rpc.proto.service","AddressBookService");

        return new ArrayList<ImportDeclaration>(){{add(ImportDeclarationBuilder.fromMetadata(metadata).build());}};
    }

    @Override
    public List<ImportDeclaration> getValidDeclarations() {
        final Map<String, Object> metadata=new Hashtable<String, Object>();
        metadata.put("id","protobuffer-importer");
        metadata.put("rpc.server.address",ENDPOINT_URL);
        metadata.put("rpc.proto.class",AddressBookProtos.class.getName());
        metadata.put("rpc.proto.message","AddressBookServiceMessage");
        metadata.put("rpc.proto.service","AddressBookService");

        return new ArrayList<ImportDeclaration>(){{add(ImportDeclarationBuilder.fromMetadata(metadata).build());}};
    }
}
