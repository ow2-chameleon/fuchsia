package org.ow2.chameleon.fuchsia.examples.jsonrpc.exporter.experiment;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Example JSONRPC Base interface
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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.ow2.chameleon.fuchsia.core.declaration.Constants.*;


@Component
@Instantiate
public class ExporterDeclarationInstance {

    BundleContext context;


    public ExporterDeclarationInstance(BundleContext context){

        this.context=context;

    }

    @Validate
    public void start(){

        exportDeclaration();

        importDeclaration();

    }


    private void exportDeclaration(){
        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("id","exporter-1");
        metadata.put("fuchsia.export.jsonrpc.class","org.ow2.chameleon.fuchsia.examples.jsonrpc.exporter.experiment.DummyIface");
        metadata.put("fuchsia.export.jsonrpc.instance","DummyPojoInstance");

        ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();

        Dictionary<String, Object> props = new Hashtable<String, Object>();

        String clazzes[] = new String[]{org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration.class.getName()};
        ServiceRegistration registration = context.registerService(clazzes, declaration, props);
    }

    private void importDeclaration(){
        Map<String, Object> metadata=new HashMap<String, Object>();
        metadata.put(ID, "endipoint");
        metadata.put(URL, "http://localhost:8080/JSONRPC/DummyPojoInstance");
        metadata.put(SERVICE_CLASS, "org.ow2.chameleon.fuchsia.examples.jsonrpc.exporter.experiment.DummyIface");
        metadata.put(CONFIGS, "jsonrpc");

        ImportDeclaration declaration = ImportDeclarationBuilder.fromMetadata(metadata).build();

        String clazzes[] = new String[]{ImportDeclaration.class.getName()};

        Dictionary<String, Object> props = new Hashtable<String, Object>();

        ServiceRegistration registration = context.registerService(clazzes, declaration, props);
    }

}
