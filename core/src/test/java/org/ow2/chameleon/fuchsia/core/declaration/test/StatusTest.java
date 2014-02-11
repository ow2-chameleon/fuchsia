package org.ow2.chameleon.fuchsia.core.declaration.test;

import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.declaration.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Check status.
 */
public class StatusTest {

    @Test
    public void testCreationFromEmptyList() throws Exception {
        Status status = Status.from(Collections.<ServiceReference>emptyList(), Collections.<ServiceReference>emptyList());
        assertThat(status.isBound()).isEqualTo(false);
        assertThat(status.isHandled()).isEqualTo(false);
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(0);
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(0);
    }

    @Test
    public void testCreationBound() throws Exception {
        List<ServiceReference> list = new ArrayList<ServiceReference>();
        list.add(mock(ServiceReference.class));
        list.add(mock(ServiceReference.class));
        Status status = Status.from(list, Collections.<ServiceReference>emptyList());
        assertThat(status.isBound()).isEqualTo(true);
        assertThat(status.isHandled()).isEqualTo(false);
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(2);
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(0);
    }

    @Test
    public void testCreationHandled() throws Exception {
        List<ServiceReference> list = new ArrayList<ServiceReference>();
        list.add(mock(ServiceReference.class));
        list.add(mock(ServiceReference.class));
        Status status = Status.from(Collections.<ServiceReference>emptyList(), list);
        assertThat(status.isBound()).isEqualTo(false);
        assertThat(status.isHandled()).isEqualTo(true);
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(0);
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(2);
    }

    @Test
    public void testCreation() throws Exception {
        List<ServiceReference> listBound = new ArrayList<ServiceReference>();
        listBound.add(mock(ServiceReference.class));
        listBound.add(mock(ServiceReference.class));
        List<ServiceReference> listHandled = new ArrayList<ServiceReference>();
        listHandled.add(mock(ServiceReference.class));
        Status status = Status.from(listBound, listHandled);
        assertThat(status.isBound()).isEqualTo(true);
        assertThat(status.isHandled()).isEqualTo(true);
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(2);
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutabilityBoundList() throws Exception {
        List<ServiceReference> list = new ArrayList<ServiceReference>();
        list.add(mock(ServiceReference.class));
        Status status = Status.from(list, Collections.<ServiceReference>emptyList());
        assertThat(status.isBound()).isEqualTo(true);
        status.getServiceReferencesBounded().add(mock(ServiceReference.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutabilityHandledList() throws Exception {
        List<ServiceReference> list = new ArrayList<ServiceReference>();
        list.add(mock(ServiceReference.class));
        Status status = Status.from(Collections.<ServiceReference>emptyList(), list);
        assertThat(status.isHandled()).isEqualTo(true);
        status.getServiceReferencesHandled().add(mock(ServiceReference.class));
    }

    @Test
    public void testDependency() throws Exception {
        List<ServiceReference> listBound = new ArrayList<ServiceReference>();
        listBound.add(mock(ServiceReference.class));
        List<ServiceReference> listHandled = new ArrayList<ServiceReference>();
        listHandled.add(mock(ServiceReference.class));
        Status status = Status.from(listBound, listHandled);
        assertThat(status.isBound()).isEqualTo(true);
        assertThat(status.isHandled()).isEqualTo(true);
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(1);
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(1);

        listBound.add(mock(ServiceReference.class));
        assertThat(status.getServiceReferencesBounded().size()).isEqualTo(1);

        listHandled.add(mock(ServiceReference.class));
        assertThat(status.getServiceReferencesHandled().size()).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectCreationBound() {
        Status.from(null, Collections.<ServiceReference>emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectCreationHandled() {
        Status.from(Collections.<ServiceReference>emptyList(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectCreation() {
        Status.from(null, null);
    }
}
