package org.ow2.chameleon.fuchsia.core;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component(name = "FuchsiaMediatorFactory", publicFactory = false)
@Instantiate(name = "FuchsiaMediator")
@Provides(specifications = FuchsiaMediator.class)
public class FuchsiaMediatorImpl implements FuchsiaMediator {

    @ServiceProperty(name = "instance.name")
    private String name;

    @Requires(optional = false, filter = "(factory.name=FuchsiaDefaultLinkerFactory)")
    private Factory defaultLinkerFactory;

    @Requires(optional = true, specification = "org.ow2.chameleon.fuchsia.core.Linker")
    private Set<Linker> linkers;

    @Requires(optional = true, specification = "org.ow2.chameleon.fuchsia.core.component.ImporterService")
    private Set<ImporterService> importerServices;

    @Requires(optional = true, specification = "org.ow2.chameleon.fuchsia.core.component.DiscoveryService")
    private Set<DiscoveryService> discoveryServices;

    private final Map<String, ComponentInstance> linkerComponentInstances;

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public FuchsiaMediatorImpl() {
        linkerComponentInstances = new HashMap<String, ComponentInstance>();
    }


    @Validate
    public void start() {
        logger.debug(name + " starting");
    }

    @Invalidate
    public void stop() {
        for (ComponentInstance ci : linkerComponentInstances.values()) {
            ci.dispose();
        }
        linkers.clear();
        logger.debug(name + " stopping");
    }

    public Set<Linker> getLinkers() {
        return linkers;
    }

    public Set<ImporterService> getImporterServices() {
        return importerServices;
    }

    public Set<DiscoveryService> getDiscoveryServices() {
        return discoveryServices;
    }

    public String getHost() {
        // TODO
        return null;
    }

    public Map<String, Object> getProperties() {
        // TODO
        return null;
    }

    public LinkerBuilder createLinker(String name) {
        return new LinkerBuilder(defaultLinkerFactory, this, name);
    }

    public LinkerUpdater updateLinker(String name) {
        ComponentInstance ci = linkerComponentInstances.get(name);
        if (ci == null) {
            throw new IllegalArgumentException("There's no Linker named " + name + ".");
        }
        return new LinkerUpdater(linkerComponentInstances.get(name));
    }

    public void destroyLinker(String name) {
        ComponentInstance ci = linkerComponentInstances.remove(name);
        ci.dispose();
    }

    public void addLinker(String name, ComponentInstance componentInstance) {
        linkerComponentInstances.put(name, componentInstance);
    }
}
