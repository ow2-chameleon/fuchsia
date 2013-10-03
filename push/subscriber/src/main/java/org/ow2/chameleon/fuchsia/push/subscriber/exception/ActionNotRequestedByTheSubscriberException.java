package org.ow2.chameleon.fuchsia.push.subscriber.exception;

public class ActionNotRequestedByTheSubscriberException extends Exception{

    public ActionNotRequestedByTheSubscriberException() {
        super();
    }

    public ActionNotRequestedByTheSubscriberException(String message) {
        super(message);
    }

    public ActionNotRequestedByTheSubscriberException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionNotRequestedByTheSubscriberException(Throwable cause) {
        super(cause);
    }
}
