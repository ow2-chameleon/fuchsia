package org.ow2.chameleon.fuchsia.core.it;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core [IntegrationTests]
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

import org.apache.felix.ipojo.ComponentInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.it.components.SimpleImporter;
import org.ow2.chameleon.fuchsia.testing.Common;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;


public class AbstractImporterComponentTest extends Common {

    private ComponentInstance testedCI;

    private SimpleImporter simpleImporter;
    private SimpleImporter spySimpleImporter;

    @Override
    protected List<String> getExtraExports() {
        return Arrays.asList(
                "org.ow2.chameleon.fuchsia.core.it.components"
        );
    }

    @Before
    public void setUp() {
        testedCI = ipojoHelper.createComponentInstance("SimpleImporterFactory", "SimpleImporterInstance");
        assertThat(testedCI).isNotNull();
        simpleImporter = (SimpleImporter) ipojoHelper.getServiceObjectByName(ImporterService.class, "SimpleImporterInstance");
        assertThat(simpleImporter).isNotNull();
        assertThat(simpleImporter.getName()).isEqualTo("simpleImporter");
        assertThat(simpleImporter).isInstanceOf(SimpleImporter.class);

        spySimpleImporter = spy(simpleImporter);
    }

    @After
    public void tearDown() {
        simpleImporter = null;
        testedCI.dispose();
    }

    @Override
    public boolean deployTestBundle() {
        return true;
    }

    @Override
    public boolean quiet() {
        return true;
    }

    @Test
    public void testInstanceIsHere() {
        assertThat(testedCI).isNotNull();
        assertThat(ipojoHelper.isInstanceValid(testedCI)).isTrue();
        assertThat(simpleImporter).isNotNull();
        assertThat(simpleImporter).isInstanceOf(ImporterService.class).isInstanceOf(AbstractImporterComponent.class);
    }

    @Test
    public void testImportDeclarationAddAndRemove() throws BinderException {
        ImportDeclaration iDec = ImportDeclarationBuilder.empty().key("id").value("1").build();
        spySimpleImporter.addDeclaration(iDec);
        assertThat(simpleImporter.getImportDeclarations()).containsOnly(iDec);
        assertThat(simpleImporter.nbProxies()).isEqualTo(1);

        spySimpleImporter.removeDeclaration(iDec);
        assertThat(simpleImporter.getImportDeclarations()).isEmpty();
        assertThat(simpleImporter.nbProxies()).isEqualTo(0);
    }


    @Test
    public void testImportDeclarationAddAndStopServiceImporter() throws BinderException {
        ImportDeclaration iDec = ImportDeclarationBuilder.empty().key("id").value("1").build();
        spySimpleImporter.addDeclaration(iDec);
        assertThat(simpleImporter.getImportDeclarations()).containsOnly(iDec);
        assertThat(simpleImporter.nbProxies()).isEqualTo(1);

        spySimpleImporter.stop();
        assertThat(simpleImporter.nbProxies()).isEqualTo(0);
        assertThat(simpleImporter.getImportDeclarations()).isEmpty();
    }


    class anyDeclaration extends ArgumentMatcher<ImportDeclaration> {
        public boolean matches(Object obj) {
            return true; //(obj instanceof ImportDeclaration);
        }
    }

}
