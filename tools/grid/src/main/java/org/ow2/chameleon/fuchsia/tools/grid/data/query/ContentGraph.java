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

import org.apache.felix.ipojo.annotations.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.component.ExportationLinkerIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinkerIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.tools.grid.ContentHelper;
import org.ow2.chameleon.fuchsia.tools.grid.model.GraphVertex;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Instantiate
public class ContentGraph extends HttpServlet {

    @Requires
    HttpService web;

    @Requires
    ContentHelper content;

    final String URL="/contentGraph";

    BundleContext context;

    public ContentGraph(BundleContext context){
        this.context=context;
    }

    @Validate
    public void validate(){
        try {
            web.registerServlet(URL,this,null,null);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (NamespaceException e) {
            e.printStackTrace();
        }
    }

    @Invalidate
    public void invalidate(){
        web.unregister(URL);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<GraphVertex> rootList=new ArrayList<GraphVertex>();

        List<ImporterService> ignoredImporterService=content.getFuchsiaService(ImporterService.class);

        List<ExporterService> ignoredExporterService=content.getFuchsiaService(ExporterService.class);

        ObjectMapper mapper=new ObjectMapper();

        rootList.add(new GraphVertex("Fuchsia","Importer","licensing"));

        for(ImportationLinkerIntrospection linker:content.fetchLinkerIntrospectionsImporter()){

            rootList.add(new GraphVertex("Importer",linker.getName(),"licensing"));

            for(ImporterService importer:linker.getLinkedImporters()){

                ignoredImporterService.remove(importer);

                GraphVertex linkerNode=new GraphVertex(linker.getName(),importer.getName(),"licensing");
                rootList.add(linkerNode);

            }

        }

        rootList.add(new GraphVertex("Fuchsia","Exporter","licensing"));

        for(ExportationLinkerIntrospection linker:content.fetchLinkerIntrospectionsExporter()){

            rootList.add(new GraphVertex("Exporter",linker.getName(),"licensing"));

            for(ExporterService exporter:linker.getLinkedExporters()){

                ignoredExporterService.remove(exporter);

                GraphVertex linkerNode=new GraphVertex(linker.getName(),exporter.getName(),"licensing");
                rootList.add(linkerNode);

            }

        }

        for(ImporterService is:ignoredImporterService){
            rootList.add(new GraphVertex(is.getName(),is.getName(),"licensing"));
        }

        for(ExporterService is:ignoredExporterService){
            rootList.add(new GraphVertex(is.getName(),is.getName(),"licensing"));
        }

        mapper.writeValue(resp.getWriter(),rootList);

    }

}
