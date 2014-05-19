/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.fuchsia.tools.grid.data.query;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.component.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;
import org.ow2.chameleon.fuchsia.tools.grid.ContentHelper;
import org.ow2.chameleon.fuchsia.tools.grid.model.LinkerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Instantiate
public class ContentLinker extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ContentLinker.class);

    private static final String URL = "/contentImportationLinker";

    public static final List<String> IMPORTATION_LINKER_INTERFACE = new ArrayList<String>();
    public static final List<String> IMPORTATION_LINKER_PROPERTIES = new ArrayList<String>();

    public static final List<String> EXPORTATION_LINKER_INTERFACE = new ArrayList<String>();
    public static final List<String> EXPORTATION_LINKER_PROPERTIES = new ArrayList<String>();

    // initialize the static lists
    static {
        IMPORTATION_LINKER_INTERFACE.add(ImportationLinker.class.getName());
        IMPORTATION_LINKER_PROPERTIES.add(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY);
        IMPORTATION_LINKER_PROPERTIES.add(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY);

        EXPORTATION_LINKER_INTERFACE.add(ExportationLinker.class.getName());
        EXPORTATION_LINKER_PROPERTIES.add(ExportationLinker.FILTER_EXPORTDECLARATION_PROPERTY);
        EXPORTATION_LINKER_PROPERTIES.add(ExportationLinker.FILTER_EXPORTERSERVICE_PROPERTY);
    }

    @Requires
    HttpService web;

    @Requires
    ContentHelper content;

    BundleContext context;

    public ContentLinker(BundleContext context) {
        this.context = context;
    }

    @Validate
    public void validate() {
        try {
            web.registerServlet(URL, this, null, null);
        } catch (ServletException e) {
            LOG.error("Error while registering the servlet", e);
        } catch (NamespaceException e) {
            LOG.error("Error while registering the servlet", e);
        }
    }

    @Invalidate
    public void invalidate() {
        web.unregister(URL);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<LinkerNode> rootList = new ArrayList<LinkerNode>();
        ObjectMapper mapper = new ObjectMapper();

        for (Factory factory : content.getFuchsiaFactories(IMPORTATION_LINKER_INTERFACE, IMPORTATION_LINKER_PROPERTIES)) {
            rootList.add(new LinkerNode(factory.getName()));
        }

        for (Factory factory : content.getFuchsiaFactories(EXPORTATION_LINKER_INTERFACE, EXPORTATION_LINKER_PROPERTIES)) {
            rootList.add(new LinkerNode(factory.getName()));
        }

        mapper.writeValue(resp.getWriter(), rootList);
    }

}
