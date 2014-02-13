package org.ow2.chameleon.fuchsia.core.declaration.test;


import org.junit.Test;
import org.ow2.chameleon.fuchsia.core.declaration.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DeclarationBuilderTest {
    @Test
    public void testBuildEmptyImport() {
        ImportDeclaration id = ImportDeclarationBuilder.empty()
                .key("md").value("value")
                .build();

        assertThat(id).isNotNull();
        assertThat(id.getMetadata()).containsEntry("md", "value");
        assertThat(id.getMetadata()).hasSize(1);
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
    public void testWrongBuild1(){
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        ImportDeclaration id2 = ImportDeclarationBuilder.fromImportDeclaration(id).key("must").value("fail").build();
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongBuild2(){
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).extraKey("must").value("fail").build();
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongBuild3(){
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).withExtraMetadata(md).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongBuild4(){
        ImportDeclaration id = ImportDeclarationBuilder.empty().build();
    }
}
