package org.ow2.chameleon.fuchsia.raspberry.pi.importer;

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

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.raspberry.pi.internal.GPIOImportDeclarationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component(name = "GpIOImporter")
@Provides(specifications = {ImporterService.class, ImporterIntrospection.class})
public class GpIOImporter extends AbstractImporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(GpIOImporter.class);

    private ServiceReference serviceReference;

    private Map<String, InstanceManager> gpioPin = new HashMap<String, InstanceManager>();

    @Requires(filter = "(factory.name=org.ow2.chameleon.fuchsia.raspberry.pi.device.GPIOOutputPinFactory)")
    private Factory lightFactory;

    @ServiceProperty(name = "target", value = "(&(importer.gpio.pin=*)(importer.gpio.name=*)(id=*)(scope=generic))")
    private String filter;

    public GpIOImporter(BundleContext context) {
        super();
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Validate
    public void validate() {
        super.start();
        LOG.info("GOIO importer is up and running");
    }

    @Invalidate
    public void invalidate() {
        super.stop();
        LOG.info("GOIO importer was shutdown");
    }

    @Override
    protected void useImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {
        GPIOImportDeclarationWrapper pojo = GPIOImportDeclarationWrapper.create(importDeclaration);

        LOG.info("importing id:" + pojo.getId());

        Dictionary<String, Object> light = new Hashtable<String, Object>();

        light.put("pin", pojo.getPin());

        try {
            super.handleImportDeclaration(importDeclaration);
            InstanceManager im = (InstanceManager) lightFactory.createComponentInstance(light);

            gpioPin.put(pojo.getId(), im);

        } catch (UnacceptableConfiguration e) {
            LOG.error("Invalid configuration", e);
            importDeclaration.unhandle(serviceReference);
        } catch (MissingHandlerException e) {
            LOG.error("Missing handle", e);
            importDeclaration.unhandle(serviceReference);
        } catch (ConfigurationException e) {
            LOG.error("Configuration exception", e);
            super.handleImportDeclaration(importDeclaration);
        }
    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {
        GPIOImportDeclarationWrapper pojo = GPIOImportDeclarationWrapper.create(importDeclaration);

        InstanceManager im = gpioPin.remove(pojo.getId());
        unhandleImportDeclaration(importDeclaration);

        if (im != null) {
            im.dispose();
        }
    }


    public String getName() {
        return this.getClass().getSimpleName();
    }

}
