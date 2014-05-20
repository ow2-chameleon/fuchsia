/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.fuchsia.importer.jsonrpc.test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import junit.framework.Assert;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.jsonrpc.JSONRPCImporter;
import org.ow2.chameleon.fuchsia.testing.common.GenericTest;
import org.ow2.chameleon.fuchsia.testing.common.ctd.ServiceForExportation;
import org.ow2.chameleon.fuchsia.testing.common.ctd.ServiceForExportationImpl;
import org.ow2.chameleon.fuchsia.testing.common.services.HttpServiceImpl;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.fest.reflect.core.Reflection.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class JSONRPCImporterTest extends GenericTest<ImportDeclaration, JSONRPCImporter> {

    private final ServiceForExportation serviceToBeExported = spy(new ServiceForExportationImpl());

    HttpServiceImpl http;

    ServiceForExportation proxyRegistered;

    @Mock
    ServiceRegistration proxyRegisteredServiceRegistration;

    @Mock
    Factory defaultProxyFactory;

    @Before
    public void initialize() throws Exception {

        super.initialize();

        fuchsiaDeclarationBinder = spy(constructor().withParameterTypes(BundleContext.class).in(JSONRPCImporter.class).newInstance(context));
        fuchsiaDeclarationBinder.registration(fuchsiaDeclarationBinderServiceReference);

        super.registerService(serviceToBeExported, serviceToBeExported);

        http = new HttpServiceImpl(HTTP_PORT);

        when(context.registerService(eq(ServiceForExportation.class.getName()), anyObject(), any(Dictionary.class))).thenAnswer(new Answer<ServiceRegistration>() {
            public ServiceRegistration answer(InvocationOnMock invocation) throws Throwable {

                proxyRegistered = (ServiceForExportation) invocation.getArguments()[1];

                return proxyRegisteredServiceRegistration;
            }
        });

        field("defaultProxyFactory").ofType(Factory.class).in(fuchsiaDeclarationBinder).set(defaultProxyFactory);

        when(defaultProxyFactory.createComponentInstance(any(Dictionary.class))).thenAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new ComponentInstance() {
                    public void start() {

                    }

                    public void stop() {

                    }

                    public void dispose() {

                    }

                    public int getState() {
                        return 0;
                    }

                    public InstanceDescription getInstanceDescription() {
                        return null;
                    }

                    public ComponentFactory getFactory() {
                        return null;
                    }

                    public BundleContext getContext() {
                        return null;
                    }

                    public String getInstanceName() {
                        return null;
                    }

                    public boolean isStarted() {
                        return false;
                    }

                    public void reconfigure(Dictionary configuration) {

                    }

                    public void addInstanceStateListener(InstanceStateListener listener) {

                    }

                    public void removeInstanceStateListener(InstanceStateListener listener) {

                    }
                };
            }
        });

        method("start").in(fuchsiaDeclarationBinder).invoke();

    }

    @After
    public void uninitialize() throws Exception {
        http.getServer().stop();
    }

    @Test
    public void useDeclaration() throws ServletException, NamespaceException, BinderException {

        ImportDeclaration declaration = spy(getValidDeclarations().get(0));

        JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBeExported, ServiceForExportation.class);

        Servlet gs = new RPCServlet(jsonRpcServer);

        Dictionary<String, Object> dic = new Hashtable<String, Object>();

        http.registerServlet("/ping", gs, dic, null);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        verify(declaration, times(1)).handle(fuchsiaDeclarationBinderServiceReference);

        Map<String, ComponentInstance> registrations = field("registrations").ofType(Map.class).in(fuchsiaDeclarationBinder).get();
        Map<String, JsonRpcHttpClient> clients = field("clients").ofType(Map.class).in(fuchsiaDeclarationBinder).get();

        Assert.assertEquals(1, registrations.size());
        Assert.assertEquals(1, clients.size());
    }

    @Test
    public void useDeclarationDefaultProxy() throws ServletException, NamespaceException, BinderException {

        Map<String, Object> metadata = new HashMap<String, Object>();

        metadata.put(Constants.ID, "my-id");
        metadata.put(Constants.URL, "http://localhost:" + HTTP_PORT + "/ping");
        //metadata.put(Constants.SERVICE_CLASS,ServiceForExportation.class.getName());

        final ImportDeclaration declaration = spy(ImportDeclarationBuilder.fromMetadata(metadata).build());

        declaration.bind(fuchsiaDeclarationBinderServiceReference);

        JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBeExported, ServiceForExportation.class);

        Servlet gs = new RPCServlet(jsonRpcServer);

        Dictionary<String, Object> dic = new Hashtable<String, Object>();

        http.registerServlet("/ping", gs, dic, null);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        verify(declaration, times(1)).handle(fuchsiaDeclarationBinderServiceReference);

        Map<String, ComponentInstance> registrations = field("componentInstances").ofType(Map.class).in(fuchsiaDeclarationBinder).get();

        Assert.assertEquals(1, registrations.size());
    }

    @Test
    public void denyDeclarationCustomProxy() throws ServletException, NamespaceException, BinderException {

        ImportDeclaration declaration = spy(getValidDeclarations().get(0));


        JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBeExported, ServiceForExportation.class);

        Servlet gs = new RPCServlet(jsonRpcServer);

        Dictionary<String, Object> dic = new Hashtable<String, Object>();

        http.registerServlet("/ping", gs, dic, null);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        verify(declaration, times(1)).handle(fuchsiaDeclarationBinderServiceReference);

        Map<String, ComponentInstance> registrations = field("registrations").ofType(Map.class).in(fuchsiaDeclarationBinder).get();
        Map<String, JsonRpcHttpClient> clients = field("clients").ofType(Map.class).in(fuchsiaDeclarationBinder).get();

        Assert.assertEquals(1, registrations.size());
        Assert.assertEquals(1, clients.size());

        fuchsiaDeclarationBinder.denyDeclaration(declaration);

        verify(declaration, times(1)).unhandle(fuchsiaDeclarationBinderServiceReference);

        Assert.assertEquals(0, registrations.size());
        Assert.assertEquals(0, clients.size());

    }

    @Test
    public void denyDeclarationDefaultProxy() throws ServletException, NamespaceException, BinderException {

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.ID, "my-id");
        metadata.put(Constants.URL, "http://localhost:" + HTTP_PORT + "/ping");
        //metadata.put(Constants.SERVICE_CLASS,ServiceForExportation.class.getName());

        ImportDeclaration declaration = spy(ImportDeclarationBuilder.fromMetadata(metadata).build());
        declaration.bind(fuchsiaDeclarationBinderServiceReference);

        JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBeExported, ServiceForExportation.class);

        Servlet gs = new RPCServlet(jsonRpcServer);

        Dictionary<String, Object> dic = new Hashtable<String, Object>();

        http.registerServlet("/ping", gs, dic, null);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        verify(declaration, times(1)).handle(fuchsiaDeclarationBinderServiceReference);

        Map<String, ComponentInstance> componentInstances = field("componentInstances").ofType(Map.class).in(fuchsiaDeclarationBinder).get();

        Assert.assertEquals(1, componentInstances.size());

        fuchsiaDeclarationBinder.denyDeclaration(declaration);

        verify(declaration, times(1)).unhandle(fuchsiaDeclarationBinderServiceReference);

        Assert.assertEquals(0, componentInstances.size());

    }

    @Test
    public void remoteInvocationCustomProxy() throws ServletException, NamespaceException, BinderException {

        ImportDeclaration declaration = getValidDeclarations().get(0);

        JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBeExported, ServiceForExportation.class);

        Servlet gs = new RPCServlet(jsonRpcServer);

        Dictionary<String, Object> emptyDictionary = new Hashtable<String, Object>();

        http.registerServlet("/ping", gs, emptyDictionary, null);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        verifyRemoteInvocation(serviceToBeExported, proxyRegistered);

    }


    @Override
    public List<ImportDeclaration> getInvalidDeclarations() {
        Map<String, Object> metadata = new HashMap<String, Object>();

        metadata.put(Constants.ID, "my-id");
        //metadata.put(Constants.URL,"http://localhost:"+HTTP_PORT+"");
        metadata.put(Constants.SERVICE_CLASS, ServiceForExportation.class.getName());

        final ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        declaration.bind(fuchsiaDeclarationBinderServiceReference);

        return new ArrayList<ImportDeclaration>() {{
            add(declaration);
        }};
    }

    @Test
    public void gracefulStop() throws BinderException, ServletException, NamespaceException, MissingHandlerException, UnacceptableConfiguration, ConfigurationException {

        ImportDeclaration declaration = getValidDeclarations().get(0);

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Constants.ID, "my-id");
        metadata.put(Constants.URL, "http://localhost:" + HTTP_PORT + "/ping");

        ImportDeclaration declarationForDefaultProxy = spy(ImportDeclarationBuilder.fromMetadata(metadata).build());

        declarationForDefaultProxy.bind(fuchsiaDeclarationBinderServiceReference);

        JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBeExported, ServiceForExportation.class);

        Servlet gs = new RPCServlet(jsonRpcServer);

        Dictionary<String, Object> emptyDictionary = new Hashtable<String, Object>();

        http.registerServlet("/ping", gs, emptyDictionary, null);

        fuchsiaDeclarationBinder.useDeclaration(declaration);
        fuchsiaDeclarationBinder.useDeclaration(declarationForDefaultProxy);

        verifyRemoteInvocation(serviceToBeExported, proxyRegistered);

        Map<String, ComponentInstance> registrations = field("registrations").ofType(Map.class).in(fuchsiaDeclarationBinder).get();
        Map<String, ComponentInstance> componentInstances = field("componentInstances").ofType(Map.class).in(fuchsiaDeclarationBinder).get();

        Assert.assertEquals(1, registrations.size());
        Assert.assertEquals(1, componentInstances.size());

        method("stop").in(fuchsiaDeclarationBinder).invoke();

        Assert.assertEquals(0, registrations.size());
        Assert.assertEquals(0, componentInstances.size());

    }

    @Override
    public List<ImportDeclaration> getValidDeclarations() {

        Map<String, Object> metadata = new HashMap<String, Object>();

        metadata.put(Constants.ID, "my-id");
        metadata.put(Constants.URL, "http://localhost:" + HTTP_PORT + "/ping");
        metadata.put(Constants.SERVICE_CLASS, ServiceForExportation.class.getName());

        final ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        declaration.bind(fuchsiaDeclarationBinderServiceReference);

        return new ArrayList<ImportDeclaration>() {{
            add(declaration);
        }};
    }

    class RPCServlet extends HttpServlet {

        private final JsonRpcServer jsonRpcServer;

        public RPCServlet(JsonRpcServer jsonRpcServer) {
            this.jsonRpcServer = jsonRpcServer;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            jsonRpcServer.handle(req, resp);
        }

    }
}
