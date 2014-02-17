package org.ow2.chameleon.fuchsia.core.declaration.test;


import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.declaration.Status;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class DeclarationTest {

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
        when(mocks.get(0).getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(new Long(0));
        mocks.put(1, mock(ServiceReference.class));
        when(mocks.get(0).getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(new Long(1));
        mocks.put(2, mock(ServiceReference.class));
        when(mocks.get(0).getProperty(org.osgi.framework.Constants.SERVICE_ID)).thenReturn(new Long(2));
        assertThat(id.getStatus().getServiceReferencesBounded()).hasSize(0);

        id.bind(mocks.get(0));
        assertThat(id.getStatus().getServiceReferencesBounded()).hasSize(1);

        id.bind(mocks.get(1));
        assertThat(id.getStatus().getServiceReferencesBounded()).hasSize(2);

        try {
            id.getStatus().getServiceReferencesBounded().add(mocks.get(2));
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UnsupportedOperationException.class)
                    .hasNoCause();
        }
        assertThat(id.getStatus().getServiceReferencesHandled()).hasSize(0);

        id.handle(mocks.get(0));
        assertThat(id.getStatus().getServiceReferencesHandled()).hasSize(1);

        id.handle(mocks.get(1));
        assertThat(id.getStatus().getServiceReferencesHandled()).hasSize(2);

        try {
            id.getStatus().getServiceReferencesHandled().add(mocks.get(2));
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UnsupportedOperationException.class)
                    .hasNoCause();
        }
        assertThat(id.getStatus().getServiceReferencesHandled()).hasSize(2);

        id.unhandle(mocks.get(0));
        assertThat(id.getStatus().getServiceReferencesHandled()).hasSize(1);

        id.unbind(mocks.get(0));
        assertThat(id.getStatus().getServiceReferencesBounded()).hasSize(1);

        id.unhandle(mocks.get(1));
        assertThat(id.getStatus().getServiceReferencesHandled()).hasSize(0);

        id.unbind(mocks.get(1));
        assertThat(id.getStatus().getServiceReferencesBounded()).hasSize(0);
    }

    @Test
    public void testDecoratorCalls(){
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();
        ImportDeclaration spyId = spy(id);

        Map<String, Object> emd = new HashMap<String, Object>();
        emd.put("emd", "value2");
        ImportDeclaration id2 = ImportDeclarationBuilder.fromImportDeclaration(spyId).withExtraMetadata(emd).build();

        ServiceReference mock = mock(ServiceReference.class);

        id2.bind(mock);
        verify(spyId).bind(mock);

        id2.handle(mock);
        verify(spyId).handle(mock);

        Status status = id2.getStatus();
        verify(spyId).getStatus();
        assertThat(status.isBound()).isTrue();
        assertThat(status.getServiceReferencesHandled()).containsExactly(mock);
        assertThat(status.getServiceReferencesBounded()).containsExactly(mock);

        id2.unhandle(mock);
        verify(spyId).unhandle(mock);

        id2.unbind(mock);
        verify(spyId).unbind(mock);
    }

    @Test(expected = IllegalStateException.class)
    public void testHandleFail() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        id.handle(mock(ServiceReference.class));
    }

    @Test
    public void testHandle() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();
        ServiceReference mock = mock(ServiceReference.class);
        id.bind(mock);
        id.handle(mock);
    }

    @Test(expected = IllegalStateException.class)
    public void testUnbindFail() {
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();
        ServiceReference mock = mock(ServiceReference.class);
        id.bind(mock);
        id.handle(mock);
        id.unbind(mock);
    }

    @Test
    public void testToString(){
        Map<String, Object> md = new HashMap<String, Object>();
        md.put("md", "value");
        ImportDeclaration id = ImportDeclarationBuilder.fromMetadata(md).build();

        ImportDeclaration id2 = ImportDeclarationBuilder.fromImportDeclaration(id).extraKey("emd").value("value2").build();

        String output = id2.toString();
        int nbLines = output.split(System.getProperty("line.separator")).length;
        assertThat(nbLines).isEqualTo(6);
    }
}
