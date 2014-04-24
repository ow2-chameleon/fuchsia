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

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Component(immediate = true)
@Instantiate
@Provides
public class GridWebView {

    private static final String SERVLET="/grid";
    private static final String RESOURCES_JS ="/js";
    private static final String RESOURCES_CSS ="/css";
    private static final String RESOURCES_IMAGES ="/images";
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
            web.registerResources(RESOURCES_JS, RESOURCES_JS, web.createDefaultHttpContext());
            web.registerResources(RESOURCES_CSS, RESOURCES_CSS, web.createDefaultHttpContext());
            web.registerResources(RESOURCES_IMAGES, RESOURCES_IMAGES, web.createDefaultHttpContext());
        } catch (NamespaceException e) {
            LOG.error("Cannot register resources", e);
            state = false;
        }
    }

    @Invalidate
    public void invalidate(){
        web.unregister(SERVLET);
        web.unregister(RESOURCES_JS);
        web.unregister(RESOURCES_CSS);
        web.unregister(RESOURCES_IMAGES);
    }

   class MainPage extends HttpServlet {

       @Override
       protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

           PrintWriter out=resp.getWriter();

           InputStream is=this.getClass().getResourceAsStream(TEMPLATE_FILE);

           BufferedReader br= new BufferedReader(new InputStreamReader(is));

           String content;

           while((content=br.readLine())!=null){
               resp.getWriter().write(content + "\n");
           }

       }
   }
}


