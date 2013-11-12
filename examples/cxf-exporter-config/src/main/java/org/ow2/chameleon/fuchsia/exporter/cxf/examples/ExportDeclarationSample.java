package org.ow2.chameleon.fuchsia.exporter.cxf.examples;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.exporter.cxf.examples.base.PojoSampleToBeExported;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component
@Instantiate
public class ExportDeclarationSample {

    BundleContext context;

    PojoSampleToBeExported dummyInstance=new PojoSampleToBeExported();

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
        metadata.put("fuchsia.export.cxf.class.name",dummyInstance.getClass().getName());
        metadata.put("fuchsia.export.cxf.instance",dummyInstance);
        metadata.put("fuchsia.export.cxf.url.context","/PojoSampleToBeExported");

        ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        String clazzes[] = new String[]{org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration.class.getName()};
        ServiceRegistration registration = context.registerService(clazzes, declaration, props);
    }


}
