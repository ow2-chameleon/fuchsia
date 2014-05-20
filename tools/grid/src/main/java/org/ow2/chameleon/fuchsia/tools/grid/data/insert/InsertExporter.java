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
package org.ow2.chameleon.fuchsia.tools.grid.data.insert;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.tools.grid.ContentHelper;
import org.ow2.chameleon.fuchsia.tools.grid.model.ViewMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

@Component
@Instantiate
public class InsertExporter extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(InsertExporter.class);

    private static final String URL = "/insertExporter";

    @Requires
    HttpService web;

    @Requires
    ContentHelper content;

    BundleContext context;

    public InsertExporter(BundleContext context) {
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

        ObjectMapper mapper = new ObjectMapper();

        try {

            Collection<ServiceReference<Factory>> services = context.getServiceReferences(Factory.class, String.format("(factory.name=%s)", req.getParameter("exporterCombo")));

            if (services.size() == 1) {

                ServiceReference factorySR = (ServiceReference) services.iterator().next();

                Factory factory = (Factory) context.getService(factorySR);

                Dictionary<String, Object> hs = new Hashtable<String, Object>();

                String filter = getValue(req.getParameter("exporterTarget"));

                FuchsiaUtils.getFilter(filter);

                hs.put(ImporterService.TARGET_FILTER_PROPERTY, filter);

                String instanceName = req.getParameter("exporterInstanceName");
                if (instanceName != null && instanceName.trim().length() != 0) {
                    hs.put(Factory.INSTANCE_NAME_PROPERTY, instanceName);
                }

                ComponentInstance ci = factory.createComponentInstance(hs);

                mapper.writeValue(resp.getWriter(), new ViewMessage("success", "Exporter created successfully."));

            }

        } catch (Exception e) {
            LOG.info("Error while preparing response", e);
            mapper.writeValue(resp.getWriter(), new ViewMessage("error", e.getMessage()));
        }

    }

    private String getValue(String value) {

        if (value == null || value.trim().length() == 0) {
            return "(objectClass=*)";
        }

        return value;

    }

}
