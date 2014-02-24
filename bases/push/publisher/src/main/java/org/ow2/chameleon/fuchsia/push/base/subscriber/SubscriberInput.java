package org.ow2.chameleon.fuchsia.push.base.subscriber;

import com.sun.syndication.feed.synd.SyndFeed;
import org.ow2.chameleon.fuchsia.push.base.hub.dto.SubscriptionConfirmationRequest;
import org.ow2.chameleon.fuchsia.push.base.hub.exception.InvalidContentNotification;
import org.ow2.chameleon.fuchsia.push.base.subscriber.exception.ActionNotRequestedByTheSubscriberException;

public interface SubscriberInput {

    void topicUpdated(String hubtopic, SyndFeed feed);

    void confirmSubscriberRequestedSubscription(SubscriptionConfirmationRequest cr) throws InvalidContentNotification, ActionNotRequestedByTheSubscriberException;

}
