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
import org.osgi.service.log.LogService;
import org.ow2.chameleon.fuchsia.core.component.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.component.AbstractExporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AbstractExporterComponentTest {
    private static final int EXPORT_MAX = 10; //Number max of Export to be tested within a single test.

    //Mock object
    @Mock
    LogService logservice;

    @Mock
    ExportationLinker importationLinker;

    //Tested Object
    private TestedClass testedClass;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); //initialize the object with mocks annotations
        testedClass = new TestedClass();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testExportDeclaration() throws BinderException {
        ExportDeclaration dec = mock(ExportDeclaration.class);
        testedClass.addDeclaration(dec);

        assertThat(testedClass.nbProxies()).isEqualTo(1); //Check that useExportDeclaration has been called

        testedClass.removeDeclaration(dec);

        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that denyExportDeclaration has been called
    }

    @Test(expected = IllegalStateException.class)
    public void testDuplicateExportDeclaration() throws BinderException {
        ExportDeclaration dec = mock(ExportDeclaration.class);
        testedClass.addDeclaration(dec);

        assertThat(testedClass.nbProxies()).isEqualTo(1); //Check that useExportDeclaration has been called

        testedClass.addDeclaration(dec);

        assertThat(testedClass.nbProxies()).isEqualTo(1); //Check that the importer handle correctly the duplication
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveExportDeclarationNotAdded() throws BinderException {
        ExportDeclaration dec = mock(ExportDeclaration.class);
        testedClass.removeDeclaration(dec);
    }

    @Test
    public void testGetExportDeclaration() throws BinderException {
        ExportDeclaration dec = mock(ExportDeclaration.class);
        testedClass.addDeclaration(dec);

        Set<ExportDeclaration> importDeclarations = testedClass.getExportDeclarations();
        assertThat(importDeclarations).containsExactly(dec);
    }


    @Test
    public void testMultiplesExportDeclaration() throws BinderException {
        Collection<ExportDeclaration> decs = new HashSet<ExportDeclaration>();

        for (int i = 0; i < EXPORT_MAX; i++) {
            ExportDeclaration dec = mock(ExportDeclaration.class);
            testedClass.addDeclaration(dec);
            assertThat(testedClass.nbProxies()).isEqualTo(i + 1); //Check that useExportDeclaration has been called
            decs.add(dec);
        }

        for (ExportDeclaration dec : decs) {
            testedClass.removeDeclaration(dec);
        }

        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that denyExportDeclaration has been called
    }


    @Test
    public void testStop() throws BinderException {
        Collection<ExportDeclaration> decs = new HashSet<ExportDeclaration>();

        for (int i = 0; i < EXPORT_MAX; i++) {
            ExportDeclaration dec = mock(ExportDeclaration.class);
            testedClass.addDeclaration(dec);
            assertThat(testedClass.nbProxies()).isEqualTo(i + 1); //Check that useExportDeclaration has been called

            decs.add(dec);
        }
        testedClass.stop();
        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that denyExportDeclaration has been called
    }

    @Test
    public void testToString() throws BinderException {
        String ts = testedClass.toString();
        assertThat(ts).isEqualTo("name");
    }

    public class TestedClass extends AbstractExporterComponent {

        private final Collection<ExportDeclaration> decs = new HashSet<ExportDeclaration>();

        @Override
        protected void useExportDeclaration(ExportDeclaration importDeclaration) {
            decs.add(importDeclaration);
        }

        @Override
        protected void denyExportDeclaration(ExportDeclaration importDeclaration) {
            decs.remove(importDeclaration);
        }

        public int nbProxies() {
            return decs.size();
        }

        @Override
        protected void stop() {
            super.stop();
        }

        @Override
        protected void start() {
            super.start();
        }

        public String getName() {
            return "name";
        }

    }

}
