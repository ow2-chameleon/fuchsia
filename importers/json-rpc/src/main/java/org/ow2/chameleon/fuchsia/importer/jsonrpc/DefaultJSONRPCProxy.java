package org.ow2.chameleon.fuchsia.importer.jsonrpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.ow2.chameleon.fuchsia.tools.proxiesutils.FuchsiaProxy;
import org.ow2.chameleon.fuchsia.tools.proxiesutils.ProxyFacetInvokable;
import org.ow2.chameleon.fuchsia.tools.proxiesutils.ProxyInvokationException;

import java.security.InvalidParameterException;
import java.util.Map;


@Component
@Provides(
        specifications = FuchsiaProxy.class,
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
