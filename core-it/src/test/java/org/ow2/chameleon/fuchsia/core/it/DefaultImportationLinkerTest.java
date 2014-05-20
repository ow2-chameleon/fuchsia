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
import org.mockito.Mock;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinkerIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.testing.Common;

import java.util.*;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.ow2.chameleon.fuchsia.core.FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME;

@ExamReactorStrategy(PerMethod.class)
public class DefaultImportationLinkerTest extends Common {

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
     * Test that ImportDeclaration are binded  when filter match.
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
     * Test that ImportDeclaration are not binded when filter doesn't match.
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
     * Test that ImporterServices are binded when filter match.
     */
    @Test
    public void testBindImporterServices() {

        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer1-instance");
        props.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

        ServiceRegistration<ImporterService> sr1 = context.registerService(ImporterService.class, importer1, props);

        ImporterService is = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer1-instance)", 0);
        assertThat(is).isNotNull();

        assertThat(importationLinkerIntrospection.getLinkedImporters()).containsOnly(importer1);

        Dictionary<String, Object> props2 = new Hashtable<String, Object>();
        props2.put(INSTANCE_NAME_PROPERTY, "importer2-instance");
        props2.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");
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
     * Test that ImporterService are not binded when filter doesn't match.
     */
    @Test
    public void testBindImporterServiceFilterDoesNotMatch() {

        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("no.instance.name", "importer1-instance");
        props.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

        ServiceRegistration<ImporterService> sr1 = context.registerService(ImporterService.class, importer1, props);

        ImporterService is = osgiHelper.waitForService(ImporterService.class, "(no.instance.name=importer1-instance)", 0);
        assertThat(is).isNotNull();

        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();

        sr1.unregister();
    }

    /**
     * Test that ImportDeclaration are given to matching ImporterService when filter of ImporterService match.
     */
    @Test
    public void testImportDeclarationToImporterService() throws BinderException {
        osgiHelper.waitForService(ImportationLinker.class, "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 0);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer-instance");
        props.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

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
        verify(importer1).addDeclaration(iD);

        iDReg.unregister();
        verify(importer1).removeDeclaration(iD);
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();

        sr.unregister();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();
    }

    /**
     * Test that ImportDeclaration are not given to matching ImporterService when filter of ImporterService doesn't match.
     */
    @Test
    public void testImportDeclarationToImporterServiceDoesNotMatch() throws BinderException {
        osgiHelper.waitForService(ImportationLinker.class, "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 0);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer-instance");
        props.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

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
        verify(importer1, never()).addDeclaration(iD);

        iDReg.unregister();
        verify(importer1, never()).removeDeclaration(iD);
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();

        sr.unregister();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();
    }

    /**
     * Test that bind() method is called when DefaultImportationLinker bind it with an ImporterService and.
     * that unbind() is called when DefaultImportationLinker unbind it from an ImporterService.
     *
     * @throws org.ow2.chameleon.fuchsia.core.exceptions.BinderException
     */
    @Test
    public void testBindOnImportDeclaration() throws BinderException {
        osgiHelper.waitForService(ImportationLinker.class, "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 0);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer-instance");
        props.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

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
        assertThat(iD.getStatus().getServiceReferencesBounded()).containsOnly(sr.getReference());

        iDReg.unregister();
        assertThat(iD.getStatus().getServiceReferencesBounded()).doesNotContain(sr.getReference());
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();

        sr.unregister();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();
    }

    /**
     * Test that unbind() is called when DefaultImportationLinker unbind it from an ImporterService when the.
     * ImporterService leave.
     */
    @Test
    public void testRemoveImporterServiceBeforeImportDeclaration() throws BinderException {
        osgiHelper.waitForService(ImportationLinker.class, "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 0);

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer-instance");
        props.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");

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
        verify(importer1).addDeclaration(iD);

        assertThat(iD.getStatus().getServiceReferencesBounded()).containsOnly(sr.getReference());

        sr.unregister();
        assertThat(iD.getStatus().getServiceReferencesBounded()).isEmpty();

        iDReg.unregister();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).isEmpty();
        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();
    }

