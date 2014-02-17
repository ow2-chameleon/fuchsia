package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import java.util.List;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

/**
 * The components providing this service are capable of creating a proxy thanks to an {@link ImportDeclaration}.
 *
 * @author barjo
 * @author Morgan Martinet
 */
public interface ImporterService {

    String TARGET_FILTER_PROPERTY = "target";

    /**
     * Reify the importDeclaration of given description as a local service.
     *
     * @param importDeclaration The {@link ImportDeclaration} of the service to be imported.
     */
    void addImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException;

    /**
     * Stop the reification of the given importDeclaration
     *
     * @param importDeclaration The {@link ImportDeclaration} of the service to stop to be imported.
     */
    void removeImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException;

    String getName();

}
