package org.ow2.chameleon.fuchsia.importer.jsonrpc.test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import junit.framework.Assert;
import org.apache.felix.ipojo.ComponentInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.base.test.common.GenericTest;
import org.ow2.chameleon.fuchsia.base.test.common.ctd.ServiceForExportation;
import org.ow2.chameleon.fuchsia.base.test.common.ctd.ServiceForExportationImpl;
import org.ow2.chameleon.fuchsia.base.test.common.services.HttpServiceImpl;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.jsonrpc.DefaultJSONRPCProxy;
import org.ow2.chameleon.fuchsia.importer.jsonrpc.JSONRPCImporter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.core.Reflection.method;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class JSONRPCImporterTest extends GenericTest<ImportDeclaration,JSONRPCImporter> {

    private final ServiceForExportation serviceToBeExported = spy(new ServiceForExportationImpl());

    HttpServiceImpl http;

    ServiceForExportation proxyRegistered;

    @Mock
    ServiceRegistration proxyRegisteredServiceRegistration;

    @Before
    public void initialize() throws Exception {

        super.initialize();

        fuchsiaDeclarationBinder=spy(constructor().withParameterTypes(BundleContext.class).in(JSONRPCImporter.class).newInstance(context));
        fuchsiaDeclarationBinder.registration(fuchsiaDeclarationBinderServiceReference);

        super.registerService(packageAdminServiceReference,packageAdmin);
        super.registerService(serviceToBeExported,serviceToBeExported);

        super.registerClass(ServiceForExportation.class);
        super.registerClass(ServiceForExportationImpl.class);

        http=new HttpServiceImpl(HTTP_PORT);

        when(context.registerService(eq(ServiceForExportation.class.getName()),anyObject(),any(Dictionary.class))).thenAnswer(new Answer<ServiceRegistration>() {
            public ServiceRegistration answer(InvocationOnMock invocation) throws Throwable {

                proxyRegistered=(ServiceForExportation)invocation.getArguments()[1];

                return proxyRegisteredServiceRegistration;
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

        ImportDeclaration declaration=spy(getValidDeclarations().get(0));

        JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBeExported, ServiceForExportation.class);

        Servlet gs = new RPCServlet(jsonRpcServer);

        Dictionary<String,Object> dic=new Hashtable<String, Object>();

        http.registerServlet("/ping", gs, dic, null);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        verify(declaration,times(1)).handle(fuchsiaDeclarationBinderServiceReference);

        Map<String, ComponentInstance> registrations=field("registrations").ofType(Map.class).in(fuchsiaDeclarationBinder).get();
        Map<String, JsonRpcHttpClient> clients=field("clients").ofType(Map.class).in(fuchsiaDeclarationBinder).get();

        Assert.assertEquals(1,registrations.size());
        Assert.assertEquals(1,clients.size());
    }

    @Test
    public void denyDeclaration() throws ServletException, NamespaceException, BinderException {

        ImportDeclaration declaration=spy(getValidDeclarations().get(0));

        JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBeExported, ServiceForExportation.class);

        Servlet gs = new RPCServlet(jsonRpcServer);

        Dictionary<String,Object> dic=new Hashtable<String, Object>();

        http.registerServlet("/ping", gs, dic, null);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        verify(declaration,times(1)).handle(fuchsiaDeclarationBinderServiceReference);

        Map<String, ComponentInstance> registrations=field("registrations").ofType(Map.class).in(fuchsiaDeclarationBinder).get();
        Map<String, JsonRpcHttpClient> clients=field("clients").ofType(Map.class).in(fuchsiaDeclarationBinder).get();

        Assert.assertEquals(1,registrations.size());
        Assert.assertEquals(1,clients.size());

        fuchsiaDeclarationBinder.denyDeclaration(declaration);

        verify(declaration,times(1)).unhandle(fuchsiaDeclarationBinderServiceReference);

        Assert.assertEquals(0,registrations.size());
        Assert.assertEquals(0,clients.size());

    }

    @Test
    public void remoteInvocationCustomProxy() throws ServletException, NamespaceException, BinderException {

        ImportDeclaration declaration=getValidDeclarations().get(0);

        JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBeExported, ServiceForExportation.class);

        Servlet gs = new RPCServlet(jsonRpcServer);

        Dictionary<String,Object> emptyDictionary=new Hashtable<String, Object>();

        http.registerServlet("/ping", gs, emptyDictionary, null);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        verifyRemoteInvocation(serviceToBeExported, proxyRegistered);

    }


    @Override
    public List<ImportDeclaration> getInvalidDeclarations() {
        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put(Constants.ID,"my-id");
        //metadata.put(Constants.URL,"http://localhost:"+HTTP_PORT+"");
        metadata.put(Constants.SERVICE_CLASS,ServiceForExportation.class.getName());

        final ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        declaration.bind(fuchsiaDeclarationBinderServiceReference);

        return new ArrayList<ImportDeclaration>(){{add(declaration);}};
    }

    @Test
    public void gracefulStop() throws BinderException, ServletException, NamespaceException {

        ImportDeclaration declaration=getValidDeclarations().get(0);

        JsonRpcServer jsonRpcServer = new JsonRpcServer(serviceToBeExported, ServiceForExportation.class);

        Servlet gs = new RPCServlet(jsonRpcServer);

        Dictionary<String,Object> emptyDictionary=new Hashtable<String, Object>();

        http.registerServlet("/ping", gs, emptyDictionary, null);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        verifyRemoteInvocation(serviceToBeExported, proxyRegistered);

        Map<String, ComponentInstance> registrations=field("registrations").ofType(Map.class).in(fuchsiaDeclarationBinder).get();

        Assert.assertEquals(1,registrations.size());

        method("stop").in(fuchsiaDeclarationBinder).invoke();

        Assert.assertEquals(0,registrations.size());

    }

    @Override
    public List<ImportDeclaration> getValidDeclarations() {

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put(Constants.ID,"my-id");
        metadata.put(Constants.URL,"http://localhost:"+HTTP_PORT+"/ping");
        metadata.put(Constants.SERVICE_CLASS,ServiceForExportation.class.getName());

        final ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        declaration.bind(fuchsiaDeclarationBinderServiceReference);

        return new ArrayList<ImportDeclaration>(){{add(declaration);}};
    }

    private void verifyRemoteInvocation(ServiceForExportation mock, ServiceForExportation proxy){

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
