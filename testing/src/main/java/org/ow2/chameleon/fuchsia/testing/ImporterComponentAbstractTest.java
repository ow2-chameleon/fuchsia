package org.ow2.chameleon.fuchsia.testing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public abstract class ImporterComponentAbstractTest extends CommonTest {

    // Number of mock object by test.
    protected static final int MAX_MOCK = 10;

    // Mock LogService
    @Mock
    private LogService logService;

    // Mock Device
    @Mock
    private LogEntry logEntry;

    private static final Logger LOG = LoggerFactory.getLogger(ImporterComponentAbstractTest.class);

    /**
     * Done some initializations.
     */
    @Before
    public void setUp() {
        //initialise the annotated mock objects
        initMocks(this);
    }

    /**
     * Closing the test.
     */
    @After
    public void tearDown() {
        //
    }

    @Override
    public boolean deployTestBundle() {
        return false;
    }

    @Override
    public boolean quiet() {
        return false;
    }

    protected ServiceReference<ImporterService> waitForImporterService() {
        String filter = "(" + INSTANCE_NAME_PROPERTY + "=" + getImporterServiceInstanceName() + ")";
        return osgiHelper.waitForService(ImporterService.class.getName(), filter, 0);
    }

    /**
     * Basic Test, in order to know if the {@link ImporterService} service is correctly provided.
     */
    @Test
    public void testAvailability() {
        //wait for the service to be available.
        ServiceReference serviceReference = waitForImporterService();
        ImporterService importer = (ImporterService) osgiHelper.getServiceObject(serviceReference);

        //Check that the importer != null
        assertThat(importer).isNotNull();
    }

    /**
     * Test the {@link ImporterService#addDeclaration(ImportDeclaration)} with
     * a valid {@link ImportDeclaration}.
     */
    @Test
    public void testImportService() {
        //wait for the service to be available.
        ServiceReference serviceReference = waitForImporterService();
        ImporterService importer = (ImporterService) osgiHelper.getServiceObject(serviceReference);

        //create an importDeclaration for logService
        ImportDeclaration iDec = createImportDeclaration("testImportService", LogService.class, logService);

        //import the logService
        iDec.bind(serviceReference);
        try {
            importer.addDeclaration(iDec);
        } catch (BinderException e) {
            fail("Cannot give the importDeclaration to the importer", e);
        }
        LogService proxy = osgiHelper.getServiceObject(LogService.class);

        //check that the client is not null
        assertThat(proxy).isNotNull();

        //check proxy calls
        for (int i = 1; i <= MAX_MOCK; i++) {
            proxy.log(LogService.LOG_WARNING, "EchoImportService" + i);
            verify(logService).log(LogService.LOG_WARNING, "EchoImportService" + i);
        }
        verifyNoMoreInteractions(logService);
    }

    @Test
    public void testImportServiceNoVoid() {
        //wait for the service to be available.
        ServiceReference serviceReference = waitForImporterService();
        ImporterService importer = (ImporterService) osgiHelper.getServiceObject(serviceReference);

        //create an endpoint for logService
        ImportDeclaration iDec = createImportDeclaration("testImportServiceNoVoid", LogEntry.class, logEntry);

        //import the logService
        iDec.bind(serviceReference);
        try {
            importer.addDeclaration(iDec);
        } catch (BinderException e) {
            fail("Cannot give the importDeclaration to the importer", e);
        }

        //get the client
        LogEntry proxy = (LogEntry) osgiHelper.getServiceObject(LogEntry.class);

        //check that the client is not null
        assertThat(proxy).isNotNull();

        //check proxy calls
        for (int i = 1; i <= MAX_MOCK; i++) {
            Mockito.when(logEntry.getMessage()).thenReturn("EchoImportServiceNoVoid" + i);
            String msg = proxy.getMessage();
            assertThat(msg).isEqualTo("EchoImportServiceNoVoid" + i);
            verify(logEntry, times(i)).getMessage();
        }
        verifyNoMoreInteractions(logEntry);
    }

    @Test
    public void testRemoveImportService() {
        LogService proxy = null;
        //wait for the service to be available.
        ServiceReference serviceReference = waitForImporterService();
        ImporterService importer = (ImporterService) osgiHelper.getServiceObject(serviceReference);

        //create an endpoint for logService
        ImportDeclaration iDec = createImportDeclaration("testRemoveImportService", LogService.class, logService);

        //import the logService
        iDec.bind(serviceReference);
        try {
            importer.addDeclaration(iDec);
        } catch (BinderException e) {
            fail("Cannot give the importDeclaration to the importer", e);
        }
        //get the client
        proxy = (LogService) osgiHelper.getServiceObject(LogService.class);

        //check that the client is not null
        assertThat(proxy).isNotNull();

        // un-import the logService
        try {
            importer.removeDeclaration(iDec);
        } catch (BinderException e) {
            fail("Cannot take the importDeclaration of the importer", e);
        }
        iDec.unbind(serviceReference);
        //get the client
        proxy = (LogService) osgiHelper.getServiceObject(LogService.class);

        //check that the client is not null
        assertThat(proxy).isNull();

        verifyNoMoreInteractions(logService);
    }

    @Test
    public void testReImportService() {
        //wait for the service to be available.
        ServiceReference serviceReference = waitForImporterService();
        ImporterService importer = (ImporterService) osgiHelper.getServiceObject(serviceReference);

        //create an endpoint for logService
        ImportDeclaration iDec = createImportDeclaration("testReImportService", LogService.class, logService);

        //import the logService
        iDec.bind(serviceReference);
        try {
            importer.addDeclaration(iDec);
        } catch (BinderException e) {
            fail("Cannot give the importDeclaration to the importer", e);
            LOG.error("Failed to add import declaration", e);
        }

        // un-import the logService
        try {
            importer.removeDeclaration(iDec);
        } catch (BinderException e) {
            fail("Cannot take back the importDeclaration of the importer", e);
        }
        iDec.unbind(serviceReference);

        // re import the logService
        iDec.bind(serviceReference);
        try {
            importer.addDeclaration(iDec);
        } catch (BinderException e) {
            fail("Cannot give the importDeclaration to the importer a second time", e);
        }

        //get the client
        LogService proxy = (LogService) osgiHelper.getServiceObject(LogService.class);

        //check that the client is not null
        assertThat(proxy).isNotNull();

        //check proxy calls
        for (int i = 1; i <= MAX_MOCK; i++) {
            proxy.log(LogService.LOG_WARNING, "EchoReImportService" + i);
            verify(logService).log(LogService.LOG_WARNING, "EchoReImportService" + i);
        }
        verifyNoMoreInteractions(logService);
    }

    /**
     * @return The name of the instance of the {@link ImporterService} to be tested.
     */
    protected abstract String getImporterServiceInstanceName();

    /**
     */
    protected abstract <T> ImportDeclaration createImportDeclaration(String endpointId, Class<T> klass, T object);
}
