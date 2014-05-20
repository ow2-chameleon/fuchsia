package org.ow2.chameleon.fuchsia.raspberry.pi.importer.test;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer Raspberry Pi GPIO
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

import junit.framework.Assert;
import org.apache.felix.ipojo.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.raspberry.pi.importer.GpIOImporter;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class GpIOImporterTest {

    @Mock
    BundleContext context;

    @Mock
    GpIOImporter importer;

    @Mock
    ImportDeclaration declaration;

    @Mock
    ServiceReference importerServiceReference;

    @Mock
    Factory lightFactory;

    @Mock
    HandlerManager hm;

    @Before
    public void before() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {

        MockitoAnnotations.initMocks(this);

        when(importerServiceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);

        declaration = spy(getValidDeclaration());
        declaration.bind(importerServiceReference);

        importer = spy(constructor().withParameterTypes(BundleContext.class).in(GpIOImporter.class).newInstance(context));

        when(lightFactory.createComponentInstance(any(Dictionary.class))).thenReturn(hm);

        field("lightFactory").ofType(Factory.class).in(importer).set(lightFactory);

        importer.validate();
        importer.registration(importerServiceReference);

    }

    @Test
    public void testInvalidDeclaration() throws BinderException {
        ImportDeclaration declaration = spy(getInvalidDeclaration());
        try {
            importer.registration(importerServiceReference);
            importer.useDeclaration(declaration);
            Assert.fail("A BinderException should have been thrown at this point");
        } catch (BinderException be) {
            //An exception is normal here
        }
        verify(declaration, times(0)).handle(importerServiceReference);
    }

    @Test
    public void testValidDeclaration() throws BinderException {
        ImportDeclaration declaration = getValidDeclaration();
        declaration.bind(importerServiceReference);
        try {
            importer.registration(importerServiceReference);
            importer.useDeclaration(declaration);
        } catch (BinderException be) {
            Assert.fail("A BinderException should have been thrown at this point");
        }
    }

    @Test
    public void testImporterUseDeclaration() throws BinderException, MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        importer.useDeclaration(declaration);
        Map<String, InstanceManager> gpioPin = field("gpioPin").ofType(Map.class).in(importer).get();
        verify(lightFactory, times(1)).createComponentInstance(any(Dictionary.class));
        verify(declaration, times(1)).handle(importerServiceReference);
        Assert.assertEquals(1, gpioPin.size());
    }

    @Test
    public void testImporterUseDeclarationNotTraceCaseException() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ImportDeclaration declaration = spy(getInvalidDeclaration());
        try {
            importer.useDeclaration(declaration);
        } catch (Exception e) {
            //Swallow exception
        }
        Map<String, InstanceManager> gpioPin = field("gpioPin").ofType(Map.class).in(importer).get();
        verify(lightFactory, times(0)).createComponentInstance(any(Dictionary.class));
        verify(declaration, times(0)).handle(importerServiceReference);
        Assert.assertEquals(0, gpioPin.size());
    }

    @Test
    public void testGracefulServiceStop() throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        ImportDeclaration declaration = spy(getValidDeclaration());
        try {
            importer.useDeclaration(declaration);
        } catch (Exception e) {
            //Swallow exception
        }

        Map<String, InstanceManager> gpioPin = field("gpioPin").ofType(Map.class).in(importer).get();
        verify(lightFactory, times(1)).createComponentInstance(any(Dictionary.class));
        verify(declaration, times(1)).handle(importerServiceReference);
        Assert.assertEquals(1, gpioPin.size());
        importer.invalidate();
        Assert.assertEquals(0, importer.getImportDeclarations().size());
    }

    @Test
    public void importerNameCannotBeNull() {
        Assert.assertNotNull(importer.getName());
    }

    @Test
    public void testImporterDenyDeclaration() throws BinderException, MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        importer.useDeclaration(declaration);
        Map<String, InstanceManager> gpioPin = field("gpioPin").ofType(Map.class).in(importer).get();
        Assert.assertEquals(1, gpioPin.size());
        importer.denyDeclaration(declaration);
        verify(declaration, times(1)).unhandle(importerServiceReference);
        Assert.assertEquals(0, gpioPin.size());
    }

    public ImportDeclaration getInvalidDeclaration() {
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("id", "id");
        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(meta).build();
        declaration.bind(importerServiceReference);
        return declaration;
    }

    public ImportDeclaration getValidDeclaration() {
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("id", "id");
        meta.put("importer.gpio.pin", "1");
        meta.put("importer.gpio.name", "lamp");
        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(meta).build();
        declaration.bind(importerServiceReference);
        return declaration;
    }

}
