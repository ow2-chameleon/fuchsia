package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

/**
 * The components providing this service are capable of creating a proxy thanks to an {@link ImportDeclaration}.
 *
 * @author barjo
 * @author Morgan Martinet
 */
public interface ImporterService extends DeclarationBinder<ImportDeclaration> {
    String getName();
}
