package org.ow2.chameleon.fuchsia.pubsubhub.hub;

import org.ow2.chameleon.fuchsia.pubsubhub.hub.dto.ContentNotification;
import org.ow2.chameleon.fuchsia.pubsubhub.hub.dto.SubscriptionRequest;
import org.ow2.chameleon.fuchsia.pubsubhub.hub.exception.SubscriptionException;

public interface HubInput {

    public void ContentNotificationReceived(ContentNotification cn);

    public void SubscriptionRequestReceived(SubscriptionRequest sr) throws SubscriptionException;

}
