package org.ow2.chameleon.fuchsia.push.base.subscriber.exception;

public class HubDiscoveryException extends Exception {
    public HubDiscoveryException() {
        super();
    }

    public HubDiscoveryException(String message) {
        super(message);
    }

    public HubDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public HubDiscoveryException(Throwable cause) {
        super(cause);
    }
}
