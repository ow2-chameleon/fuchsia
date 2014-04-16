package org.ow2.chameleon.fuchsia.tools.grid;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Tool Grid
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

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinkerIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.tools.grid.model.Edge;
import org.ow2.chameleon.fuchsia.tools.grid.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(immediate = true)
@Instantiate
@Provides
public class GridWebView {

    private static final String SERVLET="/grid";
    private static final String RESOURCES="/js";
    private static final String TEMPLATE_FILE="/index.html";
    private static final String TEMPLATE_ENCODING="UTF-8";
    private static final String TEMPLATE_FILE_REPOSITORY="/";

    private static final Logger LOG = LoggerFactory.getLogger(GridWebView.class);

    private final BundleContext bundleContext;

    @Controller
    private boolean state;

    @Requires
    HttpService web;

    public GridWebView(BundleContext context){
        this.bundleContext=context;
    }

    @Validate
    public void validate() {
        try {
            web.registerServlet(SERVLET,new MainPage(),null,null);
        } catch (Exception e) {
            LOG.error("Cannot register servlet", e);
            state = false;
            return;
        }

        try {
            web.registerResources(RESOURCES, RESOURCES, web.createDefaultHttpContext());
        } catch (NamespaceException e) {
            LOG.error("Cannot register resources", e);
            state = false;
        }
    }

    public Map generateTemplateModel(){

        List nodes=new ArrayList();
        List edges=new ArrayList();

        Map templateVariables=new HashMap<String,Object>();

        templateVariables.put("nodes", nodes);
        templateVariables.put("edges", edges);

        return templateVariables;
    }

    public void invalidate(){
        web.unregister(SERVLET);
        web.unregister(RESOURCES);
    }

   class MainPage extends HttpServlet {

       @Override
       protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

           PrintWriter out=resp.getWriter();

           Configuration cfg = new Configuration();

           cfg.setObjectWrapper(new DefaultObjectWrapper());
           cfg.setDefaultEncoding(TEMPLATE_ENCODING);
           cfg.setClassForTemplateLoading(this.getClass(),TEMPLATE_FILE_REPOSITORY);

           Template templateRT = cfg.getTemplate(TEMPLATE_FILE);

           try {
               templateRT.process(generateTemplateModel(), out);
           } catch (TemplateException e) {
               LOG.error("Failed with the message {}", e.getMessage(), e);
           }

       }
   }
}


