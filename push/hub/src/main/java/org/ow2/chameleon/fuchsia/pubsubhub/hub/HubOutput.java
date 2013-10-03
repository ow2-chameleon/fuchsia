package org.ow2.chameleon.fuchsia.pubsubhub.hub;

import org.ow2.chameleon.fuchsia.pubsubhub.hub.dto.ContentNotification;
import org.ow2.chameleon.fuchsia.pubsubhub.hub.dto.SubscriptionRequest;
import org.ow2.chameleon.fuchsia.pubsubhub.hub.exception.SubscriptionOriginVerificationException;

import java.io.IOException;
import java.net.URISyntaxException;

public interface HubOutput {

    public void NotifySubscriberCallback(ContentNotification cn);

    public Boolean VerifySubscriberRequestedSubscription(SubscriptionRequest sr) throws SubscriptionOriginVerificationException, URISyntaxException, IOException;

}
