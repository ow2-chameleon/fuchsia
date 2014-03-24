package org.ow2.chameleon.fuchsia.exporter.jsonrpc.test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.ow2.chameleon.fuchsia.testing.common.ctd.ServiceForExportation;
import org.ow2.chameleon.fuchsia.testing.common.ctd.ServiceForExportationImpl;
import org.ow2.chameleon.fuchsia.testing.common.services.HttpServiceImpl;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.exporter.jsonrpc.JSONRPCExporter;
import org.ow2.chameleon.fuchsia.exporter.jsonrpc.model.JSONRPCExportDeclarationWrapper;
import org.ow2.chameleon.fuchsia.testing.common.GenericTest;

import java.util.*;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;

public class JSONRPCExporterTest extends GenericTest<ExportDeclaration,JSONRPCExporter> {

    private final ServiceForExportation serviceToBeExported = spy(new ServiceForExportationImpl());

    private HttpServiceImpl http;

    @Before
    public void initialize() throws Exception {

        super.initialize();

        fuchsiaDeclarationBinder=constructor().withParameterTypes(BundleContext.class).in(JSONRPCExporter.class).newInstance(context);

        http=spy(new HttpServiceImpl(HTTP_PORT));

        //Field injected by IPOJO
        field("web").ofType(HttpService.class).in(fuchsiaDeclarationBinder).set(http);
        field("name").ofType(String.class).in(fuchsiaDeclarationBinder).set("instance-name");

        fuchsiaDeclarationBinder.registration(fuchsiaDeclarationBinderServiceReference);

        super.registerService(serviceToBeExported,serviceToBeExported);

    }

    @After
    public void stop() throws Exception {
        http.getServer().stop();
    }

    @Test
    public void remoteServiceInvoked() throws Exception {

        ExportDeclaration declaration=getValidDeclarations().get(0);

        JSONRPCExportDeclarationWrapper pojo=JSONRPCExportDeclarationWrapper.create(declaration);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        JsonRpcHttpClient client=new JsonRpcHttpClient(new java.net.URL("http://localhost:"+HTTP_PORT+pojo.getUrlContext()+"/"+pojo.getInstanceName()));

        Object proxy = ProxyUtil.createClientProxy(this.getClass().getClassLoader(), ServiceForExportation.class, client);

        ServiceForExportation remoteObject=(ServiceForExportation)proxy;

        remoteObject.ping();

        verify(serviceToBeExported,times(1)).ping();

    }

    @Test
    public void useDeclaration() throws Exception {

        ExportDeclaration declaration=spy(getValidDeclarations().get(0));

        JSONRPCExportDeclarationWrapper pojo=JSONRPCExportDeclarationWrapper.create(declaration);

        fuchsiaDeclarationBinder.useDeclaration(declaration);
        Set<String> registeredServlets=field("registeredServlets").ofType(Set.class).in(fuchsiaDeclarationBinder).get();

        verify(declaration,times(1)).handle(fuchsiaDeclarationBinderServiceReference);
        Assert.assertEquals(1,registeredServlets.size());

    }

    @Test
    public void nameNotNull() throws Exception {
        ExportDeclaration declaration=spy(getValidDeclarations().get(0));

        JSONRPCExportDeclarationWrapper pojo=JSONRPCExportDeclarationWrapper.create(declaration);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        Assert.assertNotNull(fuchsiaDeclarationBinder.getName());
    }

    @Test
    public void gracefulStop() throws Exception {
        ExportDeclaration declaration=spy(getValidDeclarations().get(0));

        JSONRPCExportDeclarationWrapper pojo=JSONRPCExportDeclarationWrapper.create(declaration);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        fuchsiaDeclarationBinder.stop();

        Set<String> registeredServlets=field("registeredServlets").ofType(Set.class).in(fuchsiaDeclarationBinder).get();

        Assert.assertEquals(0,registeredServlets.size());

        verify(http,times(1)).unregister(anyString());

    }

    @Test
    public void denyDeclaration() throws Exception {

        ExportDeclaration declaration=spy(getValidDeclarations().get(0));

        JSONRPCExportDeclarationWrapper pojo=JSONRPCExportDeclarationWrapper.create(declaration);

        fuchsiaDeclarationBinder.useDeclaration(declaration);

        Set<String> registeredServlets=field("registeredServlets").ofType(Set.class).in(fuchsiaDeclarationBinder).get();

        verify(declaration,times(1)).handle(fuchsiaDeclarationBinderServiceReference);
        Assert.assertEquals(1,registeredServlets.size());

        fuchsiaDeclarationBinder.denyDeclaration(declaration);

        verify(declaration,times(1)).unhandle(fuchsiaDeclarationBinderServiceReference);
        Assert.assertEquals(0,registeredServlets.size());


    }

    @Override
    public List<ExportDeclaration> getInvalidDeclarations() {
        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("id","id");
        //metadata.put("fuchsia.export.jsonrpc.class",Ping.class.getName());
        metadata.put("fuchsia.export.jsonrpc.url.context","/test");
        metadata.put("fuchsia.export.jsonrpc.instance","instanceName");

        final ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();

        return new ArrayList<ExportDeclaration>(){{add(declaration);}};
    }

    @Override
    public List<ExportDeclaration> getValidDeclarations() {

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("id","id");
        metadata.put("fuchsia.export.jsonrpc.class",ServiceForExportation.class.getName());
        metadata.put("fuchsia.export.jsonrpc.url.context","/test");
        metadata.put("fuchsia.export.jsonrpc.instance","instanceName");

        final ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();
        declaration.bind(fuchsiaDeclarationBinderServiceReference);

        return new ArrayList<ExportDeclaration>(){{add(declaration);}};
    }

}
