package org.ow2.chameleon.fuchsia.exporter.jaxws.test;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Exporter JAX-WS
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

import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
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
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;

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

        metadata.put(ID,"TestJAXWSDeclaration");
        metadata.put("fuchsia.export.cxf.class.name",ServiceForExportation.class.getName());
        metadata.put("fuchsia.export.cxf.url.context","/"+ServiceForExportation.class.getSimpleName());

        ExportDeclaration declaration = spy(ExportDeclarationBuilder.fromMetadata(metadata).build());
        declaration.bind(serviceReferenceFromExporter);

        exporter.registration(serviceReferenceFromExporter);

        exporter.addDeclaration(declaration);

        verify(httpServiceMock, times(1)).registerServlet(eq(org.ow2.chameleon.fuchsia.exporter.jaxws.internal.Constants.CXF_SERVLET), any(CXFNonSpringServlet.class), any(Dictionary.class),  any(org.osgi.service.http.HttpContext.class));

    }


}
