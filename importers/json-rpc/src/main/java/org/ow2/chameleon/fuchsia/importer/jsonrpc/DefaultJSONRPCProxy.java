package org.ow2.chameleon.fuchsia.importer.jsonrpc;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer JSON-RPC
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

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.ow2.chameleon.fuchsia.tools.proxiesutils.FuchsiaProxy;
import org.ow2.chameleon.fuchsia.tools.proxiesutils.ProxyFacetInvokable;
import org.ow2.chameleon.fuchsia.tools.proxiesutils.ProxyInvokationException;

import java.util.Map;


@Component
@Provides(
        specifications = {FuchsiaProxy.class, ProxyFacetInvokable.class},
        properties = {
                @StaticServiceProperty(name = "protocol", type = "java.lang.String", value = "json-rpc", immutable = true, mandatory = true),
        })
public class DefaultJSONRPCProxy implements ProxyFacetInvokable {

    @ServiceProperty(name = "metadata", immutable = false, mandatory = true)
    private Map proxyMetadata;

    @ServiceProperty(name = "name", immutable = false, mandatory = true)
    private String name;

    @ServiceProperty(name = "client", immutable = false, mandatory = true)
    private JsonRpcHttpClient client;

    public Object invoke(String method, Object... args) throws ProxyInvokationException {
        try {
            return client.invoke(method, args, Object.class);
        } catch (Throwable throwable) {
            throw new ProxyInvokationException("Invoke method throw a Throwable", throwable);
        }
    }

    public void invoke(String method, Integer transactionID, Object callback, Object... args) {
        throw new UnsupportedOperationException("Assynchronous invocation not implemented");
    }
}
