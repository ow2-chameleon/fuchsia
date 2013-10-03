package org.ow2.chameleon.fuchsia.push.subscriber;

import com.sun.syndication.feed.synd.SyndFeed;
import org.ow2.chameleon.fuchsia.pubsubhub.hub.dto.SubscriptionConfirmationRequest;
import org.ow2.chameleon.fuchsia.pubsubhub.hub.exception.InvalidContentNotification;
import org.ow2.chameleon.fuchsia.push.subscriber.exception.ActionNotRequestedByTheSubscriberException;

public interface SubscriberInput {

    void TopicUpdated(String hubtopic, SyndFeed feed);

    void ConfirmSubscriberRequestedSubscription(SubscriptionConfirmationRequest cr) throws InvalidContentNotification, ActionNotRequestedByTheSubscriberException;

}
