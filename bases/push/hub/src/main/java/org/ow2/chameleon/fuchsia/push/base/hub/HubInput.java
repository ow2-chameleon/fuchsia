package org.ow2.chameleon.fuchsia.push.base.hub;

import org.ow2.chameleon.fuchsia.push.base.hub.dto.ContentNotification;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionRequest;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionException;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionRequest;

public interface HubInput {

    public void ContentNotificationReceived(ContentNotification cn);

    public void SubscriptionRequestReceived(SubscriptionRequest sr) throws SubscriptionException;

}