    @Test
    public void testReconfigureIDecFilter() {
        // begin with the filter  :  "(" + Constants.PROTOCOL_NAME + "=test)"

        // create ImportDeclaration1
        Map<String, Object> metadata1 = new HashMap<String, Object>();
        metadata1.put(Constants.PROTOCOL_NAME, "test");
        metadata1.put(Constants.ID, "test");
        ImportDeclaration iD1 = ImportDeclarationBuilder.fromMetadata(metadata1).build();
        ServiceRegistration iDReg1 = registerImportDeclaration(iD1);
        assertThat(iDReg1).isNotNull();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).containsOnly(iD1);

        // create ImportDeclaration2
        Map<String, Object> metadata2 = new HashMap<String, Object>();
        metadata2.put(Constants.PROTOCOL_NAME, "not-the-one-matching");
        metadata2.put(Constants.ID, "test");
        ImportDeclaration iD2 = ImportDeclarationBuilder.fromMetadata(metadata2).build();
        ServiceRegistration iDReg2 = registerImportDeclaration(iD2);
        assertThat(iDReg2).isNotNull();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).containsOnly(iD1);

        // reconfigure the ImportDeclaration filter of the linker :
        Dictionary<String, Object> linkerProps = new Hashtable<String, Object>();
        linkerProps.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "(" + Constants.PROTOCOL_NAME + "=not-the-one-matching)");
        linkerProps.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(" + INSTANCE_NAME_PROPERTY + "=*)");
        importationLinkerCI.reconfigure(linkerProps);

        assertThat(importationLinkerIntrospection.getImportDeclarations()).containsOnly(iD2);
    }

    @Test
    public void testReconfigureIDecFilterWithImporter() throws BinderException {
        // begin with the filter  :  "(" + Constants.PROTOCOL_NAME + "=test)"

        // create ImportDeclaration1
        Map<String, Object> metadata1 = new HashMap<String, Object>();
        metadata1.put(Constants.PROTOCOL_NAME, "test");
        metadata1.put(Constants.ID, "test");
        ImportDeclaration iD1 = ImportDeclarationBuilder.fromMetadata(metadata1).build();
        ServiceRegistration iDReg1 = registerImportDeclaration(iD1);
        assertThat(iDReg1).isNotNull();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).containsOnly(iD1);

        // create ImportDeclaration2
        Map<String, Object> metadata2 = new HashMap<String, Object>();
        metadata2.put(Constants.PROTOCOL_NAME, "not-the-one-matching");
        metadata2.put(Constants.ID, "test");
        ImportDeclaration iD2 = ImportDeclarationBuilder.fromMetadata(metadata2).build();
        ServiceRegistration iDReg2 = registerImportDeclaration(iD2);
        assertThat(iDReg2).isNotNull();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).containsOnly(iD1);

        // register the importer1
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer1-instance");
        props.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");
        ServiceRegistration<ImporterService> sr1 = context.registerService(ImporterService.class, importer1, props);

        ImporterService is1 = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer1-instance)", 0);
        assertThat(is1).isNotNull();
        verify(importer1).addDeclaration(iD1);

        // reconfigure the ImportDeclaration filter of the linker :
        Dictionary<String, Object> linkerProps = new Hashtable<String, Object>();
        linkerProps.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "(" + Constants.PROTOCOL_NAME + "=not-the-one-matching)");
        linkerProps.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(" + INSTANCE_NAME_PROPERTY + "=*)");
        importationLinkerCI.reconfigure(linkerProps);

        verify(importer1).removeDeclaration(iD1);
        verify(importer1).addDeclaration(iD2);
    }

    @Test
    public void testReconfigureImportersFilter() {
        // begin with the filter  :  "(" + INSTANCE_NAME_PROPERTY + "=*)"

        // register the importer1
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer1-instance");
        props.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");
        ServiceRegistration<ImporterService> sr1 = context.registerService(ImporterService.class, importer1, props);

        ImporterService is1 = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer1-instance)", 0);
        assertThat(is1).isNotNull();

        assertThat(importationLinkerIntrospection.getLinkedImporters()).containsOnly(importer1);

        // register the importer2
        Dictionary<String, Object> props2 = new Hashtable<String, Object>();
        props2.put(INSTANCE_NAME_PROPERTY, "importer2-instance");
        props2.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");
        ServiceRegistration<ImporterService> sr2 = context.registerService(ImporterService.class, importer2, props2);

        ImporterService is2 = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer2-instance)", 0);
        assertThat(is2).isNotNull();

        assertThat(importationLinkerIntrospection.getLinkedImporters()).containsOnly(is1, is2);

        // reconfigure the ImporterService filter of the linker :
        Dictionary<String, Object> linkerProps = new Hashtable<String, Object>();
        linkerProps.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "(" + Constants.PROTOCOL_NAME + "=test)");
        linkerProps.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(" + INSTANCE_NAME_PROPERTY + "=importer2-instance)");
        importationLinkerCI.reconfigure(linkerProps);

        assertThat(importationLinkerIntrospection.getLinkedImporters()).containsOnly(is2);

        // reconfigure the ImporterService filter of the linker :
        linkerProps = new Hashtable<String, Object>();
        linkerProps.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "(" + Constants.PROTOCOL_NAME + "=test)");
        linkerProps.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(" + INSTANCE_NAME_PROPERTY + "=this-instance)");
        importationLinkerCI.reconfigure(linkerProps);

        assertThat(importationLinkerIntrospection.getLinkedImporters()).isEmpty();

        // reconfigure the ImporterService filter of the linker :
        linkerProps = new Hashtable<String, Object>();
        linkerProps.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "(" + Constants.PROTOCOL_NAME + "=test)");
        linkerProps.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(" + INSTANCE_NAME_PROPERTY + "=*)");
        importationLinkerCI.reconfigure(linkerProps);

        assertThat(importationLinkerIntrospection.getLinkedImporters()).containsOnly(is1, is2);
    }


    @Test
    public void testReconfigureImportersFilterWithIDec() throws BinderException {
        // begin with the filter  :  "(" + Constants.PROTOCOL_NAME + "=test)"

        // create ImportDeclaration1
        Map<String, Object> metadata1 = new HashMap<String, Object>();
        metadata1.put(Constants.PROTOCOL_NAME, "test");
        metadata1.put(Constants.ID, "test");
        ImportDeclaration iD1 = ImportDeclarationBuilder.fromMetadata(metadata1).build();
        ServiceRegistration iDReg1 = registerImportDeclaration(iD1);
        assertThat(iDReg1).isNotNull();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).containsOnly(iD1);

        // register the importer1
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(INSTANCE_NAME_PROPERTY, "importer1-instance");
        props.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");
        ServiceRegistration<ImporterService> sr1 = context.registerService(ImporterService.class, importer1, props);

        ImporterService is1 = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer1-instance)", 0);
        assertThat(is1).isNotNull();
        verify(importer1).addDeclaration(iD1);

        // register the importer2
        Dictionary<String, Object> props2 = new Hashtable<String, Object>();
        props2.put(INSTANCE_NAME_PROPERTY, "importer2-instance");
        props2.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test)");
        ServiceRegistration<ImporterService> sr2 = context.registerService(ImporterService.class, importer2, props2);

        ImporterService is2 = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer2-instance)", 0);
        assertThat(is2).isNotNull();
        verify(importer2).addDeclaration(iD1);

        assertThat(iD1.getStatus().getServiceReferencesBounded()).containsOnly(sr1.getReference(), sr2.getReference());

        // reconfigure the ImporterService filter of the linker :
        Dictionary<String, Object> linkerProps = new Hashtable<String, Object>();
        linkerProps.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "(" + Constants.PROTOCOL_NAME + "=test)");
        linkerProps.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(" + INSTANCE_NAME_PROPERTY + "=importer1*)");
        importationLinkerCI.reconfigure(linkerProps);

        verify(importer2).removeDeclaration(iD1);
        assertThat(iD1.getStatus().getServiceReferencesBounded()).containsOnly(sr1.getReference());

        // reconfigure the ImporterService filter of the linker :
        linkerProps = new Hashtable<String, Object>();
        linkerProps.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "(" + Constants.PROTOCOL_NAME + "=test)");
        linkerProps.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(" + INSTANCE_NAME_PROPERTY + "=importer2*)");
        importationLinkerCI.reconfigure(linkerProps);

        verify(importer1).removeDeclaration(iD1);
        verify(importer1).addDeclaration(iD1);
        assertThat(iD1.getStatus().getServiceReferencesBounded()).containsOnly(sr2.getReference());
    }


    @Test
    public void testReconfigureImporterTargetFilter() throws BinderException {
        // create ImportDeclaration1
        Map<String, Object> metadata1 = new HashMap<String, Object>();
        metadata1.put(Constants.PROTOCOL_NAME, "test");
        metadata1.put(Constants.ID, "test1");
        ImportDeclaration iD1 = ImportDeclarationBuilder.fromMetadata(metadata1).build();
        ServiceRegistration iDReg1 = registerImportDeclaration(iD1);
        assertThat(iDReg1).isNotNull();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).containsOnly(iD1);

        // create ImportDeclaration2
        Map<String, Object> metadata2 = new HashMap<String, Object>();
        metadata2.put(Constants.PROTOCOL_NAME, "test");
        metadata2.put(Constants.ID, "test2");
        ImportDeclaration iD2 = ImportDeclarationBuilder.fromMetadata(metadata2).build();
        ServiceRegistration iDReg2 = registerImportDeclaration(iD2);
        assertThat(iDReg2).isNotNull();
        assertThat(importationLinkerIntrospection.getImportDeclarations()).containsOnly(iD1, iD2);

        // register the importer
        Dictionary<String, Object> importerProps = new Hashtable<String, Object>();
        importerProps.put(INSTANCE_NAME_PROPERTY, "importer1-instance");
        importerProps.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test*)");
        ServiceRegistration<ImporterService> sr1 = context.registerService(ImporterService.class, importer1, importerProps);
        ImporterService is1 = osgiHelper.waitForService(ImporterService.class, "(" + INSTANCE_NAME_PROPERTY + "=importer1-instance)", 0);
        assertThat(is1).isNotNull();
        verify(importer1).addDeclaration(iD1);
        verify(importer1).addDeclaration(iD2);

        // reconfigure the importer
        importerProps = new Hashtable<String, Object>();
        importerProps.put(INSTANCE_NAME_PROPERTY, "importer1-instance");
        importerProps.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test1)");
        sr1.setProperties(importerProps);

        verify(importer1).removeDeclaration(iD2);
        verify(importer1, never()).removeDeclaration(iD1);
        verify(importer1, times(1)).addDeclaration(iD1);

        assertThat(iD1.getStatus().getServiceReferencesBounded()).containsOnly(sr1.getReference());
        // reconfigure the importer
        importerProps = new Hashtable<String, Object>();
        importerProps.put(INSTANCE_NAME_PROPERTY, "importer1-instance");
        importerProps.put(DeclarationBinder.TARGET_FILTER_PROPERTY, "(" + Constants.ID + "=test2)");
        sr1.setProperties(importerProps);

        verify(importer1).removeDeclaration(iD1);
        verify(importer1, times(2)).addDeclaration(iD2);
    }

    @Test
    public void testWrongReconfigurationImporterFilter() {
        // reconfigure the filter  :
        Dictionary<String, Object> linkerProps = new Hashtable<String, Object>();
        linkerProps.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "(" + Constants.PROTOCOL_NAME + "=test)");
        linkerProps.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(=");   // not a LDAP valid filter
        importationLinkerCI.reconfigure(linkerProps);

        ServiceReference serviceReference = osgiHelper.waitForService(ImportationLinker.class.getName(), "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 1, false);

        assertThat(serviceReference).isNull();
    }

    @Test
    public void testWrongReconfigurationIDecFilter() {
        // reconfigure the filter  :
        Dictionary<String, Object> linkerProps = new Hashtable<String, Object>();
        linkerProps.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY, "a wrong filter"); // not a LDAP valid filter
        linkerProps.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY, "(" + INSTANCE_NAME_PROPERTY + "=*)");
        importationLinkerCI.reconfigure(linkerProps);

        ServiceReference serviceReference = osgiHelper.waitForService(ImportationLinker.class.getName(), "(" + INSTANCE_NAME_PROPERTY + "=" + linkerInstanceName + ")", 1, false);

        assertThat(serviceReference).isNull();
    }
}
