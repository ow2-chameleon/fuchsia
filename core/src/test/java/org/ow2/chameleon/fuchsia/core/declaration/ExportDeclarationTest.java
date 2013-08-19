package org.ow2.chameleon.fuchsia.core.declaration;


import org.junit.Test;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;

public class ExportDeclarationTest {

    @Test
    public void testBuildEmpty() {
        Map<String, Object> md = new HashMap<String, Object>();
        ExportDeclaration id = ExportDeclarationBuilder.empty()
                .key("md").value("value")
                .build();

        assertThat(id).isNotNull();
        assertThat(id.getMetadata().get("md")).isEqualTo("value");
        assertThat(id.getMetadata().size()).isEqualTo(1);
    }

    @Test
    public void testBuildSimple() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        assertThat(id).isNotNull();
        assertThat(id.getMetadata().get("md")).isEqualTo("value");
        assertThat(id.getMetadata().size()).isEqualTo(1);
    }

    @Test
    public void testBuildFromADeclaration() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        ExportDeclaration id2 = ExportDeclarationBuilder.fromExportDeclaration(id).build();

        assertThat(id2).isNotNull();
        assertThat(id2.getMetadata().get("md")).isEqualTo("value");
        assertThat(id2.getMetadata().size()).isEqualTo(1);
    }

    @Test
    public void testBuildFromADeclarationWithExtraMetadata() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        Map<String, Object> emd = new HashMap<String, Object>();
        emd.put("emd", "value2");
        ExportDeclaration id2 = ExportDeclarationBuilder.fromExportDeclaration(id).withExtraMetadata(emd).build();

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
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        Map<String, Object> emd = new HashMap<String, Object>();
        emd.put("emd", "value2");
        ExportDeclaration id2 = ExportDeclarationBuilder.fromExportDeclaration(id).withExtraMetadata(emd).build();

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
        ExportDeclaration id = ExportDeclarationBuilder.fromMetadata(md).build();

        Map<Integer, ServiceReference> mocks = new HashMap<Integer, ServiceReference>();
        List<Status> statusList = new ArrayList<Status>();

        mocks.put(0, mock(ServiceReference.class));
        mocks.put(1, mock(ServiceReference.class));

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

        assertThat(statusList.remove(0).getServiceReferences().size()).isEqualTo(0);
        assertThat(statusList.remove(0).getServiceReferences().size()).isEqualTo(1);
        assertThat(statusList.remove(0).getServiceReferences().size()).isEqualTo(2);
        assertThat(statusList.remove(0).getServiceReferences().size()).isEqualTo(1);
        assertThat(statusList.remove(0).getServiceReferences().size()).isEqualTo(0);
    }


}
