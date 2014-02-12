package org.ow2.chameleon.fuchsia.importer.protobuffer.testing;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class ProtobufferImporterConfig {

    Instance ProtobufferRPCImporter = instance()
            .of("org.ow2.chameleon.fuchsia.importer.protobuffer.ProtobufferImporter")
            .with("target").setto("(&(rpc.server.address=*)(rpc.proto.class=*)(rpc.proto.service=*)(rpc.proto.message=*))");


    Instance ProtobufferRPCLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(rpc.server.address=*)(rpc.proto.class=*)(rpc.proto.service=*)(rpc.proto.message=*))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=ProtobufferRPCImporter)");

}
