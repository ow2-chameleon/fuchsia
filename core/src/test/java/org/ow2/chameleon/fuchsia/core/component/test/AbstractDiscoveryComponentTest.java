package org.ow2.chameleon.fuchsia.core.component.test;

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
    public void testInstantiation(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();
    }

    @Test
    public void testRegisterImportDeclaration(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        testedClass.addIdec(id);
        verify(bundleContext,times(1)).registerService(any(String[].class), eq(id), any(Dictionary.class));

        assertThat(testedClass.getImportDeclarations()).containsExactly(id);
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterTwoTimesImportDeclaration(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        testedClass.addIdec(id);
        testedClass.addIdec(id);
    }

    @Test
    public void testUnregisterImportDeclaration(){
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
    public void testUnregisterTwoTimesImportDeclaration(){
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
    public void testStop(){
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
    public void testToString(){
        TestedClass testedClass = new TestedClass(bundleContext);
        testedClass.start();

        String string = testedClass.toString();
        assertThat(string).isEqualTo("name");
    }



    public class TestedClass extends AbstractDiscoveryComponent{

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

        public void addIdec(ImportDeclaration importDeclaration){
            registerImportDeclaration(importDeclaration);
        }

        public void removeIdec(ImportDeclaration importDeclaration){
            unregisterImportDeclaration(importDeclaration);
        }
    }
}
