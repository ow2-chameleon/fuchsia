package org.ow2.chameleon.fuchsia.core.declaration.test;


import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;

public class ImportDeclarationTest {

    @Test
    public void testBuildEmpty() {
        ImportDeclaration id = ImportDeclarationBuilder.empty()
                .key("md").value("value")
                .build();

        assertThat(id).isNotNull();
        assertThat(id.getMetadata()).containsEntry("md", "value");
        assertThat(id.getMetadata()).hasSize(1);
    }

    @Test
    public void testBuildSimple() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        assertThat(id).isNotNull();
        assertThat(id.getMetadata()).containsEntry("md", "value");
        assertThat(id.getMetadata()).hasSize(1);
    }

    @Test
    public void testBuildFromADeclaration() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        ImportDeclaration id2 = ImportDeclarationBuilder.fromImportDeclaration(id).build();

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


    @Test
    public void testImmutability() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        Map<String, Object> emd = new HashMap<String, Object>();
        emd.put("emd", "value2");
        ImportDeclaration id2 = ImportDeclarationBuilder.fromImportDeclaration(id).withExtraMetadata(emd).build();

        try {
            id2.getMetadata().put("new_key", "new_value");
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage(null)
                    .hasNoCause();
        }

        try {
            id2.getExtraMetadata().put("new_key", "new_value");
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage(null)
                    .hasNoCause();
        }
    }

    @Test
    public void testDependencies() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        Map<Integer, ServiceReference> mocks = new HashMap<Integer, ServiceReference>();

        mocks.put(0, mock(ServiceReference.class));
        mocks.put(1, mock(ServiceReference.class));
        assertThat(id.getStatus().getServiceReferences()).hasSize(0);

        id.bind(mocks.get(0));
        assertThat(id.getStatus().getServiceReferences()).hasSize(1);

        id.bind(mocks.get(1));
        assertThat(id.getStatus().getServiceReferences()).hasSize(2);

        id.unbind(mocks.get(0));
        assertThat(id.getStatus().getServiceReferences()).hasSize(1);

        id.unbind(mocks.get(1));
        assertThat(id.getStatus().getServiceReferences()).hasSize(0);
    }


}
