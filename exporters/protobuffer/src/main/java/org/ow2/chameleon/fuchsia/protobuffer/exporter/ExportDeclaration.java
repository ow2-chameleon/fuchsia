package org.ow2.chameleon.fuchsia.protobuffer.exporter;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component
@Instantiate
public class ExportDeclaration {

    BundleContext context;

    public ExportDeclaration(BundleContext bc) {
        context = bc;
    }

    @Validate
    public void validate() {

        Map<String, Object> metadata = new HashMap<String, Object>();

        metadata.put("id", "export-tests");
        metadata.put("exporter.id", "myservice");
        metadata.put("rpc.export.address", "http://localhost:8889/AddressBookService");
        metadata.put("rpc.export.class", "org.ow2.chameleon.fuchsia.protobuffer.protoclass.AddressBookProtos$AddressBookService");
        metadata.put("rpc.export.message", "org.ow2.chameleon.fuchsia.protobuffer.protoclass.AddressBookProtos$AddressBookServiceMessage");

        org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration declaration = ExportDeclarationBuilder.fromMetadata(metadata).build();

        Dictionary<String, Object> props = new Hashtable<String, Object>();

        String clazzes[] = new String[]{org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration.class.getName()};
        ServiceRegistration registration = context.registerService(clazzes, declaration, props);

    }

}
