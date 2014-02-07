package org.ow2.chameleon.fuchsia.importer.jsonrpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;

import static java.lang.String.valueOf;
import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.*;

/**
 * Provides an {@link ImporterService} allowing to access a
 * remote endpoint through jsonrpc thanks to the jsonrpc4j implementation.
 * <p/>
 * A valid {@link ImportDeclaration} for this ImporterService contains has metadata :
 * - ID : a unique String which is the id of the JSON-RPC service
 * - URL : a String containing the URL of the JSON-RPC service to import into OSGi.
 * - SERVICE_CLASS : a String containing the name of the class to use to build the proxy
 * <p/>
 * TODO : Improves the client management, only one client should be created for a given uri.
 */
@Component()
@Provides(specifications = {ImporterService.class})
public class JSONRPCImporter extends AbstractImporterComponent {

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    @ServiceProperty(name = TARGET_FILTER_PROPERTY, value = "(&(" + PROTOCOL_NAME + "=jsonrpc)(scope=generic))")
    private String filter;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BundleContext context;

    /**
     * Map which contains the clients and theirs Client.
     */
    private final Map<String, JsonRpcHttpClient> clients = new HashMap<String, JsonRpcHttpClient>();
    private final Map<String, ServiceRegistration> registrations = new HashMap<String, ServiceRegistration>();

    public JSONRPCImporter(BundleContext pContext) {
        context = pContext;
    }

    @Override
    public void useImportDeclaration(ImportDeclaration importDeclaration) {

        final JsonRpcHttpClient client;
        final Object proxy;
        final Class<?> klass;
        final String uri, id, klassName;

        // Get the URI
        uri = valueOf(importDeclaration.getMetadata().get(URL));
        // Get an id
        id = (String) importDeclaration.getMetadata().get(ID);
        //Try to load the class
        klassName = (String) importDeclaration.getMetadata().get(SERVICE_CLASS);

        if (klassName == null) {
            throw new IllegalArgumentException("The property" + SERVICE_CLASS + "must be set and contain a valid class name");
        }
        try {

            klass = FuchsiaUtils.loadClass(context, klassName);

        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Cannot create a proxy for the ImportDeclaration : " + importDeclaration
                            + " unable to find a bundle which export the service class.", e);
        }

        try {
            client = new JsonRpcHttpClient(new java.net.URL(uri));
        } catch (MalformedURLException e) {
            logger.error("Error during connection to " + uri, e);
            return; //FIXME
        }
        clients.put(id, client);

        // create the proxy !
        proxy = ProxyUtil.createClientProxy(klass.getClassLoader(), klass, client);

        // TODO : which properties to publish on the proxy?
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        ServiceRegistration sReg = context.registerService(klassName, proxy, props);

        // Add the registration to the registration list
        registrations.put(id, sReg);
        logger.debug("JsonRPC Proxy successfully created and registered in OSGi " + uri + "");
    }

    @Override
    public void denyImportDeclaration(ImportDeclaration importDeclaration) {
        String id = (String) importDeclaration.getMetadata().get(ID);
        if (clients.containsKey(id)) {
            // Unregister the proxy from OSGi
            ServiceRegistration sReg = registrations.remove(id);
            sReg.unregister();

            // Remove the client
            clients.remove(id);
        } else {
            throw new IllegalArgumentException("The given object has not been created through this factory");
        }
    }

    protected Logger getLogger() {
        return logger;
    }

    public String getName() {
        return name;
    }


    /*------------------------------------------*
     *  Component LifeCycle method              *
     *------------------------------------------*/

    /*
     * (non-Javadoc)
     * @see org.ow2.chameleon.rose.AbstractImporterComponent#start()
     */
    @Override
    @Validate
    protected void start() {
        super.start();
    }

    /*
     * (non-Javadoc)
     * @see org.ow2.chameleon.rose.AbstractImporterComponent#stop()
     */
    @Override
    @Invalidate
    protected void stop() {
        super.stop();
    }
}
