package org.ow2.chameleon.fuchsia.core.it.util;

import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ow2.chameleon.fuchsia.core.DefaultImportationLinker;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.testing.helpers.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

public class FuchsiaUtilsTest extends BaseTest {

    @Override
    protected Option[] getCustomOptions() {
        return options(
                mavenBundle("org.ow2.chameleon.fuchsia", "fuchsia-core").versionAsInProject(),
                wrappedBundle(mavenBundle("org.assertj", "assertj-core").versionAsInProject())
        );
    }

    @Override
    public boolean deployTestBundle() {
        return false;
    }

    @Test
    public void testLoadClass() throws ClassNotFoundException {
        Class<?> aClass = FuchsiaUtils.loadClass(this.getContext(), DefaultImportationLinker.class.getName());
        assertThat(aClass).isNotNull().isSameAs(DefaultImportationLinker.class);
    }


    @Test
    public void testLoadClassFail() throws ClassNotFoundException {
        String klassName = "i.must.does.not.exist.MyClass";

        try {
            Class<?> aClass = FuchsiaUtils.loadClass(this.getContext(), klassName);
        } catch (ClassNotFoundException e) {
            assertThat(e).hasMessageContaining(klassName);
        }
    }

    @Test
    public void testLoadClassNew() throws ClassNotFoundException {
        Class<?> aClass = FuchsiaUtils.loadClassNew(this.getContext(), DefaultImportationLinker.class.getName());
        assertThat(aClass).isNotNull().isSameAs(DefaultImportationLinker.class);
    }


    @Test
    public void testLoadClassNewFail() throws ClassNotFoundException {
        String klassName = "i.must.does.not.exist.MyClass";

        try {
            Class<?> aClass = FuchsiaUtils.loadClassNew(this.getContext(), klassName);
        } catch (ClassNotFoundException e) {
            assertThat(e).hasMessageContaining(klassName);
        }
    }
}
