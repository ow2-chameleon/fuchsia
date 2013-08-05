package org.ow2.chameleon.fuchsia.core.component;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.log.LogService;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.ow2.chameleon.fuchsia.core.Linker;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class AbstractImporterComponentTest {
    private static final int IMPORT_MAX = 10; //Number max of Import to be tested within a single test.

    //Mock object
    @Mock
    LogService logservice;

    @Mock
    Linker linker;

    //Tested Object
    TestedClass creator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); //initialize the object with mocks annotations
        creator = new TestedClass();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testImportDeclaration() {
        EndpointDescription desc = mock(EndpointDescription.class);

        ImportDeclaration idec = mock(ImportDeclaration.class);
        creator.createProxy(idec);

        assertEquals(1, creator.nbProxies()); //Check that createProxy has been called

        creator.destroyProxy(idec);

        assertEquals(0, creator.nbProxies()); //Check that destroyProxy has been called

    }

    @Test
    public void testMultiplesImportDeclaration() {
        EndpointDescription desc = mock(EndpointDescription.class);
        Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

        for (int i = 0; i < IMPORT_MAX; i++) {
            ImportDeclaration idec = mock(ImportDeclaration.class);
            creator.createProxy(idec);
            assertEquals(i + 1, creator.nbProxies()); //Check that createProxy has been called

            decs.add(idec);
        }

        for (ImportDeclaration idec : decs) {
            creator.destroyProxy(idec);
        }

        assertEquals(0, creator.nbProxies()); //Check that destroyProxy has been called
    }

    public class TestedClass extends AbstractImporterComponent {

        private Collection<ImportDeclaration> decs = new HashSet<ImportDeclaration>();

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
