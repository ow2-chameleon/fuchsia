package org.ow2.chameleon.fuchsia.push.base.hub;

import org.ow2.chameleon.fuchsia.push.base.hub.dto.ContentNotification;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionRequest;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionException;

public interface HubInput {

    void contentNotificationReceived(ContentNotification cn);

    void subscriptionRequestReceived(SubscriptionRequest sr) throws SubscriptionException;

}
