package org.ow2.chameleon.fuchsia.core.exceptions;

import org.osgi.framework.InvalidSyntaxException;

public class InvalidFilterException extends Exception {


    public InvalidFilterException(String message, InvalidSyntaxException e) {
        super(message, e);
    }

    public InvalidFilterException(String s) {
        super(s);
    }
}
