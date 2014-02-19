package org.ow2.chameleon.fuchsia.tools.grid;

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

    @Requires
    HttpService web;

    @Validate
    public void validate() throws ServletException, NamespaceException {

        web.registerServlet(SERVLET,new MainPage(),null,null);

        web.registerResources(RESOURCES, RESOURCES, web.createDefaultHttpContext());


    }

    public void invalidate(){
        web.unregister(SERVLET);
        web.unregister(RESOURCES);
    }

    public GridWebView(BundleContext context){
        this.bundleContext=context;
    }

    private List<ImportationLinkerIntrospection> fetchLinkerIntrospections(){

        List<ImportationLinkerIntrospection> linkers=null;

        try {

            linkers=new ArrayList<ImportationLinkerIntrospection>();

            ServiceReference[] references=bundleContext.getServiceReferences(ImportationLinkerIntrospection.class.getName(),null);

            if(references!=null)
            for(ServiceReference sr:bundleContext.getServiceReferences(ImportationLinkerIntrospection.class.getName(),null)){

                linkers.add((ImportationLinkerIntrospection) bundleContext.getService(sr));

            }

        } catch (InvalidSyntaxException e) {
            LOG.error("Failed with the message {}", e.getMessage(), e);
        }

        return linkers;

    }

    public Map generateTemplateModel(){

        ArrayList nodes=new ArrayList();
        ArrayList edges=new ArrayList();

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


