package org.ow2.chameleon.fuchsia.jsonrpc.exporter.experiment;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


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

    }


    private void exportDeclaration(){
        Map<String, Object> metadata=new HashMap<String, Object>();

        metadata.put("id","exporter-1");
        metadata.put("fuchsia.export.jsonrpc.class","org.ow2.chameleon.fuchsia.jsonrpc.exporter.experiment.DummyIface");
        metadata.put("fuchsia.export.jsonrpc.instance","DummyPojoInstance");

        ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();

        Dictionary<String, Object> props = new Hashtable<String, Object>();

        String clazzes[] = new String[]{org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration.class.getName()};
        ServiceRegistration registration = context.registerService(clazzes, declaration, props);
    }


}
