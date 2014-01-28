package org.ow2.chameleon.fuchsia.jsonrpc.exporter.experiment;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class JSONRPCExporterInitializer {

        Instance jsonRPCExporter = instance()
                .of("Fuchsia-Exporter:JSON-RPC")
                .with("target").setto("(fuchsia.export.jsonrpc.instance=*)");

        Instance jsonRPCExporterLinker = instance()
            .of(FuchsiaConstants.DEFAULT_EXPORTATION_LINKER_FACTORY_NAME)
            .with(ExportationLinker.FILTER_EXPORTDECLARATION_PROPERTY).setto("(fuchsia.export.jsonrpc.instance=*)")
            .with(ExportationLinker.FILTER_EXPORTERSERVICE_PROPERTY).setto("(instance.name=jsonRPCExporter)");


}
