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
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.exporter.protobuffer.internal.ProtobufferExporterPojo;
import org.ow2.chameleon.fuchsia.exporter.protobuffer.test.base.ProtobufferExporterAbstractTest;
import org.ow2.chameleon.fuchsia.exporter.protobuffer.test.ctd.AddressBookProtos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.spy;

public class ProtobufferExporterTest extends ProtobufferExporterAbstractTest {

    @Test
    public void testInvalidDeclaration() {
        ExportDeclaration declaration = getInvalidDeclaration();
        try {
            exporter.useDeclaration(declaration);
            Assert.fail("This declaration should produce a BinderException duo to absence of information");
        } catch (BinderException e) {
            //If we got here, its ok
        }
    }

    @Test
    public void testValidDeclaration() throws BinderException, InvalidSyntaxException, ClassNotFoundException, InterruptedException, InvocationTargetException, EndpointException, IllegalAccessException, NoSuchMethodException {
        ExportDeclaration declaration= spy(getValidDeclaration());
        try {
            exporter.useDeclaration(declaration);
        } catch (BinderException e) {
            Assert.fail("This declaration should produce a BinderException duo to absence of information");
        }
    }

    @Test
    public void endpointStartupAndCleanup() throws BinderException, InvalidSyntaxException, ClassNotFoundException, InterruptedException, InvocationTargetException, EndpointException, IllegalAccessException, NoSuchMethodException {
        ExportDeclaration declaration = getValidDeclaration();
        exporter.useDeclaration(declaration);
        Map<String,Server> serverPublished = field("serverPublished").ofType(Map.class).in(exporter).get();
        Assert.assertEquals(1,serverPublished.size());
        exporter.stop();
        Assert.assertEquals(0,serverPublished.size());
    }

    @Test
    public void testExportDeclaration() throws BinderException, InvalidSyntaxException, ClassNotFoundException, InterruptedException, InvocationTargetException, EndpointException, IllegalAccessException, NoSuchMethodException {

        ExportDeclaration declaration = getValidDeclaration();
        exporter.useDeclaration(declaration);
        Map<String,Server> serverPublished = field("serverPublished").ofType(Map.class).in(exporter).get();

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
}
