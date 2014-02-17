package org.ow2.chameleon.fuchsia.core.component.manager;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;

import java.lang.reflect.Type;
import java.util.*;

public class DeclarationRegistrationManager<T extends Declaration> {

    private final Map<T, ServiceRegistration> declarationsRegistered;
    private final BundleContext bundleContext;
    private final Class<T> klass;

    public DeclarationRegistrationManager(BundleContext bundleContext, Class<T> klass) {
        this.declarationsRegistered = new HashMap<T, ServiceRegistration>();
        this.bundleContext = bundleContext;
        this.klass = klass;
    }

    /**
     * Utility method to register an Declaration has a Service in OSGi.
     * If you use it make sure to use unregisterDeclaration(...) to unregister the Declaration
     *
     * @param declaration the Declaration to register
     */
    public void registerDeclaration(T declaration) {
        synchronized (declarationsRegistered) {
            if (declarationsRegistered.containsKey(declaration)) {
                throw new IllegalStateException("The given Declaration has already been registered.");
            }

            Dictionary<String, Object> props = new Hashtable<String, Object>();
            String[] clazzes = new String[]{klass.getName()};
            ServiceRegistration registration;
            registration = bundleContext.registerService(clazzes, declaration, props);

            declarationsRegistered.put(declaration, registration);
        }
    }

    /**
     * Utility method to unregister an Declaration of OSGi.
     * Use it only if you have used registerDeclaration(...) to register the Declaration
     *
     * @param declaration the Declaration to unregister
     */
    public void unregisterDeclaration(T declaration) {
        ServiceRegistration registration;
        synchronized (declarationsRegistered) {
            registration = declarationsRegistered.remove(declaration);
            if (registration == null) {
                throw new IllegalStateException("The given Declaration has never been registered"
                        + "or have already been unregistered.");
            }
        }
        registration.unregister();
    }

    public void unregisterAll(){
        synchronized (declarationsRegistered) {
            for (ServiceRegistration registration : declarationsRegistered.values()) {
                if (registration != null) {
                    registration.unregister();
                }
            }
            declarationsRegistered.clear();
        }
    }

    public Set<T> getDeclarations() {
        return new HashSet<T>(declarationsRegistered.keySet());
    }

}
