package org.ow2.chameleon.fuchsia.importer.jsonrpc.test;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.chameleon.fuchsia.importer.jsonrpc.DefaultJSONRPCProxy;
import org.ow2.chameleon.fuchsia.tools.proxiesutils.ProxyInvokationException;

import java.util.Objects;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test DefaultProxy
 */
public class DefaultJSONRPCProxyTest {

    DefaultJSONRPCProxy proxy;

    @Mock
    JsonRpcHttpClient client;

    @Before
    public void init(){

        MockitoAnnotations.initMocks(this);
        proxy=spy(constructor().in(DefaultJSONRPCProxy.class).newInstance());
        field("client").ofType(JsonRpcHttpClient.class).in(proxy).set(client);

    }

    @Test
    public void invokeSync() throws Throwable {

        String methodInvoked="myMethod";
        proxy.invoke(methodInvoked,"Object");
        verify(client,times(1)).invoke(eq(methodInvoked),any(Object.class), eq(Object.class));

    }

    @Test(expected = UnsupportedOperationException.class)
    public void invokeASync() throws Throwable {

        String methodInvoked="myMethod";
        Integer transaction=1;
        Object object="Object";
        proxy.invoke(methodInvoked,transaction,object);

    }

}
