package org.ow2.chameleon.fuchsia.core.it;

import org.apache.felix.ipojo.ComponentInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;
import org.ow2.chameleon.fuchsia.core.it.components.SimpleImporter;
import org.ow2.chameleon.fuchsia.testing.CommonTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


public class AbstractImporterComponentTest extends CommonTest {

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

        simpleImporter = (SimpleImporter) osgiHelper.getServiceObject(ImporterService.class);
        assertThat(simpleImporter).isNotNull();

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
    public void testImportDeclarationAddAndRemove() throws ImporterException {
        ImportDeclaration iDec = ImportDeclarationBuilder.empty().key("id").value("1").build();
        spySimpleImporter.addImportDeclaration(iDec);
        assertThat(simpleImporter.getImportDeclarations()).containsOnly(iDec);
        verify(spySimpleImporter).useImportDeclaration(iDec);

        spySimpleImporter.removeImportDeclaration(iDec);
        assertThat(simpleImporter.getImportDeclarations()).isEmpty();
        verify(spySimpleImporter).denyImportDeclaration(iDec);
    }

    @Test
    public void testImportDeclarationAddAndStopServiceImporter() throws ImporterException {
        ImportDeclaration iDec = ImportDeclarationBuilder.empty().key("id").value("1").build();
        spySimpleImporter.addImportDeclaration(iDec);
        assertThat(simpleImporter.getImportDeclarations()).containsOnly(iDec);
        verify(spySimpleImporter).useImportDeclaration(iDec);

        spySimpleImporter.stop();
        verify(spySimpleImporter).denyImportDeclaration(iDec);
        assertThat(simpleImporter.getImportDeclarations()).isEmpty();
    }


}
