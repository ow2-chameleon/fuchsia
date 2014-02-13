package org.ow2.chameleon.fuchsia.core.component.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.log.LogService;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
    public void testImportDeclaration() throws ImporterException {
        ImportDeclaration idec = mock(ImportDeclaration.class);
        testedClass.addImportDeclaration(idec);

        assertThat(testedClass.nbProxies()).isEqualTo(1); //Check that useImportDeclaration has been called

        testedClass.removeImportDeclaration(idec);

        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that denyImportDeclaration has been called
    }

    @Test(expected = IllegalStateException.class)
    public void testDuplicateImportDeclaration() throws ImporterException {
        ImportDeclaration idec = mock(ImportDeclaration.class);
        testedClass.addImportDeclaration(idec);

        assertThat(testedClass.nbProxies()).isEqualTo(1); //Check that useImportDeclaration has been called

        testedClass.addImportDeclaration(idec);

        assertThat(testedClass.nbProxies()).isEqualTo(1); //Check that the importer handle correctly the duplication
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveImportDeclarationNotAdded() throws ImporterException {
        ImportDeclaration idec = mock(ImportDeclaration.class);
        testedClass.removeImportDeclaration(idec);
    }

    @Test
    public void testGetImportDeclaration() throws ImporterException {
        ImportDeclaration idec = mock(ImportDeclaration.class);
        testedClass.addImportDeclaration(idec);

        Set<ImportDeclaration> importDeclarations = testedClass.getImportDeclarations();
        assertThat(importDeclarations).containsExactly(idec);
    }


    @Test
    public void testMultiplesImportDeclaration() throws ImporterException {
        Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

        for (int i = 0; i < IMPORT_MAX; i++) {
            ImportDeclaration idec = mock(ImportDeclaration.class);
            testedClass.addImportDeclaration(idec);
            assertThat(testedClass.nbProxies()).isEqualTo(i + 1); //Check that useImportDeclaration has been called

            decs.add(idec);
        }

        for (ImportDeclaration idec : decs) {
            testedClass.removeImportDeclaration(idec);
        }

        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that denyImportDeclaration has been called
    }


    @Test
    public void testStop() throws ImporterException {
        Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

        for (int i = 0; i < IMPORT_MAX; i++) {
            ImportDeclaration idec = mock(ImportDeclaration.class);
            testedClass.addImportDeclaration(idec);
            assertThat(testedClass.nbProxies()).isEqualTo(i + 1); //Check that useImportDeclaration has been called

            decs.add(idec);
        }

        testedClass.stop();

        assertThat(testedClass.nbProxies()).isEqualTo(0); //Check that denyImportDeclaration has been called
    }

    @Test
    public void testToString() throws ImporterException {
        String ts = testedClass.toString();
        assertThat(ts).isEqualTo("name");

    }
    public class TestedClass extends AbstractImporterComponent {

        private final Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

        @Override
        protected void useImportDeclaration(ImportDeclaration importDeclaration) {
            decs.add(importDeclaration);
        }

        @Override
        protected void denyImportDeclaration(ImportDeclaration importDeclaration) {
            decs.remove(importDeclaration);
        }

        @Override
        protected Logger getLogger() {
            return LoggerFactory.getLogger(this.getClass());
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
