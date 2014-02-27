package org.ow2.chameleon.fuchsia.core.component;

import org.ow2.chameleon.fuchsia.core.component.manager.DeclarationBinder;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

public interface ExporterService extends DeclarationBinder<ExportDeclaration> {
    String getName();
}
