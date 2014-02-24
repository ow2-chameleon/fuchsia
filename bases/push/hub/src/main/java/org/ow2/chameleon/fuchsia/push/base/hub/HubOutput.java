package org.ow2.chameleon.fuchsia.push.base.hub;

import org.ow2.chameleon.fuchsia.push.base.hub.dto.ContentNotification;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionRequest;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionOriginVerificationException;

public interface HubOutput {

    void notifySubscriberCallback(ContentNotification cn);

    Boolean verifySubscriberRequestedSubscription(SubscriptionRequest sr) throws SubscriptionOriginVerificationException;

}
