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
import org.ow2.chameleon.fuchsia.core.component.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;
import org.ow2.chameleon.fuchsia.tools.grid.ContentHelper;
import org.ow2.chameleon.fuchsia.tools.grid.model.ViewMessage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Component
@Instantiate
public class InsertLinker extends HttpServlet {

    @Requires
    HttpService web;

    @Requires
    ContentHelper content;

    final String URL="/insertLinker";

    BundleContext context;

    public InsertLinker(BundleContext context){
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

        ObjectMapper mapper=new ObjectMapper();

        try {

            Collection<ServiceReference<Factory>> services=context.getServiceReferences(Factory.class,String.format("(factory.name=%s)",req.getParameter("linkersCombo")));

            if(services.size()==1){

                ServiceReference factorySR=(ServiceReference)services.iterator().next();

                Factory factory=(Factory)context.getService(factorySR);

                Hashtable<String,Object> hs=new Hashtable<String, Object>();

                String declarationProperty=getValue(req.getParameter("linkerDeclarationProperty"));
                String serviceProperty=getValue(req.getParameter("linkerServiceProperty"));

                List<String> interfaces=Arrays.asList(factory.getComponentDescription().getprovidedServiceSpecification());

                if(interfaces.contains(ImportationLinker.class.getName())){
                    hs.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY,declarationProperty);
                    hs.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY,serviceProperty);
                }else if(interfaces.contains(ExportationLinker.class.getName())){
                    hs.put(ExportationLinker.FILTER_EXPORTDECLARATION_PROPERTY,declarationProperty);
                    hs.put(ExportationLinker.FILTER_EXPORTERSERVICE_PROPERTY,serviceProperty);
                }

                /**
                 * This forces the filter to be evaluated before submit it to the fuchsia platform
                 */
                FuchsiaUtils.getFilter(declarationProperty);
                FuchsiaUtils.getFilter(serviceProperty);

                String instanceName=req.getParameter("linkerInstanceName");

                if(instanceName!=null && instanceName.trim().length()!=0){
                    hs.put(Factory.INSTANCE_NAME_PROPERTY,instanceName);
                }

                ComponentInstance ci=factory.createComponentInstance(hs);

                mapper.writeValue(resp.getWriter(),new ViewMessage("success","Linker created successfully."));

            }

        } catch (Exception e) {
            mapper.writeValue(resp.getWriter(),new ViewMessage("error",e.getMessage()));
        }

    }

    private String getValue(String value){

        if(value==null||value.trim().length()==0){
            return "(objectClass=*)";
        }

        return value;

    }

}
