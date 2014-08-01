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

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.architecture.PropertyDescription;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.ExportationLinkerIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinkerIntrospection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@Instantiate
@Provides
public class ContentHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ContentHelper.class);

    BundleContext context;

    @Requires(optional = true, specification = Factory.class)
    private List<Factory> factories;

    public ContentHelper(BundleContext context) {
        this.context = context;
    }

    public List<ImportationLinkerIntrospection> fetchLinkerIntrospectionsImporter() {

        List<ImportationLinkerIntrospection> linkers = null;

        try {

            linkers=new ArrayList<ImportationLinkerIntrospection>();

            Collection<ServiceReference<ImportationLinkerIntrospection>> references=context.getServiceReferences(ImportationLinkerIntrospection.class,null);

            if(references!=null) {
                for(ServiceReference<ImportationLinkerIntrospection> sr:references){
                    linkers.add(context.getService(sr));
                }
            }

        } catch (InvalidSyntaxException e) {
            LOG.error("Problem while fetch the ImportationLinkerIntrospection", e);
        }

        return linkers;

    }

    public List<ExportationLinkerIntrospection> fetchLinkerIntrospectionsExporter() {

        List<ExportationLinkerIntrospection> linkers = null;

        try {

            linkers = new ArrayList<ExportationLinkerIntrospection>();

            ServiceReference[] references = context.getServiceReferences(ExportationLinkerIntrospection.class.getName(), null);

            if (references != null) {
                for (ServiceReference sr : context.getServiceReferences(ExportationLinkerIntrospection.class.getName(), null)) {

                    linkers.add((ExportationLinkerIntrospection) context.getService(sr));

                }
            }

        } catch (InvalidSyntaxException e) {
            LOG.error("Problem while fetch the ExportationLinkerIntrospection", e);
        }

        return linkers;

    }

    public List<Factory> getFuchsiaFactories(List<String> interfaces) {

        return getFuchsiaFactories(interfaces, new ArrayList<String>());

    }

    public List<Factory> getFuchsiaFactories() {

        return getFuchsiaFactories(new ArrayList<String>(), new ArrayList<String>());

    }

    public <T> List<T> getFuchsiaService(Class<T> interfaces) {

        List<T> fuchsiaService = new ArrayList<T>();

        try {
            Collection<ServiceReference<T>> serviceReferences = context.getServiceReferences(interfaces, null);

            for (ServiceReference<? extends T> sr : serviceReferences) {

                fuchsiaService.add(context.getService(sr));

            }

        } catch (InvalidSyntaxException e) {
            LOG.error("Problem while getting the FuchsiaService " + interfaces.getName(), e);
        }

        return fuchsiaService;

    }

    public List<Factory> getFuchsiaFactories(List<String> interfaces, List<String> properties) {

        List<Factory> fuchsiaFactories = new ArrayList<Factory>();

        for (Factory factory : factories) {

            List<String> servicesProvided = Arrays.asList(factory.getComponentDescription().getprovidedServiceSpecification());

            List<String> factoryProperties = new ArrayList<String>();

            for (PropertyDescription pd : factory.getComponentDescription().getProperties()) {
                //System.out.println(factory.getName()+" Property ---->"+pd.getName()+"="+pd.getValue());
                factoryProperties.add(pd.getName());
            }

            /*
            for (org.apache.felix.ipojo.metadata.Attribute att : factory.getComponentMetadata().getAttributes()) {
                System.out.println(factory.getName()+" Data ---->"+att.getName()+"="+att.getValue());
            }
            */

            if (!servicesProvided.containsAll(interfaces)
                    || !factoryProperties.containsAll(properties)) {
                continue;
            }

            /*
            if(factoryProperties.contains(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY)){
                fuchsiaFactories.add(factory);
            }
            */

            fuchsiaFactories.add(factory);

        }

        return fuchsiaFactories;

    }

}
