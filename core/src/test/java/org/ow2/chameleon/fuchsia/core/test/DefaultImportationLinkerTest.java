package org.ow2.chameleon.fuchsia.core.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.ow2.chameleon.fuchsia.core.component.DefaultImportationLinker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;

public class DefaultImportationLinkerTest {

    @Mock
    BundleContext bundleContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); //initialize the object with mocks annotations
    }

    @After
    public void tearDown() {

    }

    public void setProperties(DefaultImportationLinker il, String name, String filterIDec, String filterIServ){
        field("linkerName").ofType(String.class).in(il).set(name);
        field("importDeclarationFilterProperty").ofType(Object.class).in(il).set(filterIDec);
        field("importerServiceFilterProperty").ofType(Object.class).in(il).set(filterIServ);
    }

    @Test
    public void testCreation() {
        DefaultImportationLinker il = new DefaultImportationLinker(bundleContext);
        il.start();
        assertThat(il.getImportDeclarations()).isEmpty();
        assertThat(il.getLinkedImporters()).isEmpty();
        il.stop();
    }

    @Test
    public void testProperties() {
        DefaultImportationLinker il = new DefaultImportationLinker(bundleContext);
        il.start();
        setProperties(il, "TestedLinker", "(scope=generic)", "(instance.name=TestImporter)");
        assertThat(il.getName()).isEqualTo("TestedLinker");
        il.updated();
        Filter importDeclarationFilter = field("importDeclarationFilter").ofType(Filter.class).in(il).get();
        Filter importerServiceFilter = field("importerServiceFilter").ofType(Filter.class).in(il).get();
        assertThat(importDeclarationFilter.toString()).isEqualTo("(scope=generic)");
        assertThat(importerServiceFilter.toString()).isEqualTo("(instance.name=TestImporter)");
        assertThat(field("state").ofType(boolean.class).in(il).get()).isTrue();
    }

    @Test
    public void testIServPropFilterFail() {
        DefaultImportationLinker il = new DefaultImportationLinker(bundleContext);
        il.start();
        setProperties(il, "TestedLinker", "(scope=generic)", "(instance.name=TestImporter");
        assertThat(il.getName()).isEqualTo("TestedLinker");
        il.updated();
        Filter importerServiceFilter = field("importerServiceFilter").ofType(Filter.class).in(il).get();
        assertThat(importerServiceFilter).isNull();
        assertThat(field("state").ofType(boolean.class).in(il).get()).isFalse();
    }

    @Test
    public void testIDecPropFilterFail() {
        DefaultImportationLinker il = new DefaultImportationLinker(bundleContext);
        il.start();
        setProperties(il, "TestedLinker", "(scope=generic", "(instance.name=TestImporter)");
        assertThat(il.getName()).isEqualTo("TestedLinker");
        il.updated();
        Filter importDeclarationFilter = field("importDeclarationFilter").ofType(Filter.class).in(il).get();
        assertThat(importDeclarationFilter).isNull();
        assertThat(field("state").ofType(boolean.class).in(il).get()).isFalse();

    }

}
