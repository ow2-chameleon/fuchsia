package org.ow2.chameleon.fuchsia.importer.philipshue.test;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer Philips Hue
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

import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import org.fest.reflect.core.Reflection;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.importer.philipshue.PhilipsHueImporter;

import java.util.Dictionary;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class PhilipsHueImporterAbstractTest {

    @Mock
    protected BundleContext context;
    @Mock
    Bundle bundle;
    @Mock
    PHLight light;
    @Mock
    PHBridge bridge;
    @Mock
    ServiceRegistration lightServiceRegistration;
    @Mock
    ServiceRegistration bridgeServiceRegistration;
    @Mock
    PackageAdmin packageAdmin;
    @Mock
    ServiceReference serviceReference;
    @Mock
    ServiceReference packageAdminServiceReference;

    PhilipsHueImporter importer;

    @Before
    public void validate(){
        MockitoAnnotations.initMocks(this);
        importer=spy(Reflection.constructor().withParameterTypes(BundleContext.class).in(PhilipsHueImporter.class).newInstance(context));
        setupInterceptors();
    }

    protected void setupInterceptors(){

        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(packageAdminServiceReference);
        when(context.getService(packageAdminServiceReference)).thenReturn(packageAdmin);
        when(context.getBundle()).thenReturn(bundle);
        when(serviceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
        when(context.registerService(eq(PHLight.class.getName()), anyObject(), any(Dictionary.class))).thenReturn(lightServiceRegistration);
        when(context.registerService(eq(PHBridge.class.getName()), anyObject(), any(Dictionary.class))).thenReturn(bridgeServiceRegistration);

    }

}
