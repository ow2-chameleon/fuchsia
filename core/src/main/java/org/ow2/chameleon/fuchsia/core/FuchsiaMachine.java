package org.ow2.chameleon.fuchsia.core;

import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.Map;
import java.util.Set;

// DO NOT USE
public interface FuchsiaMachine {
    /**
     * System property identifying the host name for this fuchsia machine.
     */
    final static String FUCHSIA_MACHINE_HOST = "host";

    /**
     * TimeStamp
     */
    final static String FUCHSIA_MACHINE_DATE = "date";

    enum EndpointListenerInterest {
        LOCAL, REMOTE, ALL
    }

    /**
     * @return The ImporterService linked to this FuchsiaMachine
     */
    Set<ImporterService> getImporters();

    /**
     * @return This fuchsia machine host.
     */
    String getHost();

    /**
     * @return This Fuchsia machine properties.
     */
    Map<String, Object> getProperties();

}
