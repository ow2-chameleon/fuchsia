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

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.component.*;
import org.ow2.chameleon.fuchsia.tools.grid.ContentHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Component
@Instantiate
public class InsertImportationLinker extends HttpServlet {

    @Requires
    HttpService web;

    @Requires
    ContentHelper content;

    final String URL="/insertImportationLinker";

    BundleContext context;

    public InsertImportationLinker(BundleContext context){
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

        System.out.println("Submitting info!!!");

        Enumeration enu=req.getParameterNames();

        while(enu.hasMoreElements()){
            String paramname= (String) enu.nextElement();
            System.out.println(paramname+"-->"+req.getParameter(paramname));
        }

        try {

            Collection<ServiceReference<Factory>> services=context.getServiceReferences(Factory.class,String.format("(factory.name=%s)",req.getParameter("linkersCombo")));

            if(services.size()==1){

                ServiceReference factorySR=(ServiceReference)services.iterator().next();

                Factory factory=(Factory)context.getService(factorySR);

                Hashtable<String,Object> hs=new Hashtable<String, Object>();

                hs.put(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY,getValue(req.getParameter("linkerDeclarationProperty")));
                hs.put(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY,getValue(req.getParameter("linkerServiceProperty")));
                hs.put(Factory.INSTANCE_NAME_PROPERTY,req.getParameter("linkerInstanceName"));

                ComponentInstance ci=factory.createComponentInstance(hs);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getValue(String value){

        if(value==null||value.trim().length()==0){
            return "(objectClass=*)";
        }

        return value;

    }

}
