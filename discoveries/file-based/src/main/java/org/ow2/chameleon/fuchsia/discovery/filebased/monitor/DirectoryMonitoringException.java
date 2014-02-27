package org.ow2.chameleon.fuchsia.discovery.filebased.monitor;

public class DirectoryMonitoringException extends Exception {
    public DirectoryMonitoringException() {
        super();
    }

    public DirectoryMonitoringException(String message) {
        super(message);
    }

    public DirectoryMonitoringException(String message, Throwable cause) {
        super(message, cause);
    }

    public DirectoryMonitoringException(Throwable cause) {
        super(cause);
    }
}
