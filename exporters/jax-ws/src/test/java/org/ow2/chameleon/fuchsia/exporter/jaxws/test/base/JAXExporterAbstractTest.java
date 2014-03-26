package org.ow2.chameleon.fuchsia.exporter.jaxws.test.base;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Exporter JAX-WS
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

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.*;
import org.osgi.service.http.HttpService;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.exporter.jaxws.JAXWSExporter;
import org.ow2.chameleon.fuchsia.exporter.jaxws.test.ctd.ServiceForExportation;
import org.ow2.chameleon.fuchsia.exporter.jaxws.test.ctd.ServiceForExportationImpl;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class JAXExporterAbstractTest {

    protected static final Integer HTTP_PORT=8043;

    @Mock
    protected org.osgi.framework.BundleContext context;

    @Mock
    protected ServiceRegistration registrationFromClassToBeExported;

    @Mock
    protected ServiceReference serviceReferenceFromExporter;

    @Mock
    protected ExportedPackage exportPackageForClass;

    @Mock
    protected Bundle bundeToLoadClassFrom;

    @Mock
    protected PackageAdmin packageAdminMock;

    protected ServiceForExportation id= spy(new ServiceForExportationImpl());

    protected ServiceReference idServiceReference[]=new ServiceReference[]{id};

    protected JAXWSExporter exporter;

    @Mock
    protected HttpService httpServiceMock;

    /**
     * Instantiate all mocks necessary for the exportation, and invokes @Validate method from the exporter.
     */
    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        prepareMockInterceptors();

        exporter.start();

    }

    /**
     * Remove all instantiation (avoid leak) and invoke @Invalidate method.
     */
    @After
    public void setupClean() {

        exporter.stop();

        registrationFromClassToBeExported = null;

        serviceReferenceFromExporter = null;

        exportPackageForClass = null;

        bundeToLoadClassFrom = null;

        packageAdminMock = null;

        id=null;

        idServiceReference=null;

    }

    protected void prepareMockInterceptors(){

        Dictionary<String, Object> props1 = new Hashtable<String, Object>();

        when(context.registerService(new String[]{ExportDeclaration.class.getName()}, id, props1)).thenReturn(registrationFromClassToBeExported);
        when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(serviceReferenceFromExporter);
        when(serviceReferenceFromExporter.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
        when(context.getService(serviceReferenceFromExporter)).thenReturn(packageAdminMock);
        when(context.getBundle()).thenReturn(bundeToLoadClassFrom);
        when(context.getService(idServiceReference[0])).thenReturn(id);
        try {
            when(context.getAllServiceReferences(ServiceForExportation.class.getName(), null)).thenReturn(idServiceReference);
            when(bundeToLoadClassFrom.loadClass(anyString())).thenAnswer(new Answer<Class>() {
                public Class answer(InvocationOnMock invocation) throws Throwable {
                    return Thread.currentThread().getContextClassLoader().loadClass((String) invocation.getArguments()[0]);
                }
            });
        } catch (InvalidSyntaxException e) {

        } catch (ClassNotFoundException e) {

        }

        when(packageAdminMock.getExportedPackage(ServiceForExportation.class.getName())).thenReturn(exportPackageForClass);

        exporter=constructor().withParameterTypes(BundleContext.class).in(JAXWSExporter.class).newInstance(context);

        field("httpPort").ofType(Integer.class).in(exporter).set(HTTP_PORT);

    }



}
