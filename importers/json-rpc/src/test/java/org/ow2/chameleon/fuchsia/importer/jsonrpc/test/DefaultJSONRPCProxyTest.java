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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.chameleon.fuchsia.importer.jsonrpc.DefaultJSONRPCProxy;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test DefaultProxy.
 */
public class DefaultJSONRPCProxyTest {

    DefaultJSONRPCProxy proxy;

    @Mock
    JsonRpcHttpClient client;

    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);
        proxy = spy(constructor().in(DefaultJSONRPCProxy.class).newInstance());
        field("client").ofType(JsonRpcHttpClient.class).in(proxy).set(client);

    }

    @Test
    public void invokeSync() throws Throwable {

        String methodInvoked = "myMethod";
        proxy.invoke(methodInvoked, "Object");
        verify(client, times(1)).invoke(eq(methodInvoked), any(Object.class), eq(Object.class));

    }

    @Test(expected = UnsupportedOperationException.class)
    public void invokeASync() throws Throwable {

        String methodInvoked = "myMethod";
        Integer transaction = 1;
        Object object = "Object";
        proxy.invoke(methodInvoked, transaction, object);

    }

}
