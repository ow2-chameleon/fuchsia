package org.ow2.chameleon.fuchsia.core;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.Map;
import java.util.Set;

@Component(name = "FuchsiaCentralFactory", publicFactory = false)
@Instantiate(name = "FuchsiaCentral")
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
     * @return The ImporterService linked to this FuchsiaMediator
     */
    Set<ImporterService> getLinkers();

    /**
     * @return This FuchsiaMediator host.
     */
    String getHost();

    /**
     * @return This FuchsiaMediator properties.
     */
    Map<String, Object> getProperties();

}
