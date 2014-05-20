package org.ow2.chameleon.fuchsia.core.declaration.test;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core
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

import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.declaration.Status;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Check status.
 */
public class StatusTest {

    @Test
    public void testCreationFromEmptySet() throws Exception {
        Status status = Status.from(Collections.<ServiceReference>emptySet(), Collections.<ServiceReference>emptySet());
        assertThat(status.isBound()).isEqualTo(false);
        assertThat(status.isHandled()).isEqualTo(false);
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(0);
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(0);
    }

    @Test
    public void testCreationBound() throws Exception {
        Set<ServiceReference> set = new HashSet<ServiceReference>();
        set.add(mock(ServiceReference.class));
        set.add(mock(ServiceReference.class));
        Status status = Status.from(set, Collections.<ServiceReference>emptySet());
        assertThat(status.isBound()).isEqualTo(true);
        assertThat(status.isHandled()).isEqualTo(false);
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(2);
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(0);
    }

    @Test
    public void testCreationHandled() throws Exception {
        Set<ServiceReference> set = new HashSet<ServiceReference>();
        set.add(mock(ServiceReference.class));
        set.add(mock(ServiceReference.class));
        Status status = Status.from(Collections.<ServiceReference>emptySet(), set);
        assertThat(status.isBound()).isEqualTo(false);
        assertThat(status.isHandled()).isEqualTo(true);
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(0);
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(2);
    }

    @Test
    public void testCreation() throws Exception {
        Set<ServiceReference> setBound = new HashSet<ServiceReference>();
        setBound.add(mock(ServiceReference.class));
        setBound.add(mock(ServiceReference.class));
        Set<ServiceReference> setHandled = new HashSet<ServiceReference>();
        setHandled.add(mock(ServiceReference.class));
        Status status = Status.from(setBound, setHandled);
        assertThat(status.isBound()).isEqualTo(true);
        assertThat(status.isHandled()).isEqualTo(true);
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(2);
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutabilityBoundSet() throws Exception {
        Set<ServiceReference> set = new HashSet<ServiceReference>();
        set.add(mock(ServiceReference.class));
        Status status = Status.from(set, Collections.<ServiceReference>emptySet());
        assertThat(status.isBound()).isEqualTo(true);
        status.getServiceReferencesBounded().add(mock(ServiceReference.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutabilityHandledSet() throws Exception {
        Set<ServiceReference> set = new HashSet<ServiceReference>();
        set.add(mock(ServiceReference.class));
        Status status = Status.from(Collections.<ServiceReference>emptySet(), set);
        assertThat(status.isHandled()).isEqualTo(true);
        status.getServiceReferencesHandled().add(mock(ServiceReference.class));
    }

    @Test
    public void testDependency() throws Exception {
        Set<ServiceReference> setBound = new HashSet<ServiceReference>();
        setBound.add(mock(ServiceReference.class));
        Set<ServiceReference> setHandled = new HashSet<ServiceReference>();
        setHandled.add(mock(ServiceReference.class));
        Status status = Status.from(setBound, setHandled);
        assertThat(status.isBound()).isEqualTo(true);
        assertThat(status.isHandled()).isEqualTo(true);
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(1);
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(1);

        setBound.add(mock(ServiceReference.class));
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(1);

        setHandled.add(mock(ServiceReference.class));
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectCreationBound() {
        Status.from(null, Collections.<ServiceReference>emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectCreationHandled() {
        Status.from(Collections.<ServiceReference>emptySet(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectCreation() {
        Status.from(null, null);
    }
}
