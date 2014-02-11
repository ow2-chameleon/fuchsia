package org.ow2.chameleon.fuchsia.protobuffer.exporter.testing;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class Config {

    Instance ProtobufferExporter = instance()
            .of("ProtobufferExporterFactory")
            .with("target").setto("(rpc.export.address=*)");

    Instance ProtobufferExporterLinker = instance()
            .of(FuchsiaConstants.DEFAULT_EXPORTATION_LINKER_FACTORY_NAME)
            .with(ExportationLinker.FILTER_EXPORTDECLARATION_PROPERTY).setto("(rpc.export.address=*)")
            .with(ExportationLinker.FILTER_EXPORTERSERVICE_PROPERTY).setto("(instance.name=ProtobufferExporter)");


}

