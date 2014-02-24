package org.ow2.chameleon.fuchsia.push.base.hub.exception;


public class SubscriptionOriginVerificationException extends Exception {

    public SubscriptionOriginVerificationException() {
        super();
    }

    public SubscriptionOriginVerificationException(String message) {
        super(message);
    }

    public SubscriptionOriginVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriptionOriginVerificationException(Throwable cause) {
        super(cause);
    }
}
