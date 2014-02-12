package org.ow2.chameleon.fuchsia.protobuffer.protoconfig;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.ImportationLinker;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class Config {

    Instance ProtobufferExporter = instance()
            .of("org.ow2.chameleon.fuchsia.exporter.protobuffer.ProtobufferExporter")
            .with("target").setto("(rpc.export.address=*)");

    Instance ProtobufferExporterLinker = instance()
            .of(FuchsiaConstants.DEFAULT_EXPORTATION_LINKER_FACTORY_NAME)
            .with(ExportationLinker.FILTER_EXPORTDECLARATION_PROPERTY).setto("(rpc.export.address=*)")
            .with(ExportationLinker.FILTER_EXPORTERSERVICE_PROPERTY).setto("(instance.name=ProtobufferExporter)");


    Instance ProtobufferRPCImporter = instance()
            .of("org.ow2.chameleon.fuchsia.importer.protobuffer.ProtobufferImporter")
            .with("target").setto("(&(rpc.server.address=*)(rpc.proto.class=*)(rpc.proto.service=*)(rpc.proto.message=*))");


    Instance ProtobufferRPCLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(rpc.server.address=*)(rpc.proto.class=*)(rpc.proto.service=*)(rpc.proto.message=*))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=ProtobufferRPCImporter)");


}

