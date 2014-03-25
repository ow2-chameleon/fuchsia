package org.ow2.chameleon.fuchsia.protobuffer.protoclient;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Example Protobuffer Proto Client
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.code.cxf.protobuf.client.SimpleRpcController;
import com.google.protobuf.RpcCallback;
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

    @Requires(filter = "(fuchsia.importer.id=cxf-protobuffer-importer)")
    AddressBookProtos.AddressBookService addressBook;

    public BookClient(BundleContext context) {
        this.context = context;
    }


    @Validate
    public void validate() {

        SimpleRpcController controller = new SimpleRpcController();


        AddressBookProtos.Person.Builder person = AddressBookProtos.Person.newBuilder();

        person.setId(1);
        person.setName("Alice");
        AddressBookProtos.Person alice = person.build();

        addressBook.addPerson(controller, alice, new RpcCallback<AddressBookProtos.AddressBookSize>() {
            public void run(AddressBookProtos.AddressBookSize size) {
                System.out.println("\nThere are " + size.getSize()
                        + " person(s) in the address book now.");
            }
        });

        controller.reset();

        System.out.println("\nSearching for people with 'A' in their name.");
        addressBook.listPeople(controller, AddressBookProtos.NamePattern.newBuilder().setPattern("A")
                .build(), new RpcCallback<AddressBookProtos.AddressBook>() {
            public void run(AddressBookProtos.AddressBook response) {

                System.out.println("\nList of people found: \n");

                for (AddressBookProtos.Person person : response.getPersonList()) {

                    System.out.println("-->" + person.getName());

                }


            }
        });

    }

}
