package org.ow2.chameleon.fuchsia.core.component.manager;

import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;

public interface DeclarationBinder<T extends Declaration> {
    void useDeclaration(T declaration) throws BinderException;

    void denyDeclaration(T declaration) throws BinderException;
}
