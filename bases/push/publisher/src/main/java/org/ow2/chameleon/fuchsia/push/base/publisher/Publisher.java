package org.ow2.chameleon.fuchsia.push.base.publisher;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Instantiate
public class Publisher {

    private static final Logger LOG = LoggerFactory.getLogger(Publisher.class);

    private static final String PUBLISHER_URL = "/publisher/main";

    @Requires
    private HttpService http;

    public Publisher() {

    }

    @Validate
    public void start() {
        try {
            http.registerServlet(PUBLISHER_URL, new PublisherServlet(), null, null);
        } catch (Exception e) {
            LOG.error("Failed to publish Publisher URL", e);
        }
    }

    @Invalidate
    public void stop() {
        http.unregister(PUBLISHER_URL);
    }

}
