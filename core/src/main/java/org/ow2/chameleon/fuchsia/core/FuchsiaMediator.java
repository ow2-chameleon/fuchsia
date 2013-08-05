package org.ow2.chameleon.fuchsia.core;

import org.apache.felix.ipojo.ComponentInstance;
import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.Map;
import java.util.Set;


/**
 * Work in progress.
 * <p/>
 * Does this interface should be split into multiples interfaces more concept centered
 * (Administration, introspection, configuration) ?
 */
public interface FuchsiaMediator {
    /**
     * System property identifying the host name for this FuchsiaMediator.
     */
    final static String FUCHSIA_MEDIATOR_HOST = "host";

    /**
     * TimeStamp
     */
    final static String FUCHSIA_MEDIATOR_DATE = "date";

    enum EndpointListenerInterest {
        LOCAL, REMOTE, ALL
    }

    /**
     * @return The Linkers services on the platform
     */
    Set<Linker> getLinkers();

    /**
     * @return The ImporterServices services on the platform
     */
    Set<ImporterService> getImporterServices();

    /**
     * @return The DiscoveryService services on the platform
     */
    Set<DiscoveryService> getDiscoveryServices();

    /**
     * @return This FuchsiaMediator host.
     */
    String getHost();

    /**
     * @return This FuchsiaMediator properties.
     */
    Map<String, Object> getProperties();


    /**
     * Initiate the build of a Linker named name
     *
     * @param name the name of the new Linker
     * @return a LinkerBuilder to configure and build the linker
     */
    public LinkerBuilder createLinker(String name);

    /**
     *  Initiate the reconfiguration of the Linker named name
     *
     * @param name the name of the Linker to update
     * @return a LinkerUpdater to reconfigure the linker
     */
    public LinkerUpdater updateLinker(String name);

    /**
     * Destroy the linker named name
     *
     * @param name the name of the Linker that must be destroyed
     */
    public void destroyLinker(String name);

    /**
     * Add the ComponentInstance of the linker named name to the FuchsiaMediator
     *
     * @param name the name of the Linker
     * @param componentInstance the ComponentInstance of the Linker
     */
    void addLinker(String name, ComponentInstance componentInstance);


}
