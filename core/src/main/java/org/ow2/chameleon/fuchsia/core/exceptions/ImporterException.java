package org.ow2.chameleon.fuchsia.core.exceptions;

public class ImporterException extends Exception {

    public ImporterException(Throwable cause) {
        super(cause);
    }

    public ImporterException(String message) {
        super(message);
    }

    public ImporterException(String message, Throwable e) {
        super(message, e);
    }
}
