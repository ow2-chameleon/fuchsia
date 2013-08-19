package org.ow2.chameleon.fuchsia.core.declaration;

import java.util.Map;

public class ImportDeclarationBuilder extends DeclarationBuilder<ImportDeclarationBuilder, ImportDeclaration> {
    private ImportDeclarationBuilder() {
        super();
    }

    private ImportDeclarationBuilder(Map<String, Object> metadata) {
        super(metadata);
    }

    private ImportDeclarationBuilder(ImportDeclaration importDeclaration) {
        super(importDeclaration);
    }

    public static ImportDeclarationBuilder empty() {
        return new ImportDeclarationBuilder();
    }

    public static ImportDeclarationBuilder fromMetadata(Map<String, Object> metadata) {
        return new ImportDeclarationBuilder(metadata);
    }

    public static ImportDeclarationBuilder fromImportDeclaration(ImportDeclaration importDeclaration) {
        return new ImportDeclarationBuilder(importDeclaration);
    }
}
