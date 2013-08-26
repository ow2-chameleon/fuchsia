package org.ow2.chameleon.fuchsia.examples.jaxws.helloWS.iDecCreator;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.examples.jaxws.helloWS.HelloWorldWS;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 12/08/13
 * Time: 14:01
 * To change this template use File | Settings | File Templates.
 */
@Component
@Instantiate
public class HelloWSIDecCreator {

    private final BundleContext bundleContext;

    private ImportDeclaration importDeclaration;

    private ServiceRegistration serviceRegistration;
    private Map<String, Object> metadata;

    public HelloWSIDecCreator(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Validate
    public void start() {
        metadata = new HashMap<String, Object>();
        List<String> interfaces = new ArrayList<String>();
        interfaces.add(HelloWorldWS.class.getCanonicalName());

        metadata.put("jax-ws.importer.interfaces",interfaces);
        metadata.put("endpoint.url","http://localhost:9090/helloWS");

        importDeclaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        serviceRegistration = registerImportDeclaration(importDeclaration);
    }


    @Invalidate
    public void stop() {
        metadata = null;
        serviceRegistration.unregister();
    }

    protected ServiceRegistration registerImportDeclaration(ImportDeclaration importDeclaration) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        String clazzes[] = new String[]{ImportDeclaration.class.getName()};
        return bundleContext.registerService(clazzes, importDeclaration, props);
    }
}
