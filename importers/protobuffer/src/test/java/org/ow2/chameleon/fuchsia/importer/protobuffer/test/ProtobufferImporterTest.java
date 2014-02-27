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
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.importer.protobuffer.ProtobufferImporter;
import org.ow2.chameleon.fuchsia.importer.protobuffer.common.ProtobufferTestAbstract;
import org.ow2.chameleon.fuchsia.importer.protobuffer.common.ctd.AddressBookProtos;
import org.ow2.chameleon.fuchsia.importer.protobuffer.internal.ProtobufferImporterPojo;

import java.util.*;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ProtobufferImporterTest extends ProtobufferTestAbstract<ImportDeclaration,ProtobufferImporter> {

    protected Server server;

    private AddressBookProtos.AddressBookService protobufferRemoteServiceProxy;

    private ImportDeclaration declaration;

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

        when(context.registerService(anyString(), anyObject(), any(Dictionary.class))).thenAnswer(new Answer<ServiceRegistration>() {
            public ServiceRegistration answer(InvocationOnMock invocationOnMock) throws Throwable {

                if(invocationOnMock.getArguments()[1]==null) {
                    throw new NullPointerException("object to be registered is null");
                }

                protobufferRemoteServiceProxy =(AddressBookProtos.AddressBookService)invocationOnMock.getArguments()[1];
                return proxyServiceRegistration;
            }
        });

        fuchsiaDeclarationBinder=spy(constructor().withParameterTypes(BundleContext.class).in(ProtobufferImporter.class).newInstance(context));
        declaration=getValidDeclarations().get(0);
        declaration.bind(fuchsiaDeclarationBinderServiceReference);
        fuchsiaDeclarationBinder.registration(fuchsiaDeclarationBinderServiceReference);
        fuchsiaDeclarationBinder.start();
    }

    @Test
    public void testImportProxyInvocation() throws Exception {

        ProtobufferImporterPojo pojo=ProtobufferImporterPojo.create(declaration);

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
        ProtobufferImporterPojo pojo=ProtobufferImporterPojo.create(declaration);
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

        final ImportDeclaration declarationInvalid=ImportDeclarationBuilder.fromMetadata(metadata).build();
        //Its mandatory to be binded in order that the importer is able to process it
        //declarationInvalid.bind(fuchsiaDeclarationBinderServiceReference);

        return new ArrayList<ImportDeclaration>(){{add(declarationInvalid);}};
    }

    @Override
    public List<ImportDeclaration> getValidDeclarations() {
        final Map<String, Object> metadata=new Hashtable<String, Object>();
        metadata.put("id","protobuffer-importer-valid-declaration");
        metadata.put("rpc.server.address",ENDPOINT_URL);
        metadata.put("rpc.proto.class",AddressBookProtos.class.getName());
        metadata.put("rpc.proto.message","AddressBookServiceMessage");
        metadata.put("rpc.proto.service","AddressBookService");

        final ImportDeclaration declarationValid=ImportDeclarationBuilder.fromMetadata(metadata).build();
        //Its mandatory to be binded in order that the importer is able to process it
        declarationValid.bind(fuchsiaDeclarationBinderServiceReference);

        return new ArrayList<ImportDeclaration>(){{add(declarationValid);}};
    }
}
