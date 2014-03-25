package org.ow2.chameleon.fuchsia.exporter.cxf.examples;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Example CXF Exporter Configuration
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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.exporter.cxf.examples.base.PojoSampleToBeExportedIface;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component
@Instantiate
public class ExportDeclarationSample {

    BundleContext context;

    public ExportDeclarationSample(BundleContext context){

        this.context=context;

    }

    @Validate
    public void start(){

        exportDeclaration();

    }


    private void exportDeclaration(){
        Map<String, Object>  metadata=new HashMap<String, Object>();

        metadata.put("id","a");
        metadata.put("exporter.id","myservice");
        metadata.put("fuchsia.export.cxf.class.name",PojoSampleToBeExportedIface.class.getName());
        metadata.put("fuchsia.export.cxf.instance","PojoSampleToBeExported");
        metadata.put("fuchsia.export.cxf.url.context","/PojoSampleToBeExported");

        ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        String clazzes[] = new String[]{org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration.class.getName()};
        ServiceRegistration registration = context.registerService(clazzes, declaration, props);
    }


}
