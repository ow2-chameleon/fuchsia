package org.ow2.chameleon.fuchsia.push.base.subscriber;


import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionException;

import java.util.List;

public interface SubscriberOutput {

    int subscribe(String hub, String topicUrl, String hostname, String verifyToken, String leaseSeconds) throws SubscriptionException;

    int unsubscribe(String hub, String topicUrl, String hostname, String verifyToken) throws SubscriptionException;

    List<String> getApprovedActions();

}
