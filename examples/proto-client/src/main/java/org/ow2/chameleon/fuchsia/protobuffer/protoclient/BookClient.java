package org.ow2.chameleon.fuchsia.protobuffer.protoclient;

import com.google.code.cxf.protobuf.client.SimpleRpcController;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.Service;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.fuchsia.protobuffer.protoclass.AddressBookProtos;

@Component
@Instantiate
public class BookClient {

    private BundleContext context;

    @Requires (filter = "(fuchsia.importer.id=virtual-camera)",proxy = false)
    AddressBookProtos.AddressBookService serviceRaw;

    public BookClient(BundleContext context){
        this.context=context;
    }


    @Validate
    public void validate(){

        SimpleRpcController controller = new SimpleRpcController();


        AddressBookProtos.Person.Builder person = AddressBookProtos.Person.newBuilder();

        person.setId(1);
        person.setName("Alice");
        AddressBookProtos.Person alice = person.build();

        serviceRaw.addPerson(controller, alice, new RpcCallback<AddressBookProtos.AddressBookSize>() {
            public void run(AddressBookProtos.AddressBookSize size) {
                System.out.println("\nThere are " + size.getSize()
                        + " person(s) in the address book now.");
            }
        });

        controller.reset();

        System.out.println("\nSearching for people with 'A' in their name.");
        serviceRaw.listPeople(controller, AddressBookProtos.NamePattern.newBuilder().setPattern("A")
                .build(), new RpcCallback<AddressBookProtos.AddressBook>() {
            public void run(AddressBookProtos.AddressBook response) {

                System.out.println("\nList of people found: \n");

                for(AddressBookProtos.Person person:response.getPersonList()){

                    System.out.println("-->"+person.getName());

                }


            }
        });

    }

}
