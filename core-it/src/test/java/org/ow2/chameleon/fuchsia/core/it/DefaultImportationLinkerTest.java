package org.ow2.chameleon.fuchsia.core.it;


import org.apache.felix.ipojo.ComponentInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;
import org.ow2.chameleon.fuchsia.core.ImportationLinkerIntrospection;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.ImporterException;
import org.ow2.chameleon.fuchsia.testing.CommonTest;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.ow2.chameleon.fuchsia.core.FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME;

@ExamReactorStrategy(PerMethod.class)
public class DefaultImportationLinkerTest extends CommonTest {

    private final String linkerInstanceName = "linker";

    //Tested Object
    private ImportationLinker importationLinker;
    private ImportationLinkerIntrospection importationLinkerIntrospection;

    //Tested Object ComponentInstance
    private ComponentInstance importationLinkerCI;

    @Mock
    private AbstractImporterComponent importer1;

    @Mock
    private AbstractImporterComponent importer2;

    @Before
    public void setUp() {
        initMocks(this);

        Properties props = new Properties();
        props.put(INSTANCE_NAME_PROPERTY, linkerInstanceName);
        props.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY,
                "(" + Constants.PROTOCOL_NAME + "=test)");
        props.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY,
                "(" + INSTANCE_NAME_PROPERTY + "=*)");
        props.put(ImportationLinker.UNIQUE_IMPORTATION_PROPERTY, false);
        importationLinkerCI = ipojoHelper.createComponentInstance(DEFAULT_IMPORTATION_LINKER_FACTORY_NAME, props);
        assertThat(importationLinkerCI).isNotNull();

        importationLinker = (ImportationLinker) osgiHelper.getServiceObject(ImportationLinker.class);
        assertThat(importationLinker).isNotNull();
        assertThat(importationLinker).isInstanceOf(ImportationLinkerIntrospection.class);
        importationLinkerIntrospection = (ImportationLinkerIntrospection) importationLinker;
    }

    @After
    public void tearDown() {
        importationLinker = null;
        importationLinkerCI.dispose();
        importationLinkerCI = null;
    }

    @Override
    public boolean deployTestBundle() {
        return false;
    }

    protected ServiceRegistration<ImportDeclaration> registerImportDeclaration(ImportDeclaration importDeclaration) {
        Dictionary<String, Object> props = new Hashtable<String, Object>(importDeclaration.getMetadata());
        return context.registerService(ImportDeclaration.class, importDeclaration, props);
    }

    /**
     * Test that ImportDeclaration are binded  when filter match
     *
     * @throws InvalidSyntaxException
     */
    @Test
    public void testBindImportDeclaration() throws InvalidSyntaxException {

        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.PROTOCOL_NAME, "test");
        metadata.put("n", 0);
        metadata.put(INSTANCE_NAME_PROPERTY, "importdec-" + 0);
        ImportDeclaration iD = ImportDeclarationBuilder.fromMetadata(metadata).build();

        ServiceRegistration reg = registerImportDeclaration(iD);
        assertThat(reg).isNotNull();
        ImportDeclaration serviceID = osgiHelper.waitForService(ImportDeclaration.class, "(" + INSTANCE_NAME_PROPERTY + "=importdec-0)", 0);
        assertThat(serviceID).isNotNull();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).containsOnly(iD);

        reg.unregister();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();
    }

    /**
     * Test that ImportDeclaration are not binded when filter doesn't match
     *
     * @throws InvalidSyntaxException
     */
    @Test
    public void testBindImportDeclarationFilterDoesNotMatch() throws InvalidSyntaxException {

        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.PROTOCOL_NAME, "not test");
        metadata.put("n", 0);
        metadata.put(INSTANCE_NAME_PROPERTY, "importdec-" + 0);
        ImportDeclaration iD = ImportDeclarationBuilder.fromMetadata(metadata).build();

        ServiceRegistration reg = registerImportDeclaration(iD);
        assertThat(reg).isNotNull();
        ImportDeclaration serviceID = osgiHelper.waitForService(ImportDeclaration.class, "(" + INSTANCE_NAME_PROPERTY + "=importdec-0)", 0);
        assertThat(serviceID).isNotNull();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();

        reg.unregister();
    }

    /**
     * Test that ImporterServices are binded when filter match
     */
    @Test
    public void testBindImporterServices() {

        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer1-instance");
        props.put(ImporterService.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

        ServiceRegistration<ImporterService> sr1 = context.registerService(ImporterService.class, importer1, props);

        ImporterService is = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer1-instance)", 0);
        assertThat(is).isNotNull();

        assertThat(importationLinkerIntrospection.getLinkedImporters()).containsOnly(importer1);

        Dictionary<String, Object> props2 = new Hashtable<String, Object>();
        props2.put(INSTANCE_NAME_PROPERTY, "importer2-instance");
        props2.put(ImporterService.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");
        ServiceRegistration<ImporterService> sr2 = context.registerService(ImporterService.class, importer2, props2);

        ImporterService is2 = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer2-instance)", 0);
        assertThat(is2).isNotNull();

        assertThat(importationLinkerIntrospection.getLinkedImporters()).containsOnly(importer1, importer2);

        sr1.unregister();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).containsOnly(importer2);

        sr2.unregister();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();
    }

    /**
     * Test that ImporterService are not binded when filter doesn't match
     */
    @Test
    public void testBindImporterServiceFilterDoesNotMatch() {

        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("no.instance.name", "importer1-instance");
        props.put(ImporterService.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

        ServiceRegistration<ImporterService> sr1 = context.registerService(ImporterService.class, importer1, props);

        ImporterService is = osgiHelper.waitForService(ImporterService.class, "(no.instance.name=importer1-instance)", 0);
        assertThat(is).isNotNull();

        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();

        sr1.unregister();
    }

    /**
     * Test that ImportDeclaration are given to matching ImporterService when filter of ImporterService match
     */
    @Test
    public void testImportDeclarationToImporterService() throws ImporterException {
        osgiHelper.waitForService(ImportationLinker.class, "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 0);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer-instance");
        props.put(ImporterService.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

        ServiceRegistration<ImporterService> sr = context.registerService(ImporterService.class, importer1, props);

        ImporterService is = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer-instance)", 0);
        assertThat(is).isNotNull();

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.PROTOCOL_NAME, "test");
        metadata.put(Constants.ID, "test");
        metadata.put("n", 0);
        ImportDeclaration iD = ImportDeclarationBuilder.fromMetadata(metadata).build();

        ServiceRegistration iDReg = registerImportDeclaration(iD);
        assertThat(iDReg).isNotNull();
        verify(importer1).addImportDeclaration(iD);

        iDReg.unregister();
        verify(importer1).removeImportDeclaration(iD);
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();

        sr.unregister();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();
    }

    /**
     * Test that ImportDeclaration are not given to matching ImporterService when filter of ImporterService doesn't match
     */
    @Test
    public void testImportDeclarationToImporterServiceDoesNotMatch() throws ImporterException {
        osgiHelper.waitForService(ImportationLinker.class, "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 0);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer-instance");
        props.put(ImporterService.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

        ServiceRegistration<ImporterService> sr = context.registerService(ImporterService.class, importer1, props);

        ImporterService is = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer-instance)", 0);
        assertThat(is).isNotNull();

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.PROTOCOL_NAME, "test");
        metadata.put(Constants.ID, "not test");
        metadata.put("n", 0);
        ImportDeclaration iD = ImportDeclarationBuilder.fromMetadata(metadata).build();

        ServiceRegistration iDReg = registerImportDeclaration(iD);
        assertThat(iDReg).isNotNull();
        verify(importer1, never()).addImportDeclaration(iD);

        iDReg.unregister();
        verify(importer1, never()).removeImportDeclaration(iD);
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();

        sr.unregister();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();
    }

    /**
     * Test that bind() method is called when DefaultImportationLinker bind it with an ImporterService and
     * that unbind() is called when DefaultImportationLinker unbind it from an ImporterService.
     *
     * @throws ImporterException
     */
    @Test
    public void testBindOnImportDeclaration() throws ImporterException {
        osgiHelper.waitForService(ImportationLinker.class, "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 0);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer-instance");
        props.put(ImporterService.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

        ServiceRegistration<ImporterService> sr = context.registerService(ImporterService.class, importer1, props);

        ImporterService is = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer-instance)", 0);
        assertThat(is).isNotNull();

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.PROTOCOL_NAME, "test");
        metadata.put(Constants.ID, "test");
        metadata.put("n", 0);
        ImportDeclaration iD = ImportDeclarationBuilder.fromMetadata(metadata).build();

        ServiceRegistration iDReg = registerImportDeclaration(iD);
        assertThat(iDReg).isNotNull();
        assertThat(iD.getStatus().getServiceReferences()).containsOnly(sr.getReference());

        iDReg.unregister();
        assertThat(iD.getStatus().getServiceReferences()).doesNotContain(sr.getReference());
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();

        sr.unregister();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();
    }

    /**
     * Test that unbind() is called when DefaultImportationLinker unbind it from an ImporterService when the
     * ImporterService leave.
     */
    @Test
    public void testRemoveImporterServiceBeforeImportDeclaration() throws ImporterException {
        osgiHelper.waitForService(ImportationLinker.class, "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 0);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer-instance");
        props.put(ImporterService.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

        ServiceRegistration<ImporterService> sr = context.registerService(ImporterService.class, importer1, props);
        ImporterService is = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer-instance)", 0);
        assertThat(is).isNotNull();

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.PROTOCOL_NAME, "test");
        metadata.put(Constants.ID, "test");
        metadata.put("n", 0);
        ImportDeclaration iD = ImportDeclarationBuilder.fromMetadata(metadata).build();

        ServiceRegistration iDReg = registerImportDeclaration(iD);
        assertThat(iDReg).isNotNull();
        verify(importer1).addImportDeclaration(iD);

        assertThat(iD.getStatus().getServiceReferences()).containsOnly(sr.getReference());

        sr.unregister();
        assertThat(iD.getStatus().getServiceReferences()).isEmpty();

        iDReg.unregister();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();
    }

    /**
     * Test unique importation
     */
    @Test
    public void testUniqueImportation() throws ImporterException {
        osgiHelper.waitForService(ImportationLinker.class, "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 0);

        Dictionary<String, Object> linkerProps = new Hashtable<String, Object>();
        linkerProps.put(ImportationLinker.UNIQUE_IMPORTATION_PROPERTY, true);
        importationLinkerCI.reconfigure(linkerProps);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer1-instance");
        props.put(ImporterService.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

        ServiceRegistration<ImporterService> sr1 = context.registerService(ImporterService.class, importer1, props);

        ImporterService is = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer1-instance)", 0);
        assertThat(is).isNotNull();

        assertThat(importationLinkerIntrospection.getLinkedImporters()).containsOnly(importer1);

        Dictionary<String, Object> props2 = new Hashtable<String, Object>();
        props2.put(INSTANCE_NAME_PROPERTY, "importer2-instance");
        props2.put(ImporterService.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");
        ServiceRegistration<ImporterService> sr2 = context.registerService(ImporterService.class, importer2, props2);

        ImporterService is2 = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer2-instance)", 0);
        assertThat(is2).isNotNull();

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.PROTOCOL_NAME, "test");
        metadata.put(Constants.ID, "test");
        metadata.put("n", 0);
        ImportDeclaration iD = ImportDeclarationBuilder.fromMetadata(metadata).build();

        ServiceRegistration iDReg = registerImportDeclaration(iD);
        assertThat(iDReg).isNotNull();

        List<ServiceReference> listSR = iD.getStatus().getServiceReferences();
        assertThat(listSR).hasSize(1);

        // FIXME : ugly
        if (listSR.get(0).equals(sr1.getReference())) {
            assertThat(listSR).containsOnly(sr1.getReference());
        } else if (listSR.get(0).equals(sr2.getReference())) {
            assertThat(listSR).containsOnly(sr2.getReference());
        } else {
            fail("No importer has been binded to the ImportDeclaration, should have one.");
        }

        iDReg.unregister();
        assertThat(iD.getStatus().getServiceReferences()).isEmpty();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();

        sr1.unregister();
        sr2.unregister();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();
    }
}
