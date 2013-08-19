package org.ow2.chameleon.fuchsia.core.declaration;

import java.util.Map;

public class ExportDeclarationBuilder extends DeclarationBuilder<ExportDeclarationBuilder, ExportDeclaration> {
    private ExportDeclarationBuilder() {
        super();
    }

    private ExportDeclarationBuilder(Map<String, Object> metadata) {
        super(metadata);
    }

    private ExportDeclarationBuilder(ExportDeclaration exportDeclaration) {
        super(exportDeclaration);
    }

    public static ExportDeclarationBuilder empty() {
        return new ExportDeclarationBuilder();
    }

    public static ExportDeclarationBuilder fromMetadata(Map<String, Object> metadata) {
        return new ExportDeclarationBuilder(metadata);
    }

    public static ExportDeclarationBuilder fromExportDeclaration(ExportDeclaration exportDeclaration) {
        return new ExportDeclarationBuilder(exportDeclaration);
    }
}
