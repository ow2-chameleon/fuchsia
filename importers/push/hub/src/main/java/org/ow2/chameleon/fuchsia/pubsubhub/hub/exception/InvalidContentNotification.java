package org.ow2.chameleon.fuchsia.pubsubhub.hub.exception;


public class InvalidContentNotification extends Exception {

    public InvalidContentNotification() {
        super();
    }

    public InvalidContentNotification(String message) {
        super(message);
    }

    public InvalidContentNotification(String message, Throwable cause) {
        super(message, cause);
    }
}
