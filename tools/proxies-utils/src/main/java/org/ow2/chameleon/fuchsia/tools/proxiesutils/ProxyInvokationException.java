package org.ow2.chameleon.fuchsia.tools.proxiesutils;

public class ProxyInvokationException extends Exception{

    public ProxyInvokationException() {
        super();
    }

    public ProxyInvokationException(String message) {
        super(message);
    }

    public ProxyInvokationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyInvokationException(Throwable cause) {
        super(cause);
    }

    protected ProxyInvokationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
