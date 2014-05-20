package org.ow2.chameleon.fuchsia.core.component.test;

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
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AbstractDiscoveryComponentTest {


    @Mock
    BundleContext bundleContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); //initialize the object with mocks annotations
        when(bundleContext.registerService(any(String[].class), any(Declaration.class), any(Dictionary.class)))
                .thenReturn(mock(ServiceRegistration.class));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testInstantiation() {
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();
    }

    @Test
    public void testRegisterImportDeclaration() {
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        testedClass.addIdec(id);
        verify(bundleContext, times(1)).registerService(any(String[].class), eq(id), any(Dictionary.class));

        assertThat(testedClass.getImportDeclarations()).containsExactly(id);
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterTwoTimesImportDeclaration() {
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        testedClass.addIdec(id);
        testedClass.addIdec(id);
    }

    @Test
    public void testUnregisterImportDeclaration() {
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        ServiceRegistration mockSR = mock(ServiceRegistration.class);

        testedClass.addIdec(id);
        testedClass.removeIdec(id);

        assertThat(testedClass.getImportDeclarations()).isEmpty();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnregisterTwoTimesImportDeclaration() {
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        testedClass.addIdec(id);
        testedClass.removeIdec(id);
        testedClass.removeIdec(id);
    }

    @Test
    public void testStop() {
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        testedClass.addIdec(id);
        testedClass.stop();

        assertThat(testedClass.getImportDeclarations()).isEmpty();
    }

    @Test
    public void testToString() {
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        String string = testedClass.toString();
        assertThat(string).isEqualTo("name");
    }


    public class TestedClass extends AbstractDiscoveryComponent {

        protected TestedClass(BundleContext bundleContext) {
            super(bundleContext);
        }

        public String getName() {
            return "name";
        }

        @Override
        protected void start() {
            super.start();
        }

        @Override
        protected void stop() {
            super.stop();
        }

        public void addIdec(ImportDeclaration importDeclaration) {
            registerImportDeclaration(importDeclaration);
        }

        public void removeIdec(ImportDeclaration importDeclaration) {
            unregisterImportDeclaration(importDeclaration);
        }
    }
}
