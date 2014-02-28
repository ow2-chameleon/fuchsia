package org.ow2.chameleon.fuchsia.exporter.protobuffer.test;

import com.google.code.cxf.protobuf.binding.ProtobufBindingFactory;
import com.google.code.cxf.protobuf.client.SimpleRpcChannel;
import com.google.code.cxf.protobuf.client.SimpleRpcController;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import junit.framework.Assert;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.Server;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.exporter.protobuffer.ProtobufferExporter;
import org.ow2.chameleon.fuchsia.exporter.protobuffer.internal.ProtobufferExporterPojo;
import org.ow2.chameleon.fuchsia.importer.protobuffer.common.ProtobufferTestAbstract;
import org.ow2.chameleon.fuchsia.importer.protobuffer.common.ctd.AddressBookProtos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ProtobufferExporterTest extends ProtobufferTestAbstract<ExportDeclaration,ProtobufferExporter> {

    @Mock
    protected ServiceRegistration registrationFromClassToBeExported;

    @Mock
    protected ServiceReference serviceReferenceFromExporter;

    @Mock
    ServiceReference serviceReference;

    @After
    public void setupClean() {

        fuchsiaDeclarationBinder.stop();

    }

    @Override
    public void initInterceptors() throws Exception {

        super.initInterceptors();

        //when(context.registerService(any(Class.class), any(protobufferRemoteService.getClass()), any(Dictionary.class))).thenReturn(proxyServiceRegistration);
        when(context.registerService(new String[]{ExportDeclaration.class.getName()}, protobufferRemoteService, new Hashtable<String, Object>())).thenReturn(registrationFromClassToBeExported);
        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(serviceReferenceFromExporter);
        when(serviceReferenceFromExporter.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
        when(context.getService(serviceReferenceFromExporter)).thenReturn(packageAdminMock);

        when(context.getServiceReferences(any(Class.class), any(String.class))).thenAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                Class load=(Class)invocationOnMock.getArguments()[0];

                Collection<ServiceReference> references=null;

                if(load.toString().contains("AddressBookProtos$AddressBookService") ){
                    references=new HashSet<ServiceReference>(){{add(protobufferRemoteService);}};
                }else {
                    references=new HashSet<ServiceReference>(){{add(serviceReference);}};
                }
                return references;
            }
        });
        when(context.getService(serviceReference)).thenReturn(protobufferRemoteService);
        when(context.getService(protobufferRemoteService)).thenReturn(protobufferRemoteService);

        fuchsiaDeclarationBinder =spy(constructor().withParameterTypes(BundleContext.class).in(ProtobufferExporter.class).newInstance(context));

        //Inject HTTP_PORT, that usually is done by OSGi
        field("httpPort").ofType(Integer.class).in(fuchsiaDeclarationBinder).set(HTTP_PORT);

        fuchsiaDeclarationBinder.start();

    }

    @Test
    public void endpointStartupAndCleanup() throws BinderException, InvalidSyntaxException, ClassNotFoundException, InterruptedException, InvocationTargetException, EndpointException, IllegalAccessException, NoSuchMethodException {
        ExportDeclaration declaration = getValidDeclarations().get(0);
        fuchsiaDeclarationBinder.useDeclaration(declaration);
        Map<String,Server> serverPublished = field("serverPublished").ofType(Map.class).in(fuchsiaDeclarationBinder).get();
        Assert.assertEquals(1,serverPublished.size());
        fuchsiaDeclarationBinder.stop();
        Assert.assertEquals(0,serverPublished.size());
    }

    @Test
    public void testDenyDeclaration() throws BinderException, InvalidSyntaxException, ClassNotFoundException, InterruptedException, InvocationTargetException, EndpointException, IllegalAccessException, NoSuchMethodException {
        ExportDeclaration declaration = getValidDeclarations().get(0);
        fuchsiaDeclarationBinder.useDeclaration(declaration);
        Map<String,Server> serverPublished = field("serverPublished").ofType(Map.class).in(fuchsiaDeclarationBinder).get();
        Assert.assertEquals(1,serverPublished.size());
        fuchsiaDeclarationBinder.denyDeclaration(declaration);
        Assert.assertEquals(0,serverPublished.size());
    }

    @Test
    public void testNameWasProvided(){
        org.junit.Assert.assertNotNull(fuchsiaDeclarationBinder.getName());
    }

    @Test
    public void testExportDeclaration() throws BinderException, InvalidSyntaxException, ClassNotFoundException, InterruptedException, InvocationTargetException, EndpointException, IllegalAccessException, NoSuchMethodException {

        ExportDeclaration declaration = getValidDeclarations().get(0);
        fuchsiaDeclarationBinder.useDeclaration(declaration);
        Map<String,Server> serverPublished = field("serverPublished").ofType(Map.class).in(fuchsiaDeclarationBinder).get();

        Assert.assertEquals(1,serverPublished.size());

        AddressBookProtos.AddressBookService addressBook=connectExportedProtobufAddress(declaration);

        SimpleRpcController controller = new SimpleRpcController();

        final AddressBookProtos.Person.Builder personAlice = AddressBookProtos.Person.newBuilder();
        personAlice.setId(1);
        personAlice.setName("Alice");
        AddressBookProtos.Person alice = personAlice.build();

        final AddressBookProtos.Person.Builder personBrandon = AddressBookProtos.Person.newBuilder();
        personBrandon.setId(2);
        personBrandon.setName("Brandon");
        AddressBookProtos.Person brandon = personBrandon.build();

        addressBook.addPerson(controller, alice, new RpcCallback<AddressBookProtos.AddressBookSize>() {
            public void run(AddressBookProtos.AddressBookSize size) {
                Assert.assertEquals(1, size.getSize());
            }
        });
        controller.reset();
        addressBook.listPeople(controller, AddressBookProtos.NamePattern.newBuilder().setPattern("A")
                .build(), new RpcCallback<AddressBookProtos.AddressBook>() {
            public void run(AddressBookProtos.AddressBook response) {
                Assert.assertEquals(1, response.getPersonList().size());
                for (AddressBookProtos.Person personIt : response.getPersonList()) {
                    Assert.assertEquals(personIt.getName(), personAlice.getName());
                }

            }
        });
        controller.reset();
        addressBook.addPerson(controller, brandon, new RpcCallback<AddressBookProtos.AddressBookSize>() {
            public void run(AddressBookProtos.AddressBookSize size) {
                Assert.assertEquals(2, size.getSize());
            }
        });
        controller.reset();
        addressBook.listPeople(controller, AddressBookProtos.NamePattern.newBuilder()//.setPattern(".")
                .build(), new RpcCallback<AddressBookProtos.AddressBook>() {
            public void run(AddressBookProtos.AddressBook response) {
                Assert.assertEquals(2, response.getPersonList().size());
            }
        });
    }


    private AddressBookProtos.AddressBookService connectExportedProtobufAddress(ExportDeclaration declaration) throws EndpointException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, BinderException {
        ProtobufferExporterPojo pojo=ProtobufferExporterPojo.create(declaration);
        Bus cxfbus = BusFactory.getThreadDefaultBus();
        BindingFactoryManager mgr = cxfbus.getExtension(BindingFactoryManager.class);
        mgr.registerBindingFactory(ProtobufBindingFactory.PROTOBUF_BINDING_ID, new ProtobufBindingFactory(cxfbus));
        Class<?> bufferService = AddressBookProtos.AddressBookService.class;
        Class<?> bufferMessage = AddressBookProtos.AddressBookServiceMessage.class;
        Class<? extends Message> generic = bufferMessage.asSubclass(Message.class);
        RpcChannel channel = new SimpleRpcChannel(pojo.getAddress(), generic);
        Method method = bufferService.getMethod("newStub", RpcChannel.class);
        Object service = method.invoke(bufferService, channel);
        AddressBookProtos.AddressBookService addressBook= (AddressBookProtos.AddressBookService)service;
        return  addressBook;
    }

    @Override
    public List<ExportDeclaration> getInvalidDeclarations() {
        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("id","protobuffer-exporter");
        //metadata.put("rpc.export.address","http://localhost:8085/cxf/AddressBookService");
        metadata.put("rpc.export.class",AddressBookProtos.class.getName());
        metadata.put("rpc.export.message","AddressBookServiceMessage");
        metadata.put("rpc.export.service","AddressBookService");

        final ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();
        return new ArrayList<ExportDeclaration>(){{add(declaration);}};
    }

    @Override
    public List<ExportDeclaration> getValidDeclarations() {

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("id","protobuffer-exporter");
        metadata.put("rpc.export.address","http://localhost:8085/cxf/AddressBookService");//"http://localhost:8085/cxf/AddressBookService"
        metadata.put("rpc.export.class", AddressBookProtos.class.getName());
        metadata.put("rpc.export.message","AddressBookServiceMessage");
        metadata.put("rpc.export.service","AddressBookService");

        final ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();

        return new ArrayList<ExportDeclaration>(){{add(declaration);}};
    }
}
