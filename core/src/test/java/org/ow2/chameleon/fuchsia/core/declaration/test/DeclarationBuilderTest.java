package org.ow2.chameleon.fuchsia.core.declaration.test;

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


import org.junit.Test;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DeclarationBuilderTest {
    @Test
    public void testBuildEmptyImport() {
        ImportDeclaration id = ImportDeclarationBuilder.empty()
                .key("md").value("value")
                .key("md2").value("value2")
                .build();

        assertThat(id).isNotNull();
        assertThat(id.getMetadata()).containsEntry("md", "value");
        assertThat(id.getMetadata()).containsEntry("md2", "value2");
        assertThat(id.getMetadata()).hasSize(2);
    }

    @Test
    public void testBuildFromMetadataImport() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        assertThat(id).isNotNull();
        assertThat(id.getMetadata()).containsEntry("md", "value");
        assertThat(id.getMetadata()).hasSize(1);
    }

    @Test
    public void testBuildFromADeclarationImport() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        ImportDeclaration id2 = ImportDeclarationBuilder.fromImportDeclaration(id).build();

        assertThat(id2).isNotNull();
        assertThat(id2.getMetadata()).containsEntry("md", "value");
        assertThat(id2.getMetadata()).hasSize(1);
    }

    @Test
    public void testBuildEmptyExport() {
        ExportDeclaration id = ExportDeclarationBuilder.empty()
                .key("md").value("value")
                .build();

        assertThat(id).isNotNull();
        assertThat(id.getMetadata()).containsEntry("md", "value");
        assertThat(id.getMetadata()).hasSize(1);
    }

    @Test
    public void testBuildFromMetadataExport() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        assertThat(id).isNotNull();
        assertThat(id.getMetadata()).containsEntry("md", "value");
        assertThat(id.getMetadata()).hasSize(1);
    }

    @Test
    public void testBuildFromADeclarationExport() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        ExportDeclaration id2 = ExportDeclarationBuilder.fromExportDeclaration(id).build();

        assertThat(id2).isNotNull();
        assertThat(id2.getMetadata()).containsEntry("md", "value");
        assertThat(id2.getMetadata()).hasSize(1);
    }

    @Test
    public void testBuildFromADeclarationWithExtraMetadata() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        Map<String, Object> emd = new HashMap<String, Object>();
        emd.put("emd", "value2");
        ImportDeclaration id2 = ImportDeclarationBuilder.fromImportDeclaration(id).withExtraMetadata(emd).build();

        assertThat(id2).isNotNull();
        assertThat(id2.getMetadata()).containsEntry("md", "value");
        assertThat(id2.getMetadata()).hasSize(1);

        assertThat(id2.getExtraMetadata()).containsEntry("emd", "value2");
        assertThat(id2.getExtraMetadata()).hasSize(1);
    }

    @Test
    public void testBuildFromADeclarationAddingExtraMetadata() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        ImportDeclaration id2 = ImportDeclarationBuilder.fromImportDeclaration(id).extraKey("emd").value("value2").build();

        assertThat(id2).isNotNull();
        assertThat(id2.getMetadata()).containsEntry("md", "value");
        assertThat(id2.getMetadata()).hasSize(1);

        assertThat(id2.getExtraMetadata()).containsEntry("emd", "value2");
        assertThat(id2.getExtraMetadata()).hasSize(1);
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongBuild1() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        ImportDeclaration id2 = ImportDeclarationBuilder.fromImportDeclaration(id).key("must").value("fail").build();
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongBuild2() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).extraKey("must").value("fail").build();
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongBuild3() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).withExtraMetadata(md).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongBuild4() {
        ImportDeclaration id = ImportDeclarationBuilder.empty().build();
    }
}
