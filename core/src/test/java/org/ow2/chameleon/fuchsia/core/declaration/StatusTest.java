package org.ow2.chameleon.fuchsia.core.declaration;

import org.junit.Test;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;

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
        Status status = Status.from(Collections.<ImporterService>emptyList());
        assertThat(status.isBound()).isEqualTo(false);
        assertThat(status.getImporterServices().size()).isEqualTo(0);
    }

    @Test
    public void testCreation() throws Exception {
        List<ImporterService> list = new ArrayList<ImporterService>();
        list.add(mock(ImporterService.class));
        list.add(mock(ImporterService.class));
        Status status = Status.from(list);
        assertThat(status.isBound()).isEqualTo(true);
        assertThat(status.getImporterServices().size()).isEqualTo(2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutability() throws Exception {
        List<ImporterService> list = new ArrayList<ImporterService>();
        list.add(mock(ImporterService.class));
        Status status = Status.from(list);
        assertThat(status.isBound()).isEqualTo(true);

        status.getImporterServices().add(mock(ImporterService.class));
    }

    @Test
    public void testDependency() throws Exception {
        List<ImporterService> list = new ArrayList<ImporterService>();
        list.add(mock(ImporterService.class));
        Status status = Status.from(list);
        assertThat(status.isBound()).isEqualTo(true);
        assertThat(status.getImporterServices().size()).isEqualTo(1);

        list.add(mock(ImporterService.class));
        assertThat(status.getImporterServices().size()).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectCreation() {
        Status status = Status.from(null);
    }
}
