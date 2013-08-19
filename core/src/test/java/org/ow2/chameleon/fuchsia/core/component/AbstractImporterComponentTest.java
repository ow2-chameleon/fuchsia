package org.ow2.chameleon.fuchsia.core.component;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.log.LogService;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AbstractImporterComponentTest {
    private static final int IMPORT_MAX = 10; //Number max of Import to be tested within a single test.

    //Mock object
    @Mock
    LogService logservice;

    @Mock
    ImportationLinker importationLinker;

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
    public void testImportDeclaration() {
        ImportDeclaration idec = mock(ImportDeclaration.class);
        testedClass.createProxy(idec);

        assertThat(testedClass.nbProxies()).isEqualTo(1); //Check that createProxy has been called

        testedClass.destroyProxy(idec);

        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that destroyProxy has been called
    }

    @Test
    public void testMultiplesImportDeclaration() {
        Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

        for (int i = 0; i < IMPORT_MAX; i++) {
            ImportDeclaration idec = mock(ImportDeclaration.class);
            testedClass.createProxy(idec);
            assertThat(testedClass.nbProxies()).isEqualTo(i + 1); //Check that createProxy has been called

            decs.add(idec);
        }

        for (ImportDeclaration idec : decs) {
            testedClass.destroyProxy(idec);
        }

        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that destroyProxy has been called
    }

    public class TestedClass extends AbstractImporterComponent {

        private final Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

        @Override
        protected void createProxy(ImportDeclaration importDeclaration) {
            decs.add(importDeclaration);
        }

        @Override
        protected void destroyProxy(ImportDeclaration importDeclaration) {
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

        public List<String> getConfigPrefix() {
            return Collections.emptyList();
        }

        public String getName() {
            return "name";
        }
    }

}
