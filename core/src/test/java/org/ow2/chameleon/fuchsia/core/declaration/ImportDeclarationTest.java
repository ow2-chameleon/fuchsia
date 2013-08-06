package org.ow2.chameleon.fuchsia.core.declaration;


import org.junit.Test;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;

public class ImportDeclarationTest {

    @Test
    public void testBuildSimple() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.create().withMetadata(md).build();

        assertThat(id).isNotNull();
        assertThat(id.getMetadata().get("md")).isEqualTo("value");
        assertThat(id.getMetadata().size()).isEqualTo(1);
    }

    @Test
    public void testBuildFromAnImportDeclaration() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.create().withMetadata(md).build();

        ImportDeclaration id2 = ImportDeclarationBuilder.create().from(id).build();

        assertThat(id2).isNotNull();
        assertThat(id2.getMetadata().get("md")).isEqualTo("value");
        assertThat(id2.getMetadata().size()).isEqualTo(1);
    }

    @Test
    public void testBuildFromAnImportDeclarationWithExtraMetadata() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.create().withMetadata(md).build();

        Map<String, Object> emd = new HashMap<String, Object>();
        emd.put("emd", "value2");
        ImportDeclaration id2 = ImportDeclarationBuilder.create().from(id).withExtraMetadata(emd).build();

        assertThat(id2).isNotNull();
        assertThat(id2.getMetadata().get("md")).isEqualTo("value");
        assertThat(id2.getMetadata().size()).isEqualTo(1);

        assertThat(id2.getExtraMetadata().get("emd")).isEqualTo("value2");
        assertThat(id2.getExtraMetadata().size()).isEqualTo(1);
    }

    @Test
    public void testImmutability() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.create().withMetadata(md).build();

        Map<String, Object> emd = new HashMap<String, Object>();
        emd.put("emd", "value2");
        ImportDeclaration id2 = ImportDeclarationBuilder.create().from(id).withExtraMetadata(emd).build();

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
        ImportDeclaration id = ImportDeclarationBuilder.create().withMetadata(md).build();

        Map<Integer, ImporterService> mocks = new HashMap<Integer, ImporterService>();
        List<Status> statusList = new ArrayList<Status>();

        mocks.put(0, mock(ImporterService.class));
        mocks.put(1, mock(ImporterService.class));

        // 1
        statusList.add(id.getStatus());

        id.bind(mocks.get(0));
        // 2
        statusList.add(id.getStatus());

        id.bind(mocks.get(1));
        // 3
        statusList.add(id.getStatus());

        id.unbind(mocks.get(0));
        // 4
        statusList.add(id.getStatus());

        id.unbind(mocks.get(1));
        // 5
        statusList.add(id.getStatus());

        assertThat(statusList.remove(0).getImporterServices().size()).isEqualTo(0);
        assertThat(statusList.remove(0).getImporterServices().size()).isEqualTo(1);
        assertThat(statusList.remove(0).getImporterServices().size()).isEqualTo(2);
        assertThat(statusList.remove(0).getImporterServices().size()).isEqualTo(1);
        assertThat(statusList.remove(0).getImporterServices().size()).isEqualTo(0);
    }


}
