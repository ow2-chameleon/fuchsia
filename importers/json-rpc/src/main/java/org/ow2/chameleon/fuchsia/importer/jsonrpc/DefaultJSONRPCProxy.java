package org.ow2.chameleon.fuchsia.importer.jsonrpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;
import org.ow2.chameleon.fuchsia.tools.proxiesutils.FuchsiaProxy;
import org.ow2.chameleon.fuchsia.tools.proxiesutils.ProxyFacetInvokable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Map proxy_metadata;

    @ServiceProperty(name = "name", immutable = false, mandatory = true)
    private String name;

    @ServiceProperty(name = "client", immutable = false, mandatory = true)
    private JsonRpcHttpClient client;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public Object invoke(String method, Object... args) {
        try {
            return client.invoke(method, args, Object.class);
        } catch (Throwable throwable) {
            logger.error("Invokation throw an exception",throwable);
        }
        return null;
    }

    public void invoke(String method, Integer transactionID, Object callback, Object... args) {
        throw new InvalidParameterException();
    }
}
