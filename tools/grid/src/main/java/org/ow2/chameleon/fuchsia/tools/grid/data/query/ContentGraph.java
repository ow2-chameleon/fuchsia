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
import org.ow2.chameleon.fuchsia.core.declaration.Constants;
import org.ow2.chameleon.fuchsia.core.declaration.Declaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
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

        final String NODE_FUCHSIA="Fuchsia";
        final String NODE_IMPORTER="Importer";
        final String NODE_EXPORTER="Exporter";
        final String NODE_TYPE_IMPORTER="importer";
        final String NODE_TYPE_EXPORTER="exporter";
        final String NODE_TYPE_DECLARATION="declaration";
        final String NODE_TYPE_NATIVE="native";
        final String NODE_TYPE_LINKER="linker";

        List<GraphVertex> rootList=new ArrayList<GraphVertex>();
        List<ImporterService> ignoredImporterService=content.getFuchsiaService(ImporterService.class);
        List<ExporterService> ignoredExporterService=content.getFuchsiaService(ExporterService.class);

        List<ImportDeclaration> ignoredImporterDeclaration=content.getFuchsiaService(ImportDeclaration.class);
        List<ExportDeclaration> ignoredExporterDeclaration=content.getFuchsiaService(ExportDeclaration.class);
        ObjectMapper mapper=new ObjectMapper();
        rootList.add(new GraphVertex(NODE_FUCHSIA,NODE_IMPORTER,NODE_TYPE_NATIVE));
        for(ImportationLinkerIntrospection linker:content.fetchLinkerIntrospectionsImporter()){
            rootList.add(new GraphVertex(NODE_IMPORTER,linker.getName(),NODE_TYPE_LINKER));
            for(ImporterService importer:linker.getLinkedImporters()){
                ignoredImporterService.remove(importer);
                GraphVertex linkerNode=new GraphVertex(linker.getName(),importer.getName(),NODE_TYPE_IMPORTER);
                rootList.add(linkerNode);
                if(ImportationLinkerIntrospection.class.isInstance(importer)){
                    ImportationLinkerIntrospection ili=(ImportationLinkerIntrospection)importer;
                    for(ImportDeclaration id:ili.getImportDeclarations()){
                        //This should be handled differently, since the ID can become optional in the future
                        String declarationId=id.getMetadata().get(Constants.ID).toString();
                        ignoredImporterDeclaration.remove(id);
                        GraphVertex declarationNode=new GraphVertex(importer.getName(),declarationId,NODE_TYPE_DECLARATION);
                        rootList.add(declarationNode);
                    }
                }
            }
        }

        rootList.add(new GraphVertex(NODE_FUCHSIA,NODE_EXPORTER,NODE_TYPE_NATIVE));
        for(ExportationLinkerIntrospection linker:content.fetchLinkerIntrospectionsExporter()){
            rootList.add(new GraphVertex(NODE_EXPORTER,linker.getName(),NODE_TYPE_LINKER));
            for(ExporterService exporter:linker.getLinkedExporters()){
                ignoredExporterService.remove(exporter);
                GraphVertex linkerNode=new GraphVertex(linker.getName(),exporter.getName(),NODE_TYPE_EXPORTER);
                rootList.add(linkerNode);
                if(ExportationLinkerIntrospection.class.isInstance(exporter)){
                    ExportationLinkerIntrospection ili=(ExportationLinkerIntrospection)exporter;
                    for(ExportDeclaration id:ili.getExportDeclarations()){
                        //This should be handled differently, since the ID can become optional in the future
                        String declarationId=id.getMetadata().get(Constants.ID).toString();
                        ignoredExporterDeclaration.remove(id);
                        GraphVertex declarationNode=new GraphVertex(exporter.getName(),declarationId,NODE_TYPE_DECLARATION);
                        rootList.add(declarationNode);
                    }
                }
            }
        }

        for(ImporterService is:ignoredImporterService){
            rootList.add(new GraphVertex(is.getName(),is.getName(),NODE_TYPE_IMPORTER));
        }

        for(ExporterService is:ignoredExporterService){
            rootList.add(new GraphVertex(is.getName(),is.getName(),NODE_TYPE_EXPORTER));
        }

        for(ImportDeclaration id:ignoredImporterDeclaration){
            String declarationId=id.getMetadata().get(Constants.ID).toString();
            rootList.add(new GraphVertex(declarationId,declarationId,NODE_TYPE_DECLARATION));
        }

        for(ExportDeclaration id:ignoredExporterDeclaration){
            String declarationId=id.getMetadata().get(Constants.ID).toString();
            rootList.add(new GraphVertex(declarationId,declarationId,NODE_TYPE_DECLARATION));
        }

        mapper.writeValue(resp.getWriter(),rootList);

    }

}
