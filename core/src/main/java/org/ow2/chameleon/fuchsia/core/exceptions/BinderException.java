package org.ow2.chameleon.fuchsia.core.exceptions;

public class BinderException extends Exception {

    public BinderException(Throwable cause) {
        super(cause);
    }

    public BinderException(String message) {
        super(message);
    }

    public BinderException(String message, Throwable e) {
        super(message, e);
    }
}
