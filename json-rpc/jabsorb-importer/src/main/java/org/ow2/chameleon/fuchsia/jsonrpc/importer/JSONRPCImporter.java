package org.ow2.chameleon.fuchsia.jsonrpc.importer;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.jabsorb.client.Client;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;
import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.*;

/**
 * Provides an {@link ImporterService} allowing to access a
 * remote endpoint through jsonrpc thanks to the jabsorb implementation.
 * <p/>
 * A valid {@link ImportDeclaration} for this ImporterService contains has metadata :
 * - ID : a unique String which is the id of the JSON-RPC service
 * - URL : a String containing the URL of the JSON-RPC service to import into OSGi.
 * - SERVICE_CLASS : a String containing the name of the class to use to build the proxy
 * <p/>
 * TODO : Improves the client management, only one client should be created for a given uri.
 */
@Component(name = "Fuchsia-Importer:JSON-RPC")
@Provides(specifications = {ImporterService.class})
public class JSONRPCImporter extends AbstractImporterComponent {

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BundleContext context;

    /**
     * Map which contains the proxies and theirs Client.
     */
    private final Map<String, Client> proxies = new HashMap<String, Client>();
    private final Map<String, ServiceRegistration> registrations = new HashMap<String, ServiceRegistration>();

    public JSONRPCImporter(BundleContext pContext) {
        context = pContext;
    }

    @Override
    public void createProxy(ImportDeclaration importDeclaration) {
        final Object proxy;
        final Client client;

        // Get the URI
        String uri = valueOf(importDeclaration.getMetadata().get(URL));

        // Get an id
        String id = (String) importDeclaration.getMetadata().get(ID);

        //Try to load the class
        String klassName = (String) importDeclaration.getMetadata().get(SERVICE_CLASS);
        if (klassName == null) {
            throw new IllegalArgumentException("The property" + SERVICE_CLASS + "must be set and contain a valid class name");
        }
        final Class<?> klass;
        try {
            klass = FuchsiaUtils.loadClass(context, klassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Cannot create a proxy for the ImportDeclaration : " + importDeclaration
                            + " unable to find a bundle which export the service class.", e);
        }

        try {
            ImporterHTTPSession session = new ImporterHTTPSession(new URI(uri));
            client = new ImporterClient(session);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The property" + URL + "must be set and a valid String form of the endpoint URL", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("CATAPOSTROPHE !", e);
        }

        // Create the proxy thanks to jabsorb
        proxy = client.openProxy(id, klass);

        // Add the proxy to the proxy list
        proxies.put(id, client);

        // TODO : which properties to publish on the proxy?
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        ServiceRegistration sReg = context.registerService(klassName, proxy, props);

        // Add the registration to the registration list
        registrations.put(id, sReg);
    }

    @Override
    public void destroyProxy(ImportDeclaration importDeclaration) {
        String id = (String) importDeclaration.getMetadata().get(ID);
        if (proxies.containsKey(id)) {
            // Unregister the proxy
            ServiceRegistration sReg = registrations.remove(id);
            sReg.unregister();

            // Close the proxy
            Client client = proxies.remove(id);
            client.closeProxy(id);
        } else {
            throw new IllegalArgumentException("The given object has not been created through this factory");
        }
    }

    protected Logger getLogger() {
        return logger;
    }

    public List<String> getConfigPrefix() {
        return null;
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
