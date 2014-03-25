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
import org.ow2.chameleon.fuchsia.core.component.AbstractDiscoveryComponent;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.it.components.SimpleDiscovery;
import org.ow2.chameleon.fuchsia.core.it.services.SimulateBindingInterface;
import org.ow2.chameleon.fuchsia.testing.Common;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class AbstractDiscoveryComponentTest extends Common {

    private ComponentInstance testedCI;

    private SimpleDiscovery simpleDiscovery;

    @Override
    protected List<String> getExtraExports() {
        return Arrays.asList(
                "org.ow2.chameleon.fuchsia.core.it.components"
        );
    }

    @Before
    public void setUp() {
        testedCI = ipojoHelper.createComponentInstance("SimpleDiscoveryFactory", "SimpleDiscoveryInstance");
        assertThat(testedCI).isNotNull();

        simpleDiscovery = (SimpleDiscovery) osgiHelper.getServiceObject(DiscoveryService.class);
        assertThat(simpleDiscovery).isNotNull();
    }

    @After
    public void tearDown() {
        simpleDiscovery = null;
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
        assertThat(simpleDiscovery).isNotNull();
        assertThat(simpleDiscovery).isInstanceOf(SimulateBindingInterface.class);
        assertThat(simpleDiscovery).isInstanceOf(DiscoveryService.class).isInstanceOf(AbstractDiscoveryComponent.class);
    }


    @Test
    public void testImportDeclarationPublication() {
        ImportDeclaration iDec = ImportDeclarationBuilder.empty().key("id").value("1").build();
        simpleDiscovery.bind(iDec);


        List<ImportDeclaration> iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).contains(iDec);

        assertThat(simpleDiscovery.getImportDeclarations()).containsOnly(iDec);

        simpleDiscovery.unbind(iDec);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).doesNotContain(iDec);

        assertThat(simpleDiscovery.getImportDeclarations()).isEmpty();
    }

    @Test
    public void testMultiplesImportDeclarationPublication() {
        List<ImportDeclaration> iDecs;
        Set<ImportDeclaration> iDecs2;

        ImportDeclaration iDec1, iDec2, iDec3, iDec4;
        iDec1 = ImportDeclarationBuilder.empty().key("id").value("1").build();
        iDec2 = ImportDeclarationBuilder.empty().key("id").value("2").build();
        iDec3 = ImportDeclarationBuilder.empty().key("id").value("3").build();
        iDec4 = ImportDeclarationBuilder.empty().key("id").value("4").build();

        simpleDiscovery.bind(iDec1);
        simpleDiscovery.bind(iDec2);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        iDecs2 = simpleDiscovery.getImportDeclarations();
        assertThat(iDecs).containsOnly(iDec1, iDec2);
        assertThat(iDecs2).containsOnly(iDec1, iDec2);

        simpleDiscovery.bind(iDec3);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        iDecs2 = simpleDiscovery.getImportDeclarations();
        assertThat(iDecs).containsOnly(iDec1, iDec2, iDec3);
        assertThat(iDecs2).containsOnly(iDec1, iDec2, iDec3);

        simpleDiscovery.unbind(iDec1);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        iDecs2 = simpleDiscovery.getImportDeclarations();
        assertThat(iDecs).containsOnly(iDec2, iDec3);
        assertThat(iDecs2).containsOnly(iDec2, iDec3);

        simpleDiscovery.bind(iDec4);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        iDecs2 = simpleDiscovery.getImportDeclarations();
        assertThat(iDecs).containsOnly(iDec2, iDec3, iDec4);
        assertThat(iDecs2).containsOnly(iDec2, iDec3, iDec4);

        simpleDiscovery.unbind(iDec2);
        simpleDiscovery.unbind(iDec4);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        iDecs2 = simpleDiscovery.getImportDeclarations();
        assertThat(iDecs).containsOnly(iDec3);
        assertThat(iDecs2).containsOnly(iDec3);

        simpleDiscovery.unbind(iDec3);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        iDecs2 = simpleDiscovery.getImportDeclarations();
        assertThat(iDecs).isEmpty();
        assertThat(iDecs2).isEmpty();
    }

    @Test
    public void testDiscoveryDispose() {
        ImportDeclaration iDec = ImportDeclarationBuilder.empty().key("id").value("1").build();
        simpleDiscovery.bind(iDec);


        List<ImportDeclaration> iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).contains(iDec);
        assertThat(simpleDiscovery.getImportDeclarations()).contains(iDec);

        testedCI.dispose();

        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).doesNotContain(iDec);
    }


}
