package org.ow2.chameleon.fuchsia.exporter.jaxws.test;

import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.exporter.jaxws.test.base.JAXExporterAbstractTest;
import org.ow2.chameleon.fuchsia.exporter.jaxws.test.ctd.ServiceForExportation;

import javax.servlet.ServletException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;

public class JAXWSExporterWithHttpServiceTest extends JAXExporterAbstractTest {

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        prepareMockInterceptors();

        exporter.start();

    }

    @Override
    protected void prepareMockInterceptors() {
        super.prepareMockInterceptors();

        //Add httpservice mock
        field("http").ofType(HttpService.class).in(exporter).set(httpServiceMock);
    }

    @Test
    public void worksInCaseHttpServiceWasInjected() throws BinderException, ServletException, NamespaceException {

        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("fuchsia.export.cxf.class.name",ServiceForExportation.class.getName());
        metadata.put("fuchsia.export.cxf.url.context","/"+ServiceForExportation.class.getSimpleName());

        ExportDeclaration declaration = spy(ExportDeclarationBuilder.fromMetadata(metadata).build());
        declaration.bind(serviceReferenceFromExporter);

        exporter.registration(serviceReferenceFromExporter);

        exporter.addDeclaration(declaration);

        verify(httpServiceMock, times(1)).registerServlet(eq(org.ow2.chameleon.fuchsia.exporter.jaxws.internal.Constants.CXF_SERVLET), any(CXFNonSpringServlet.class), any(Dictionary.class),  any(org.osgi.service.http.HttpContext.class));

    }


}