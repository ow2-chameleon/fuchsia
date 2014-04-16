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
package org.ow2.chameleon.fuchsia.tools.grid;

import org.apache.felix.ipojo.annotations.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.component.ExportationLinkerIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinkerIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.tools.grid.model.Edge;
import org.ow2.chameleon.fuchsia.tools.grid.model.Node;
import org.ow2.chameleon.fuchsia.tools.grid.model.TreeNode;

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

@Component
@Instantiate
public class Content extends HttpServlet {

    @Requires
    HttpService web;

    BundleContext context;

    public Content(BundleContext context){
        this.context=context;
    }

    @Validate
    public void validate(){
        try {
            web.registerServlet("/content",this,null,null);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (NamespaceException e) {
            e.printStackTrace();
        }
    }

    @Invalidate
    public void invalidate(){
        web.unregister("/content");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<TreeNode> rootList=new ArrayList<TreeNode>();

        ObjectMapper mapper=new ObjectMapper();

        rootList.add(new TreeNode("Fuchsia","Importer","licensing"));

        for(ImportationLinkerIntrospection linker:fetchLinkerIntrospections()){

            rootList.add(new TreeNode("Importer",linker.getName(),"licensing"));

            for(ImporterService importer:linker.getLinkedImporters()){

                TreeNode linkerNode=new TreeNode(linker.getName(),importer.getName(),"licensing");
                rootList.add(linkerNode);

            }

            /*
            for(ImportDeclaration declaration:linker.getImportDeclarations()){
                nodes.add(new Node(declaration.getMetadata().get("id").toString()));
                edges.add(new Edge(linker.getName(),declaration.getMetadata().get("id").toString()));
            }
            */

        }

        rootList.add(new TreeNode("Fuchsia","Exporter","licensing"));

        for(ExportationLinkerIntrospection linker:fetchLinkerIntrospectionsExporter()){

            rootList.add(new TreeNode("Exporter",linker.getName(),"licensing"));

            for(ExporterService exporter:linker.getLinkedExporters()){

                TreeNode linkerNode=new TreeNode(linker.getName(),exporter.getName(),"licensing");
                rootList.add(linkerNode);

            }

            /*
            for(ImportDeclaration declaration:linker.getImportDeclarations()){
                nodes.add(new Node(declaration.getMetadata().get("id").toString()));
                edges.add(new Edge(linker.getName(),declaration.getMetadata().get("id").toString()));
            }
            */

        }

        mapper.writeValue(resp.getWriter(),rootList);

        resp.addHeader("Origin","http://localhost:8080/content");
        resp.addHeader("access-control-allow-origin","*");


    }

    private List<ImportationLinkerIntrospection> fetchLinkerIntrospections(){

        List<ImportationLinkerIntrospection> linkers=null;

        try {

            linkers=new ArrayList<ImportationLinkerIntrospection>();

            ServiceReference[] references=context.getServiceReferences(ImportationLinkerIntrospection.class.getName(),null);

            if(references!=null) {
                for (ServiceReference sr : context.getServiceReferences(ImportationLinkerIntrospection.class.getName(), null)) {

                    linkers.add((ImportationLinkerIntrospection) context.getService(sr));

                }
            }

        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }

        return linkers;

    }

    private List<ExportationLinkerIntrospection> fetchLinkerIntrospectionsExporter(){

        List<ExportationLinkerIntrospection> linkers=null;

        try {

            linkers=new ArrayList<ExportationLinkerIntrospection>();

            ServiceReference[] references=context.getServiceReferences(ExportationLinkerIntrospection.class.getName(),null);

            if(references!=null) {
                for (ServiceReference sr : context.getServiceReferences(ExportationLinkerIntrospection.class.getName(), null)) {

                    linkers.add((ExportationLinkerIntrospection) context.getService(sr));

                }
            }

        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }

        return linkers;

    }

    public Map generateTemplateModel(){

        List nodes=new ArrayList();
        List edges=new ArrayList();

        Map templateVariables=new HashMap<String,Object>();

        templateVariables.put("nodes", nodes);
        templateVariables.put("edges", edges);

        for(ImportationLinkerIntrospection linker:fetchLinkerIntrospections()){

            nodes.add(new Node(linker.getName()));

            for(ImporterService importer:linker.getLinkedImporters()){
                nodes.add(new Node(importer.getName()));
                edges.add(new Edge(linker.getName(),importer.getName()));
            }

            for(ImportDeclaration declaration:linker.getImportDeclarations()){
                nodes.add(new Node(declaration.getMetadata().get("id").toString()));
                edges.add(new Edge(linker.getName(),declaration.getMetadata().get("id").toString()));
            }

        }
        return templateVariables;
    }

}
