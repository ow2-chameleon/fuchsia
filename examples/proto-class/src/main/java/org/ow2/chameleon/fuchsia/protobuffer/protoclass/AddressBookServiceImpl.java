package org.ow2.chameleon.fuchsia.protobuffer.protoclass;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Example Protobuffer Proto Class
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

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddressBookServiceImpl extends AddressBookProtos.AddressBookService implements ServiceReference {
    Map<Integer, AddressBookProtos.Person> records = new ConcurrentHashMap<Integer, AddressBookProtos.Person>();

    public void listPeople(RpcController controller,
                           AddressBookProtos.NamePattern request, RpcCallback<AddressBookProtos.AddressBook> done) {
        AddressBookProtos.AddressBook.Builder addressbook = AddressBookProtos.AddressBook
                .newBuilder();

        for (AddressBookProtos.Person person : records.values()) {
            if (person.getName().indexOf(request.getPattern()) >= 0) {
                addressbook.addPerson(person);
            }
        }

        done.run(addressbook.build());
    }

    public void addPerson(RpcController controller,
                          AddressBookProtos.Person request, RpcCallback<AddressBookProtos.AddressBookSize> done) {
        if (records.containsKey(request.getId())) {
            System.out.println("Warning: will replace existing person: " + records.get(request.getId()).getName());
        }
        records.put(request.getId(), request);
        done.run(AddressBookProtos.AddressBookSize.newBuilder().setSize(
                records.size()).build());
    }

    public Object getProperty(String key) {
        return null;
    }

    public String[] getPropertyKeys() {
        return new String[0];
    }

    public Bundle getBundle() {
        return null;
    }

    public Bundle[] getUsingBundles() {
        return new Bundle[0];
    }

    public boolean isAssignableTo(Bundle bundle, String className) {
        return false;
    }

    public int compareTo(Object reference) {
        return 0;
    }
}
