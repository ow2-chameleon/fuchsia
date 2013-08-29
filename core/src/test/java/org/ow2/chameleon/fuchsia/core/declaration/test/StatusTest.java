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
        Status status = Status.from(Collections.<ServiceReference>emptyList());
        assertThat(status.isBound()).isEqualTo(false);
        assertThat(status.getServiceReferences().size()).isEqualTo(0);
    }

    @Test
    public void testCreation() throws Exception {
        List<ServiceReference> list = new ArrayList<ServiceReference>();
        list.add(mock(ServiceReference.class));
        list.add(mock(ServiceReference.class));
        Status status = Status.from(list);
        assertThat(status.isBound()).isEqualTo(true);
        assertThat(status.getServiceReferences().size()).isEqualTo(2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutability() throws Exception {
        List<ServiceReference> list = new ArrayList<ServiceReference>();
        list.add(mock(ServiceReference.class));
        Status status = Status.from(list);
        assertThat(status.isBound()).isEqualTo(true);

        status.getServiceReferences().add(mock(ServiceReference.class));
    }

    @Test
    public void testDependency() throws Exception {
        List<ServiceReference> list = new ArrayList<ServiceReference>();
        list.add(mock(ServiceReference.class));
        Status status = Status.from(list);
        assertThat(status.isBound()).isEqualTo(true);
        assertThat(status.getServiceReferences().size()).isEqualTo(1);

        list.add(mock(ServiceReference.class));
        assertThat(status.getServiceReferences().size()).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectCreation() {
        Status status = Status.from(null);
    }
}
