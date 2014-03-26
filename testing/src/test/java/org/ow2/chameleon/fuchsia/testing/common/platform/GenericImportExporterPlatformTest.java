package org.ow2.chameleon.fuchsia.testing.common.platform;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Base Test: Utilities for test
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.testing.common.ctd.ServiceForExportation;
import org.ow2.chameleon.fuchsia.testing.common.ctd.ServiceForExportationImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * This class is responsible in filling the main field by to be used by the test, all basic needs from an importer/exporter are filled up with Mockito
 * @param <T> The declaration type (ImportDeclaration or ExportDeclaration)
 * @param <S> Importer or an Exporter
 */
public abstract class GenericImportExporterPlatformTest<T extends Declaration,S extends DeclarationBinder> {

    public static final Integer HTTP_PORT=8046;

    protected final Map<ServiceReference,Object> services=new HashMap<ServiceReference,Object>();

    protected final Map<String,Class> clazzes=new HashMap<String,Class>();

    @Mock
    protected BundleContext context;

    @Mock
    protected PackageAdmin packageAdmin;

    @Mock
    protected ServiceReference packageAdminServiceReference;

    @Mock
    protected Bundle bundle;

    protected S fuchsiaDeclarationBinder;

    @Mock
    protected ServiceReference fuchsiaDeclarationBinderServiceReference;


    public void initialize() throws Exception {

        MockitoAnnotations.initMocks(this);

        try {
            when(context.getBundle()).thenReturn(bundle);
            when(bundle.loadClass(anyString())).thenAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return clazzes.get((String)invocation.getArguments()[0]);
                }
            });

            when(context.getServiceReference(PackageAdmin.class.getName())).thenReturn(packageAdminServiceReference);
            when(fuchsiaDeclarationBinderServiceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(1l);
            when(context.getService(any(ServiceReference.class))).thenAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Throwable {

                    ServiceReference sr = (ServiceReference) invocation.getArguments()[0];

                    return services.get(sr);
                }
            });
            when(context.getServiceReferences(any(Class.class), anyString())).then(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    ArrayList<ServiceReference> list = new ArrayList<ServiceReference>();
                    Class clazz = (Class) invocation.getArguments()[0];
                    for (Map.Entry<ServiceReference, Object> entry : services.entrySet()) {

                        if (clazz == null || clazz.isInstance(entry.getValue()))
                            list.add(entry.getKey());

                    }
                    return list;

                }
            });
            when(context.getServiceReference(any(Class.class))).thenAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    Class clazz = (Class) invocationOnMock.getArguments()[0];
                    for (Map.Entry<ServiceReference, Object> entry : services.entrySet()) {
                        if (clazz.isInstance(entry.getValue())) {
                            return entry.getValue();
                        }
                    }
                    return null;
                }
            });
            registerService(packageAdminServiceReference, packageAdmin);
            registerClass(ServiceForExportation.class);
            registerClass(ServiceForExportationImpl.class);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    protected void registerService(ServiceReference sr,Object object){
        services.put(sr,object);
    }

    /**
     * This are the classes that should be visible by bundle.loadClass. Only those classes will be allowed to be loaded
     * @param clazz
     */
    protected void registerClass(Class clazz){
        clazzes.put(clazz.getName(),clazz);
    }

    protected void verifyRemoteInvocation(ServiceForExportation mock, ServiceForExportation proxy){

        final String stringValue="coucou";
        final Integer intValue=1789;

        proxy.ping();
        verify(mock, times(1)).ping();

        proxy.ping(intValue);
        verify(mock, times(1)).ping(intValue);

        proxy.ping(stringValue);
        verify(mock, times(1)).ping(stringValue);

        String returnPongString=proxy.pongString(stringValue);
        verify(mock, times(1)).pongString(stringValue);
        Assert.assertEquals(returnPongString, stringValue);

        Integer returnPongInteger=proxy.pongInteger(intValue);
        verify(mock, times(1)).pongInteger(intValue);
        Assert.assertEquals(returnPongInteger, intValue);

    }

    /**
     * Force test class to give some samples of invalid declaration in order to be tested, if the proper exception are thrown
     * @return
     */
    public abstract List<T> getInvalidDeclarations();

    /**
     * Force test class to give some samples of valid declaration in order to be tested, if the proper exception are thrown
     * @return
     */
    public abstract List<T> getValidDeclarations();


}
