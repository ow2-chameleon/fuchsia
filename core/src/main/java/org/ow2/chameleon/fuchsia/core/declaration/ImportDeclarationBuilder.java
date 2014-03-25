package org.ow2.chameleon.fuchsia.core.declaration;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Core
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
