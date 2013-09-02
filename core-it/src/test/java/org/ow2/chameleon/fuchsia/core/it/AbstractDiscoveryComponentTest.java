package org.ow2.chameleon.fuchsia.core.it;

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
import org.ow2.chameleon.fuchsia.testing.CommonTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class AbstractDiscoveryComponentTest extends CommonTest {

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

        simpleDiscovery.unbind(iDec);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).doesNotContain(iDec);
    }

    @Test
    public void testMultiplesImportDeclarationPublication() {
        List<ImportDeclaration> iDecs;
        ImportDeclaration iDec1, iDec2, iDec3, iDec4;
        iDec1 = ImportDeclarationBuilder.empty().key("id").value("1").build();
        iDec2 = ImportDeclarationBuilder.empty().key("id").value("2").build();
        iDec3 = ImportDeclarationBuilder.empty().key("id").value("3").build();
        iDec4 = ImportDeclarationBuilder.empty().key("id").value("4").build();

        simpleDiscovery.bind(iDec1);
        simpleDiscovery.bind(iDec2);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).contains(iDec1, iDec2);
        assertThat(iDecs).doesNotContain(iDec3, iDec4);

        simpleDiscovery.bind(iDec3);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).contains(iDec1, iDec2, iDec3);
        assertThat(iDecs).doesNotContain(iDec4);

        simpleDiscovery.unbind(iDec1);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).contains(iDec2, iDec3);
        assertThat(iDecs).doesNotContain(iDec1, iDec4);

        simpleDiscovery.bind(iDec4);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).contains(iDec2, iDec3, iDec4);
        assertThat(iDecs).doesNotContain(iDec1);

        simpleDiscovery.unbind(iDec2);
        simpleDiscovery.unbind(iDec4);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).contains(iDec3);
        assertThat(iDecs).doesNotContain(iDec1, iDec2, iDec4);


        simpleDiscovery.unbind(iDec3);
        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).doesNotContain(iDec1, iDec2, iDec3, iDec4);
    }

    @Test
    public void testDiscoveryDispose() {
        ImportDeclaration iDec = ImportDeclarationBuilder.empty().key("id").value("1").build();
        simpleDiscovery.bind(iDec);


        List<ImportDeclaration> iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).contains(iDec);

        testedCI.dispose();

        iDecs = osgiHelper.getServiceObjects(ImportDeclaration.class);
        assertThat(iDecs).doesNotContain(iDec);
    }


}
