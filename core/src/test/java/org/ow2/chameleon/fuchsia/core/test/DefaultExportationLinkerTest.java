package org.ow2.chameleon.fuchsia.core.test;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.ow2.chameleon.fuchsia.core.component.DefaultExportationLinker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;

public class DefaultExportationLinkerTest {

    @Mock
    BundleContext bundleContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); //initialize the object with mocks annotations
    }

    @After
    public void tearDown() {

    }

    public void setProperties(DefaultExportationLinker il, String name, String filterIDec, String filterIServ) {
        field("linkerName").ofType(String.class).in(il).set(name);
        field("exportDeclarationFilterProperty").ofType(Object.class).in(il).set(filterIDec);
        field("exporterServiceFilterProperty").ofType(Object.class).in(il).set(filterIServ);
    }

    @Test
    public void testCreation() {
        DefaultExportationLinker il = new DefaultExportationLinker(bundleContext);
        il.start();
        assertThat(il.getExportDeclarations()).isEmpty();
        assertThat(il.getLinkedExporters()).isEmpty();
        il.stop();
    }

    @Test
    public void testProperties() {
        DefaultExportationLinker il = new DefaultExportationLinker(bundleContext);
        il.start();
        setProperties(il, "TestedLinker", "(scope=generic)", "(instance.name=TestExporter)");
        assertThat(il.getName()).isEqualTo("TestedLinker");
        il.updated();
        Filter exportDeclarationFilter = field("exportDeclarationFilter").ofType(Filter.class).in(il).get();
        Filter exporterServiceFilter = field("exporterServiceFilter").ofType(Filter.class).in(il).get();
        assertThat(exportDeclarationFilter.toString()).isEqualTo("(scope=generic)");
        assertThat(exporterServiceFilter.toString()).isEqualTo("(instance.name=TestExporter)");
        assertThat(field("state").ofType(boolean.class).in(il).get()).isTrue();
    }

    @Test
    public void testIServPropFilterFail() {
        DefaultExportationLinker il = new DefaultExportationLinker(bundleContext);
        il.start();
        setProperties(il, "TestedLinker", "(scope=generic)", "(instance.name=TestExporter");
        assertThat(il.getName()).isEqualTo("TestedLinker");
        il.updated();
        Filter exporterServiceFilter = field("exporterServiceFilter").ofType(Filter.class).in(il).get();
        assertThat(exporterServiceFilter).isNull();
        assertThat(field("state").ofType(boolean.class).in(il).get()).isFalse();
    }

    @Test
    public void testIDecPropFilterFail() {
        DefaultExportationLinker il = new DefaultExportationLinker(bundleContext);
        il.start();
        setProperties(il, "TestedLinker", "(scope=generic", "(instance.name=TestExporter)");
        assertThat(il.getName()).isEqualTo("TestedLinker");
        il.updated();
        Filter exportDeclarationFilter = field("exportDeclarationFilter").ofType(Filter.class).in(il).get();
        assertThat(exportDeclarationFilter).isNull();
        assertThat(field("state").ofType(boolean.class).in(il).get()).isFalse();

    }

}
