package org.ow2.chameleon.fuchsia.core.it.services;

import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;

public interface SimulateBindingInterface {

    public void bind(ImportDeclaration id);

    public void unbind(ImportDeclaration id);

}