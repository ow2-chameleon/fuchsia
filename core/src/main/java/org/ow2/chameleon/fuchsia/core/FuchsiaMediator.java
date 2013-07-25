package org.ow2.chameleon.fuchsia.core;

import org.ow2.chameleon.fuchsia.core.component.DiscoveryService;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.Map;
import java.util.Set;

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
     * @return The Linkers created the the FuchsiaMediator
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

}
