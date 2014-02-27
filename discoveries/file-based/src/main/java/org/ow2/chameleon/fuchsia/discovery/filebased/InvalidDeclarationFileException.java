package org.ow2.chameleon.fuchsia.discovery.filebased;

public class InvalidDeclarationFileException extends Exception {
    public InvalidDeclarationFileException() {
        super();
    }

    public InvalidDeclarationFileException(String message) {
        super(message);
    }

    public InvalidDeclarationFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDeclarationFileException(Throwable cause) {
        super(cause);
    }
}
